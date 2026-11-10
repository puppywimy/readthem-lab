# Other Considerations

이미 논의를 거쳐 결정된 내용을 다시 논의하지 않도록 기록하는 문서입니다.

## 인증 쿠키 SameSite

- `cookieSecure=true`(운영)면 `SameSite=None`, 아니면 `Strict`를 쓴다. (`Rq.setCookie`)
- 이유: 프론트(`pages.dev`)와 백엔드가 서로 다른 사이트라 운영에서는 `None`이 필요하고, `None`은 `Secure` 없이는 브라우저가 거부해 로컬에서는 `Strict`를 쓴다.
- `CUSTOM__PROD__COOKIE_DOMAIN`은 프론트가 아니라 백엔드 도메인이어야 한다.
- 한계: 서드파티 쿠키를 차단하는 브라우저(Safari, Chrome 시크릿)에서는 로그인이 유지되지 않는다. 프론트와 백엔드를 같은 사이트로 묶으면 운영도 `Strict`로 되돌릴 수 있다.
