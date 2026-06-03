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

# 패키지 업데이트 및 크롬 설치 (chromium-browser와 chromium-chromedriver 명칭 사용)
# Dockerfile의 실행 스테이지 부분
RUN apt-get update && apt-get install -y --no-install-recommends \
    chromium-browser \
    chromium-chromedriver \
    libnss3 \
    libgconf-2-4 \
    libfontconfig1 \
    libatk-bridge2.0-0 \
    libgtk-3-0 \
    libgbm1 \
    libasound2 \
    && rm -rf /var/lib/apt/lists/*

# 빌드 스테이지에서 생성된 jar 파일 복사
COPY --from=builder /apps/build/libs/*-SNAPSHOT.jar /app/application.jar

# 컨테이너 실행 시 기본 실행 명령
ENTRYPOINT ["java", "-Xmx850m", "-Xms256m", "-Dspring.profiles.active=prod", "-jar", "/app/application.jar"]
EXPOSE 8080