package handlers

import (
	"net/http"
	"strconv"
	"time"

	"lexipath-backend/internal/models"
	"lexipath-backend/internal/services"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"go.uber.org/zap"
)

type Handlers struct {
	authService       *services.AuthService
	profileService    *services.ProfileService
	contentService    *services.ContentService
	quizService       *services.QuizService
	weeklyPlanService *services.WeeklyPlanService
	logger            *zap.Logger
}

func New(
	authService *services.AuthService,
	profileService *services.ProfileService,
	contentService *services.ContentService,
	quizService *services.QuizService,
	weeklyPlanService *services.WeeklyPlanService,
	logger *zap.Logger,
) *Handlers {
	return &Handlers{
		authService:       authService,
		profileService:    profileService,
		contentService:    contentService,
		quizService:       quizService,
		weeklyPlanService: weeklyPlanService,
		logger:            logger,
	}
}

func (h *Handlers) HealthCheck(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{
		"status":    "healthy",
		"timestamp": time.Now().UTC(),
	})
}

func (h *Handlers) Metrics(c *gin.Context) {
	promhttp.Handler().ServeHTTP(c.Writer, c.Request)
}

func (h *Handlers) UpsertProfile(c *gin.Context) {
	user := c.MustGet("user").(*models.User)

	var req models.UpsertProfileRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	profile, err := h.profileService.UpsertProfile(c.Request.Context(), user.ID, &req)
	if err != nil {
		h.logger.Error("Failed to upsert profile", zap.Error(err), zap.String("user_id", user.ID.String()))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to update profile"})
		return
	}

	c.JSON(http.StatusOK, profile)
}

func (h *Handlers) GetDailyContent(c *gin.Context) {
	user := c.MustGet("user").(*models.User)

	var req models.DailyContentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// Get user profile
	profile, err := h.profileService.GetProfile(c.Request.Context(), user.ID)
	if err != nil {
		h.logger.Error("Failed to get profile", zap.Error(err), zap.String("user_id", user.ID.String()))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get profile"})
		return
	}

	if profile == nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Profile not found. Please complete onboarding first."})
		return
	}

	content, err := h.contentService.GetDailyContent(c.Request.Context(), user.ID, profile, req.Date)
	if err != nil {
		h.logger.Error("Failed to get daily content", zap.Error(err), zap.String("user_id", user.ID.String()))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get daily content"})
		return
	}

	c.JSON(http.StatusOK, content)
}

func (h *Handlers) SubmitQuiz(c *gin.Context) {
	user := c.MustGet("user").(*models.User)

	var req models.QuizSubmissionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// For now, we'll generate the quiz question on-the-fly
	// In a production system, you might want to store quiz questions
	// when content is generated to ensure consistency

	// This is a simplified implementation - you would typically
	// retrieve the stored quiz question and correct answer
	quizLog, err := h.quizService.SubmitQuiz(c.Request.Context(), user.ID, &req, "placeholder_correct_answer", "placeholder_question", []string{})
	if err != nil {
		h.logger.Error("Failed to submit quiz", zap.Error(err), zap.String("user_id", user.ID.String()))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to submit quiz"})
		return
	}

	c.JSON(http.StatusOK, quizLog)
}

func (h *Handlers) GenerateWeeklyPlan(c *gin.Context) {
	user := c.MustGet("user").(*models.User)

	// Get user profile
	profile, err := h.profileService.GetProfile(c.Request.Context(), user.ID)
	if err != nil {
		h.logger.Error("Failed to get profile", zap.Error(err), zap.String("user_id", user.ID.String()))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get profile"})
		return
	}

	if profile == nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Profile not found"})
		return
	}

	plan, err := h.weeklyPlanService.GenerateWeeklyPlan(c.Request.Context(), user.ID, profile)
	if err != nil {
		h.logger.Error("Failed to generate weekly plan", zap.Error(err), zap.String("user_id", user.ID.String()))
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, plan)
}

func (h *Handlers) Translate(c *gin.Context) {
	user := c.MustGet("user").(*models.User)

	var req models.TranslateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	translation, err := h.contentService.Translate(c.Request.Context(), user.ID, &req)
	if err != nil {
		h.logger.Error("Failed to translate", zap.Error(err), zap.String("user_id", user.ID.String()))
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, translation)
}

func (h *Handlers) GetContentHistory(c *gin.Context) {
	user := c.MustGet("user").(*models.User)

	limitStr := c.DefaultQuery("limit", "20")
	offsetStr := c.DefaultQuery("offset", "0")

	limit, err := strconv.Atoi(limitStr)
	if err != nil || limit <= 0 || limit > 100 {
		limit = 20
	}

	offset, err := strconv.Atoi(offsetStr)
	if err != nil || offset < 0 {
		offset = 0
	}

	history, err := h.contentService.GetContentHistory(c.Request.Context(), user.ID, limit, offset)
	if err != nil {
		h.logger.Error("Failed to get content history", zap.Error(err), zap.String("user_id", user.ID.String()))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get content history"})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"content": history,
		"limit":   limit,
		"offset":  offset,
	})
}
