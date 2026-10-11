package com.back.domain.review.controller.request

import com.back.domain.review.validation.ValidRating
import com.back.domain.review.validation.ValidTags
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class ReviewUpdateRequest(
    @field:NotNull
    @field:ValidRating
    val rating: Float?,
    @field:Size(max = 500)
    val content: String,
    @field:ValidTags
    val tags: List<String>
)