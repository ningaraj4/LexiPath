package repositories

import (
	"context"
	"database/sql"
	"fmt"
	"time"

	"lexipath-backend/internal/models"

	"github.com/google/uuid"
	"github.com/lib/pq"
)

type ContentRepository struct {
	db *sql.DB
}

func NewContentRepository(db *sql.DB) *ContentRepository {
	return &ContentRepository{db: db}
}

func (r *ContentRepository) GetByUserAndDate(ctx context.Context, userID uuid.UUID, date time.Time) (*models.DailyContent, error) {
	query := `
		SELECT id, user_id, date, word, meaning, examples_target, examples_base, created_at
		FROM daily_content
		WHERE user_id = $1 AND date::date = $2::date
	`

	var content models.DailyContent
	err := r.db.QueryRowContext(ctx, query, userID, date).Scan(
		&content.ID,
		&content.UserID,
		&content.Date,
		&content.Word,
		&content.Meaning,
		pq.Array(&content.ExamplesTarget),
		pq.Array(&content.ExamplesBase),
		&content.CreatedAt,
	)

	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, fmt.Errorf("failed to get daily content: %w", err)
	}

	return &content, nil
}

func (r *ContentRepository) Create(ctx context.Context, userID uuid.UUID, date time.Time, geminiResp *models.GeminiDailyContentResponse) (*models.DailyContent, error) {
	content := &models.DailyContent{
		ID:             uuid.New(),
		UserID:         userID,
		Date:           date,
		Word:           geminiResp.Word,
		Meaning:        geminiResp.Meaning,
		ExamplesTarget: geminiResp.ExamplesTarget,
		ExamplesBase:   geminiResp.ExamplesBase,
		CreatedAt:      time.Now().UTC(),
	}

	query := `
		INSERT INTO daily_content (id, user_id, date, word, meaning, examples_target, examples_base, created_at)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
	`

	_, err := r.db.ExecContext(ctx, query,
		content.ID,
		content.UserID,
		content.Date,
		content.Word,
		content.Meaning,
		pq.Array(content.ExamplesTarget),
		pq.Array(content.ExamplesBase),
		content.CreatedAt,
	)

	if err != nil {
		return nil, fmt.Errorf("failed to create daily content: %w", err)
	}

	return content, nil
}

func (r *ContentRepository) GetHistoryByUser(ctx context.Context, userID uuid.UUID, limit, offset int) ([]*models.DailyContent, error) {
	query := `
		SELECT id, user_id, date, word, meaning, examples_target, examples_base, created_at
		FROM daily_content
		WHERE user_id = $1
		ORDER BY date DESC
		LIMIT $2 OFFSET $3
	`

	rows, err := r.db.QueryContext(ctx, query, userID, limit, offset)
	if err != nil {
		return nil, fmt.Errorf("failed to get content history: %w", err)
	}
	defer rows.Close()

	var contents []*models.DailyContent
	for rows.Next() {
		var content models.DailyContent
		err := rows.Scan(
			&content.ID,
			&content.UserID,
			&content.Date,
			&content.Word,
			&content.Meaning,
			pq.Array(&content.ExamplesTarget),
			pq.Array(&content.ExamplesBase),
			&content.CreatedAt,
		)
		if err != nil {
			return nil, fmt.Errorf("failed to scan content row: %w", err)
		}
		contents = append(contents, &content)
	}

	return contents, nil
}
