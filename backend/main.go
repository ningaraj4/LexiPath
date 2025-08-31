package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"lexipath-backend/internal/config"
	"lexipath-backend/internal/handlers"
	"lexipath-backend/internal/middleware"
	"lexipath-backend/internal/repositories"
	"lexipath-backend/internal/services"

	"github.com/gin-contrib/cors"
	"github.com/gin-contrib/gzip"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func main() {
	// Initialize logger
	logger, err := zap.NewProduction()
	if err != nil {
		log.Fatal("Failed to initialize logger:", err)
	}
	defer logger.Sync()

	// Load configuration
	cfg, err := config.Load()
	if err != nil {
		logger.Fatal("Failed to load configuration", zap.Error(err))
	}

	// Initialize database
	db, err := repositories.NewPostgresDB(cfg.DatabaseURL)
	if err != nil {
		logger.Fatal("Failed to connect to database", zap.Error(err))
	}
	defer db.Close()

	// Initialize Redis
	redisClient, err := repositories.NewRedisClient(cfg.RedisURL)
	if err != nil {
		logger.Fatal("Failed to connect to Redis", zap.Error(err))
	}
	defer redisClient.Close()

	// Initialize Firebase
	firebaseApp, err := services.NewFirebaseApp(cfg.FirebaseProjectID)
	if err != nil {
		logger.Fatal("Failed to initialize Firebase", zap.Error(err))
	}

	// Initialize repositories
	userRepo := repositories.NewUserRepository(db)
	profileRepo := repositories.NewProfileRepository(db)
	contentRepo := repositories.NewContentRepository(db)
	quizRepo := repositories.NewQuizRepository(db)
	masteryRepo := repositories.NewMasteryRepository(db)
	weeklyPlanRepo := repositories.NewWeeklyPlanRepository(db)
	cacheRepo := repositories.NewCacheRepository(redisClient)

	// Initialize services
	authService := services.NewAuthService(firebaseApp, userRepo)
	geminiService := services.NewGeminiService(cfg.GeminiAPIKey, logger)
	profileService := services.NewProfileService(profileRepo, userRepo)
	contentService := services.NewContentService(contentRepo, cacheRepo, geminiService, logger)
	quizService := services.NewQuizService(quizRepo, masteryRepo, logger)
	weeklyPlanService := services.NewWeeklyPlanService(weeklyPlanRepo, masteryRepo, geminiService, logger)

	// Initialize handlers
	h := handlers.New(
		authService,
		profileService,
		contentService,
		quizService,
		weeklyPlanService,
		logger,
	)

	// Setup Gin router
	if cfg.Environment == "production" {
		gin.SetMode(gin.ReleaseMode)
	}

	router := gin.New()

	// Middleware
	router.Use(gin.Recovery())
	router.Use(middleware.RequestID())
	router.Use(middleware.Logger(logger))
	router.Use(gzip.Gzip(gzip.DefaultCompression))
	router.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"*"},
		AllowMethods:     []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"*"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
		MaxAge:           12 * time.Hour,
	}))

	// Health check
	router.GET("/healthz", h.HealthCheck)
	router.GET("/metrics", h.Metrics)

	// API routes
	v1 := router.Group("/v1")
	{
		// Profile routes
		v1.POST("/profile/upsert", middleware.AuthRequired(authService), h.UpsertProfile)

		// Content routes
		v1.POST("/daily-content", middleware.AuthRequired(authService), h.GetDailyContent)
		v1.POST("/translate", middleware.AuthRequired(authService), h.Translate)

		// Quiz routes
		v1.POST("/quiz/submit", middleware.AuthRequired(authService), h.SubmitQuiz)

		// Weekly plan routes
		v1.POST("/weekly-plan/generate", middleware.AuthRequired(authService), h.GenerateWeeklyPlan)
	}

	// Start server
	srv := &http.Server{
		Addr:    fmt.Sprintf(":%s", cfg.Port),
		Handler: router,
	}

	// Graceful shutdown
	go func() {
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			logger.Fatal("Failed to start server", zap.Error(err))
		}
	}()

	logger.Info("Server started", zap.String("port", cfg.Port))

	// Wait for interrupt signal to gracefully shutdown the server
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	logger.Info("Shutting down server...")

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		logger.Fatal("Server forced to shutdown", zap.Error(err))
	}

	logger.Info("Server exited")
}
