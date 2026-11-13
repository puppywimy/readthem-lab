# API

이 프로젝트의 API 명세입니다.

## 도서

### 도서 순위 다건 조회

```text
GET /api/v1/books/rank?type={type}&size={size}
```

로그인 없이 호출할 수 있습니다. 페이징 없이 항상 상위 `size`권을 반환하며, 도서가 `size`권보다 적을 때만 덜 내려옵니다.

| 파라미터 | 기본값 | 설명 |
| --- | --- | --- |
| `type` | `""` | 정렬 기준. `views`, `rating`, 그 밖의 값(`reviewCount` 등) |
| `size` | `10` | 반환할 도서 수 |

| `type` | 정렬 기준 |
| --- | --- |
| `views` | 인기 도서. 아래 순서로 채웁니다. |
| `rating` | 평균 별점 내림차순, 같으면 운영 정보 `id` 내림차순 |
| 그 밖의 값 | 리뷰 수 내림차순, 같으면 운영 정보 `id` 내림차순 |

`rating`과 리뷰 수 순위에서 운영 정보(`book_operational`)가 있는 도서가 `size`권보다 적으면, 운영 정보가 없는 도서를 도서 `id` 내림차순으로 이어 붙입니다.

`views`는 다음 순서로 `size`권이 찰 때까지 채웁니다. 앞 단계에 들어간 도서는 뒤 단계에서 뺍니다.

1. 최근 60분 조회수 순 (Redis 분 단위 키 `viewCount:minute:*`). Redis 조회에 실패하면 건너뜁니다.
2. 누적 조회수 순 (DB `book_operational.view_count`). Redis `viewCount`에 쌓인 조회수를 `BookScheduler`가 5분마다(`prod` 프로필) 반영하므로 최대 5분 늦을 수 있습니다.
3. 운영 정보가 없는 도서, 도서 `id` 내림차순

응답은 도서 목록(`id`, `title`, `imgUrl`, `averageRating`)입니다.
