# Other Considerations

이미 논의를 거쳐 결정된 내용을 다시 논의하지 않도록 기록하는 문서입니다.

## DB 스키마는 Flyway로 관리한다

스키마는 `back/src/main/resources/db/migration/`의 마이그레이션 파일이 기준이고, JPA는 `ddl-auto: validate`로 엔티티와 스키마가 맞는지만 확인합니다.

- 스키마 변경이 기록으로 남고, 적용 전에 리뷰할 수 있으며, 배포 시 자동으로 적용됩니다.
- 도서 검색용 FULLTEXT 인덱스처럼 엔티티로 표현할 수 없는 스키마를 저장소에서 관리할 수 있습니다.
- 대신 엔티티를 바꿀 때마다 마이그레이션 파일을 함께 써야 합니다. 스키마를 한 곳에서 명시적으로 관리하는 이점이 더 크다고 판단했습니다.

## 로컬·테스트 DB도 MySQL을 쓴다 (H2 제거)

마이그레이션에 H2에서 실행되지 않는 MySQL 문법(FULLTEXT 인덱스 등)이 들어가므로 H2를 걷어냈습니다. 로컬은 `infra/local/compose.yaml`의 MySQL, 테스트는 Testcontainers의 MySQL을 씁니다. 그래서 테스트 실행에는 Docker가 필요합니다.

## 인증 쿠키 SameSite

- `cookieSecure=true`(운영)면 `SameSite=None`, 아니면 `Strict`를 쓴다. (`Rq.setCookie`)
- 이유: 프론트(`pages.dev`)와 백엔드가 서로 다른 사이트라 운영에서는 `None`이 필요하고, `None`은 `Secure` 없이는 브라우저가 거부해 로컬에서는 `Strict`를 쓴다.
- `CUSTOM__PROD__COOKIE_DOMAIN`은 프론트가 아니라 백엔드 도메인이어야 한다.
- 한계: 서드파티 쿠키를 차단하는 브라우저(Safari, Chrome 시크릿)에서는 로그인이 유지되지 않는다. 프론트와 백엔드를 같은 사이트로 묶으면 운영도 `Strict`로 되돌릴 수 있다.
