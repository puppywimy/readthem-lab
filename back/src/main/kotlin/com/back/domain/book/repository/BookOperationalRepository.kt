package com.back.domain.book.repository

import com.back.domain.book.entity.BookOperational
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BookOperationalRepository : JpaRepository<BookOperational, Long> {

    fun findByIsbn(isbn: String) : BookOperational?
    fun findByIsbnIn(isbns: Collection<String>): List<BookOperational>
    fun deleteByIsbn(isbn: String)

    @Query(
        """
            SELECT bo.isbn AS isbn, bo.averageRating AS averageRating
            FROM BookOperational bo
            WHERE EXISTS (
                SELECT 1
                FROM Book b
                WHERE b.isbn = bo.isbn
            )
            ORDER BY bo.averageRating DESC, bo.id DESC
        """
    )
    fun findRankedBooksByAverageRating(pageable: Pageable): List<BookRankRow>

    @Query(
        """
            SELECT bo.isbn AS isbn, bo.averageRating AS averageRating
            FROM BookOperational bo
            WHERE EXISTS (
                SELECT 1
                FROM Book b
                WHERE b.isbn = bo.isbn
            )
            ORDER BY bo.reviewCount DESC, bo.id DESC
        """
    )
    fun findRankedBooksByReviewCount(pageable: Pageable): List<BookRankRow>

    @Query(
        """
            SELECT bo.isbn AS isbn, bo.averageRating AS averageRating
            FROM BookOperational bo
            WHERE EXISTS (
                SELECT 1
                FROM Book b
                WHERE b.isbn = bo.isbn
            )
            ORDER BY bo.viewCount DESC, bo.id DESC
        """
    )
    fun findRankedBooksByViewCount(pageable: Pageable): List<BookRankRow>
}
