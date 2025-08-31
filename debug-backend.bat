@echo off
title LexiPath Debug - Backend Startup
echo ========================================
echo      LexiPath Debug - Backend Startup
echo ========================================
echo.

echo Checking prerequisites...
echo.

echo [CHECK 1] Go installation:
go version
if errorlevel 1 (
    echo ERROR: Go is not installed or not in PATH
    echo Please install Go from: https://golang.org/dl/
    pause
    exit /b 1
)
echo.

echo [CHECK 2] Docker installation:
docker --version
if errorlevel 1 (
    echo ERROR: Docker is not installed or not running
    echo Please install Docker Desktop and make sure it's running
    pause
    exit /b 1
)
echo.

echo [CHECK 3] Changing to backend directory:
cd /d "d:\Ningaraj\AndroidStudioProjects\LexiPath\backend"
if errorlevel 1 (
    echo ERROR: Cannot find backend directory
    pause
    exit /b 1
)
echo Current directory: %CD%
echo.

echo [CHECK 4] Starting Docker services:
docker-compose up -d postgres redis
if errorlevel 1 (
    echo ERROR: Failed to start Docker services
    echo Make sure Docker Desktop is running
    pause
    exit /b 1
)
echo.

echo [CHECK 5] Waiting for services...
timeout /t 5 /nobreak > nul
echo.

echo [CHECK 6] Starting Go backend:
echo If this fails, check the error message above
echo.
go run main.go
echo.
echo Backend stopped or failed to start
pause
