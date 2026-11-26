package com.back.domain.book.service

import com.back.domain.book.dto.BookDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.dao.DataAccessResourceFailureException
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BookSearchFallbackTest {
    private val bookQueryService: BookQueryService = mock(BookQueryService::class.java)

    private val fullTextResult = listOf(BookDto(1L, "전체 검색 결과", "", 0.0))
    private val prefixResult = listOf(BookDto(2L, "1~3단계 결과", "", 0.0))

    private fun bookService(maxConcurrent: Int = 2, acquireTimeout: Duration = Duration.ofMillis(100)) =
        BookService(
            bookQueryService,
            mock(BookCommandService::class.java),
            mock(BookViewCountService::class.java),
            maxConcurrent,
            acquireTimeout,
        )

    @Test
    @DisplayName("FULLTEXT 검색이 성공하면 그 결과로 응답한다")
    fun t1() {
        `when`(bookQueryService.getBooksByFullTextSearch("검색어", 0, 10)).thenReturn(fullTextResult)

        assertThat(bookService().getBooksBySearch("검색어", 0, 10)).isEqualTo(fullTextResult)
        verify(bookQueryService, never()).getBooksByPrefixSearch("검색어", 0, 10)
    }

    @Test
    @DisplayName("FULLTEXT 검색이 실패하면 1~3단계 결과로 응답하고, 자리를 돌려준다")
    fun t2() {
        `when`(bookQueryService.getBooksByFullTextSearch("검색어", 0, 10))
            .thenThrow(DataAccessResourceFailureException("FTS query exceeds result cache limit"))
        `when`(bookQueryService.getBooksByPrefixSearch("검색어", 0, 10)).thenReturn(prefixResult)
        `when`(bookQueryService.getBooksByFullTextSearch("다른검색어", 0, 10)).thenReturn(fullTextResult)
        val service = bookService(maxConcurrent = 1)

        assertThat(service.getBooksBySearch("검색어", 0, 10)).isEqualTo(prefixResult)
        // 실패한 검색이 자리를 돌려주지 않았다면 다음 검색도 폴백한다.
        assertThat(service.getBooksBySearch("다른검색어", 0, 10)).isEqualTo(fullTextResult)
    }

    @Test
    @DisplayName("FULLTEXT 검색 자리가 대기 시간 안에 나지 않으면 1~3단계 결과로 응답한다")
    fun t3() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        `when`(bookQueryService.getBooksByFullTextSearch("느린검색어", 0, 10)).thenAnswer {
            started.countDown()
            release.await(5, TimeUnit.SECONDS)
            fullTextResult
        }
        `when`(bookQueryService.getBooksByPrefixSearch("검색어", 0, 10)).thenReturn(prefixResult)
        val service = bookService(maxConcurrent = 1, acquireTimeout = Duration.ofMillis(50))

        val executor = Executors.newSingleThreadExecutor()
        try {
            val slow = executor.submit<List<BookDto>> { service.getBooksBySearch("느린검색어", 0, 10) }
            assertThat(started.await(5, TimeUnit.SECONDS)).isTrue()

            assertThat(service.getBooksBySearch("검색어", 0, 10)).isEqualTo(prefixResult)
            verify(bookQueryService, never()).getBooksByFullTextSearch("검색어", 0, 10)

            release.countDown()
            assertThat(slow.get(5, TimeUnit.SECONDS)).isEqualTo(fullTextResult)
        } finally {
            release.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    @DisplayName("기호만 있는 검색어는 쿼리 없이 빈 결과를 돌려준다")
    fun t4() {
        assertThat(bookService().getBooksBySearch("!?~", 0, 10)).isEmpty()
        verify(bookQueryService, never()).getBooksByFullTextSearch("!?~", 0, 10)
        verify(bookQueryService, never()).getBooksByPrefixSearch("!?~", 0, 10)
    }
}
