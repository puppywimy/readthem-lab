package com.back.domain.book.controller.request

class BookUpdateRequest(
    val title: String,
    val description: String?,
    val authors: String?,
    val publisher: String?,
    val imgUrl: String?
)