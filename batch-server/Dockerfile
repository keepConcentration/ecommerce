# Multi-stage build for batch-server
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copy root gradle files
COPY ../gradlew .
COPY ../gradle gradle
COPY ../settings.gradle .
COPY ../gradle.properties .

# Copy common module
COPY ../common common/

# Copy batch-server module
COPY build.gradle .
COPY src src/

# Build all modules
RUN chmod +x ./gradlew && ./gradlew :batch-server:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

# Batch server doesn't need health check (no HTTP endpoints)
# But add basic process check
HEALTHCHECK --interval=60s --timeout=3s --start-period=30s --retries=3 \
    CMD ps aux | grep '[j]ava' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
