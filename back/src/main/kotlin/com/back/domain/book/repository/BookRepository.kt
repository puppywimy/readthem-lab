package com.back.domain.book.repository

import com.back.domain.book.entity.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BookRepository : JpaRepository<Book, Long>, BookRepositoryCustom {

    fun existsByIsbn(isbn: String): Boolean
    fun findByIsbn(isbn: String): Book?
    fun findByIsbnIn(isbns: Collection<String>): List<Book>

    fun findByTitleContaining(searchTerm: String, pageable: Pageable): Page<Book>

    @Query(
        """
            SELECT b
            FROM Book b
            LEFT JOIN BookOperational bo ON bo.isbn = b.isbn
            ORDER BY COALESCE(bo.averageRating, 0.0) DESC, b.id DESC
        """
    )
    fun findAllOrderByAverageRatingDesc(pageable: Pageable): Page<Book>

    @Query(
        """
            SELECT b
            FROM Book b
            LEFT JOIN BookOperational bo ON bo.isbn = b.isbn
            ORDER BY COALESCE(bo.reviewCount, 0) DESC, b.id DESC
        """
    )
    fun findAllOrderByReviewCountDesc(pageable: Pageable): Page<Book>

    @Query(
        """
            SELECT b
            FROM Book b
            LEFT JOIN BookOperational bo ON bo.isbn = b.isbn
            ORDER BY COALESCE(bo.viewCount, 0) DESC, b.id DESC
        """
    )
    fun findAllOrderByViewCountDesc(pageable: Pageable): Page<Book>

    @Query(
        """
            SELECT b
            FROM Book b
            WHERE NOT EXISTS (
                SELECT 1
                FROM BookOperational bo
                WHERE bo.isbn = b.isbn
            )
            ORDER BY b.id DESC
        """
    )
    fun findBooksWithoutOperationalOrderByIdDesc(pageable: Pageable): List<Book>

    /**
     * 제목·저자 전처리 컬럼(`search_text`)에서 [keyword]를 구문 검색한다.
     *
     * [keyword]는 BookSearchTextNormalizer로 전처리한 비어 있지 않은 값이어야 한다.
     * 전처리 결과에는 글자·숫자·대체 문자만 남으므로 BOOLEAN MODE 연산자나 LIKE 와일드카드가 들어가지 않는다.
     *
     * ngram 구문 검색은 토큰이 연달아 나오는지만 봐서, 제목 끝 글자와 저자 첫 글자가 같으면
     * 둘에 걸친 검색어도 일치한다(제목 '하얼빈', 저자 '빈센트', 검색어 '얼빈센'). LIKE 조건이 이를 걸러낸다.
     * countQuery에는 이 조건이 없어 총 건수가 조금 많을 수 있다. 총 건수는 지금 쓰이지 않는다(#36).
     *
     * 정렬: 제목 완전 일치 → 제목 앞부분 일치 → 관련도 내림차순 → id 내림차순.
     * 컬럼 값이 '전처리한 제목 + 공백 + 전처리한 저자'라서 'keyword %'는 제목 완전 일치, 'keyword%'는 제목 앞부분 일치다.
     */
    @Query(
        value = """
            SELECT b.*
            FROM book b
            WHERE MATCH(b.search_text)
                  AGAINST(CONCAT('"', :keyword, '"') IN BOOLEAN MODE)
              AND b.search_text LIKE CONCAT('%', :keyword, '%')
            ORDER BY
                b.search_text LIKE CONCAT(:keyword, ' %') DESC,
                b.search_text LIKE CONCAT(:keyword, '%') DESC,
                MATCH(b.search_text)
                AGAINST(CONCAT('"', :keyword, '"') IN BOOLEAN MODE) DESC,
                b.id DESC
        """,
        countQuery = """
            SELECT COUNT(*)
            FROM book b
            WHERE MATCH(b.search_text)
                  AGAINST(CONCAT('"', :keyword, '"') IN BOOLEAN MODE)
        """,
        nativeQuery = true
    )
    fun searchByKeyword(
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): Page<Book>
}
