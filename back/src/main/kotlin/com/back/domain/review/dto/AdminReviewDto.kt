package com.back.domain.review.dto

import com.back.domain.member.dto.MemberDto
import com.back.domain.review.entity.Review
import java.time.LocalDateTime

class AdminReviewDto(
    val id: Long,
    val bookTitle: String,
    val rating: Float,
    val content: String,
    val createdDate: LocalDateTime,
    val reviewer: MemberDto,
    val tags: List<String>,
    val likeCount: Int
) {
    constructor(review: Review) : this(
        review.id,
        review.book.title,
        review.rating,
        review.content,
        review.createdDate,
        MemberDto(review.reviewer),
        review.tags,
        review.likeCount
    )
}