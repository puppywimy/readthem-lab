package com.back.domain.book.dto

import com.back.domain.book.entity.Book

open class BookDto(
    id: Long,
    title: String,
    imgUrl: String?,
    val averageRating: Double
) : BookDtoBase(id, title, imgUrl) {
    constructor(book: Book, averageRating: Double) : this(
        book.id,
        book.title,
        book.imgUrl,
        averageRating)
}