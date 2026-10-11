package com.back.domain.review.dto

import com.back.domain.member.dto.MemberDto
import com.back.domain.review.entity.Review
import java.time.LocalDateTime

open class ReviewDto(
    val id: Long,
    val bookId: Long,
    val bookTitle: String,
    val rating: Float,
    val content: String,
    val modifiedDate: LocalDateTime,
    val createdDate: LocalDateTime,
    val reviewer: MemberDto,
    val tags: List<String>,
    val likeCount: Int,
    val likedByMe: Boolean = false
) {
    constructor(review: Review, likedByMe: Boolean = false) : this(
        review.id,
        review.book.id,
        review.book.title,
        review.rating,
        review.content,
        review.modifiedDate,
        review.createdDate,
        MemberDto(review.reviewer),
        review.tags,
        review.likeCount,
        likedByMe
    )
}
