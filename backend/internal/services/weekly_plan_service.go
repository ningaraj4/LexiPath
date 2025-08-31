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

type WeeklyPlanService struct {
	weeklyPlanRepo *repositories.WeeklyPlanRepository
	masteryRepo    *repositories.MasteryRepository
	geminiService  *GeminiService
	logger         *zap.Logger
}

func NewWeeklyPlanService(weeklyPlanRepo *repositories.WeeklyPlanRepository, masteryRepo *repositories.MasteryRepository, geminiService *GeminiService, logger *zap.Logger) *WeeklyPlanService {
	return &WeeklyPlanService{
		weeklyPlanRepo: weeklyPlanRepo,
		masteryRepo:    masteryRepo,
		geminiService:  geminiService,
		logger:         logger,
	}
}

func (s *WeeklyPlanService) GenerateWeeklyPlan(ctx context.Context, userID uuid.UUID, profile *models.Profile) (*models.WeeklyPlan, error) {
	// Only generate on Sundays
	now := time.Now()
	if now.Weekday() != time.Sunday {
		return nil, fmt.Errorf("weekly plans can only be generated on Sundays")
	}

	// Get start of current week (Sunday)
	weekStart := s.getWeekStart(now)

	// Check if plan already exists for this week
	existingPlan, err := s.weeklyPlanRepo.GetByUserAndWeek(ctx, userID, weekStart)
	if err != nil {
		return nil, fmt.Errorf("failed to check existing plan: %w", err)
	}
	if existingPlan != nil {
		return existingPlan, nil
	}

	// Get mastery statistics
	masteryStats, err := s.masteryRepo.GetUserMasteryStats(ctx, userID)
	if err != nil {
		s.logger.Warn("Failed to get mastery stats", zap.Error(err))
		masteryStats = make(map[string]int)
	}

	// Generate plan based on performance buckets
	planItems := s.generatePlanItems(weekStart, masteryStats)

	// Create weekly plan
	plan, err := s.weeklyPlanRepo.Create(ctx, userID, weekStart, planItems)
	if err != nil {
		return nil, fmt.Errorf("failed to create weekly plan: %w", err)
	}

	s.logger.Info("Generated weekly plan", 
		zap.String("user_id", userID.String()),
		zap.Time("week_start", weekStart),
		zap.Int("plan_items", len(planItems)))

	return plan, nil
}

func (s *WeeklyPlanService) GetWeeklyPlan(ctx context.Context, userID uuid.UUID, weekStart time.Time) (*models.WeeklyPlan, error) {
	return s.weeklyPlanRepo.GetByUserAndWeek(ctx, userID, weekStart)
}

func (s *WeeklyPlanService) generatePlanItems(weekStart time.Time, masteryStats map[string]int) []models.PlanItem {
	var planItems []models.PlanItem

	// Sunday is always review day
	planItems = append(planItems, models.PlanItem{
		Date:        weekStart,
		ContentIDs:  []uuid.UUID{}, // Will be populated with review content
		IsReviewDay: true,
	})

	// Generate plan for Monday-Saturday based on performance
	totalWords := masteryStats["strong"] + masteryStats["moderate"] + masteryStats["weak"]
	
	for i := 1; i <= 6; i++ {
		date := weekStart.AddDate(0, 0, i)
		isReviewDay := false

		// Inject review days mid-week based on performance
		if masteryStats["weak"] > totalWords/3 {
			// High number of weak words - review on Wednesday and Friday
			if i == 3 || i == 5 {
				isReviewDay = true
			}
		} else if masteryStats["moderate"] > totalWords/2 {
			// High number of moderate words - review on Wednesday
			if i == 3 {
				isReviewDay = true
			}
		}

		planItems = append(planItems, models.PlanItem{
			Date:        date,
			ContentIDs:  []uuid.UUID{}, // Will be populated with new content or review content
			IsReviewDay: isReviewDay,
		})
	}

	return planItems
}

func (s *WeeklyPlanService) getWeekStart(date time.Time) time.Time {
	// Get the start of the week (Sunday)
	weekday := int(date.Weekday())
	if weekday == 0 {
		weekday = 7 // Sunday = 7 for calculation
	}
	weekStart := date.AddDate(0, 0, -weekday)
	return time.Date(weekStart.Year(), weekStart.Month(), weekStart.Day(), 0, 0, 0, 0, weekStart.Location())
}
