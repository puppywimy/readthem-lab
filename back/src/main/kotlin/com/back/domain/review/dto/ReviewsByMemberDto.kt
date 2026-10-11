package com.back.domain.review.dto

class ReviewsByMemberDto(
    val rating: Map<String, Any>,
    val results: List<ReviewWithBookImgUrlDto>
)