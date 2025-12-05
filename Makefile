.PHONY: build up down logs clean test seed reset-db

# Build all services
build:
	docker-compose build

# Start all services (3 API instances by default)
up:
	docker-compose up -d --scale api-server=3

# Start with build
up-build:
	docker-compose up -d --build --scale api-server=3

# Start with custom scale (usage: make up-scale N=5)
up-scale:
	docker-compose up -d --scale api-server=$(N)

# Stop all services
down:
	docker-compose down

# Stop and remove volumes
down-v:
	docker-compose down -v

# View logs
logs:
	docker-compose logs -f

# View specific service logs
logs-api:
	docker-compose logs -f api-server

logs-nginx:
	docker-compose logs -f nginx

logs-redis:
	docker-compose logs -f redis

logs-mysql:
	docker-compose logs -f mysql

# Run tests (local)
test:
	./gradlew test

# Test specific module
test-api:
	./gradlew :api-server:test

test-batch:
	./gradlew :batch-server:test

test-common:
	./gradlew :common:test

# Clean build artifacts
clean:
	./gradlew clean
	docker-compose down -v --rmi local

# Restart services
restart:
	docker-compose restart

# Health check
health:
	@echo "Checking Nginx..."
	@curl -s http://localhost:8085/health || echo "Nginx not responding"
	@echo "\nChecking Redis..."
	@docker-compose exec redis redis-cli ping || echo "Redis not responding"
	@echo "\nChecking MySQL..."
	@docker-compose exec mysql mysqladmin ping -h localhost -u sa -psa || echo "MySQL not responding"

# Seed test data
seed:
	./gradlew :api-server:seedData

# Reset MySQL data (delete all data)
reset-db:
	@echo "Resetting MySQL database..."
	docker-compose exec mysql mysql -u sa -psa -e "DROP DATABASE IF EXISTS ecommerce; CREATE DATABASE ecommerce;"
	@echo "Database reset complete."
