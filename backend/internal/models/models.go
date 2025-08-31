package models

import (
	"time"

	"github.com/google/uuid"
)

type User struct {
	ID          uuid.UUID `json:"id" db:"id"`
	FirebaseUID string    `json:"firebase_uid" db:"firebase_uid"`
	Email       string    `json:"email" db:"email"`
	CreatedAt   time.Time `json:"created_at" db:"created_at"`
	UpdatedAt   time.Time `json:"updated_at" db:"updated_at"`
}

type GoalType string

const (
	GoalTypeLanguage GoalType = "language"
	GoalTypeIndustry GoalType = "industry"
)

type LanguageLevel string

const (
	LanguageLevelBeginner     LanguageLevel = "beginner"
	LanguageLevelIntermediate LanguageLevel = "intermediate"
	LanguageLevelAdvanced     LanguageLevel = "advanced"
)

type Profile struct {
	ID           uuid.UUID     `json:"id" db:"id"`
	UserID       uuid.UUID     `json:"user_id" db:"user_id"`
	GoalType     GoalType      `json:"goal_type" db:"goal_type"`
	TargetLang   *string       `json:"target_lang,omitempty" db:"target_lang"`
	BaseLang     *string       `json:"base_lang,omitempty" db:"base_lang"`
	Level        LanguageLevel `json:"level" db:"level"`
	IndustrySector *string     `json:"industry_sector,omitempty" db:"industry_sector"`
	CreatedAt    time.Time     `json:"created_at" db:"created_at"`
	UpdatedAt    time.Time     `json:"updated_at" db:"updated_at"`
}

type DailyContent struct {
	ID          uuid.UUID `json:"id" db:"id"`
	UserID      uuid.UUID `json:"user_id" db:"user_id"`
	Date        time.Time `json:"date" db:"date"`
	Word        string    `json:"word" db:"word"`
	Meaning     string    `json:"meaning" db:"meaning"`
	ExamplesTarget []string `json:"examples_target" db:"examples_target"`
	ExamplesBase   []string `json:"examples_base,omitempty" db:"examples_base"`
	CreatedAt   time.Time `json:"created_at" db:"created_at"`
}

type QuizType string

const (
	QuizTypeMCQ        QuizType = "mcq"
	QuizTypeFillBlank  QuizType = "fill_blank"
	QuizTypeSituation  QuizType = "situation"
)

type QuizLog struct {
	ID          uuid.UUID `json:"id" db:"id"`
	UserID      uuid.UUID `json:"user_id" db:"user_id"`
	ContentID   uuid.UUID `json:"content_id" db:"content_id"`
	QuizType    QuizType  `json:"quiz_type" db:"quiz_type"`
	Question    string    `json:"question" db:"question"`
	Options     []string  `json:"options,omitempty" db:"options"`
	CorrectAnswer string  `json:"correct_answer" db:"correct_answer"`
	UserAnswer  string    `json:"user_answer" db:"user_answer"`
	IsCorrect   bool      `json:"is_correct" db:"is_correct"`
	CreatedAt   time.Time `json:"created_at" db:"created_at"`
}

type Mastery struct {
	ID              uuid.UUID  `json:"id" db:"id"`
	UserID          uuid.UUID  `json:"user_id" db:"user_id"`
	ContentID       uuid.UUID  `json:"content_id" db:"content_id"`
	MasteryScore    int        `json:"mastery_score" db:"mastery_score"`
	NextReviewDate  *time.Time `json:"next_review_date,omitempty" db:"next_review_date"`
	LastReviewedAt  *time.Time `json:"last_reviewed_at,omitempty" db:"last_reviewed_at"`
	CreatedAt       time.Time  `json:"created_at" db:"created_at"`
	UpdatedAt       time.Time  `json:"updated_at" db:"updated_at"`
}

type WeeklyPlan struct {
	ID        uuid.UUID `json:"id" db:"id"`
	UserID    uuid.UUID `json:"user_id" db:"user_id"`
	WeekStart time.Time `json:"week_start" db:"week_start"`
	Plan      []PlanItem `json:"plan" db:"plan"`
	CreatedAt time.Time `json:"created_at" db:"created_at"`
}

type PlanItem struct {
	Date        time.Time   `json:"date"`
	ContentIDs  []uuid.UUID `json:"content_ids"`
	IsReviewDay bool        `json:"is_review_day"`
}

// Request/Response DTOs
type UpsertProfileRequest struct {
	GoalType       GoalType      `json:"goal_type" binding:"required"`
	TargetLang     *string       `json:"target_lang,omitempty"`
	BaseLang       *string       `json:"base_lang,omitempty"`
	Level          LanguageLevel `json:"level" binding:"required"`
	IndustrySector *string       `json:"industry_sector,omitempty"`
}

type DailyContentRequest struct {
	Date time.Time `json:"date" binding:"required"`
}

type QuizSubmissionRequest struct {
	ContentID  uuid.UUID `json:"content_id" binding:"required"`
	QuizType   QuizType  `json:"quiz_type" binding:"required"`
	UserAnswer string    `json:"user_answer" binding:"required"`
}

type TranslateRequest struct {
	Text       string `json:"text" binding:"required"`
	TargetLang string `json:"target_lang" binding:"required"`
	BaseLang   string `json:"base_lang" binding:"required"`
}

type TranslateResponse struct {
	Translation string `json:"translation"`
}

// Gemini API Response Structures
type GeminiDailyContentResponse struct {
	Word           string   `json:"word"`
	Meaning        string   `json:"meaning"`
	ExamplesTarget []string `json:"examples_target"`
	ExamplesBase   []string `json:"examples_base,omitempty"`
}

type GeminiQuizResponse struct {
	Question      string   `json:"question"`
	Options       []string `json:"options,omitempty"`
	CorrectAnswer string   `json:"correct_answer"`
}

type GeminiTranslateResponse struct {
	Translation string `json:"translation"`
}
