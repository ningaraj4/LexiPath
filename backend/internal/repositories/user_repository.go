package repositories

import (
	"context"
	"database/sql"
	"fmt"
	"time"

	"lexipath-backend/internal/models"

	"github.com/google/uuid"
)

type UserRepository struct {
	db *sql.DB
}

func NewUserRepository(db *sql.DB) *UserRepository {
	return &UserRepository{db: db}
}

func (r *UserRepository) GetByFirebaseUID(ctx context.Context, firebaseUID string) (*models.User, error) {
	query := `
		SELECT id, firebase_uid, email, created_at, updated_at
		FROM users
		WHERE firebase_uid = $1
	`

	var user models.User
	err := r.db.QueryRowContext(ctx, query, firebaseUID).Scan(
		&user.ID,
		&user.FirebaseUID,
		&user.Email,
		&user.CreatedAt,
		&user.UpdatedAt,
	)

	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, fmt.Errorf("failed to get user by firebase_uid: %w", err)
	}

	return &user, nil
}

func (r *UserRepository) Create(ctx context.Context, firebaseUID, email string) (*models.User, error) {
	user := &models.User{
		ID:          uuid.New(),
		FirebaseUID: firebaseUID,
		Email:       email,
		CreatedAt:   time.Now().UTC(),
		UpdatedAt:   time.Now().UTC(),
	}

	query := `
		INSERT INTO users (id, firebase_uid, email, created_at, updated_at)
		VALUES ($1, $2, $3, $4, $5)
	`

	_, err := r.db.ExecContext(ctx, query,
		user.ID,
		user.FirebaseUID,
		user.Email,
		user.CreatedAt,
		user.UpdatedAt,
	)

	if err != nil {
		return nil, fmt.Errorf("failed to create user: %w", err)
	}

	return user, nil
}

func (r *UserRepository) GetOrCreate(ctx context.Context, firebaseUID, email string) (*models.User, error) {
	user, err := r.GetByFirebaseUID(ctx, firebaseUID)
	if err != nil {
		return nil, err
	}

	if user == nil {
		user, err = r.Create(ctx, firebaseUID, email)
		if err != nil {
			return nil, err
		}
	}

	return user, nil
}
