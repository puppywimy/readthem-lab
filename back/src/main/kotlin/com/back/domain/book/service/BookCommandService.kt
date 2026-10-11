package com.back.domain.book.service

import com.back.domain.book.dto.BookDto
import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.review.repository.ReviewRepository
import com.back.domain.wish.repository.WishRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookCommandService(
    private val bookRepository: BookRepository,
    private val bookOperationalRepository: BookOperationalRepository,
    private val reviewRepository: ReviewRepository,
    private val wishRepository: WishRepository,
) {

    @Transactional
    fun updateBook(
        id: Long,
        title: String,
        description: String?,
        authors: String?,
        publisher: String?,
        imgUrl: String?
    ): BookDto {
        val book = getBookById(id)

        book.update(title, description, authors, publisher, imgUrl)

        return getBookDto(book)
    }

    @Transactional
    fun deleteBook(id: Long) {
        val book = getBookById(id)

        reviewRepository.deleteAll(reviewRepository.findByBook(book))
        wishRepository.deleteAllByBook(book)
        bookOperationalRepository.deleteByIsbn(book.isbn)

        bookRepository.delete(book)
    }

    private fun getBookById(bookId: Long): Book {
        return bookRepository.findById(bookId)
            .orElseThrow { NoSuchElementException("존재하지 않는 도서입니다.") }
    }

    private fun getBookOperational(book: Book) =
        bookOperationalRepository.findByIsbn(book.isbn)

    private fun getBookDto(book: Book): BookDto {
        return BookDto(book, getBookOperational(book)?.averageRating ?: 0.0)
    }
}
