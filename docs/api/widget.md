## 위젯 API `/widgets`

### `GET` `/widgets/{githubId}` - 위젯 단건 조회 ✅

<aside>

**Request**

- Path Variables : `githubId: String`

**Response**

- 200
  Media Type: `image/svg+xml`
  ```xml
  <svg> ... </svg>
  ```
- 404-1 githubId로 Member를 찾을 수 없는 경우 (NoSuchElementException)

**Validation**

- githubId → githubId 규칙과 동일하게
</aside>

### `GET` `/widgets/{githubId}/raw` - 위젯 정보 단건 조회 ✅

<aside>

**Request**

- Path Variables : `githubId: String`

**Response**

- 200
  Media Type: `application/json`
  ```json
  {
    "recentReadBooks": [
      {
        "title": "",
        "withReview": true
      }
    ],
    "readCount": 1,
    "reviewCount": 1,
    "wishCount": 1
  }
  ```
- 404-1 githubId로 Member를 찾을 수 없는 경우 (NoSuchElementException)

**Validation**

- githubId → githubId 규칙과 동일하게
</aside>

### `GET` `/v2/widgets/{githubId}` - 위젯 단건 조회(V2) ✅

<aside>

**Request**

- Path Variables : `githubId: String`

**Response**

- 200
  Media Type: `image/svg+xml`
  ```xml
  <svg> ... </svg>
  ```
- 404-1 githubId로 Member를 찾을 수 없는 경우 (NoSuchElementException)
</aside>
