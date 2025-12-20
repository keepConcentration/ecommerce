.PHONY: build up down logs clean test reset-db health

# Build all services
build:
	docker-compose build

# Start all services (각 마이크로서비스는 docker-compose.yml에서 자동으로 6개씩 스케일링됨)
up:
	docker-compose up -d

# Start with build
up-build:
	docker-compose up -d --build

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
logs-gateway:
	docker-compose logs -f api-gateway

logs-product:
	docker-compose logs -f product-service

logs-user:
	docker-compose logs -f user-service

logs-order:
	docker-compose logs -f order-service

logs-payment:
	docker-compose logs -f payment-service

logs-promotion:
	docker-compose logs -f promotion-service

logs-redis:
	docker-compose logs -f redis

logs-kafka:
	docker-compose logs -f kafka-1 kafka-2 kafka-3

logs-kafka-ui:
	docker-compose logs -f kafka-ui

# Database logs
logs-product-db:
	docker-compose logs -f product-db

logs-user-db:
	docker-compose logs -f user-db

logs-order-db:
	docker-compose logs -f order-db

logs-payment-db:
	docker-compose logs -f payment-db

logs-promotion-db:
	docker-compose logs -f promotion-db

# Run tests (local)
test:
	./gradlew test

# Test specific module
test-product:
	./gradlew :product-service:test

test-user:
	./gradlew :user-service:test

test-order:
	./gradlew :order-service:test

test-payment:
	./gradlew :payment-service:test

test-promotion:
	./gradlew :promotion-service:test

test-gateway:
	./gradlew :api-gateway:test

test-common:
	./gradlew :common:test

# Clean build artifacts
clean:
	./gradlew clean
	docker-compose down -v --rmi local

# Restart services
restart:
	docker-compose restart

# Restart specific service
restart-product:
	docker-compose restart product-service

restart-user:
	docker-compose restart user-service

restart-order:
	docker-compose restart order-service

restart-payment:
	docker-compose restart payment-service

restart-promotion:
	docker-compose restart promotion-service

restart-gateway:
	docker-compose restart api-gateway

# Health check
health:
	@echo "Checking API Gateway..."
	@curl -s http://localhost:8080/actuator/health || echo "API Gateway not responding"
	@echo "\nChecking Redis..."
	@docker-compose exec redis redis-cli ping || echo "Redis not responding"
	@echo "\nChecking Kafka UI..."
	@curl -s http://localhost:8090 > /dev/null && echo "Kafka UI OK" || echo "Kafka UI not responding"

# Database health checks
health-db:
	@echo "Checking Product DB..."
	@docker-compose exec product-db mysqladmin ping -h localhost -u sa -psa || echo "Product DB not responding"
	@echo "\nChecking User DB..."
	@docker-compose exec user-db mysqladmin ping -h localhost -u sa -psa || echo "User DB not responding"
	@echo "\nChecking Order DB..."
	@docker-compose exec order-db mysqladmin ping -h localhost -u sa -psa || echo "Order DB not responding"
	@echo "\nChecking Payment DB..."
	@docker-compose exec payment-db mysqladmin ping -h localhost -u sa -psa || echo "Payment DB not responding"
	@echo "\nChecking Promotion DB..."
	@docker-compose exec promotion-db mysqladmin ping -h localhost -u sa -psa || echo "Promotion DB not responding"

# Reset databases (각 서비스별 DB 초기화)
reset-db-product:
	@echo "Resetting Product database..."
	docker-compose exec product-db mysql -u sa -psa -e "DROP DATABASE IF EXISTS product_db; CREATE DATABASE product_db;"
	@echo "Product database reset complete."

reset-db-user:
	@echo "Resetting User database..."
	docker-compose exec user-db mysql -u sa -psa -e "DROP DATABASE IF EXISTS user_db; CREATE DATABASE user_db;"
	@echo "User database reset complete."

reset-db-order:
	@echo "Resetting Order database..."
	docker-compose exec order-db mysql -u sa -psa -e "DROP DATABASE IF EXISTS order_db; CREATE DATABASE order_db;"
	@echo "Order database reset complete."

reset-db-payment:
	@echo "Resetting Payment database..."
	docker-compose exec payment-db mysql -u sa -psa -e "DROP DATABASE IF EXISTS payment_db; CREATE DATABASE payment_db;"
	@echo "Payment database reset complete."

reset-db-promotion:
	@echo "Resetting Promotion database..."
	docker-compose exec promotion-db mysql -u sa -psa -e "DROP DATABASE IF EXISTS promotion_db; CREATE DATABASE promotion_db;"
	@echo "Promotion database reset complete."

# Reset all databases
reset-db-all: reset-db-product reset-db-user reset-db-order reset-db-payment reset-db-promotion
	@echo "All databases reset complete."

# Kafka management
kafka-topics:
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --list

kafka-create-topics:
	@echo "Creating Kafka topics..."
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --create --topic order.created --partitions 6 --replication-factor 3 --if-not-exists
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --create --topic stock.reserved --partitions 6 --replication-factor 3 --if-not-exists
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --create --topic coupon.reserved --partitions 6 --replication-factor 3 --if-not-exists
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --create --topic payment.completed --partitions 6 --replication-factor 3 --if-not-exists
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --create --topic order.completed --partitions 6 --replication-factor 3 --if-not-exists
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --create --topic stock.compensation.required --partitions 6 --replication-factor 3 --if-not-exists
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --create --topic coupon.compensation.required --partitions 6 --replication-factor 3 --if-not-exists
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --create --topic payment.compensation.required --partitions 6 --replication-factor 3 --if-not-exists
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --create --topic payment.failed --partitions 6 --replication-factor 3 --if-not-exists
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --create --topic order.failed --partitions 6 --replication-factor 3 --if-not-exists
	@echo "Kafka topics created."

kafka-delete-topics:
	@echo "Deleting all Kafka topics..."
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic order.created
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic stock.reserved
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic coupon.reserved
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic payment.completed
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic order.completed
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic stock.compensation.required
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic coupon.compensation.required
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic payment.compensation.required
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic payment.failed
	docker-compose exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic order.failed
	@echo "Kafka topics deleted."

# View consumer groups
kafka-consumer-groups:
	docker-compose exec kafka-1 kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

# Describe specific consumer group (usage: make kafka-describe-group GROUP=product-service-group)
kafka-describe-group:
	docker-compose exec kafka-1 kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group $(GROUP)

# View all consumer groups details
kafka-all-groups:
	@echo "Product Service Group:"
	@docker-compose exec kafka-1 kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group product-service-group
	@echo "\nOrder Service Group:"
	@docker-compose exec kafka-1 kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group order-service-group
	@echo "\nPayment Service Group:"
	@docker-compose exec kafka-1 kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group payment-service-group
	@echo "\nPromotion Service Group:"
	@docker-compose exec kafka-1 kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group promotion-service-group
