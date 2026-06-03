# 1단계: 빌드 스테이지
FROM gradle:8-jdk21 AS builder
WORKDIR /apps
COPY build.gradle.kts settings.gradle.kts /apps/
RUN gradle build -x test --no-daemon || true
COPY src /apps/src
RUN gradle build -x test --no-daemon

# 2단계: 실행 스테이지
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# [추가] 브라우저 및 의존성 라이브러리 설치
RUN apt-get update && apt-get install -y \
    chromium \
    chromium-driver \
    libnss3 \
    libgconf-2-4 \
    libfontconfig1 \
    && rm -rf /var/lib/apt/lists/*

# 빌드 스테이지에서 생성된 jar 파일 복사
COPY --from=builder /apps/build/libs/*-SNAPSHOT.jar /app/application.jar

# 컨테이너 실행 시 기본 실행 명령
ENTRYPOINT ["java", "-Xmx850m", "-Xms256m", "-Dspring.profiles.active=prod", "-jar", "/app/application.jar"]
EXPOSE 8080