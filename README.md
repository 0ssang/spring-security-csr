# JWT Authentication Service

Spring Boot 기반의 JWT 인증 서비스입니다. 로컬 인증(이메일/비밀번호)과 소셜 로그인(Google, Kakao, Naver)을 지원합니다.

## 주요 기능

- 🔐 **JWT 기반 인증**: Access Token + Refresh Token
- 🌐 **소셜 로그인**: Google, Kakao, Naver (OIDC)
- 🔄 **Token Rotation**: Refresh Token 자동 갱신
- 📊 **구조화된 로깅**: JSON 형식 로그 (Logstash Encoder)
- 🎯 **요청 추적**: MDC 기반 requestId 추적
- 🛡️ **보안**: BCrypt 암호화, CORS 설정

## 기술 스택

- **Framework**: Spring Boot 3.x
- **Security**: Spring Security 6.x
- **Database**: MariaDB 10.11
- **Cache**: Redis 7
- **Authentication**: JWT (JJWT), OAuth2/OIDC
- **Logging**: Logback + Logstash Encoder
- **Build Tool**: Gradle 8

## 시작하기

### 방법 1: Docker Compose (권장)

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

### 방법 2: 로컬 환경 실행

#### 사전 요구사항

- JDK 17
- MariaDB 10.11+
- Redis 7+

#### 환경 설정

```bash
# 1. 데이터베이스 생성
mysql -u root -p
CREATE DATABASE jwt_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 환경 변수 설정
export DB_URL=jdbc:mariadb://localhost:3306/jwt_auth
export DB_USERNAME=root
export DB_PASSWORD=your_password
export DB_DRIVER=org.mariadb.jdbc.Driver
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=your-jwt-secret-key-minimum-32-characters-long
export GOOGLE_CLIENT_ID=your_google_client_id
export GOOGLE_CLIENT_SECRET=your_google_client_secret
export KAKAO_CLIENT_ID=your_kakao_client_id
export KAKAO_CLIENT_SECRET=your_kakao_client_secret
export NAVER_CLIENT_ID=your_naver_client_id
export NAVER_CLIENT_SECRET=your_naver_client_secret
export OAUTH2_REDIRECT_URI=http://localhost:3000/oauth2/redirect

# 3. 애플리케이션 실행
./gradlew bootRun
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

## 개발

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 테스트
./gradlew test --tests AuthServiceTest
```

### 빌드

```bash
# JAR 파일 생성
./gradlew clean build

# 테스트 제외 빌드
./gradlew clean build -x test
```

### Docker 이미지 빌드

```bash
# 이미지 빌드
docker build -t jwt-auth:latest .

# 이미지 실행
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mariadb://host.docker.internal:3306/jwt_auth \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_secret \
  jwt-auth:latest
```

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

## 트러블슈팅

### Docker 관련

**Q: 컨테이너가 시작되지 않아요**
```bash
# 로그 확인
docker compose logs backend

# 포트 충돌 확인
lsof -i :8080
```

**Q: 데이터베이스 연결 실패**
```bash
# MariaDB 상태 확인
docker compose ps mariadb

# 헬스체크 확인
docker compose exec mariadb healthcheck.sh --connect
```

### 인증 관련

**Q: JWT 토큰이 만료되었어요**
```bash
# Refresh Token으로 갱신
POST /api/auth/refresh
{
  "refreshToken": "your_refresh_token"
}
```

**Q: OAuth2 로그인이 안돼요**
- Client ID와 Client Secret이 올바른지 확인
- Redirect URI가 OAuth2 설정과 일치하는지 확인
- `.env` 파일의 환경 변수가 올바른지 확인

## 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 기여

PR과 이슈는 언제든 환영합니다!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request