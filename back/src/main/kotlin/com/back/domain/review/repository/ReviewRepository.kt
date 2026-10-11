package com.back.domain.review.repository

import com.back.domain.book.entity.Book
import com.back.domain.member.entity.Member
import com.back.domain.review.entity.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReviewRepository : JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = ["book", "reviewer"])
    fun findAllByOrderByIdDesc(pageable: Pageable): Page<Review>

    @EntityGraph(attributePaths = ["book", "reviewer"])
    fun findAllByOrderByLikeCountDescIdDesc(pageable: Pageable): Page<Review>

    @EntityGraph(attributePaths = ["book", "reviewer", "reviewTags", "reviewTags.tag"])
    fun findByBook(book: Book): List<Review>
    fun findByBook(book: Book, pageable: Pageable): Page<Review>

    fun findByBookId(bookId: Long): List<Review>

    fun findByReviewer(member: Member): List<Review>
    fun findByReviewer(member: Member, pageable: Pageable): Page<Review>

    @Query(
        "SELECT DISTINCT rl.review FROM ReviewLike rl " +
                "LEFT JOIN FETCH rl.review.book " +
                "LEFT JOIN FETCH rl.review.reviewer " +
                "LEFT JOIN FETCH rl.review.reviewTags rt " +
                "LEFT JOIN FETCH rt.tag " +
                "WHERE rl.member = :member " +
                "ORDER BY rl.review.id DESC"
    )
    fun findLikedReviewsByMember(@Param("member") member: Member): List<Review>

    fun countByReviewerAndRating(member: Member, rating: Float): Int
    fun countByReviewer(member: Member): Int
    fun countByReviewerAndContentNot(member: Member, content: String): Int

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.reviewer = :member")
    fun getAverageRatingByMember(member: Member): Double

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.book = :book")
    fun getAverageRatingByBook(@Param("book") book: Book): Double

    fun countByBook(book: Book): Int

    fun findFirstByOrderByIdDesc(): Review?

    fun countByBookAndRating(book: Book?, rating: Float): Int

    fun findFirstByBookAndReviewer(book: Book, reviewer: Member): Review?

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    fun increaseLikeCount(@Param("reviewId") reviewId: Long): Int

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :reviewId")
    fun decreaseLikeCount(@Param("reviewId") reviewId: Long): Int
}
