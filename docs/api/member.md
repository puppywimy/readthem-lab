## 회원 API `/members`

### `POST` `/members` - 회원가입 ✅

<aside>

**Request**

- Body
  Media Type: `application/json`
  ```json
  {
    "username": "",
    "password": "",
    "githubId": ""
  }
  ```

**Response**

- 200
  Header
  ```json
  {
    "Set-Cookie": ""
  }
  ```
  Media Type: `application/json`
  ```json
  {
    "resultCode": "200-1",
    "message": "회원가입을 성공했습니다.",
    "data": {
      "accessToken": "",
      "refreshToken": ""
    }
  }
  ```

**Validation**

- username → 2~30자, 공백일 수 없습니다.
- password → 2~30자, 공백일 수 없습니다.
- githubId → 1~39자, 영문 소문자/숫자/하이픈만 가능하며 하이픈은 처음/끝/연속으로 올 수 없습니다.
</aside>

### `POST` `/members/login` - 로그인 ✅

<aside>

**Request**

- Body
  Media Type: `application/json`
  ```json
  {
    "username": "",
    "password": ""
  }
  ```

**Response**

- 200
  Header
  ```json
  {
    "Set-Cookie": ""
  }
  ```
  Media Type: `application/json`
  ```json
  {
    "resultCode": "200-1",
    "message": "로그인을 성공했습니다.",
    "data": {
      "accessToken": "",
      "refreshToken": ""
    }
  }
  ```

**Validation**

- username → 2~30자, 공백일 수 없습니다.
- password → 2~30자, 공백일 수 없습니다.
</aside>

### `GET` `/members/{id}` - 회원 단건 조회 ✅

<aside>

**Request**

- Path Variables : `id: long`

**Response**

- 200
  Media Type: `application/json`
  ```json
  {
    "id": 1,
    "githubId": "",
    "githubLink": ""
  }
  ```
- 404-1: 존재하지 않는 회원입니다. (NoSuchElementException) - id의 회원이 존재하지 않는 경우
</aside>

### `GET` `/members/me` - 내 정보 조회 🔑

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
    "id": 1,
    "username": "",
    "githubId": "",
    "githubLink": "",
    "widgetLink": ""
  }
  ```

</aside>

### `DELETE` `/members/logout` - 로그아웃 ✅

<aside>

**Response**

- 200
  Header
  ```json
  {
    "Set-Cookie": ""
  }
  ```
  Media Type: `application/json`
  ```json
  {
    "resultCode": "200-1",
    "message": "로그아웃을 성공했습니다."
  }
  ```

</aside>

### `DELETE` `/members` - 회원 탈퇴(소프트 삭제) 🔑

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
    "resultCode": "200-1",
    "message": "회원 탈퇴를 성공했습니다."
  }
  ```

</aside>
