package services

import (
	"context"
	"fmt"

	"lexipath-backend/internal/models"
	"lexipath-backend/internal/repositories"

	firebase "firebase.google.com/go/v4"
	"firebase.google.com/go/v4/auth"
	"google.golang.org/api/option"
)

type AuthService struct {
	authClient *auth.Client
	userRepo   *repositories.UserRepository
}

func NewFirebaseApp(projectID string) (*firebase.App, error) {
	config := &firebase.Config{
		ProjectID: projectID,
	}

	app, err := firebase.NewApp(context.Background(), config, option.WithCredentialsFile("firebase-service-account.json"))
	if err != nil {
		return nil, fmt.Errorf("failed to initialize Firebase app: %w", err)
	}

	return app, nil
}

func NewAuthService(app *firebase.App, userRepo *repositories.UserRepository) *AuthService {
	authClient, err := app.Auth(context.Background())
	if err != nil {
		panic(fmt.Sprintf("Failed to initialize Firebase Auth client: %v", err))
	}

	return &AuthService{
		authClient: authClient,
		userRepo:   userRepo,
	}
}

func (s *AuthService) VerifyToken(ctx context.Context, idToken string) (*models.User, error) {
	token, err := s.authClient.VerifyIDToken(ctx, idToken)
	if err != nil {
		return nil, fmt.Errorf("failed to verify ID token: %w", err)
	}

	// Get or create user
	user, err := s.userRepo.GetOrCreate(ctx, token.UID, token.Claims["email"].(string))
	if err != nil {
		return nil, fmt.Errorf("failed to get or create user: %w", err)
	}

	return user, nil
}
