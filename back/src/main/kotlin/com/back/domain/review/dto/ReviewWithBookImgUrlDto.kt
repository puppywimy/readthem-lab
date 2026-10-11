package com.back.domain.review.dto

import com.back.domain.review.entity.Review

class ReviewWithBookImgUrlDto(review: Review)
    : ReviewDto(review) {
    val bookImgUrl : String? = review.book.imgUrl
}