.PHONY: build run clean test docker-build docker-run docker-compose-up docker-compose-down

# Variables
APP_NAME=ln-foot
DOCKER_IMAGE=ln-foot
DOCKER_TAG=latest

# Gradle commands
build:
	./gradlew clean build

run:
	./gradlew bootRun

test:
	./gradlew test

clean:
	./gradlew clean

# Docker commands
docker-build:
	docker build -t $(DOCKER_IMAGE):$(DOCKER_TAG) .

docker-run:
	docker run -p 8080:8080 $(DOCKER_IMAGE):$(DOCKER_TAG)

# Docker Compose commands
docker-compose-up:
	docker compose up --wait

docker-compose-down:
	docker compose down -v

# Combined commands
all: build test docker-build

# Development workflow
dev: docker-compose-up run

# Clean everything
clean-all: clean docker-compose-down
	docker rmi $(DOCKER_IMAGE):$(DOCKER_TAG) || true

# Help command
help:
	@echo "Available commands:"
	@echo "  build              - Build the application"
	@echo "  run               - Run the application"
	@echo "  test              - Run tests"
	@echo "  clean             - Clean build files"
	@echo "  docker-build      - Build Docker image"
	@echo "  docker-run        - Run Docker container"
	@echo "  docker-compose-up - Start all services"
	@echo "  docker-compose-down - Stop all services"
	@echo "  all               - Build, test, and create Docker image"
	@echo "  dev               - Start services and run application"
	@echo "  clean-all         - Clean everything including Docker"
	@echo "  help              - Show this help message" 