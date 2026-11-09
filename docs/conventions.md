# Conventions

커밋 메시지 컨벤션, 개발 프로세스, 브랜치 규칙, 이슈·PR 제목 규칙을 정리할 문서입니다. 개발 프로세스는 아직 작성하지 않았습니다.

## 커밋 메시지

[Conventional Commits](https://www.conventionalcommits.org/ko/v1.0.0/)를 따릅니다.

```text
{타입}({범위}): {설명}
```

| 항목 | 규칙 |
| --- | --- |
| 타입 | `feat`, `fix`, `docs`, `refactor`, `test`, `ci`, `chore` 등 영어 소문자 |
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

레이블은 GitHub 레이블(`fix`, `ci`, `docs`, `feat`, `refactor`) 중 하나를 첫 글자만 대문자로 씁니다.

예: `[Docs] 문서 구조 개편`, `[Fix] 도서 데이터 적재 오류 수정`

## 줄바꿈

저장소의 줄바꿈은 LF로 통일합니다. 루트 `.gitattributes`가 적용하며, `.bat` 파일만 `back/.gitattributes`에 따라 CRLF를 씁니다.
