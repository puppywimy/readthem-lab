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
     * 제목·저자 전처리 컬럼(`search_text`)에서 [keyword]를 구문 검색해 [offset]부터 [size]건을 돌려준다.
     *
     * [keyword]는 BookSearchTextNormalizer로 전처리한 비어 있지 않은 값이어야 한다.
     * 전처리 결과에는 글자·숫자·대체 문자만 남으므로 BOOLEAN MODE 연산자나 LIKE 와일드카드가 들어가지 않는다.
     *
     * 정렬: 제목 완전 일치 → 제목 앞부분 일치 → 저자 앞부분 일치 → 나머지(관련도 내림차순).
     * 한 쿼리로 정렬하면 매칭된 행 전부에 접근해야 해서 흔한 검색어가 수 초씩 걸린다(#73).
     * 그래서 단계마다 인덱스로 앞쪽 [phaseLimit](= offset + size)건만 고르고 UNION ALL로 이어 붙인 뒤 바깥에서 자른다.
     * 앞 단계에 걸린 행은 뒤 단계 조건에서 빼서 한 번만, 가장 앞 단계에서 나온다.
     * - 1~3단계: search_title, search_author 인덱스(V3). 단계 안에서는 id 내림차순이다.
     *   2·3단계는 인덱스를 강제한다. 옵티마이저는 ORDER BY id DESC LIMIT을 보고 PK 역순 스캔을 고를 때가 있는데,
     *   맞는 행이 오래된 id에 몰려 있으면 수십만 행을 읽는다(3단계 '한국': 17만 행, 1초).
     *   인덱스 경로는 맞는 건수만큼만 읽어 응답 시간 상한이 정해진다.
     *   1단계는 완전 일치라 인덱스가 id 순서까지 처리해 PK 역순을 고를 이유가 없으므로 강제하지 않는다.
     * - 4단계: ORDER BY가 관련도 하나뿐이어야 FULLTEXT 인덱스 안에서 정렬된 순서로 읽는다(Ft_hints: sorted).
     *   이 순서에서 관련도가 같은 행은 FULLTEXT 문서 id 오름차순으로 나오므로, 바깥에서도 id 오름차순으로 맞춘다.
     *   한계: 관리자가 제목·저자를 고친 도서는 문서 id가 새로 붙어 id 순서와 어긋나므로, 관련도가 같은 행이
     *   단계별 LIMIT 경계에 걸리면 페이지 사이에서 누락·중복될 수 있다. 4단계 정렬 기준을 바꿀 때 함께 다룬다.
     *
     * ngram 구문 검색은 토큰이 연달아 나오는지만 봐서, 제목 끝 글자와 저자 첫 글자가 같으면
     * 둘에 걸친 검색어도 일치한다(제목 '하얼빈', 저자 '빈센트', 검색어 '얼빈센'). 4단계의 LIKE 조건이 이를 걸러낸다.
     */
    @Query(
        value = """
            SELECT b.*
            FROM (
                (SELECT id, 1 AS phase, 0 AS score
                 FROM book
                 WHERE search_title = :keyword
                 ORDER BY id DESC
                 LIMIT :phaseLimit)
                UNION ALL
                (SELECT id, 2, 0
                 FROM book FORCE INDEX (idx_book_search_title)
                 WHERE search_title LIKE CONCAT(:keyword, '%')
                   AND search_title <> :keyword
                 ORDER BY id DESC
                 LIMIT :phaseLimit)
                UNION ALL
                (SELECT id, 3, 0
                 FROM book FORCE INDEX (idx_book_search_author)
                 WHERE search_author LIKE CONCAT(:keyword, '%')
                   AND search_title NOT LIKE CONCAT(:keyword, '%')
                 ORDER BY id DESC
                 LIMIT :phaseLimit)
                UNION ALL
                (SELECT id, 4, MATCH(search_text) AGAINST(CONCAT('"', :keyword, '"') IN BOOLEAN MODE)
                 FROM book
                 WHERE MATCH(search_text) AGAINST(CONCAT('"', :keyword, '"') IN BOOLEAN MODE)
                   AND search_text LIKE CONCAT('%', :keyword, '%')
                   AND search_title NOT LIKE CONCAT(:keyword, '%')
                   AND search_author NOT LIKE CONCAT(:keyword, '%')
                 ORDER BY MATCH(search_text) AGAINST(CONCAT('"', :keyword, '"') IN BOOLEAN MODE) DESC
                 LIMIT :phaseLimit)
            ) t
            JOIN book b ON b.id = t.id
            ORDER BY t.phase, t.score DESC, IF(t.phase = 4, t.id, -t.id)
            LIMIT :size OFFSET :offset
        """,
        nativeQuery = true
    )
    fun searchByKeyword(
        @Param("keyword") keyword: String,
        @Param("offset") offset: Long,
        @Param("size") size: Int,
        @Param("phaseLimit") phaseLimit: Long = offset + size
    ): List<Book>
}
