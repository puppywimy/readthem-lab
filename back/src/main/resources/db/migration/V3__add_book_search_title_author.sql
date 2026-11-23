-- 도서 검색(BookRepository.searchByKeyword)의 단계별 조회용 컬럼과 인덱스를 추가한다(#73).
-- search_text가 '전처리한 제목 + 공백 + 전처리한 저자'이고 전처리 결과에는 공백이 없으므로, 첫 공백 앞이 제목, 뒤가 저자다.
-- B-Tree 인덱스 키 길이 제한 때문에 VARCHAR(255)로 자르며, 255자를 넘는 값은 잘린 값으로 비교된다.

-- book을 ALTER하면 기존 FULLTEXT 인덱스도 다시 만들어질 수 있으므로 ALTER보다 앞에 둔다(docs/conventions.md).
SET SESSION innodb_ft_enable_stopword = OFF;

ALTER TABLE `book`
  ADD COLUMN `search_title` varchar(255) GENERATED ALWAYS AS (
    LEFT(SUBSTRING_INDEX(`search_text`, ' ', 1), 255)
  ) VIRTUAL,
  ADD COLUMN `search_author` varchar(255) GENERATED ALWAYS AS (
    LEFT(SUBSTRING(`search_text`, LOCATE(' ', `search_text`) + 1), 255)
  ) VIRTUAL,
  ADD INDEX `idx_book_search_title` (`search_title`),
  -- 저자 앞부분 일치 단계는 제목 앞부분 일치를 제외해야 해서, 행에 접근하지 않고 인덱스만으로 판단하도록 제목을 함께 둔다.
  ADD INDEX `idx_book_search_author` (`search_author`, `search_title`);
