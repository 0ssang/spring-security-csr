# JWT Authentication Example

JWT 기반의 인증 서비스입니다. 폼 로그인(이메일/비밀번호)과 소셜 로그인(Google, Kakao, Naver)을 지원합니다.
하나의 이메일을 기준으로 폼 로그인과 소셜로그인을 통합하여 지원합니다.

## 주요 특징

- 🔐 **JWT 기반 인증**: Access Token + Refresh Token
- 🌐 **소셜 로그인**: Google, Kakao, Naver (OIDC)
- ✨ **폼 로그인과 통합**: 하나의 이메일로 다양한 로그인 지원
- 🔄 **Token Rotation**: Refresh Token 자동 갱신
- 📊 **구조화된 로깅**: JSON 형식 로그 (Logstash Encoder)
- 🎯 **요청 추적**: MDC 기반 requestId 추적
- 💾 **redis**: refresh token 관리

## 시작하기

### Docker Compose

#### 사전 요구사항

- Docker 20.10+
- Docker Compose 2.0+

#### 빠른 시작

```bash
# 1. 저장소 클론
git clone https://github.com/your-username/jwt-auth.git
cd jwt-auth

# 2. 환경 변수 설정
cp .env.example .env
# .env 파일을 열어서 실제 값으로 수정

# 3. Docker Compose로 실행
docker compose up -d

# 4. 로그 확인
docker compose logs -f backend

# 5. 애플리케이션 접속
# http://localhost:8080
```

#### 서비스 관리

```bash
# 서비스 중지
docker compose down

# 서비스 재시작
docker compose restart backend

# 전체 서비스 상태 확인
docker compose ps

# 볼륨 포함 완전 삭제
docker compose down -v
```

## 환경 변수 설정

### 필수 환경 변수

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `DB_PASSWORD` | 데이터베이스 비밀번호 | `your_password` |
| `JWT_SECRET` | JWT 서명 키 (32자 이상) | `your-secret-key-at-least-32-chars` |

### OAuth2 환경 변수 (선택)

소셜 로그인을 사용하려면 아래 환경 변수를 설정하세요:

#### Google OAuth2
- 발급 URL: https://console.cloud.google.com/apis/credentials
- `GOOGLE_CLIENT_ID`: Google Client ID
- `GOOGLE_CLIENT_SECRET`: Google Client Secret

#### Kakao OAuth2
- 발급 URL: https://developers.kakao.com/console/app
- `KAKAO_CLIENT_ID`: Kakao REST API Key
- `KAKAO_CLIENT_SECRET`: Kakao Client Secret

#### Naver OAuth2
- 발급 URL: https://developers.naver.com/apps
- `NAVER_CLIENT_ID`: Naver Client ID
- `NAVER_CLIENT_SECRET`: Naver Client Secret

### 선택적 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `JWT_ACCESS_TOKEN_EXPIRATION` | Access Token 만료 시간 (ms) | `900000` (15분) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh Token 만료 시간 (ms) | `604800000` (7일) |
| `OAUTH2_REDIRECT_URI` | OAuth2 리다이렉트 URI | `http://localhost:3000/oauth2/redirect` |

## API 엔드포인트

### 인증 API

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| POST | `/api/auth/signup` | 회원가입 | ❌ |
| POST | `/api/auth/login` | 로그인 | ❌ |
| POST | `/api/auth/refresh` | 토큰 갱신 | ❌ |
| POST | `/api/auth/logout` | 로그아웃 | ✅ |

### 소셜 로그인

| Provider | Endpoint |
|----------|----------|
| Google | `/oauth2/authorization/google` |
| Kakao | `/oauth2/authorization/kakao` |
| Naver | `/oauth2/authorization/naver` |

### 게시판 API (예시)

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| GET | `/api/boards` | 게시판 목록 | ✅ |
| POST | `/api/boards` | 게시판 생성 | ✅ |
| GET | `/api/boards/{id}` | 게시판 상세 | ✅ |
| GET | `/api/boards/me` | 내 게시판 | ✅ |

## 로그 파일

Docker Compose 실행 시 로그는 `./logs/` 디렉토리에 저장됩니다:

- `application.log`: 전체 애플리케이션 로그
- `auth-json.log`: 인증 관련 JSON 로그
- `api-json.log`: API 호출 JSON 로그
- `error-json.log`: 에러 JSON 로그



## 프로젝트 구조

```
src/
├── main/
│   ├── java/com/study/jwtauth/
│   │   ├── application/service/          # 서비스 계층
│   │   ├── domain/                       # 도메인 모델
│   │   │   ├── auth/                     # 인증 도메인
│   │   │   ├── user/                     # 사용자 도메인
│   │   │   └── exception/                # 도메인 예외
│   │   ├── infrastructure/               # 인프라 계층
│   │   │   ├── config/                   # 설정
│   │   │   ├── logging/                  # 로깅
│   │   │   └── security/                 # 보안
│   │   │       ├── jwt/                  # JWT 인증
│   │   │       └── oidc/                 # OIDC 소셜 로그인
│   │   └── presentation/                 # 표현 계층
│   │       ├── api/                      # REST 컨트롤러
│   │       ├── dto/                      # DTO
│   │       ├── exception/                # 예외 핸들러
│   │       ├── filter/                   # 필터
│   │       └── interceptor/              # 인터셉터
│   └── resources/
│       ├── application.yaml              # 애플리케이션 설정
│       └── logback-spring.xml            # 로깅 설정
```

## 아키텍처

### 레이어 아키텍처 (DDD 원칙)

```
Presentation Layer (Controller, Filter, Interceptor)
    ↓
Application Layer (Service)
    ↓
Domain Layer (Entity, Repository, Exception)
    ↑
Infrastructure Layer (Config, Security, Logging)
```

### 주요 디자인 패턴

- **Factory Pattern**: OidcUserInfoFactory
- **Strategy Pattern**: OidcUserInfo 인터페이스
- **Adapter Pattern**: CustomUserDetails
- **Repository Pattern**: UserRepository, RefreshTokenRepository
- **Builder Pattern**: User, CustomUserDetails

## CI/CD

### GitHub Actions CI

이 프로젝트는 GitHub Actions를 사용하여 자동 빌드 검증을 수행합니다.

#### CI 워크플로우

- **트리거**: Pull Request 생성/업데이트, main/develop 브랜치 푸시
- **테스트 환경**: H2 인메모리 데이터베이스 (빠르고 간편)
- **작업**:
  - Java 17 환경 설정 (Amazon Corretto)
  - Gradle 의존성 캐싱
  - 빌드 및 테스트 실행 (`./gradlew clean build`)

#### 테스트 전략

- **통합 테스트**: `@SpringBootTest` + H2 인메모리 DB
- **테스트 프로파일**: `application-test.yaml` (src/test/resources)
- **장점**:
  - ⚡ 빠른 실행 속도 (~5초)
  - 🔧 환경변수 설정 불필요
  - 🚀 CI/CD 파이프라인 간소화

#### GitHub Secrets 설정

**현재는 필요하지 않습니다.** 테스트는 H2 인메모리 DB와 더미 값을 사용하므로 별도의 secrets 설정이 불필요합니다.

#### CI 워크플로우 파일 위치

```
.github/workflows/ci.yml
```

