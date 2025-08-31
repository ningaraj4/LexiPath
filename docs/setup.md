# LexiPath Setup Guide

This guide provides step-by-step instructions for setting up the LexiPath AI Vocabulary Learning Platform in development and production environments.

## Prerequisites

### Required Software
- **Go 1.22+** - Backend development
- **Android Studio Hedgehog (2023.1.1)+** - Android development
- **Docker & Docker Compose** - Local development environment
- **Git** - Version control

### Required Services
- **Firebase Project** - Authentication and user management
- **Google Cloud Project** - Gemini AI API access
- **PostgreSQL Database** - Primary data storage
- **Redis Instance** - Caching and rate limiting

## Firebase Setup

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project"
3. Enter project name: `lexipath-app`
4. Enable Google Analytics (optional)

### 2. Configure Authentication
1. Navigate to **Authentication** > **Sign-in method**
2. Enable the following providers:
   - **Email/Password** - For basic authentication
   - **Google** - For social login
   - **Phone** - For OTP verification

### 3. Generate Service Account Key
1. Go to **Project Settings** > **Service accounts**
2. Click "Generate new private key"
3. Save as `firebase-service-account.json`
4. Keep this file secure (never commit to version control)

### 4. Configure Android App
1. In Firebase Console, click "Add app" > Android
2. Enter package name: `com.example.lexipath`
3. Download `google-services.json`
4. Place in `app/` directory

## Google Cloud Setup

### 1. Enable Gemini API
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create new project or select existing
3. Enable **Generative Language API**
4. Create API key in **Credentials**
5. Restrict key to Generative Language API only

### 2. Set Up Cloud Resources (Production)
```bash
# Enable required APIs
gcloud services enable run.googleapis.com
gcloud services enable sql-component.googleapis.com
gcloud services enable redis.googleapis.com

# Create Cloud SQL instance
gcloud sql instances create lexipath-db \
  --database-version=POSTGRES_14 \
  --tier=db-f1-micro \
  --region=us-central1

# Create database
gcloud sql databases create lexipath --instance=lexipath-db

# Create Redis instance
gcloud redis instances create lexipath-cache \
  --size=1 \
  --region=us-central1 \
  --redis-version=redis_6_x
```

## Backend Development Setup

### 1. Environment Configuration
```bash
cd backend
cp .env.example .env
```

Edit `.env` with your configuration:
```bash
# Server Configuration
PORT=8080
TIMEZONE=America/New_York

# Database (Development)
DATABASE_URL=postgres://lexipath:password@localhost:5432/lexipath?sslmode=disable

# Redis (Development)
REDIS_URL=redis://localhost:6379

# External APIs
GEMINI_API_KEY=your-gemini-api-key-here
FIREBASE_PROJECT_ID=your-firebase-project-id

# Firebase Service Account (Development)
GOOGLE_APPLICATION_CREDENTIALS=./firebase-service-account.json
```

### 2. Start Development Dependencies
```bash
# Start PostgreSQL and Redis
docker-compose up -d postgres redis

# Wait for services to be ready
docker-compose logs -f postgres redis
```

### 3. Database Migration
```bash
# Install golang-migrate (if not installed)
go install -tags 'postgres' github.com/golang-migrate/migrate/v4/cmd/migrate@latest

# Run migrations
make migrate-up

# Verify migration
make migrate-status
```

### 4. Install Dependencies and Run
```bash
# Install Go dependencies
go mod tidy

# Run backend server
make run

# Or run with hot reload (install air first)
go install github.com/cosmtrek/air@latest
air
```

### 5. Verify Backend Setup
```bash
# Health check
curl http://localhost:8080/health

# Metrics endpoint
curl http://localhost:8080/metrics
```

## Android Development Setup

### 1. Project Configuration
1. Open Android Studio
2. Open project from `LexiPath/` directory
3. Wait for Gradle sync to complete

### 2. Configure Build Variants
Edit `app/build.gradle.kts`:
```kotlin
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
        isDebuggable = true
    }
    release {
        buildConfigField("String", "BASE_URL", "\"https://your-backend-url.run.app\"")
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

### 3. Firebase Configuration
1. Ensure `google-services.json` is in `app/` directory
2. Verify Firebase dependencies in `gradle/libs.versions.toml`
3. Check Firebase configuration in `FirebaseModule.kt`

### 4. Build and Run
```bash
# Clean and build
./gradlew clean assembleDebug

# Install on device/emulator
./gradlew installDebug

# Or use Android Studio Run button
```

## Testing Setup

### Backend Testing
```bash
# Run all tests
make test

# Run tests with coverage
make test-coverage

