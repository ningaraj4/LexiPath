package repositories

import (
	"context"
	"database/sql"
	"encoding/json"
	"fmt"
	"time"

	"lexipath-backend/internal/models"

	"github.com/google/uuid"
)

type WeeklyPlanRepository struct {
	db *sql.DB
}

func NewWeeklyPlanRepository(db *sql.DB) *WeeklyPlanRepository {
	return &WeeklyPlanRepository{db: db}
}

func (r *WeeklyPlanRepository) GetByUserAndWeek(ctx context.Context, userID uuid.UUID, weekStart time.Time) (*models.WeeklyPlan, error) {
	query := `
		SELECT id, user_id, week_start, plan, created_at
		FROM weekly_plans
		WHERE user_id = $1 AND week_start = $2
	`

	var plan models.WeeklyPlan
	var planJSON []byte
	err := r.db.QueryRowContext(ctx, query, userID, weekStart).Scan(
		&plan.ID,
		&plan.UserID,
		&plan.WeekStart,
		&planJSON,
		&plan.CreatedAt,
	)

	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, fmt.Errorf("failed to get weekly plan: %w", err)
	}

	if err := json.Unmarshal(planJSON, &plan.Plan); err != nil {
		return nil, fmt.Errorf("failed to unmarshal plan: %w", err)
	}

	return &plan, nil
}

func (r *WeeklyPlanRepository) Create(ctx context.Context, userID uuid.UUID, weekStart time.Time, planItems []models.PlanItem) (*models.WeeklyPlan, error) {
	plan := &models.WeeklyPlan{
		ID:        uuid.New(),
		UserID:    userID,
		WeekStart: weekStart,
		Plan:      planItems,
		CreatedAt: time.Now().UTC(),
	}

	planJSON, err := json.Marshal(plan.Plan)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal plan: %w", err)
	}

	query := `
		INSERT INTO weekly_plans (id, user_id, week_start, plan, created_at)
		VALUES ($1, $2, $3, $4, $5)
		ON CONFLICT (user_id, week_start)
		DO UPDATE SET
			plan = EXCLUDED.plan,
			created_at = EXCLUDED.created_at
		RETURNING id, created_at
	`

	err = r.db.QueryRowContext(ctx, query,
		plan.ID,
		plan.UserID,
		plan.WeekStart,
		planJSON,
		plan.CreatedAt,
	).Scan(&plan.ID, &plan.CreatedAt)

	if err != nil {
		return nil, fmt.Errorf("failed to create weekly plan: %w", err)
	}

	return plan, nil
}
