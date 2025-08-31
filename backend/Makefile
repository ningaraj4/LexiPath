.PHONY: build run test clean migrate-up migrate-down docker-build docker-run

# Build the application
build:
	go build -o bin/lexipath-backend ./main.go

# Run the application
run:
	go run main.go

# Run tests
test:
	go test -v ./...

# Clean build artifacts
clean:
	rm -rf bin/

# Database migrations
migrate-up:
	migrate -path migrations -database "$(DATABASE_URL)" up

migrate-down:
	migrate -path migrations -database "$(DATABASE_URL)" down

# Docker commands
docker-build:
	docker build -t lexipath-backend .

docker-run:
	docker run -p 8080:8080 --env-file .env lexipath-backend

# Development with docker-compose
dev-up:
	docker-compose up -d

dev-down:
	docker-compose down

# Install dependencies
deps:
	go mod download
	go mod tidy

# Format code
fmt:
	go fmt ./...

# Lint code
lint:
	golangci-lint run

# Generate swagger docs (if using swaggo)
swagger:
	swag init -g main.go
