package repositories

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"lexipath-backend/internal/models"

	"github.com/go-redis/redis/v8"
	"github.com/google/uuid"
)

type CacheRepository struct {
	client *redis.Client
}

func NewCacheRepository(client *redis.Client) *CacheRepository {
	return &CacheRepository{client: client}
}

func (r *CacheRepository) GetDailyContent(ctx context.Context, userID uuid.UUID, date time.Time) (*models.DailyContent, error) {
	key := fmt.Sprintf("daily_content:%s:%s", userID.String(), date.Format("2006-01-02"))
	
	data, err := r.client.Get(ctx, key).Result()
	if err == redis.Nil {
		return nil, nil
	}
	if err != nil {
		return nil, fmt.Errorf("failed to get cached daily content: %w", err)
	}

	var content models.DailyContent
	if err := json.Unmarshal([]byte(data), &content); err != nil {
		return nil, fmt.Errorf("failed to unmarshal cached content: %w", err)
	}

	return &content, nil
}

func (r *CacheRepository) SetDailyContent(ctx context.Context, content *models.DailyContent) error {
	key := fmt.Sprintf("daily_content:%s:%s", content.UserID.String(), content.Date.Format("2006-01-02"))
	
	data, err := json.Marshal(content)
	if err != nil {
		return fmt.Errorf("failed to marshal content for cache: %w", err)
	}

	// Cache for 24 hours
	if err := r.client.Set(ctx, key, data, 24*time.Hour).Err(); err != nil {
		return fmt.Errorf("failed to cache daily content: %w", err)
	}

	return nil
}

func (r *CacheRepository) IncrementRateLimit(ctx context.Context, userID uuid.UUID, endpoint string) (int, error) {
	key := fmt.Sprintf("rate_limit:%s:%s:%s", userID.String(), endpoint, time.Now().Format("2006-01-02"))
	
	count, err := r.client.Incr(ctx, key).Result()
	if err != nil {
		return 0, fmt.Errorf("failed to increment rate limit: %w", err)
	}

	// Set expiry on first increment
	if count == 1 {
		r.client.Expire(ctx, key, 24*time.Hour)
	}

	return int(count), nil
}

func (r *CacheRepository) GetRateLimit(ctx context.Context, userID uuid.UUID, endpoint string) (int, error) {
	key := fmt.Sprintf("rate_limit:%s:%s:%s", userID.String(), endpoint, time.Now().Format("2006-01-02"))
	
	count, err := r.client.Get(ctx, key).Int()
	if err == redis.Nil {
		return 0, nil
	}
	if err != nil {
		return 0, fmt.Errorf("failed to get rate limit: %w", err)
	}

	return count, nil
}
