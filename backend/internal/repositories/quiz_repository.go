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

type QuizRepository struct {
	db *sql.DB
}

func NewQuizRepository(db *sql.DB) *QuizRepository {
	return &QuizRepository{db: db}
}

func (r *QuizRepository) Create(ctx context.Context, userID, contentID uuid.UUID, quizType models.QuizType, question string, options []string, correctAnswer, userAnswer string, isCorrect bool) (*models.QuizLog, error) {
	quiz := &models.QuizLog{
		ID:            uuid.New(),
		UserID:        userID,
		ContentID:     contentID,
		QuizType:      quizType,
		Question:      question,
		Options:       options,
		CorrectAnswer: correctAnswer,
		UserAnswer:    userAnswer,
		IsCorrect:     isCorrect,
		CreatedAt:     time.Now().UTC(),
	}

	query := `
		INSERT INTO quiz_logs (id, user_id, content_id, quiz_type, question, options, correct_answer, user_answer, is_correct, created_at)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
	`

	_, err := r.db.ExecContext(ctx, query,
		quiz.ID,
		quiz.UserID,
		quiz.ContentID,
		quiz.QuizType,
		quiz.Question,
		pq.Array(quiz.Options),
		quiz.CorrectAnswer,
		quiz.UserAnswer,
		quiz.IsCorrect,
		quiz.CreatedAt,
	)

	if err != nil {
		return nil, fmt.Errorf("failed to create quiz log: %w", err)
	}

	return quiz, nil
}

func (r *QuizRepository) GetByUserAndContent(ctx context.Context, userID, contentID uuid.UUID) ([]*models.QuizLog, error) {
	query := `
		SELECT id, user_id, content_id, quiz_type, question, options, correct_answer, user_answer, is_correct, created_at
		FROM quiz_logs
		WHERE user_id = $1 AND content_id = $2
		ORDER BY created_at DESC
	`

	rows, err := r.db.QueryContext(ctx, query, userID, contentID)
	if err != nil {
		return nil, fmt.Errorf("failed to get quiz logs: %w", err)
	}
	defer rows.Close()

	var quizzes []*models.QuizLog
	for rows.Next() {
		var quiz models.QuizLog
		err := rows.Scan(
			&quiz.ID,
			&quiz.UserID,
			&quiz.ContentID,
			&quiz.QuizType,
			&quiz.Question,
			pq.Array(&quiz.Options),
			&quiz.CorrectAnswer,
			&quiz.UserAnswer,
			&quiz.IsCorrect,
			&quiz.CreatedAt,
		)
		if err != nil {
			return nil, fmt.Errorf("failed to scan quiz row: %w", err)
		}
		quizzes = append(quizzes, &quiz)
	}

	return quizzes, nil
}
