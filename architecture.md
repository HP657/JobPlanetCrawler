# JobKoreaCrawler 프로젝트 아키텍처

이 문서는 JobKoreaCrawler 프로젝트의 전반적인 아키텍처, 기술 스택, 주요 실행 흐름에 대해 설명합니다.

## 1. 개요

이 프로젝트는 특정 채용 사이트(예: 잡플래닛)의 채용 공고를 주기적으로 크롤링하여 데이터를 수집하고, 데이터베이스에 저장 및 관리하는 것을 목표로 하는 Spring Boot 기반의 애플리케이션입니다.

## 2. 주요 기술 스택

- **언어**: Kotlin
- **프레임워크**: Spring Boot
- **빌드 도구**: Gradle (Kotlin DSL)
- **데이터베이스**: SQL 데이터베이스 (MySQL, PostgreSQL 등)
- **DB 마이그레이션**: Flyway (`src/main/resources/db/migration`)
- **ORM**: Spring Data JPA
- **크롤링**: Selenium (WebDriver)
- **컨테이너**: Docker, Docker Compose
- **CI/CD**: GitHub Actions

## 3. 프로젝트 구조

```
.
├── build.gradle.kts              # Gradle 빌드 스크립트
├── Dockerfile                    # Docker 이미지 생성을 위한 설정 파일
├── docker-compose.yml            # Docker 컨테이너 실행을 위한 설정 파일
├── .github/workflows/ci.yml      # GitHub Actions를 이용한 CI 파이프라인
└── src
    ├── main
    │   ├── kotlin/com/datascience/jobplanetcrawler
    │   │   ├── JobplanetcrawlerApplication.kt  # Spring Boot 메인 애플리케이션
    │   │   ├── crawler/              # 크롤링 관련 로직
    │   │   │   ├── dto/              # 크롤링 데이터 전송 객체 (DTO)
    │   │   │   ├── scheduler/        # 크롤링 스케줄러
    │   │   │   └── service/          # 크롤링 실행 서비스
    │   │   ├── global/               # 전역 설정
    │   │   │   ├── config/           # WebDriver, RestTemplate 등 설정
    │   │   │   └── exception/        # 전역 예외 처리
    │   │   ├── job/                  # 채용 공고 데이터 관리 로직
    │   │   │   ├── controller/       # API 컨트롤러
    │   │   │   ├── dto/              # 채용 공고 데이터 DTO
    │   │   │   ├── entity/           # JPA 엔티티
    │   │   │   ├── repository/       # Spring Data JPA 리포지토리
    │   │   │   ├── scheduler/        # 백업 등 보조 스케줄러
    │   │   │   └── service/          # 채용 공고 조회/저장 서비스
    │   └── resources
    │       ├── application.yaml      # Spring Boot 설정 파일
    │       └── db/migration/         # Flyway DB 마이그레이션 스크립트
    └── test                          # 테스트 코드
```

## 4. 주요 실행 흐름

### 4.1. 크롤링 및 데이터 저장

1.  **스케줄러 실행**: `JobPlanetCrawlerScheduler`가 정해진 시간마다 `JobPlanetCrawler` 서비스를 호출합니다.
2.  **웹 페이지 크롤링**: `JobPlanetCrawler` 서비스는 `WebDriverConfig`를 통해 생성된 Selenium WebDriver를 사용하여 잡플래닛 사이트의 채용 공고를 크롤링합니다.
3.  **데이터 파싱**: 크롤링한 HTML에서 필요한 정보(회사명, 직무, 기술 스택 등)를 추출하여 `JobScrapDto`에 담습니다.
4.  **데이터베이스 저장**: `JobSaveService`는 `JobScrapDto`를 `JobOpening`, `Skill` 등의 JPA 엔티티로 변환하여 `JobOpeningRepository`, `SkillRepository`를 통해 데이터베이스에 저장합니다.

### 4.2. API 제공

-   `CrawlingController`를 통해 수동으로 크롤링을 트리거하거나, 저장된 채용 공고 데이터를 조회하는 API를 제공할 수 있습니다. (현재 구조 기준)

