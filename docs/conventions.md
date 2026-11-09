# Conventions

커밋 메시지 컨벤션, 개발 프로세스, 브랜치 규칙, 이슈·PR 제목 규칙을 정리할 문서입니다. 커밋 메시지와 개발 프로세스는 아직 작성하지 않았습니다.

## 브랜치

```text
{타입}/{수정도메인}_{작업요약}
```

| 항목 | 규칙 |
| --- | --- |
| 타입 | 버그 수정은 `fix`, 기능 추가·변경은 `feature` |
| 수정도메인 | `back/src/main/kotlin/com/back/domain` 하위 패키지명 (`book`, `member`, `review`, `tag`, `widget`, `wish`). 도메인 밖의 공통 코드를 고치면 `global` |
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
