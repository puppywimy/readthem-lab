package com.back.domain.book.service

import com.back.domain.book.dto.BookDetailDto
import com.back.domain.book.dto.BookDto
import com.back.domain.book.entity.Book
import com.back.domain.member.entity.Member
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

data class BookDetailResult(
    val bookDetail: BookDetailDto,
    val viewCountIncreased: Boolean
)

@Service
class BookService(
    private val bookQueryService: BookQueryService,
    private val bookCommandService: BookCommandService,
    private val bookViewCountService: BookViewCountService,
) {

    companion object {
        private val log = LoggerFactory.getLogger(Companion::class.java)
    }

    fun getBookById(bookId: Long): Book {
        return bookQueryService.getBookById(bookId)
    }

    fun getBookViewCount(book: Book): Int {
        return bookViewCountService.getBookViewCount(book)
    }

    fun getBooksOrderByRank(type: String, page: Int, size: Int): List<BookDto> {
        return bookQueryService.getBooksOrderByRank(type, page, size)
    }

    fun incrementViewCount(book: Book, alreadyViewed: Boolean = false): Boolean {
        return bookViewCountService.incrementViewCount(book, alreadyViewed)
    }

    fun updateBooksViewCountInDb() {
        bookViewCountService.updateBooksViewCountInDb()
    }

    fun getBook(id: Long): Book {
        return bookQueryService.getBook(id)
    }

    fun getBookDetail(id: Long, actor: Member?, alreadyViewed: Boolean): BookDetailResult {
        val book = getBook(id)

        val viewCountIncreased = bookViewCountService.incrementViewCount(book, alreadyViewed)

        return BookDetailResult(
            bookQueryService.getBookDetailDto(id, actor),
            viewCountIncreased
        )
    }

    fun updateBook(
        id: Long,
        title: String,
        description: String?,
        authors: String?,
        publisher: String?,
        imgUrl: String?
    ): BookDto {
        return bookCommandService.updateBook(id, title, description, authors, publisher, imgUrl)
    }

    fun deleteBook(id: Long) {
        bookCommandService.deleteBook(id)
    }

    fun getBooks(page: Int, size: Int): Page<BookDto> {
        return bookQueryService.getBooks(page, size)
    }

    fun getTagsByBookId(bookId: Long): List<String> {
        return bookQueryService.getTagsByBookId(bookId)
    }

    fun getBooksBySearch(
        searchTerm: String,
        page: Int,
        size: Int
    ): List<BookDto> {

        try {
            return bookQueryService.getBooksByFullTextSearch(searchTerm, page, size)
        } catch (e: Exception) {
            log.error("FULLTEXT search failed", e)
            return bookQueryService.getBooksByLikeSearch(searchTerm, page, size)
        }
    }
}
