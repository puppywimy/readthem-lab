# GlobalExceptionHandler

---

- 400 ConstraintViolationException, MethodArgumentNotValidException
  Media Type: `application/json`
  ```json
  {
    "resultCode": "400-1",
    "message": "필드-코드-메세지"
  }
  ```
- 400 HttpMessageNotReadableException
  Media Type: `application/json`
  ```json
  {
    "resultCode": "400-2",
    "message": "요청 본문이 올바르지 않습니다."
  }
  ```
- 400 MissingRequestHeaderException
  Media Type: `application/json`
  ```json
  {
    "resultCode": "400-2",
    "message": "헤더에 xx이 필요합니다."
  }
  ```
- 404 NoSuchElementException
  Media Type: `application/json`
  ```json
  {
    "resultCode": "404-1",
    "message": "존재하지 않는 xx입니다."
  }
  ```
- 404 NoResourceFoundException
  Media Type: `application/json`
  ```json
  {
    "resultCode": "404-1",
    "message": "리소스를 찾을 수 없습니다."
  }
  ```
- ServiceException
  - `ServiceException(resultCode, message)`의 `resultCode`와 `message`를 그대로 반환합니다.
- 500 Exception
  Media Type: `application/json`
  ```json
  {
    "resultCode": "500-1",
    "message": "일시적인 장애가 발생했습니다."
  }
  ```

### SecurityConfig

---

- 401
  Media Type: `application/json`
  ```json
  {
    "resultCode": "401-1",
    "message": "로그인 후 이용해주세요."
  }
  ```
