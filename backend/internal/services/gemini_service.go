package services

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"lexipath-backend/internal/models"

	"go.uber.org/zap"
)

type GeminiService struct {
	apiKey     string
	httpClient *http.Client
	logger     *zap.Logger
}

type GeminiRequest struct {
	Contents []GeminiContent `json:"contents"`
}

type GeminiContent struct {
	Parts []GeminiPart `json:"parts"`
}

type GeminiPart struct {
	Text string `json:"text"`
}

type GeminiResponse struct {
	Candidates []GeminiCandidate `json:"candidates"`
}

type GeminiCandidate struct {
	Content GeminiContent `json:"content"`
}

func NewGeminiService(apiKey string, logger *zap.Logger) *GeminiService {
	return &GeminiService{
		apiKey: apiKey,
		httpClient: &http.Client{
			Timeout: 30 * time.Second,
		},
		logger: logger,
	}
}

func (s *GeminiService) GenerateDailyContent(ctx context.Context, profile *models.Profile) (*models.GeminiDailyContentResponse, error) {
	var prompt string
	if profile.GoalType == models.GoalTypeLanguage {
		prompt = s.buildLanguagePrompt(profile)
	} else {
		prompt = s.buildIndustryPrompt(profile)
	}

	response, err := s.callGemini(ctx, prompt)
	if err != nil {
		return nil, err
	}

	var contentResp models.GeminiDailyContentResponse
	if err := json.Unmarshal([]byte(response), &contentResp); err != nil {
		return nil, fmt.Errorf("failed to parse Gemini response: %w", err)
	}

	// Validate response
	if err := s.validateDailyContentResponse(&contentResp, profile); err != nil {
		return nil, fmt.Errorf("invalid Gemini response: %w", err)
	}

	return &contentResp, nil
}

func (s *GeminiService) GenerateQuiz(ctx context.Context, content *models.DailyContent, quizType models.QuizType) (*models.GeminiQuizResponse, error) {
	prompt := s.buildQuizPrompt(content, quizType)

	response, err := s.callGemini(ctx, prompt)
	if err != nil {
		return nil, err
	}

	var quizResp models.GeminiQuizResponse
	if err := json.Unmarshal([]byte(response), &quizResp); err != nil {
		return nil, fmt.Errorf("failed to parse quiz response: %w", err)
	}

	return &quizResp, nil
}

func (s *GeminiService) Translate(ctx context.Context, text, targetLang, baseLang string) (*models.GeminiTranslateResponse, error) {
	prompt := fmt.Sprintf(`Translate the following text from %s to %s. Return only a JSON object with the translation.

Text: %s

Required JSON format:
{
  "translation": "translated text here"
}`, baseLang, targetLang, text)

	response, err := s.callGemini(ctx, prompt)
	if err != nil {
		return nil, err
	}

	var translateResp models.GeminiTranslateResponse
	if err := json.Unmarshal([]byte(response), &translateResp); err != nil {
		return nil, fmt.Errorf("failed to parse translate response: %w", err)
	}

	return &translateResp, nil
}

