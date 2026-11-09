## 리뷰 API(별점 정보 필요) `/reviews`

### `POST` `/reviews/book/{bookId}` - 리뷰 생성 🔑

<aside>

**Request**

- Header
  ```json
  {
    "Authorization": "Bearer {{refreshToken}} {{accessToken}}",
    "Cookie": ""
  }
  ```
- Path Variables : `bookId: long`
- Body
  Media Type: `application/json`
  ```json
  {
    "rating": 3.5,
    "content": "책 좋네요 ㅎㅎ.",
    "tags": ["", ""]
  }
  ```

**Response**

- 201
  Media Type: `application/json`
  ```json
  {
    "resultCode": "201-1",
    "message": "리뷰 생성을 성공했습니다.",
    "data": {
      "id": 1,
      "bookId": 1,
      "bookTitle": "",
      "rating": 3.5,
      "content": "책 좋네요 ㅎㅎ.",
      "modifiedDate": "2026-06-22T10:00:00",
      "createdDate": "2026-06-22T10:00:00",
      "reviewer": {
        "id": 1,
        "githubId": "githubId123",
        "githubLink": ""
      },
      "tags": ["", ""]
    }
  }
  ```
- 409
  Media Type: `application/json`
  ```json
  {
    "resultCode": "409-1",
    "message": "이미 존재하는 리뷰입니다."
  }
  ```
- 404-1 bookId로 Book를 찾을 수 없는 경우 (NoSuchElementException)

**Validation**

- rating → 0 ~ 5 사이 0.5 단위 숫자
- content → 500자 이하
- tags → 1~30자, 최대 5개, 중복이 없어야 함.
</aside>

### `GET` `/reviews/book/{bookId}` - 리뷰 다건 조회 ✅

<aside>

**Request**

- Path Variables : `bookId: long`

**Response**

- 200
  Media Type: `application/json`
  ```json
  [
    {
      "id": 1,
      "bookId": 1,
      "bookTitle": "",
      "rating": 3.5,
      "content": "책 좋네요 ㅎㅎ.",
      "modifiedDate": "2026-06-22T11:00:00",
      "createdDate": "2026-06-22T11:00:00",
      "reviewer": {
        "id": 1,
        "githubId": "githubId123",
        "githubLink": ""
      },
      "tags": ["", ""]
    }
  ]
  ```
- 404-1 bookId로 Book를 찾을 수 없는 경우 (NoSuchElementException)
</aside>

### `GET` `/reviews/member/{memberId}` - 회원별 리뷰 다건 조회 ✅

<aside>

**Request**

- Path Variables : `memberId: long`

**Response**

- 200
  Media Type: `application/json`
  ```json
  {
    "rating": {
      "average": 3.8,
      "0.0": 1,
      "0.5": 3
    },
    "results": [
      {
        "id": 1,
        "bookId": 1,
        "bookTitle": "",
        "rating": 3.5,
        "content": "책 좋네요 ㅎㅎ.",
        "modifiedDate": "2026-06-22T11:00:00",
        "createdDate": "2026-06-22T11:00:00",
        "reviewer": {
          "id": 1,
          "githubId": "githubId123",
          "githubLink": ""
        },
        "bookImgUrl": "",
        "tags": ["", ""]
      }
    ]
  }
  ```
- 404-1 memberId로 Member를 찾을 수 없는 경우 (NoSuchElementException)
</aside>

### `GET` `/reviews/member/mine` - 내 리뷰 다건 조회(통계 포함) 🔑

<aside>

**Request**

- Header
  ```json
  {
    "Authorization": "Bearer {{refreshToken}} {{accessToken}}",
    "Cookie": ""
  }
  ```

**Response**

- 200
  Media Type: `application/json`
  ```json
  {
    "rating": {
      "average": 3.8,
      "0.0": 1,
      "0.5": 3
    },
    "results": [
      {
        "id": 1,
        "bookId": 1,
        "bookTitle": "",
        "rating": 3.5,
        "content": "책 좋네요 ㅎㅎ.",
        "modifiedDate": "2026-06-22T11:00:00",
        "createdDate": "2026-06-22T11:00:00",
        "reviewer": {
          "id": 1,
          "githubId": "githubId123",
          "githubLink": ""
        },
        "bookImgUrl": "",
        "tags": ["", ""]
      }
    ]
  }
  ```

</aside>

### `PUT` `/reviews/{reviewId}` - 리뷰 수정 🔑

<aside>

**Request**

- Header
  ```json
  {
    "Authorization": "Bearer {{refreshToken}} {{accessToken}}",
    "Cookie": ""
  }
  ```
- Path Variables : `reviewId: long`
- Body
  Media Type: `application/json`
  ```json
  {
    "rating": 5,
    "content": "다시 읽어보니 더 좋네요 ㅎㅎ",
    "tags": ["", ""]
  }
  ```

**Response**

- 200
  Media Type: `application/json`
  ```json
  {
    "resultCode": "200-1",
    "message": "리뷰 수정을 성공했습니다.",
    "data": {
      "id": 1,
      "rating": 5,
      "bookId": 1,
      "bookTitle": "",
      "content": "다시 읽어보니 더 좋네요.",
      "modifiedDate": "2026-06-22T11:00:00",
      "createdDate": "2026-06-22T10:00:00",
      "reviewer": {
        "id": 1,
        "githubId": "githubId123",
        "githubLink": ""
      },
      "tags": ["", ""]
    }
  }
  ```
- 403
  Media Type: `application/json`
  ```json
  {
    "resultCode": "403-1",
    "message": "리뷰 수정 권한이 없습니다."
  }
  ```
- 404-1 reviewId로 Review를 찾을 수 없는 경우 (NoSuchElementException)

**Validation**

- rating → 0 ~ 5 사이 0.5 단위 숫자
- content → 500자 이하
- tags → 1~30자, 최대 5개, 중복이 없어야 함.
</aside>

### `DELETE` `/reviews/{reviewId}` - 리뷰 삭제 🔑

<aside>

**Request**

- Header
  ```json
  {
    "Authorization": "Bearer {{refreshToken}} {{accessToken}}",
    "Cookie": ""
  }
  ```
- Path Variables : `reviewId: long`

**Response**

- 200
  Media Type: `application/json`
  ```json
  {
    "resultCode": "200-1",
    "message": "리뷰 삭제를 성공했습니다."
  }
  ```
- 403
  Media Type: `application/json`
  ```json
  {
    "resultCode": "403-1",
    "message": "리뷰 삭제 권한이 없습니다."
  }
  ```
- 404-1 reviewId로 Review를 찾을 수 없는 경우 (NoSuchElementException)
</aside>

### `GET` `/reviews/latest` - 최신 리뷰 다건 조회 ✅

<aside>

**Request**

- Query Parameter
  | page | int | 기본값 0 |
  | ---- | --- | -------- |
  | size | int | 기본값 10 |

**Response**

- 200
  Media Type: `application/json`
  ```json
  [
    {
      "id": 1,
      "bookId": 1,
      "bookTitle": "",
      "rating": 3.5,
      "content": "책 좋네요 ㅎㅎ.",
      "modifiedDate": "2026-06-22T11:00:00",
      "createdDate": "2026-06-22T11:00:00",
      "reviewer": {
        "id": 1,
        "githubId": "githubId123",
        "githubLink": ""
      },
      "tags": ["", ""],
      "bookImgUrl": ""
    }
  ]
  ```
</aside>
