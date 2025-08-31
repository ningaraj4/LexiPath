@echo off
title LexiPath - Docker All-in-One
echo ========================================
echo    LexiPath - Docker All-in-One Setup
echo ========================================
echo.

cd /d "d:\Ningaraj\AndroidStudioProjects\LexiPath"

echo [1/4] Checking Docker...
docker --version
if errorlevel 1 (
    echo ERROR: Docker is not installed or not running
    echo Please install Docker Desktop and make sure it's running
    pause
    exit /b 1
)
echo Docker is available!
echo.

echo [2/4] Building and starting all services...
echo This may take a few minutes on first run...
docker-compose -f docker-compose.dev.yml up --build -d
if errorlevel 1 (
    echo ERROR: Failed to start services
    pause
    exit /b 1
)
echo.

echo [3/4] Waiting for services to be ready...
timeout /t 10 /nobreak > nul
echo.

echo [4/4] Running database migrations...
docker-compose -f docker-compose.dev.yml exec backend migrate -path migrations -database "postgres://lexipath_user:lexipath_pass@postgres:5432/lexipath?sslmode=disable" up
echo.

echo ========================================
echo          🚀 LexiPath is Ready!
echo ========================================
echo.
echo Backend API: http://localhost:8080
echo.
echo Services running:
echo - PostgreSQL Database (port 5432)
echo - Redis Cache (port 6379)  
echo - Go Backend API (port 8080)
echo.
echo To stop all services: docker-compose -f docker-compose.dev.yml down
echo To view logs: docker-compose -f docker-compose.dev.yml logs -f
echo.
echo Now you can run the Android app in Android Studio!
echo.
pause