# Run specific test package
go test ./internal/services/... -v
```

### Android Testing
```bash
# Unit tests
./gradlew testDebugUnitTest

# Generate test report
./gradlew testDebugUnitTest --continue
# Report: app/build/reports/tests/testDebugUnitTest/index.html

# Integration tests (requires device/emulator)
./gradlew connectedDebugAndroidTest
```

## Production Deployment

### Backend Production Setup

#### 1. Prepare Environment
```bash
# Production environment variables
export DATABASE_URL="postgres://user:pass@/lexipath?host=/cloudsql/project:region:instance"
export REDIS_URL="redis://10.x.x.x:6379"
export GEMINI_API_KEY="production-api-key"
export FIREBASE_PROJECT_ID="lexipath-prod"
export PORT="8080"
```

#### 2. Build and Deploy
```bash
# Build production image
make docker-build

# Tag for Cloud Run
docker tag lexipath-backend gcr.io/PROJECT-ID/lexipath-backend:latest

# Push to registry
docker push gcr.io/PROJECT-ID/lexipath-backend:latest

# Deploy to Cloud Run
gcloud run deploy lexipath-backend \
  --image gcr.io/PROJECT-ID/lexipath-backend:latest \
  --platform managed \
  --region us-central1 \
  --set-env-vars="DATABASE_URL=${DATABASE_URL},REDIS_URL=${REDIS_URL}" \
  --set-env-vars="GEMINI_API_KEY=${GEMINI_API_KEY},FIREBASE_PROJECT_ID=${FIREBASE_PROJECT_ID}" \
  --allow-unauthenticated \
  --memory 512Mi \
  --cpu 1 \
  --max-instances 10
```

### Android Production Setup

#### 1. Configure Release Build
```bash
# Generate release keystore
keytool -genkey -v -keystore lexipath-release.keystore \
  -alias lexipath -keyalg RSA -keysize 2048 -validity 10000

# Add to app/build.gradle.kts
signingConfigs {
    create("release") {
        storeFile = file("lexipath-release.keystore")
        storePassword = "your-store-password"
        keyAlias = "lexipath"
        keyPassword = "your-key-password"
    }
}
```

#### 2. Build Release
```bash
# Build release APK
./gradlew assembleRelease

# Build AAB for Play Store
./gradlew bundleRelease

# Outputs in app/build/outputs/
```

## Troubleshooting

### Common Backend Issues

#### Database Connection Issues
```bash
# Check PostgreSQL status
docker-compose ps postgres

# View PostgreSQL logs
docker-compose logs postgres

# Connect to database manually
docker-compose exec postgres psql -U lexipath -d lexipath
```

#### Redis Connection Issues
```bash
# Check Redis status
docker-compose ps redis

# Test Redis connection
docker-compose exec redis redis-cli ping
```

#### Migration Issues
```bash
# Check migration status
make migrate-status

# Force migration version
migrate -path migrations -database $DATABASE_URL force 1

# Rollback migration
make migrate-down
```

### Common Android Issues

#### Build Issues
```bash
# Clean build
./gradlew clean

# Clear Gradle cache
rm -rf ~/.gradle/caches/

# Invalidate caches in Android Studio
File > Invalidate Caches and Restart
```

#### Firebase Issues
- Verify `google-services.json` is in correct location
- Check Firebase project configuration
- Ensure SHA-1 fingerprint is added to Firebase project

#### Network Issues
- Use `10.0.2.2` instead of `localhost` for emulator
- Check Android network security config for HTTP traffic
- Verify backend URL in BuildConfig

### Performance Optimization

#### Backend Optimization
```bash
# Enable Go profiling
go tool pprof http://localhost:8080/debug/pprof/profile

# Monitor database queries
# Add logging in repository layer

# Redis monitoring
redis-cli monitor
```

#### Android Optimization
```bash
# Analyze APK size
./gradlew analyzeReleaseBundle

# Profile app performance
# Use Android Studio Profiler

# Check memory leaks
# Use LeakCanary in debug builds
```

## Development Workflow

### Daily Development
1. Start backend dependencies: `docker-compose up -d`
2. Run backend: `make run`
3. Open Android Studio and run app
4. Use Android emulator or physical device for testing

### Code Quality
```bash
# Backend linting
golangci-lint run

# Android linting
./gradlew lintDebug

# Format code
go fmt ./...
./gradlew ktlintFormat
```

### Git Workflow
```bash
# Feature development
git checkout -b feature/new-feature
git commit -m "feat: add new feature"
git push origin feature/new-feature

# Create pull request
# Merge after review
```

This setup guide provides all necessary steps to get LexiPath running in both development and production environments. Follow the sections relevant to your deployment needs.
