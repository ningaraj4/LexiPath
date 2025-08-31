@echo off
title LexiPath Development Server
echo ========================================
echo      LexiPath Development Server
echo ========================================
echo.

cd /d "d:\Ningaraj\AndroidStudioProjects\LexiPath\backend"

echo [1/3] Starting Docker services...
docker-compose up -d postgres redis
if errorlevel 1 (
    echo ERROR: Failed to start Docker services
    pause
    exit /b 1
)

echo [2/3] Waiting for services to be ready...
timeout /t 3 /nobreak > nul

echo [3/3] Starting Go backend server...
echo Backend will be available at: http://localhost:8080
echo Press Ctrl+C to stop the server
echo.
go run main.go
if errorlevel 1 (
    echo.
    echo ERROR: Failed to start Go backend server
    echo Make sure Go is installed and try running: go version
    echo.
    pause
    exit /b 1
)
pause