### 4.3. 데이터베이스 관리

-   **스키마 관리**: 애플리케이션 실행 시 Flyway가 `src/main/resources/db/migration` 경로의 SQL 파일을 읽어 데이터베이스 스키마를 자동으로 최신 상태로 유지합니다.
-   **데이터 백업**: `BackupScheduler`가 매시간 정각에 데이터베이스의 모든 채용 공고 데이터를 조회하여 **JSON 파일 형태**로 백업합니다. 백업 파일은 `backups/job_openings_backup_YYYY-MM-DD_HH-mm-ss.json` 형식으로 생성됩니다. Docker 환경에서는 이 `backups` 디렉터리가 호스트와 볼륨으로 연결되어 있어 컨테이너가 삭제되어도 백업 파일이 안전하게 유지됩니다.

## 5. CI/CD 및 배포

-   **CI (Continuous Integration)**: `.github/workflows/ci.yml` 설정에 따라, 소스 코드가 main 브랜치에 푸시될 때마다 GitHub Actions가 자동으로 프로젝트를 빌드하고 테스트를 실행합니다.
-   **배포 (Deployment)**: `Dockerfile`과 `docker-compose.yml`을 사용하여 애플리케이션을 Docker 컨테이너 환경에서 손쉽게 빌드하고 실행할 수 있습니다.

## 6. 실행 방법

-   **로컬 개발 환경**: 다음 명령어를 통해 Spring Boot 애플리케이션을 실행할 수 있습니다.
    ```bash
    ./gradlew :application:bootRun
    ```
-   **Docker 환경**: 다음 명령어를 통해 Docker 컨테이너로 애플리케이션을 실행할 수 있습니다.
    ```bash
    docker-compose up --build
    ```

## 7. 배포 아키텍처 (라즈베리파이 / Docker Compose)

`docker-compose.yml` 파일을 통해 라즈베리파이와 같은 ARM 기반 환경에 컨테이너화된 애플리케이션을 배포합니다. 이 구조는 세 개의 주요 서비스 컨테이너로 구성됩니다.

-   **`api` (애플리케이션 서버)**
    -   **이미지**: `ghcr.io/hp657/jobplanetcrawler/crawler:latest`
    -   **역할**: 크롤링 스케줄링, 데이터 처리 및 저장을 담당하는 메인 Spring Boot 애플리케이션을 실행합니다. `Dockerfile`에 의해 빌드되며, ARM 아키텍처와 호환되는 `eclipse-temurin` JRE 이미지를 기반으로 합니다.

-   **`postgres-db` (데이터베이스)**
    -   **이미지**: `postgres:17-alpine`
    -   **역할**: 크롤링된 채용 공고 데이터를 저장하는 PostgreSQL 데이터베이스 서버입니다. 데이터는 Docker 볼륨(`postgres_data`)에 영구적으로 저장됩니다.

-   **`chrome` (Selenium WebDriver)**
    -   **이미지**: `seleniarm/standalone-chromium:latest`
    -   **역할**: 실제 웹 페이지를 렌더링하고 크롤링을 수행하기 위한 헤드리스 Chrome 브라우저 환경을 제공합니다. 특히 `seleniarm` 이미지는 라즈베리파이와 같은 ARM 아키텍처에서 Selenium을 실행하기 위해 최적화된 이미지입니다.

### 아키텍처 흐름

1.  `docker-compose up` 명령어로 세 개의 컨테이너(api, postgres-db, chrome)가 실행됩니다.
2.  `api` 컨테이너의 Spring Boot 애플리케이션은 스케줄에 따라 크롤링 작업을 시작합니다.
3.  `api` 컨테이너는 내장된 네트워크를 통해 `chrome` 컨테이너(`http://chrome:4444/wd/hub`)에 접속하여 Selenium WebDriver를 제어하고 웹 페이지 크롤링을 수행합니다.
4.  크롤링으로 수집된 데이터는 `api` 컨테이너에서 처리된 후, `postgres-db` 컨테이너에 영구적으로 저장됩니다.
