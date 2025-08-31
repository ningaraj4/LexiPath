# LexiPath API Contract

This document defines the complete API contract for the LexiPath backend service, including request/response schemas, authentication requirements, and error handling.

## Base URL
- **Development**: `http://localhost:8080`
- **Production**: `https://your-backend.run.app`

## Authentication

All API endpoints require Firebase ID token authentication unless otherwise specified.

### Headers
```http
Authorization: Bearer <firebase-id-token>
Content-Type: application/json
```

### Error Responses
```json
{
  "error": "error message",
  "code": "ERROR_CODE",
  "details": "additional details"
}
```

## Health & Monitoring

### Health Check
```http
GET /health
```

**Response (200 OK)**:
```json
{
  "status": "healthy",
  "timestamp": "2024-01-15T10:30:00Z",
  "version": "1.0.0"
}
```

### Metrics
```http
GET /metrics
```
Returns Prometheus metrics (text/plain format).

## Profile Management

### Submit Profile
Create or update user profile with learning preferences.

```http
POST /api/profile
Content-Type: application/json
```

**Request Body**:
```json
{
  "goalType": "bilingual_language_track",
  "languageLevel": "intermediate",
  "targetLanguage": "Spanish",
  "nativeLanguage": "English",
  "industry": null
}
```

**Goal Types**:
- `bilingual_language_track` - Language learning track
- `monolingual_industry_track` - Industry-specific vocabulary

**Language Levels**:
- `beginner` - A1-A2 level
- `intermediate` - B1-B2 level
- `advanced` - C1-C2 level

**Response (200 OK)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "firebase-user-id",
  "goalType": "bilingual_language_track",
  "languageLevel": "intermediate",
  "targetLanguage": "Spanish",
  "nativeLanguage": "English",
  "industry": null,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### Get Profile
```http
GET /api/profile
```

**Response (200 OK)**: Same as submit profile response.

**Response (404 Not Found)**:
```json
{
  "error": "Profile not found",
  "code": "PROFILE_NOT_FOUND"
}
```

## Daily Content

### Get Today's Content
Retrieve daily vocabulary content for specified date.

```http
GET /api/content/today?date=2024-01-15
```

**Query Parameters**:
- `date` (required): Date in YYYY-MM-DD format

**Response (200 OK)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "word": "serendipity",
  "meaning": "The occurrence of events by chance in a happy way",
  "pronunciation": "/ˌserənˈdipədē/",
  "date": "2024-01-15",
  "examplesTarget": [
    "It was pure serendipity that we met at the coffee shop.",
    "The discovery was a result of serendipity rather than planning."
  ],
  "examplesNative": [
    "Fue pura casualidad que nos encontráramos en la cafetería.",
    "El descubrimiento fue resultado de la casualidad más que de la planificación."
  ]
}
```

### Get Content History
Retrieve paginated history of daily content.

```http
GET /api/content/history?limit=20&offset=0
```

**Query Parameters**:
- `limit` (optional): Number of items to return (default: 20, max: 100)
- `offset` (optional): Number of items to skip (default: 0)

**Response (200 OK)**:
```json
{
  "items": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "word": "serendipity",
      "meaning": "The occurrence of events by chance in a happy way",
      "date": "2024-01-15",
      "examplesTarget": ["..."],
      "examplesNative": ["..."]
    }
  ],
  "total": 150,
  "limit": 20,
  "offset": 0,
  "hasMore": true
}
```

## Quiz System

### Generate Quiz Question
Generate a quiz question for specific content.

```http
POST /api/quiz/generate
Content-Type: application/json
```

**Request Body**:
```json
{
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "type": "mcq"
}
```

**Quiz Types**:
- `mcq` - Multiple choice question
- `fill_blank` - Fill in the blank
- `situation` - Situational usage

**Response (200 OK)**:
```json
{
  "question": "What does 'serendipity' mean?",
  "type": "mcq",
  "options": [
    "The occurrence of events by chance in a happy way",
    "A planned sequence of events",
    "An unfortunate coincidence",
    "A scientific discovery method"
  ],
  "correctAnswer": "The occurrence of events by chance in a happy way"
}
```

### Submit Quiz Answer
Submit quiz answer and receive feedback with mastery update.

```http
POST /api/quiz/submit
Content-Type: application/json
```

**Request Body**:
```json
{
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "type": "mcq",
  "userAnswer": "The occurrence of events by chance in a happy way",
  "isCorrect": true,
  "timeSpentMs": 5000
}
```

**Response (200 OK)**:
```json
{
  "isCorrect": true,
  "correctAnswer": "The occurrence of events by chance in a happy way",
  "explanation": "Excellent! You correctly identified the meaning of serendipity.",
  "masteryUpdate": {
    "wordId": "serendipity",
    "oldScore": 65,
    "newScore": 75,
    "change": 10
  }
}
```

## Mastery Tracking

### Get Mastery Stats
Retrieve user's overall mastery statistics.

```http
GET /api/mastery/stats
```

**Response (200 OK)**:
```json
{
  "totalWords": 150,
  "masteredWords": 45,
  "averageScore": 72.5,
  "streakDays": 12,
  "levelDistribution": {
    "beginner": 30,
    "intermediate": 75,
    "advanced": 45
  }
}
```

### Get Word Mastery
Get mastery level for specific word.

```http
GET /api/mastery/word/{wordId}
```

**Response (200 OK)**:
```json
{
  "wordId": "serendipity",
  "masteryScore": 75,
  "level": "intermediate",
  "lastPracticed": "2024-01-15T10:30:00Z",
  "practiceCount": 8,
  "correctCount": 6
}
```

## Weekly Planning

### Get Weekly Plan
Retrieve weekly learning plan for specified week.

```http
GET /api/weekly-plan?weekStart=2024-01-14
```

**Query Parameters**:
- `weekStart` (required): Start date of week (Sunday) in YYYY-MM-DD format

**Response (200 OK)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "firebase-user-id",
  "weekStart": "2024-01-14",
  "focusAreas": [
    "Advanced vocabulary",
    "Business terminology",
    "Idiomatic expressions"
  ],
  "dailyGoals": {
    "monday": "Learn 3 new business terms",
    "tuesday": "Practice advanced adjectives",
    "wednesday": "Master idiomatic expressions",
    "thursday": "Review week's vocabulary",
    "friday": "Apply terms in context",
    "saturday": "Mixed practice session",
    "sunday": "Weekly review and planning"
  },
  "reviewWords": [
    "serendipity",
    "ubiquitous",
    "paradigm"
  ],
  "createdAt": "2024-01-14T07:05:00Z"
}
```

