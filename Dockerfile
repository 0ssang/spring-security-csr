# Multi-stage build for optimized image size

# Stage 1: Build
FROM gradle:8-jdk17 AS builder

WORKDIR /app

# Gradle 의존성 캐싱을 위한 레이어 분리
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY . .
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime
FROM amazoncorretto:17-alpine AS runtime

WORKDIR /app

# 타임존 설정 (한국 시간)
RUN apk add --no-cache tzdata wget && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 비root 사용자 생성 (보안 강화)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/ || exit 1

# 포트 노출
EXPOSE 8080

# JVM 옵션 설정
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]