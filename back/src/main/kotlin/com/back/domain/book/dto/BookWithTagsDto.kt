package com.back.domain.book.dto

import com.back.domain.book.entity.Book

class BookWithWishIdAndTagsDto(
    book: Book,
    averageRating: Double,
    val wishId: Long,
    val tags: List<String>,
) : BookDto(book, averageRating)
