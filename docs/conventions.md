# Conventions

커밋 메시지 컨벤션, 개발 프로세스, 브랜치 규칙, 이슈·PR 제목 규칙, DB 마이그레이션 파일 규칙을 정리할 문서입니다. 개발 프로세스는 아직 작성하지 않았습니다.

## 커밋 메시지

[Conventional Commits](https://www.conventionalcommits.org/ko/v1.0.0/)를 따릅니다.

```text
{타입}({범위}): {설명}
```

| 항목 | 규칙 |
| --- | --- |
| 타입 | `feat`, `fix`, `docs`, `refactor`, `test`, `cicd`, `chore` 등 영어 소문자 |
| 범위 | 선택 사항. 변경한 영역을 씁니다 (예: `front`, `back`, `widget`, `design`) |
| 설명 | 한국어로 무엇을 바꿨는지 짧게 씁니다 |

예: `feat(front): 새 디자인 적용`, `fix(back): 도서 데이터 적재 오류 수정`, `docs: 커밋 메시지 규칙 추가`

## 브랜치

```text
{타입}/{수정도메인}_{작업요약}
```

| 항목 | 규칙 |
| --- | --- |
| 타입 | 버그 수정은 `fix`, 기능 추가·변경은 `feature` |
| 수정도메인 | `back/src/main/kotlin/com/back/domain` 하위 패키지명 (`book`, `member`, `review`, `tag`, `widget`, `wish`). 도메인 밖의 백엔드 공통 코드를 고치면 `global`, 프론트엔드를 고치면 `front` |
| 작업요약 | 영어 소문자 `snake_case` |

예: `fix/book_fetch_books`, `feature/wish_add_sorting`

## 이슈·PR 제목

```text
[레이블] 내용
```

레이블은 GitHub 레이블(`fix`, `cicd`, `docs`, `feat`, `refactor`) 중 하나를 첫 글자만 대문자로 씁니다.

예: `[Docs] 문서 구조 개편`, `[Fix] 도서 데이터 적재 오류 수정`

도메인은 제목에 쓰지 않고 도메인 레이블(`front`, `back`, `infra`)을 이슈에 붙입니다. 도메인 밖의 공통 작업이나 문서 작업이면 도메인 레이블을 붙이지 않습니다.

PR에는 관련 이슈와 같은 레이블을 붙입니다. `ready`는 붙이지 않습니다.

`claude` 레이블은 Claude 클라우드 세션에서 작업 중인 이슈에 붙입니다. 작업 중 여부는 assignee가 아니라 이 레이블로 확인합니다.

## DB 마이그레이션 파일

```text
V{번호}__{설명}.sql
```

| 항목 | 규칙 |
| --- | --- |
| 위치 | `back/src/main/resources/db/migration/` |
| 번호 | 마지막 파일 다음 정수 |
| 설명 | 영어 소문자 `snake_case` |

예: `V2__add_member_bio.sql`

스키마가 바뀌는 엔티티 변경에는 같은 PR에 마이그레이션 파일을 함께 추가합니다. 빠뜨리면 기동이 `validate`에서 실패합니다.

`validate`는 테이블·컬럼·타입만 대조하고 인덱스와 유니크 제약은 검사하지 않습니다. 엔티티의 `@Table(indexes = ...)`, `unique = true`는 스키마에 반영되지 않으며 어떤 인덱스가 있는지 알려 주는 표시일 뿐입니다. 인덱스·제약을 추가하거나 바꿀 때는 마이그레이션 파일에 직접 쓰고, 엔티티의 표시도 함께 맞춥니다.

FULLTEXT 인덱스를 만드는 마이그레이션과 FULLTEXT 인덱스가 있는 테이블을 `ALTER`하는 마이그레이션은 파일 맨 위에 `SET SESSION innodb_ft_enable_stopword = OFF;`를 둡니다(`V1__init.sql`, `V2__add_book_search_text.sql` 참고). 테이블을 다시 만드는 `ALTER`는 기존 FULLTEXT 인덱스도 그 세션의 설정으로 다시 만들기 때문에, `CREATE FULLTEXT INDEX` 바로 앞이 아니라 첫 `ALTER`보다 앞이어야 합니다. 빠뜨리면 불용어(`a`, `i`, `in` 등)가 든 ngram 토큰이 인덱스에서 빠져 영문 검색 결과가 줄어듭니다.

## 줄바꿈

저장소의 줄바꿈은 LF로 통일합니다. 루트 `.gitattributes`가 적용하며, `.bat` 파일만 `back/.gitattributes`에 따라 CRLF를 씁니다.
