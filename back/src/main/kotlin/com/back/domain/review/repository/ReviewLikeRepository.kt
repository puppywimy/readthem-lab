package com.back.domain.review.repository

import com.back.domain.member.entity.Member
import com.back.domain.review.entity.Review
import com.back.domain.review.entity.ReviewLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReviewLikeRepository : JpaRepository<ReviewLike, Long> {
    fun existsByReviewAndMember(review: Review, member: Member): Boolean

    @Modifying
    @Query("DELETE FROM ReviewLike rl WHERE rl.review = :review AND rl.member = :member")
    fun deleteByReviewAndMember(@Param("review") review: Review, @Param("member") member: Member): Int

    fun deleteAllByReview(review: Review)

    @Query("SELECT rl.review.id FROM ReviewLike rl WHERE rl.member = :member AND rl.review IN :reviews")
    fun findReviewIdsByMemberAndReviewIn(@Param("member") member: Member, @Param("reviews") reviews: List<Review>): List<Long>

}
