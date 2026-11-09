## 회원 관리자 API `/members`

### `GET` `/members/admin` - 회원 다건 조회 🔑

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
        "githubId": "",
        "githubLink": "",
        "username": "",
        "nickname": "",
        "isAdmin": false,
        "isDeleted": false,
        "createdDate": "2026-06-22T10:00:00"
      }
    ],
    "pageable": {},
    "totalElements": 1,
    "totalPages": 1
  }
  ```
</aside>

### `DELETE` `/members/admin/{id}` - 회원 강제 탈퇴 🔑

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
    "message": "회원 강제 탈퇴를 성공했습니다."
  }
  ```
</aside>
