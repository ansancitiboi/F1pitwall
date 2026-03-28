# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Gradle wrapper + dependency cache layer
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q

# Source copy & build (skip tests — run separately in CI)
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S f1group && adduser -S f1user -G f1group

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown f1user:f1group app.jar
USER f1user

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
