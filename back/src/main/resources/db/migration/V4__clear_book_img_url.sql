-- 원본이 지나치게 큰 표지가 홈 표지 월을 끊기게 해서(#60) 도서 표지를 카카오 썸네일로 통일한다.
-- 표지 주소와 조회 이력을 비우면 도서가 조회될 때 GET /api/v1/books/{id}/thumbnail이 카카오 썸네일 주소로 다시 채운다.
-- 컬럼 값만 비우므로 이전 버전 코드와도 호환된다. book을 ALTER하지 않으므로 FULLTEXT 불용어 설정은 필요 없다.
UPDATE `book`
SET `img_url` = NULL,
    `img_url_fetched_at` = NULL
WHERE `img_url` IS NOT NULL
   OR `img_url_fetched_at` IS NOT NULL;
