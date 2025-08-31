package config

import (
	"os"
	"strconv"
)

type Config struct {
	Port              string
	Environment       string
	DatabaseURL       string
	RedisURL          string
	GeminiAPIKey      string
	FirebaseProjectID string
	Timezone          string
}

func Load() (*Config, error) {
	cfg := &Config{
		Port:              getEnv("PORT", "8080"),
		Environment:       getEnv("ENVIRONMENT", "development"),
		DatabaseURL:       getEnv("DATABASE_URL", "postgres://user:pass@localhost:5432/lexipath?sslmode=disable"),
		RedisURL:          getEnv("REDIS_URL", "redis://localhost:6379/0"),
		GeminiAPIKey:      getEnv("GEMINI_API_KEY", ""),
		FirebaseProjectID: getEnv("FIREBASE_PROJECT_ID", ""),
		Timezone:          getEnv("TIMEZONE", "Asia/Kolkata"),
	}

	return cfg, nil
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}

func getEnvAsInt(key string, defaultValue int) int {
	if value := os.Getenv(key); value != "" {
		if intValue, err := strconv.Atoi(value); err == nil {
			return intValue
		}
	}
	return defaultValue
}

func getEnvAsBool(key string, defaultValue bool) bool {
	if value := os.Getenv(key); value != "" {
		if boolValue, err := strconv.ParseBool(value); err == nil {
			return boolValue
		}
	}
	return defaultValue
}