func (s *GeminiService) callGemini(ctx context.Context, prompt string) (string, error) {
	url := fmt.Sprintf("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=%s", s.apiKey)

	reqBody := GeminiRequest{
		Contents: []GeminiContent{
			{
				Parts: []GeminiPart{
					{Text: prompt},
				},
			},
		},
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return "", fmt.Errorf("failed to marshal request: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return "", fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")

	// Retry logic with exponential backoff
	var resp *http.Response
	for attempt := 0; attempt < 3; attempt++ {
		resp, err = s.httpClient.Do(req)
		if err == nil && resp.StatusCode == http.StatusOK {
			break
		}
		if resp != nil {
			resp.Body.Close()
		}
		
		backoff := time.Duration(1<<attempt) * time.Second
		s.logger.Warn("Gemini API call failed, retrying", 
			zap.Int("attempt", attempt+1), 
			zap.Duration("backoff", backoff))
		
		select {
		case <-ctx.Done():
			return "", ctx.Err()
		case <-time.After(backoff):
		}
	}

	if err != nil {
		return "", fmt.Errorf("failed to call Gemini API after retries: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return "", fmt.Errorf("Gemini API error %d: %s", resp.StatusCode, string(body))
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", fmt.Errorf("failed to read response: %w", err)
	}

	var geminiResp GeminiResponse
	if err := json.Unmarshal(body, &geminiResp); err != nil {
		return "", fmt.Errorf("failed to parse Gemini response: %w", err)
	}

	if len(geminiResp.Candidates) == 0 || len(geminiResp.Candidates[0].Content.Parts) == 0 {
		return "", fmt.Errorf("empty response from Gemini")
	}

	return geminiResp.Candidates[0].Content.Parts[0].Text, nil
}

func (s *GeminiService) buildLanguagePrompt(profile *models.Profile) string {
	return fmt.Sprintf(`Generate daily vocabulary content for language learning. User is learning %s with base language %s at %s level.

Requirements:
- Return ONLY valid JSON, no additional text
- Word should be appropriate for %s level
- Meaning should be in %s
- Provide 2-3 examples in %s (examples_target)
- Provide 2-3 examples in %s (examples_base)
- Keep examples under 15 words each

Required JSON format:
{
  "word": "vocabulary word in %s",
  "meaning": "meaning/definition in %s", 
  "examples_target": ["example 1 in %s", "example 2 in %s"],
  "examples_base": ["example 1 in %s", "example 2 in %s"]
}`, *profile.TargetLang, *profile.BaseLang, profile.Level, profile.Level, *profile.BaseLang, *profile.TargetLang, *profile.BaseLang, *profile.TargetLang, *profile.BaseLang, *profile.TargetLang, *profile.TargetLang, *profile.BaseLang, *profile.BaseLang)
}

func (s *GeminiService) buildIndustryPrompt(profile *models.Profile) string {
	return fmt.Sprintf(`Generate daily vocabulary content for %s industry professionals.

Requirements:
- Return ONLY valid JSON, no additional text
- Word should be industry-specific technical term
- Meaning should be professional definition
- Provide 2-3 professional examples
- Keep examples under 20 words each
- All content in English

Required JSON format:
{
  "word": "industry term",
  "meaning": "professional definition",
  "examples_target": ["professional example 1", "professional example 2", "professional example 3"]
}`, *profile.IndustrySector)
}

func (s *GeminiService) buildQuizPrompt(content *models.DailyContent, quizType models.QuizType) string {
	switch quizType {
	case models.QuizTypeMCQ:
		return fmt.Sprintf(`Create a multiple choice question for the word "%s" with meaning "%s".

Requirements:
- Return ONLY valid JSON
- Question should test understanding
- Provide 4 options with only 1 correct
- Make distractors plausible

Required JSON format:
{
  "question": "What does '%s' mean?",
  "options": ["correct answer", "distractor 1", "distractor 2", "distractor 3"],
  "correct_answer": "correct answer"
}`, content.Word, content.Meaning, content.Word)

	case models.QuizTypeFillBlank:
		return fmt.Sprintf(`Create a fill-in-the-blank question using one of these examples: %v

Requirements:
- Return ONLY valid JSON
- Replace the word "%s" with _____ in the sentence
- Question should be clear

Required JSON format:
{
  "question": "Fill in the blank: [sentence with _____ replacing the word]",
  "correct_answer": "%s"
}`, content.ExamplesTarget, content.Word, content.Word)

	case models.QuizTypeSituation:
		return fmt.Sprintf(`Create a situational question for the word "%s".

Requirements:
- Return ONLY valid JSON
- Describe a situation where this word would be used
- Ask user to identify the appropriate word

Required JSON format:
{
  "question": "In what situation would you use the word that means '%s'?",
  "correct_answer": "%s"
}`, content.Word, content.Meaning, content.Word)

	default:
		return ""
	}
}

func (s *GeminiService) validateDailyContentResponse(resp *models.GeminiDailyContentResponse, profile *models.Profile) error {
	if resp.Word == "" {
		return fmt.Errorf("word is required")
	}
	if resp.Meaning == "" {
		return fmt.Errorf("meaning is required")
	}
	if len(resp.ExamplesTarget) == 0 {
		return fmt.Errorf("examples_target is required")
	}
	if profile.GoalType == models.GoalTypeLanguage && len(resp.ExamplesBase) == 0 {
		return fmt.Errorf("examples_base is required for language learning")
	}
	return nil
}
