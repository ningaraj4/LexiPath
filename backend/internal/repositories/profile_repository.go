package repositories

import (
	"context"
	"database/sql"
	"fmt"
	"time"

	"lexipath-backend/internal/models"

	"github.com/google/uuid"
)

type ProfileRepository struct {
	db *sql.DB
}

func NewProfileRepository(db *sql.DB) *ProfileRepository {
	return &ProfileRepository{db: db}
}

func (r *ProfileRepository) GetByUserID(ctx context.Context, userID uuid.UUID) (*models.Profile, error) {
	query := `
		SELECT id, user_id, goal_type, target_lang, base_lang, level, industry_sector, created_at, updated_at
		FROM profiles
		WHERE user_id = $1
	`

	var profile models.Profile
	err := r.db.QueryRowContext(ctx, query, userID).Scan(
		&profile.ID,
		&profile.UserID,
		&profile.GoalType,
		&profile.TargetLang,
		&profile.BaseLang,
		&profile.Level,
		&profile.IndustrySector,
		&profile.CreatedAt,
		&profile.UpdatedAt,
	)

	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, fmt.Errorf("failed to get profile by user_id: %w", err)
	}

	return &profile, nil
}

func (r *ProfileRepository) Upsert(ctx context.Context, userID uuid.UUID, req *models.UpsertProfileRequest) (*models.Profile, error) {
	profile := &models.Profile{
		ID:             uuid.New(),
		UserID:         userID,
		GoalType:       req.GoalType,
		TargetLang:     req.TargetLang,
		BaseLang:       req.BaseLang,
		Level:          req.Level,
		IndustrySector: req.IndustrySector,
		CreatedAt:      time.Now().UTC(),
		UpdatedAt:      time.Now().UTC(),
	}

	query := `
		INSERT INTO profiles (id, user_id, goal_type, target_lang, base_lang, level, industry_sector, created_at, updated_at)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
		ON CONFLICT (user_id)
		DO UPDATE SET
			goal_type = EXCLUDED.goal_type,
			target_lang = EXCLUDED.target_lang,
			base_lang = EXCLUDED.base_lang,
			level = EXCLUDED.level,
			industry_sector = EXCLUDED.industry_sector,
			updated_at = EXCLUDED.updated_at
		RETURNING id, created_at
	`

	err := r.db.QueryRowContext(ctx, query,
		profile.ID,
		profile.UserID,
		profile.GoalType,
		profile.TargetLang,
		profile.BaseLang,
		profile.Level,
		profile.IndustrySector,
		profile.CreatedAt,
		profile.UpdatedAt,
	).Scan(&profile.ID, &profile.CreatedAt)

	if err != nil {
		return nil, fmt.Errorf("failed to upsert profile: %w", err)
	}

	return profile, nil
}
