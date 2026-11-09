## 도서 관리자 API `/books`

### `GET` `/books/admin` - 도서 다건 조회 🔑

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
        "title": "",
        "imgUrl": "",
        "averageRating": 3.8
      }
    ],
    "pageable": {},
    "totalElements": 1,
    "totalPages": 1
  }
  ```
</aside>

### `PUT` `/books/{id}` - 도서 수정 🔑

<aside>

**Request**

- Path Variables : `id: long`
- Header
  ```json
  {
    "Authorization": "Bearer {{refreshToken}} {{accessToken}}",
    "Cookie": ""
  }
  ```
- Body
  Media Type: `application/json`
  ```json
  {
    "title": "",
    "description": "",
    "authors": "",
    "publisher": "",
    "imgUrl": ""
  }
  ```

**Response**

- 200
  Media Type: `application/json`
  ```json
  {
    "resultCode": "200-1",
    "message": "도서 수정을 성공했습니다.",
    "data": {
      "id": 1,
      "title": "",
      "imgUrl": "",
      "averageRating": 3.8
    }
  }
  ```
</aside>

### `DELETE` `/books/{id}` - 도서 삭제 🔑

<aside>

**Request**

- Path Variables : `id: long`
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
    "resultCode": "200-1",
    "message": "도서 삭제를 성공했습니다."
  }
  ```
</aside>
