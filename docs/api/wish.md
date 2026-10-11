## 찜 API `/wishes`

### `POST` `/wishes/book/{bookId}` - 찜 생성 🔑

<aside>

**Request**

- Path Variables : `bookId: long`
- Header
  ```json
  {
    "Authorization": "Bearer {{refreshToken}} {{accessToken}}",
    "Cookie": ""
  }
  ```

**Response**

- 201
  Media Type: `application/json`
  ```json
  {
    "resultCode": "201-1",
    "message": "찜 생성을 성공했습니다."
  }
  ```
- 409-1: 이미 존재하는 찜입니다. - 회원의 찜 목록 중 bookId가 존재하는 경우
</aside>

### `GET` `/wishes/mine` - 내 찜 다건 조회 🔑

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
  [
    {
      "id": 1,
      "title": "",
      "imgUrl": "",
      "averageRating": 3.8,
      "tags": ["", ""]
    }
  ]
  ```

</aside>

### `DELETE` `/wishes/{id}` - 찜 삭제 🔑

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
    "message": "찜 삭제를 성공했습니다."
  }
  ```
- 403
  Media Type: `application/json`
  ```json
  {
    "resultCode": "403-1",
    "message": "찜 삭제 권한이 없습니다."
  }
  ```
- 404-1: 존재하지 않는 찜입니다. - 회원에게 id의 찜 정보가 존재하지 않는 경우
</aside>
