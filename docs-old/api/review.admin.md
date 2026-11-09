## 리뷰 관리자 API `/reviews`

### `GET` `/reviews/admin` - 리뷰 다건 조회 🔑

<aside>

**Request**

- Header
  ```json
  {
    "Authorization": "Bearer {{refreshToken}} {{accessToken}}",
    "Cookie": ""
  }
  ```
- Query Parameter
  | 이름 | 타입 | 설명 |
  | ---- | ---- | ---- |
  | page | int | 기본값 0 |
  | size | int | 기본값 10 |

**Response**

- 200
  Media Type: `application/json`
  ```json
  {
    "content": [
      {
        "id": 1,
        "bookTitle": "",
        "rating": 3.5,
        "content": "책 좋네요 ㅎㅎ.",
        "createdDate": "2026-06-22T10:00:00",
        "reviewer": {
          "id": 1,
          "githubId": "",
          "githubLink": ""
        },
        "tags": ["", ""]
      }
    ],
    "pageable": {},
    "totalElements": 1,
    "totalPages": 1
  }
  ```
</aside>
