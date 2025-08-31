package repositories

import (
	"context"
	"database/sql"
	"fmt"
	"time"

	"lexipath-backend/internal/models"

	"github.com/google/uuid"
)

type MasteryRepository struct {
	db *sql.DB
}

func NewMasteryRepository(db *sql.DB) *MasteryRepository {
	return &MasteryRepository{db: db}
}

func (r *MasteryRepository) GetByUserAndContent(ctx context.Context, userID, contentID uuid.UUID) (*models.Mastery, error) {
	query := `
		SELECT id, user_id, content_id, mastery_score, next_review_date, last_reviewed_at, created_at, updated_at
		FROM mastery
		WHERE user_id = $1 AND content_id = $2
	`

	var mastery models.Mastery
	err := r.db.QueryRowContext(ctx, query, userID, contentID).Scan(
		&mastery.ID,
		&mastery.UserID,
		&mastery.ContentID,
		&mastery.MasteryScore,
		&mastery.NextReviewDate,
		&mastery.LastReviewedAt,
		&mastery.CreatedAt,
		&mastery.UpdatedAt,
	)

	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, fmt.Errorf("failed to get mastery: %w", err)
	}

	return &mastery, nil
}

func (r *MasteryRepository) Upsert(ctx context.Context, userID, contentID uuid.UUID, masteryScore int, nextReviewDate *time.Time) (*models.Mastery, error) {
	// Clamp mastery score between 0 and 100
	if masteryScore < 0 {
		masteryScore = 0
	}
	if masteryScore > 100 {
		masteryScore = 100
	}

	now := time.Now().UTC()
	mastery := &models.Mastery{
		ID:             uuid.New(),
		UserID:         userID,
		ContentID:      contentID,
		MasteryScore:   masteryScore,
		NextReviewDate: nextReviewDate,
		LastReviewedAt: &now,
		CreatedAt:      now,
		UpdatedAt:      now,
	}

	query := `
		INSERT INTO mastery (id, user_id, content_id, mastery_score, next_review_date, last_reviewed_at, created_at, updated_at)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
		ON CONFLICT (user_id, content_id)
		DO UPDATE SET
			mastery_score = EXCLUDED.mastery_score,
			next_review_date = EXCLUDED.next_review_date,
			last_reviewed_at = EXCLUDED.last_reviewed_at,
			updated_at = EXCLUDED.updated_at
		RETURNING id, created_at
	`

	err := r.db.QueryRowContext(ctx, query,
		mastery.ID,
		mastery.UserID,
		mastery.ContentID,
		mastery.MasteryScore,
		mastery.NextReviewDate,
		mastery.LastReviewedAt,
		mastery.CreatedAt,
		mastery.UpdatedAt,
	).Scan(&mastery.ID, &mastery.CreatedAt)

	if err != nil {
		return nil, fmt.Errorf("failed to upsert mastery: %w", err)
	}

	return mastery, nil
}

func (r *MasteryRepository) GetUserMasteryStats(ctx context.Context, userID uuid.UUID) (map[string]int, error) {
	query := `
		SELECT 
			CASE 
				WHEN mastery_score >= 80 THEN 'strong'
				WHEN mastery_score >= 50 THEN 'moderate'
				ELSE 'weak'
			END as bucket,
			COUNT(*) as count
		FROM mastery
		WHERE user_id = $1
		GROUP BY bucket
	`

	rows, err := r.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, fmt.Errorf("failed to get mastery stats: %w", err)
	}
	defer rows.Close()

	stats := make(map[string]int)
	for rows.Next() {
		var bucket string
		var count int
		if err := rows.Scan(&bucket, &count); err != nil {
			return nil, fmt.Errorf("failed to scan mastery stats: %w", err)
		}
		stats[bucket] = count
	}

	return stats, nil
}

func (r *MasteryRepository) GetContentForReview(ctx context.Context, userID uuid.UUID, date time.Time) ([]uuid.UUID, error) {
	query := `
		SELECT content_id
		FROM mastery
		WHERE user_id = $1 
		AND next_review_date IS NOT NULL 
		AND next_review_date::date <= $2::date
		ORDER BY next_review_date ASC
	`

	rows, err := r.db.QueryContext(ctx, query, userID, date)
	if err != nil {
		return nil, fmt.Errorf("failed to get content for review: %w", err)
	}
	defer rows.Close()

	var contentIDs []uuid.UUID
	for rows.Next() {
		var contentID uuid.UUID
		if err := rows.Scan(&contentID); err != nil {
			return nil, fmt.Errorf("failed to scan content ID: %w", err)
		}
		contentIDs = append(contentIDs, contentID)
	}

	return contentIDs, nil
}
