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

type QuizService struct {
	quizRepo    *repositories.QuizRepository
	masteryRepo *repositories.MasteryRepository
	logger      *zap.Logger
}

func NewQuizService(quizRepo *repositories.QuizRepository, masteryRepo *repositories.MasteryRepository, logger *zap.Logger) *QuizService {
	return &QuizService{
		quizRepo:    quizRepo,
		masteryRepo: masteryRepo,
		logger:      logger,
	}
}

func (s *QuizService) SubmitQuiz(ctx context.Context, userID uuid.UUID, req *models.QuizSubmissionRequest, correctAnswer string, question string, options []string) (*models.QuizLog, error) {
	// Determine if answer is correct
	isCorrect := req.UserAnswer == correctAnswer

	// Create quiz log
	quizLog, err := s.quizRepo.Create(ctx, userID, req.ContentID, req.QuizType, question, options, correctAnswer, req.UserAnswer, isCorrect)
	if err != nil {
		return nil, fmt.Errorf("failed to create quiz log: %w", err)
	}

	// Update mastery score
	if err := s.updateMastery(ctx, userID, req.ContentID, isCorrect); err != nil {
		s.logger.Error("Failed to update mastery", zap.Error(err))
		// Don't fail the entire request if mastery update fails
	}

	return quizLog, nil
}

func (s *QuizService) updateMastery(ctx context.Context, userID, contentID uuid.UUID, isCorrect bool) error {
	// Get current mastery
	mastery, err := s.masteryRepo.GetByUserAndContent(ctx, userID, contentID)
	if err != nil {
		return fmt.Errorf("failed to get mastery: %w", err)
	}

	var newScore int
	if mastery == nil {
		// First attempt
		if isCorrect {
			newScore = 60 // Start with 60% for correct first attempt
		} else {
			newScore = 20 // Start with 20% for incorrect first attempt
		}
	} else {
		// Update existing score
		if isCorrect {
			// Increase score by 15-25 points based on current score
			increase := 25 - (mastery.MasteryScore / 5)
			if increase < 15 {
				increase = 15
			}
			newScore = mastery.MasteryScore + increase
		} else {
			// Decrease score by 10-20 points
			decrease := 10 + (mastery.MasteryScore / 10)
			if decrease > 20 {
				decrease = 20
			}
			newScore = mastery.MasteryScore - decrease
		}
	}

	// Calculate next review date based on mastery score
	var nextReviewDate *time.Time
	if newScore < 50 {
		// Review tomorrow for low mastery
		tomorrow := time.Now().AddDate(0, 0, 1)
		nextReviewDate = &tomorrow
	} else if newScore < 80 {
		// Review in 3 days for moderate mastery
		reviewDate := time.Now().AddDate(0, 0, 3)
		nextReviewDate = &reviewDate
	} else {
		// Review in 7 days for high mastery
		reviewDate := time.Now().AddDate(0, 0, 7)
		nextReviewDate = &reviewDate
	}

	// Update mastery
	_, err = s.masteryRepo.Upsert(ctx, userID, contentID, newScore, nextReviewDate)
	if err != nil {
		return fmt.Errorf("failed to update mastery: %w", err)
	}

	return nil
}
