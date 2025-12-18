# ===== Build Stage =====
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon -x test

# ===== Runtime Stage =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# uploads 디렉토리 생성
RUN mkdir -p /app/uploads

# 환경변수 기본값 설정 (docker-compose 또는 .env에서 오버라이드)
ENV SPRING_PROFILES_ACTIVE=local \
    DB_URL=jdbc:mysql://localhost:3306/vericerti \
    DB_USERNAME=vericerti \
    DB_PASSWORD= \
    REDIS_HOST=localhost \
    REDIS_PORT=6379 \
    JWT_SECRET= \
    STORAGE_UPLOAD_DIR=/app/uploads \
    LOG_LEVEL=INFO \
    JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

# JAVA_OPTS를 사용하여 JVM 옵션 조정 가능
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
