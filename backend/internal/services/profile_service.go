package services

import (
	"context"
	"fmt"

	"lexipath-backend/internal/models"
	"lexipath-backend/internal/repositories"

	"github.com/google/uuid"
)

type ProfileService struct {
	profileRepo *repositories.ProfileRepository
	userRepo    *repositories.UserRepository
}

func NewProfileService(profileRepo *repositories.ProfileRepository, userRepo *repositories.UserRepository) *ProfileService {
	return &ProfileService{
		profileRepo: profileRepo,
		userRepo:    userRepo,
	}
}

func (s *ProfileService) UpsertProfile(ctx context.Context, userID uuid.UUID, req *models.UpsertProfileRequest) (*models.Profile, error) {
	// Validate request based on goal type
	if err := s.validateProfileRequest(req); err != nil {
		return nil, fmt.Errorf("invalid profile request: %w", err)
	}

	profile, err := s.profileRepo.Upsert(ctx, userID, req)
	if err != nil {
		return nil, fmt.Errorf("failed to upsert profile: %w", err)
	}

	return profile, nil
}

func (s *ProfileService) GetProfile(ctx context.Context, userID uuid.UUID) (*models.Profile, error) {
	profile, err := s.profileRepo.GetByUserID(ctx, userID)
	if err != nil {
		return nil, fmt.Errorf("failed to get profile: %w", err)
	}

	return profile, nil
}

func (s *ProfileService) validateProfileRequest(req *models.UpsertProfileRequest) error {
	if req.GoalType == models.GoalTypeLanguage {
		if req.TargetLang == nil || *req.TargetLang == "" {
			return fmt.Errorf("target_lang is required for language learning")
		}
		if req.BaseLang == nil || *req.BaseLang == "" {
			return fmt.Errorf("base_lang is required for language learning")
		}
	} else if req.GoalType == models.GoalTypeIndustry {
		if req.IndustrySector == nil || *req.IndustrySector == "" {
			return fmt.Errorf("industry_sector is required for industry learning")
		}
	}

	return nil
}
