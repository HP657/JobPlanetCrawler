# 1단계: 빌드 스테이지 (Gradle 8.x + Java 21)
FROM gradle:8-jdk21 AS builder
WORKDIR /apps

# 의존성 캐싱을 위해 빌드 파일 먼저 복사
COPY build.gradle.kts settings.gradle.kts /apps/
RUN gradle build -x test --no-daemon || true

# 전체 소스 복사 및 빌드
COPY src /apps/src
RUN gradle bootJar -x test --no-daemon

# 2단계: 실행 스테이지 (컴팩트한 JRE 환경)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 빌드 스테이지에서 생성된 jar 파일 복사
COPY --from=builder /apps/build/libs/*-SNAPSHOT.jar /app/application.jar

# 컨테이너 실행 시 기본 실행 명령
ENTRYPOINT ["java", "-Xmx850m", "-Xms256m", "-Dspring.profiles.active=prod", "-jar", "/app/application.jar"]
EXPOSE 8080
