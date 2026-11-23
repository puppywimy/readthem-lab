package com.back.domain.book.service

import com.back.domain.book.dto.BookDetailDto
import com.back.domain.book.dto.BookDto
import com.back.domain.book.entity.Book
import com.back.domain.member.entity.Member
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

data class BookDetailResult(
    val bookDetail: BookDetailDto,
    val viewCountIncreased: Boolean
)

@Service
class BookService(
    private val bookQueryService: BookQueryService,
    private val bookCommandService: BookCommandService,
    private val bookViewCountService: BookViewCountService,
    @Value("\${custom.book-search.full-text.max-concurrent}") fullTextMaxConcurrent: Int,
    @Value("\${custom.book-search.full-text.acquire-timeout}") private val fullTextAcquireTimeout: Duration,
) {

    // FULLTEXT 검색은 검색어에 따라 쿼리 하나가 수백 MB~1GB 넘게 메모리를 쓴다(#66).
    // 동시에 도는 FULLTEXT 검색 수를 제한해 검색 전체가 쓰는 메모리를 묶는다.
    private val fullTextPermits = Semaphore(fullTextMaxConcurrent)

    companion object {
        private val log = LoggerFactory.getLogger(Companion::class.java)
    }

    fun getBookById(bookId: Long): Book {
        return bookQueryService.getBookById(bookId)
    }

    fun getBookViewCount(book: Book): Int {
        return bookViewCountService.getBookViewCount(book)
    }

    fun getBooksOrderByRank(type: String, size: Int): List<BookDto> {
        return bookQueryService.getBooksOrderByRank(type, size)
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

    /**
     * 1~4단계 전체 검색을 하되, FULLTEXT 검색을 실행할 자리가 [fullTextAcquireTimeout] 안에 나지 않거나
     * FULLTEXT 검색이 실패하면(메모리 상한 초과 등) 1~3단계 결과로 응답한다.
     * 폴백하면 4단계(제목·저자 중간에 검색어가 든 도서)가 빠진다.
     *
     * 로그에는 검색어 원문을 남기지 않고, 전처리한 검색어의 길이와 ngram 토큰 수만 남긴다.
     */
    fun getBooksBySearch(
        searchTerm: String,
        page: Int,
        size: Int
    ): List<BookDto> {
        val keyword = BookSearchTextNormalizer.normalize(searchTerm)
        if (keyword.isEmpty()) return emptyList()

        if (!tryAcquireFullTextPermit()) {
            log.warn(
                "FULLTEXT search skipped: no permit within {}ms. Falling back to prefix search. {}",
                fullTextAcquireTimeout.toMillis(), describeKeyword(keyword)
            )
            return bookQueryService.getBooksByPrefixSearch(searchTerm, page, size)
        }

        try {
            return bookQueryService.getBooksByFullTextSearch(searchTerm, page, size)
        } catch (e: Exception) {
            log.error("FULLTEXT search failed. Falling back to prefix search. {}", describeKeyword(keyword), e)
        } finally {
            fullTextPermits.release()
        }

        return bookQueryService.getBooksByPrefixSearch(searchTerm, page, size)
    }

    private fun tryAcquireFullTextPermit(): Boolean =
        try {
            fullTextPermits.tryAcquire(fullTextAcquireTimeout.toMillis(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        }

    // ngram_token_size=2라 전처리한 검색어(공백 없음)의 토큰 수는 길이 - 1이다.
    private fun describeKeyword(keyword: String): String =
        "keywordLength=${keyword.length}, ngramTokens=${(keyword.length - 1).coerceAtLeast(0)}"
}
