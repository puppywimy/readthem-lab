# READTHEM.md

개발자들의 도서 리뷰 공유 플랫폼

- 문서 최신화 기준 커밋: `9fdc044` (`2026-08-04`, `[fix/ADP-211] 도서 수집 정보, 도서 운영 정보 분리 (#48)`)

- 프론트엔드 배포: [READTHEM.md](https://readthem-md.pages.dev)
- 백엔드 API 문서: [Swagger UI](https://powell-remix-off-grade.trycloudflare.com/swagger-ui/index.html)

## 프로젝트 개요

READTHEM.md는 도서를 검색하고, 리뷰를 작성하고, 찜 목록을 관리하고, 개인 책장 위젯을 공유할 수 있는 서비스입니다.

## 역할 분담

| 팀원   | 역할 | 담당업무                   |
| ------ | ---- | -------------------------- |
| 이호영 | 팀장 | 배포                       |
| 김민준 | 팀원 | 도서 정보 수집             |
| 이지헌 | 팀원 | 소셜 로그인, 추천 알고리즘 |

## 주요 기능

- 도서 검색 및 상세 조회
- 인기 도서, 후기 좋은 도서 랭킹
- 리뷰 작성, 수정, 삭제
- 찜 목록 관리
- GitHub 로그인 기반 회원 기능
- 관리자용 도서, 회원, 리뷰 관리
- 개인 책장 위젯 생성 및 공유

## 문서

- API 문서: [docs/api/book.md](docs/api/book.md), [docs/api/review.md](docs/api/review.md), [docs/api/member.md](docs/api/member.md), [docs/api/wish.md](docs/api/wish.md), [docs/api/widget.md](docs/api/widget.md), [docs/api/tag.md](docs/api/tag.md), [docs/api/global-exception.md](docs/api/global-exception.md)
- 관리자 API 문서: [docs/api/book.admin.md](docs/api/book.admin.md), [docs/api/member.admin.md](docs/api/member.admin.md), [docs/api/review.admin.md](docs/api/review.admin.md)
- 프로젝트 구조: [docs/project-structure.md](docs/project-structure.md)
- ERD: [docs/erd.mermaid](docs/erd.mermaid)
- 설계 규칙: [docs/review-rules/software-architecture.md](docs/review-rules/software-architecture.md), [docs/review-rules/refactoring-rules.md](docs/review-rules/refactoring-rules.md)

## 기술 스택

### Backend

- Java 25, Spring Boot 4.0.6
- MySQL, H2 Database, Redis, Spring Data JPA
- Springdoc OpenAPI, Spring Validation, Spring AOP

### Frontend

- Next.js 16.2.9, React 19.2.4

## 실행 환경

- Node.js 20 이상 권장
- pnpm 또는 npm
- Java 25
- Spring Boot, MySQL, Redis

## 환경 변수

각 패키지에는 예시 파일이 함께 있습니다.

- `front/.env.example`
- `back/.env.example`
- `widget/.env.example`

### `front/.env`

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_FRONTEND_BASE_URL=http://localhost:3000
```

### `back/.env`

```env
SPRING__PROFILES__ACTIVE=dev

SPRING__SECURITY__OAUTH2__CLIENT__REGISTRATION__GITHUB__CLIENT_ID=YOUR_GITHUB_CLIENT_ID
SPRING__SECURITY__OAUTH2__CLIENT__REGISTRATION__GITHUB__CLIENT_SECRET=YOUR_GITHUB_CLIENT_SECRET

CUSTOM__JWT__SECRET_KEY=YOUR_JWT_SECRET
CUSTOM__JWT__EXPIRATION_MINUTES=60
CUSTOM__BOOK__FETCH__API_KEYS=
CUSTOM__SECURITY__ALLOWED_ORIGINS=http://localhost:3000
CUSTOM__SECURITY__COOKIE_DOMAIN=localhost
```

## 실행 방법

### 1. 백엔드 실행

```bash
cd back
./gradlew bootRun
```

### 2. 프론트엔드 실행

```bash
cd front
pnpm install
pnpm dev
```

### 3. 위젯 패키지

```bash
cd widget
npm install
npm start
```

## API 개요

백엔드는 REST API를 제공합니다. 상세 계약은 `docs/api/`를 참고하세요.

- [도서 API](docs/api/book.md)
- [도서 관리자 API](docs/api/book.admin.md)
- [리뷰 API](docs/api/review.md)
- [리뷰 관리자 API](docs/api/review.admin.md)
- [회원 API](docs/api/member.md)
- [회원 관리자 API](docs/api/member.admin.md)
- [찜 API](docs/api/wish.md)
- [태그 API](docs/api/tag.md)
- [위젯 API](docs/api/widget.md)
- [예외 처리](docs/api/global-exception.md)

## 개발 스크립트

### `front`

- `pnpm dev`: 개발 서버 실행
- `pnpm build`: 프로덕션 빌드
- `pnpm start`: 빌드된 앱 실행
- `pnpm lint`: ESLint 실행
- `pnpm format`: Prettier로 포맷
- `pnpm tsc`: 타입 검사
- `pnpm check`: 포맷, 타입 검사, 린트 순차 실행

### `back`

- `./gradlew bootRun`: 서버 실행
- `./gradlew test`: 테스트 실행
- `./gradlew bootJar`: 실행 JAR 생성

## 브랜치 전략

- `main`: GitHub Actions를 통해 배포되는 운영 브랜치
- `dev`: 개발 내용을 모으는 통합 브랜치
- `feat`: 세부 기능별 작업 브랜치

## 참고

- 프론트는 `NEXT_PUBLIC_API_BASE_URL`을 통해 백엔드와 통신합니다.
- 로그인과 위젯 링크는 GitHub OAuth와 연동되어 있습니다.
- 위젯 렌더러 기본 주소는 `http://localhost:3001`입니다.
