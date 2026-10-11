package com.back.domain.book.service

import com.back.domain.book.dto.BookDetailDto
import com.back.domain.book.dto.BookDto
import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookRankRow
import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.book.repository.BookViewCountRedisRepository
import com.back.domain.member.entity.Member
import com.back.domain.review.entity.Review
import com.back.domain.review.repository.ReviewRepository
import com.back.domain.wish.repository.WishRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookQueryService(
    private val bookRepository: BookRepository,
    private val bookOperationalRepository: BookOperationalRepository,
    private val bookViewCountRedisRepository: BookViewCountRedisRepository,
    private val reviewRepository: ReviewRepository,
    private val wishRepository: WishRepository,
) {

    fun getBookById(bookId: Long): Book {
        return bookRepository.findByIdOrNull(bookId)
            ?: throw NoSuchElementException("존재하지 않는 도서입니다.")
    }

    fun getBook(id: Long): Book {
        return getBookById(id)
    }

    fun getBookDetailDto(bookId: Long, actor: Member?): BookDetailDto {
        val book = getBookById(bookId)

        return BookDetailDto(
            book,
            getBookOperational(book)?.reviewCount ?: 0,
            getRatingMap(book),
            getBookTags(book),
            getWishIdOrNull(book, actor)
        )
    }

    fun getBooks(page: Int, size: Int): Page<BookDto> {
        val books = bookRepository
            .findAll(
                PageRequest.of(
                    page,
                    size,
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")
                )
            )

        val ratingsByIsbn = bookOperationalRepository.findByIsbnIn(
            books.content.map { it.isbn }
        ).associate { it.isbn to it.averageRating }

        return books.map { BookDto(it, ratingsByIsbn[it.isbn] ?: 0.0) }
    }

    fun getBooksOrderByTopViewedInLastHour(page: Int, size: Int): List<BookDto> {
        val books = bookViewCountRedisRepository.findBookIdOrderByTopViewedInLastHout(page, size)

        if (books.isNullOrEmpty()) {
            return getBooksOrderByOperationalRank("views", page, size)
        }

        return getBookDtosByBookIds(books)
    }

    fun getBooksOrderByRank(type: String, page: Int, size: Int): List<BookDto> {
        if (type == "views")
            return getBooksOrderByTopViewedInLastHour(page, size)

        return getBooksOrderByOperationalRank(type, page, size)
    }

    private fun getBooksOrderByOperationalRank(type: String, page: Int, size: Int): List<BookDto> {
        val offset = page * size
        val rankedRows = when (type) {
            "rating" -> bookOperationalRepository.findRankedBooksByAverageRating(PageRequest.of(page, size))
            "views" -> bookOperationalRepository.findRankedBooksByViewCount(PageRequest.of(page, size))
            else -> bookOperationalRepository.findRankedBooksByReviewCount(PageRequest.of(page, size))
        }

        val rankedBooks = getBookDtosByRankRows(rankedRows)
        if (rankedBooks.size == size) return rankedBooks

        val rankedBookCount = if (rankedRows.isEmpty() && offset > 0) {
            bookOperationalRepository.countRankedBooks().toInt()
        } else {
            offset + rankedRows.size
        }
        val fallbackOffset = (offset - rankedBookCount).coerceAtLeast(0)
        val fallbackPage = fallbackOffset / size
        val fallbackPageOffset = fallbackOffset % size
        val neededFallbackSize = size - rankedBooks.size

        val currentFallbackPage = bookRepository.findBooksWithoutOperationalOrderByIdDesc(
            PageRequest.of(fallbackPage, size)
        )
        val fallbackBooks = currentFallbackPage
            .drop(fallbackPageOffset)
            .let { currentPageBooks ->
                if (currentPageBooks.size >= neededFallbackSize || fallbackPageOffset == 0) {
                    currentPageBooks.take(neededFallbackSize)
                } else {
                    val nextFallbackPage = bookRepository.findBooksWithoutOperationalOrderByIdDesc(
                        PageRequest.of(fallbackPage + 1, size)
                    )

                    (currentPageBooks + nextFallbackPage).take(neededFallbackSize)
                }
            }

        return rankedBooks + getBookDtos(fallbackBooks)
    }

    fun getTagsByBookId(bookId: Long): List<String> {
        val reviews: List<Review> = reviewRepository.findByBookId(bookId)

        return reviews
            .flatMap { r: Review -> r.tags }
            .distinct()
            .toList()
    }

    fun getBooksByFullTextSearch(
        searchTerm: String,
        page: Int,
        size: Int
    ): List<BookDto> {
        val pageable = PageRequest.of(page, size)

        return getBookDtos(
            bookRepository
                .searchByKeyword(searchTerm, pageable)
                .toList()
        )
    }

    fun getBooksByLikeSearch(
        searchTerm: String,
        page: Int,
        size: Int
    ): List<BookDto> {
        val pageable = PageRequest.of(page, size)

        return getBookDtos(
            bookRepository
                .findByTitleContaining(searchTerm, pageable)
                .toList()
        )
    }

    private fun getBookOperational(book: Book) =
        bookOperationalRepository.findByIsbn(book.isbn)

    private fun getBookDtos(books: List<Book>): List<BookDto> {
        val ratingsByIsbn = bookOperationalRepository.findByIsbnIn(books.map { it.isbn })
            .associate { it.isbn to it.averageRating }

        return books.map { BookDto(it, ratingsByIsbn[it.isbn] ?: 0.0) }
    }

    private fun getBookDtosByBookIds(bookIds: List<Long>): List<BookDto> {
        val books = bookRepository.findAllById(bookIds)
        val booksById = books.associateBy { it.id }
        val ratingsByIsbn = bookOperationalRepository.findByIsbnIn(books.map { it.isbn })
            .associate { it.isbn to it.averageRating }

        return bookIds.mapNotNull { bookId ->
            booksById[bookId]?.let { BookDto(it, ratingsByIsbn[it.isbn] ?: 0.0) }
        }
    }

    private fun getBookDtosByRankRows(rankRows: List<BookRankRow>): List<BookDto> {
        val isbns = rankRows.map { it.isbn }
        val books = bookRepository.findByIsbnIn(isbns)
        val booksByIsbn = books.associateBy { it.isbn }
        val ratingsByIsbn = rankRows.associate { it.isbn to it.averageRating }

        return isbns.mapNotNull { isbn ->
            booksByIsbn[isbn]?.let { BookDto(it, ratingsByIsbn[isbn] ?: 0.0) }
        }
    }

    private fun getBookTags(book: Book): List<String> {
        val reviews: List<Review> = reviewRepository.findByBook(book)

        return reviews
            .flatMap { r: Review -> r.tags }
            .distinct()
            .toList()
    }

    private fun getWishIdOrNull(book: Book, actor: Member?): Long? {
        if (actor == null) return null

        return wishRepository.findByMemberAndBook(actor, book)?.id
    }

    private fun getRatingMap(book: Book): MutableMap<String, Any> {
        val ratingMap: MutableMap<String, Any> = mutableMapOf()

        ratingMap["average"] =
            getBookOperational(book)?.averageRating ?: 0.0

        for (i in 1..10) {
            val rating = i * 0.5f
            ratingMap["%.1f".format(rating)] =
                reviewRepository.countByBookAndRating(book, rating)
        }

        return ratingMap
    }
}
