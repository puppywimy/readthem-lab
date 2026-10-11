package com.back.domain.book.dto

import com.back.domain.book.entity.Book
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class BookDetailDto(
    id: Long,
    title: String,
    imgUrl: String?,
    val description: String?,
    val isbn: String,
    val publishedDate: String,
    val authors: List<String>,
    val publisher: String?,
    val translators: List<String>,
    val reviewCount: Int,
    val rating: Map<String, Any>,
    val tags: List<String>,
    val wishId: Long?
) : BookDtoBase(id, title, imgUrl) {
    constructor(
        book: Book,
        reviewCount: Int,
        ratingMap: Map<String, Any>,
        tags: List<String>,
        wishId: Long?
    ) : this(
        book.id,
        book.title,
        book.imgUrl,
        book.description,
        book.isbn,
        formatPublishedDate(book),
        parseAuthors(book),
        book.publisher,
        listOf<String>(),
        reviewCount,
        ratingMap,
        tags,
        wishId
    )

    companion object {

        private val AUTHOR_REGEX = Regex(",\\s*")

        private fun formatPublishedDate(book: Book): String {
            return book.publishedDate?.toLocalDate().toString() ?: ""
        }

        private fun parseAuthors(book: Book): List<String> {
            val authors = book.authors
            if (authors.isNullOrBlank()) return listOf()
            return authors.split(AUTHOR_REGEX)
        }
    }
}