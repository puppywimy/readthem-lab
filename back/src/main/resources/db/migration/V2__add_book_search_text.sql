-- 도서 검색(BookRepository.searchByKeyword)을 제목·저자 전처리 컬럼 기반 구문 검색으로 바꾼다.
-- 전처리 규칙은 BookSearchTextNormalizer와 같아야 한다. 규칙을 바꾸면 둘을 함께 고친다.
--   1. 기호 7개(+ # & . : - /)를 ngram이 글자로 취급하는 대체 문자로 바꾼다.
--   2. 글자(\p{L})·숫자(\p{Nd})가 아닌 나머지 문자(공백 포함)를 모두 지운다.
-- 값은 '전처리한 제목 + 공백 + 전처리한 저자'다. 가운데 공백에서 ngram 토큰이 끊겨 제목 끝과 저자 앞에 걸친 검색은 일치하지 않는다.

-- 불용어 설정은 현재 세션에만 적용되며, FULLTEXT 인덱스를 만들거나 다시 만드는 시점에 적용된다.
-- book을 ALTER하면 기존 FULLTEXT 인덱스도 다시 만들어지므로 ALTER보다 앞에 둔다.
SET SESSION innodb_ft_enable_stopword = OFF;

ALTER TABLE `book`
  ADD COLUMN `search_text` longtext GENERATED ALWAYS AS (
    CONCAT(
      REGEXP_REPLACE(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
          `title`,
          '+', 'ǂ'), '#', 'ǁ'), '&', 'ǀ'), '.', 'ǃ'), ':', 'ƻ'), '-', 'ʔ'), '/', 'ƾ'),
        '[^\\p{L}\\p{Nd}]', ''
      ),
      ' ',
      REGEXP_REPLACE(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
          COALESCE(`authors`, ''),
          '+', 'ǂ'), '#', 'ǁ'), '&', 'ǀ'), '.', 'ǃ'), ':', 'ƻ'), '-', 'ʔ'), '/', 'ƾ'),
        '[^\\p{L}\\p{Nd}]', ''
      )
    )
  ) STORED;

CREATE FULLTEXT INDEX ft_book_search_text
  ON book (search_text)
  WITH PARSER ngram;

DROP INDEX ft_book_search ON book;
