package com.back.domain.book.controller.response

import com.back.domain.book.dto.BookDto
import org.springframework.data.domain.Page

data class BookSearchResponse(
    val content: List<BookDto>,
    val last: Boolean,
    val totalElements: Long
) {
    constructor(page: Page<BookDto>) : this(
        page.content,
        page.isLast,
        page.totalElements
    )
}