### Generate Weekly Plan
Trigger generation of new weekly plan.

```http
POST /api/weekly-plan/generate
Content-Type: application/json
```

**Request Body**:
```json
{
  "weekStart": "2024-01-21"
}
```

**Response (201 Created)**: Same as get weekly plan response.

## Error Codes

### Authentication Errors
- `UNAUTHORIZED` (401) - Missing or invalid Firebase token
- `FORBIDDEN` (403) - Token valid but insufficient permissions

### Validation Errors
- `INVALID_REQUEST` (400) - Malformed request body or parameters
- `VALIDATION_ERROR` (422) - Request validation failed

### Resource Errors
- `NOT_FOUND` (404) - Requested resource not found
- `CONFLICT` (409) - Resource already exists

### Server Errors
- `INTERNAL_ERROR` (500) - Internal server error
- `SERVICE_UNAVAILABLE` (503) - External service unavailable
- `RATE_LIMITED` (429) - Too many requests

### AI Service Errors
- `AI_SERVICE_ERROR` (502) - Gemini AI service error
- `CONTENT_GENERATION_FAILED` (503) - Failed to generate content
- `INVALID_AI_RESPONSE` (502) - AI returned invalid response

## Rate Limiting

API endpoints are rate limited per user:
- **Content Generation**: 10 requests per hour
- **Quiz Submission**: 100 requests per hour
- **General API**: 1000 requests per hour

Rate limit headers are included in responses:
```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1642262400
```

## Caching

Content responses include caching headers:
```http
Cache-Control: public, max-age=3600
ETag: "550e8400-e29b-41d4-a716-446655440000"
Last-Modified: Mon, 15 Jan 2024 10:30:00 GMT
```

## Webhooks (Future)

### Content Ready Webhook
Notifies when daily content is generated:

```json
{
  "event": "content.ready",
  "userId": "firebase-user-id",
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "date": "2024-01-15",
  "timestamp": "2024-01-15T07:00:00Z"
}
```

### Weekly Plan Webhook
Notifies when weekly plan is generated:

```json
{
  "event": "weekly_plan.ready",
  "userId": "firebase-user-id",
  "planId": "550e8400-e29b-41d4-a716-446655440000",
  "weekStart": "2024-01-14",
  "timestamp": "2024-01-14T07:05:00Z"
}
```

## SDK Examples

### JavaScript/TypeScript
```typescript
const response = await fetch('/api/content/today?date=2024-01-15', {
  headers: {
    'Authorization': `Bearer ${firebaseToken}`,
    'Content-Type': 'application/json'
  }
});

const content = await response.json();
```

### Kotlin (Android)
```kotlin
@GET("content/today")
suspend fun getTodaysContent(
    @Query("date") date: String
): Response<DailyContent>
```

### cURL
```bash
curl -X GET "http://localhost:8080/api/content/today?date=2024-01-15" \
  -H "Authorization: Bearer $FIREBASE_TOKEN" \
  -H "Content-Type: application/json"
```

This API contract provides a complete reference for integrating with the LexiPath backend service.
