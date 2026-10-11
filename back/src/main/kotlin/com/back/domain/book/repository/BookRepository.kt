package com.back.domain.book.repository

import com.back.domain.book.entity.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BookRepository : JpaRepository<Book, Long> {

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

    @Query(
        value = """
            SELECT b.*
            FROM book b
            WHERE MATCH(b.title, b.authors, b.publisher)
                  AGAINST(:keyword IN BOOLEAN MODE)
            ORDER BY
                MATCH(b.title, b.authors, b.publisher)
                AGAINST(:keyword IN BOOLEAN MODE) DESC,
                b.id DESC
        """,
        countQuery = """
            SELECT COUNT(*)
            FROM book b
            WHERE MATCH(b.title, b.authors, b.publisher)
                  AGAINST(:keyword IN BOOLEAN MODE)
        """,
        nativeQuery = true
    )
    fun searchByKeyword(
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): Page<Book>
}
