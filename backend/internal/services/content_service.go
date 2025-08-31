package services

import (
	"context"
	"fmt"
	"time"

	"lexipath-backend/internal/models"
	"lexipath-backend/internal/repositories"

	"github.com/google/uuid"
	"go.uber.org/zap"
)

type ContentService struct {
	contentRepo   *repositories.ContentRepository
	cacheRepo     *repositories.CacheRepository
	geminiService *GeminiService
	logger        *zap.Logger
}

func NewContentService(contentRepo *repositories.ContentRepository, cacheRepo *repositories.CacheRepository, geminiService *GeminiService, logger *zap.Logger) *ContentService {
	return &ContentService{
		contentRepo:   contentRepo,
		cacheRepo:     cacheRepo,
		geminiService: geminiService,
		logger:        logger,
	}
}

func (s *ContentService) GetDailyContent(ctx context.Context, userID uuid.UUID, profile *models.Profile, date time.Time) (*models.DailyContent, error) {
	// Check rate limit
	count, err := s.cacheRepo.GetRateLimit(ctx, userID, "daily-content")
	if err != nil {
		s.logger.Warn("Failed to check rate limit", zap.Error(err))
	} else if count >= 10 { // Max 10 requests per day
		return nil, fmt.Errorf("daily rate limit exceeded")
	}

	// Try cache first
	content, err := s.cacheRepo.GetDailyContent(ctx, userID, date)
	if err != nil {
		s.logger.Warn("Failed to get cached content", zap.Error(err))
	}
	if content != nil {
		return content, nil
	}

	// Try database
	content, err = s.contentRepo.GetByUserAndDate(ctx, userID, date)
	if err != nil {
		return nil, fmt.Errorf("failed to get content from database: %w", err)
	}
	if content != nil {
		// Cache the result
		if err := s.cacheRepo.SetDailyContent(ctx, content); err != nil {
			s.logger.Warn("Failed to cache content", zap.Error(err))
		}
		return content, nil
	}

	// Generate new content using Gemini
	s.logger.Info("Generating new daily content", zap.String("user_id", userID.String()), zap.Time("date", date))

	// Increment rate limit
	if _, err := s.cacheRepo.IncrementRateLimit(ctx, userID, "daily-content"); err != nil {
		s.logger.Warn("Failed to increment rate limit", zap.Error(err))
	}

	geminiResp, err := s.geminiService.GenerateDailyContent(ctx, profile)
	if err != nil {
		return nil, fmt.Errorf("failed to generate content: %w", err)
	}

	// Save to database
	content, err = s.contentRepo.Create(ctx, userID, date, geminiResp)
	if err != nil {
		return nil, fmt.Errorf("failed to save content: %w", err)
	}

	// Cache the result
	if err := s.cacheRepo.SetDailyContent(ctx, content); err != nil {
		s.logger.Warn("Failed to cache new content", zap.Error(err))
	}

	return content, nil
}

func (s *ContentService) GetContentHistory(ctx context.Context, userID uuid.UUID, limit, offset int) ([]*models.DailyContent, error) {
	return s.contentRepo.GetHistoryByUser(ctx, userID, limit, offset)
}

func (s *ContentService) Translate(ctx context.Context, userID uuid.UUID, req *models.TranslateRequest) (*models.TranslateResponse, error) {
	// Check rate limit
	count, err := s.cacheRepo.GetRateLimit(ctx, userID, "translate")
	if err != nil {
		s.logger.Warn("Failed to check translate rate limit", zap.Error(err))
	} else if count >= 50 { // Max 50 translations per day
		return nil, fmt.Errorf("daily translation rate limit exceeded")
	}

	// Increment rate limit
	if _, err := s.cacheRepo.IncrementRateLimit(ctx, userID, "translate"); err != nil {
		s.logger.Warn("Failed to increment translate rate limit", zap.Error(err))
	}

	geminiResp, err := s.geminiService.Translate(ctx, req.Text, req.TargetLang, req.BaseLang)
	if err != nil {
		return nil, fmt.Errorf("failed to translate: %w", err)
	}

	return &models.TranslateResponse{
		Translation: geminiResp.Translation,
	}, nil
}
