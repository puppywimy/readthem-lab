## **도서 API** `/books`

### `GET` `/books/rank` - 도서 다건 조회 ✅

<aside>

**Request**

- Query Parameter
  | 이름 | 타입 | 설명 |
  | ---- | ---- | ---- |
  | type | string | 기본값 "" |
  | page | int | 기본값 0 |
  | size | int | 기본값 10 |

**Response**

- 200

Media Type: `application/json`

```json
[
  {
    "id": 1,
    "title": "",
    "imgUrl": "",
    "averageRating": 3.8
  }
]
```

</aside>

### `GET` `/books/{id}` - 도서 단건 조회 ✅ 🔑

<aside>

**Request**

- Path Variables : `id: long`
- Header (선택)
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
    "id": 1,
    "title": "",
    "description": "",
    "isbn": "",
    "publishedDate": "",
    "authors": ["", ""],
    "publisher": "", // 출판사
    "translators": ["", ""],
    "imgUrl": "",
    "reviewCount": 10,
    "rating": {
      "average": 3.8,
      "0.5": 1,
      "1.0": 2
    },
    "tags": ["", ""],
    "isWished": false // 인증된 사용자에 따라 찜 여부 확인 (선택)
  }
  ```
- 404-1 bookId로 Book을 찾을 수 없는 경우 (NoSuchElementException)
</aside>

### `GET` `/books/search` - 도서 검색 다건 조회 ✅

<aside>

**Request**

- Query Parameter
  | 이름 | 타입 | 설명 |
  | ---- | ---- | ---- |
  | searchTerm | string | 필수 |
  | page | int | 기본값 0 |
  | size | int | 기본값 10 |

**Response**

- 200
  Media Type: `application/json`
  ```json
  [
    {
      "id": 1,
      "title": "",
      "imgUrl": "",
      "averageRating": 3.8
    }
  ]
  ```

**Validation**

- searchTerm → 필수이며 공백일 수 없습니다.
</aside>

### `GET` `/books/recommend` - 도서 추천 다건 조회 🔑

<aside>

**Request**

- Header (선택)
  ```json
  {
    "Authorization": "Bearer {{refreshToken}} {{accessToken}}",
    "Cookie": ""
  }
  ```
- Query Parameter
  | 이름 | 타입 | 설명 |
  | ---- | ---- | ---- |
  | maxCount | int | 기본값 10 |

**Response**

- 200
  Media Type: `application/json`
  ```json
  [
    {
      "id": 1,
      "title": "",
      "imgUrl": "",
      "averageRating": 3.8
    }
  ]
  ```
</aside>
