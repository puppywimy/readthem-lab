## 태그 API `/tags`

### `POST` `/tags` - 태그 생성 ✅

<aside>

**Request**

- Body
  Media Type: `application/json`
  ```json
  {
    "name": ""
  }
  ```

**Response**

- 201
  Media Type: `application/json`
  ```json
  {
    "resultCode": "201-1",
    "message": "태그 생성을 성공했습니다."
  }
  ```
- 409-1: 이미 존재하는 태그입니다.

**Validation**

- name → 1~20자이며 공백일 수 없습니다.
</aside>
