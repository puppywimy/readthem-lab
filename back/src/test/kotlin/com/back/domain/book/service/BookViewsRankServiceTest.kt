package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.book.entity.BookOperational
import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.book.repository.BookViewCountRedisRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BookViewsRankServiceTest {
    @Autowired
    private lateinit var bookService: BookService

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var bookOperationalRepository: BookOperationalRepository

    @MockitoBean
    private lateinit var bookViewCountRedisRepository: BookViewCountRedisRepository

    private fun saveBook(isbn: String, viewCount: Int? = null): Book {
        val book = bookRepository.save(
            Book("인기 도서 $isbn", "설명", isbn, "작가", LocalDateTime.now(), "출판사", "")
        )
        if (viewCount != null) {
            bookOperationalRepository.save(BookOperational(isbn, viewCount = viewCount))
        }
        return book
    }

    private fun givenRecentBookIds(bookIds: List<Long>?) {
        given(bookViewCountRedisRepository.findBookIdOrderByTopViewedInLastHour(anyInt()))
            .willReturn(bookIds)
    }

    private fun rankedIdsByViewCount(): List<Long> {
        val isbns = bookOperationalRepository
            .findRankedBooksByViewCount(PageRequest.of(0, Int.MAX_VALUE))
            .map { it.isbn }
        val idsByIsbn = bookRepository.findByIsbnIn(isbns).associate { it.isbn to it.id }
        return isbns.mapNotNull { idsByIsbn[it] }
    }

    private fun fallbackIds(): List<Long> =
        bookRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
            .filter { bookOperationalRepository.findByIsbn(it.isbn) == null }
            .map { it.id }

    @Test
    @DisplayName("최근 1시간 조회 도서가 size보다 적으면 누적 조회수 순으로 size권을 채운다")
    fun t1() {
        val recent1 = saveBook("isbn-views-recent-1")
        val recent2 = saveBook("isbn-views-recent-2")
        val recent3 = saveBook("isbn-views-recent-3")
        val cumulative1 = saveBook("isbn-views-cumulative-1", viewCount = 2_000_000)
        val cumulative2 = saveBook("isbn-views-cumulative-2", viewCount = 1_000_000)
        givenRecentBookIds(listOf(recent1.id, recent2.id, recent3.id))

        val actualIds = bookService.getBooksOrderByRank("views", 5).map { it.id }

        assertThat(actualIds).containsExactly(recent1.id, recent2.id, recent3.id, cumulative1.id, cumulative2.id)
    }

    @Test
    @DisplayName("누적 조회수로 채운 도서에 최근 1시간 조회 도서가 중복되지 않는다")
    fun t2() {
        val recent = saveBook("isbn-views-recent-dup", viewCount = 3_000_000)
        val cumulative1 = saveBook("isbn-views-cumulative-3", viewCount = 2_000_000)
        val cumulative2 = saveBook("isbn-views-cumulative-4", viewCount = 1_000_000)
        givenRecentBookIds(listOf(recent.id))

        val actualIds = bookService.getBooksOrderByRank("views", 3).map { it.id }

        assertThat(actualIds).containsExactly(recent.id, cumulative1.id, cumulative2.id)
    }

    @Test
    @DisplayName("누적 조회 기록까지 모자라면 운영 정보가 없는 도서를 id 내림차순으로 채운다")
    fun t3() {
        saveBook("isbn-views-fallback-1")
        saveBook("isbn-views-fallback-2")
        val recent = saveBook("isbn-views-recent-fallback")
        givenRecentBookIds(listOf(recent.id))

        val rankedIds = rankedIdsByViewCount()
        val size = 1 + rankedIds.size + 2
        val expectedIds = listOf(recent.id) + rankedIds + fallbackIds().filterNot { it == recent.id }.take(2)

        val actualIds = bookService.getBooksOrderByRank("views", size).map { it.id }

        assertThat(actualIds).isEqualTo(expectedIds)
        assertThat(actualIds).doesNotHaveDuplicates()
    }

    @Test
    @DisplayName("최근 1시간 조회 도서가 size 이상이면 그 도서들만 반환한다")
    fun t4() {
        val recent1 = saveBook("isbn-views-recent-4")
        val recent2 = saveBook("isbn-views-recent-5")
        saveBook("isbn-views-cumulative-5", viewCount = 2_000_000)
        givenRecentBookIds(listOf(recent1.id, recent2.id))

        val actualIds = bookService.getBooksOrderByRank("views", 2).map { it.id }

        assertThat(actualIds).containsExactly(recent1.id, recent2.id)
    }

    @Test
    @DisplayName("최근 1시간 조회가 없으면 누적 조회수 순부터 반환한다")
    fun t5() {
        saveBook("isbn-views-cumulative-6", viewCount = 2_000_000)
        givenRecentBookIds(emptyList())

        val expectedIds = (rankedIdsByViewCount() + fallbackIds()).take(5)
        val actualIds = bookService.getBooksOrderByRank("views", 5).map { it.id }

        assertThat(actualIds).isEqualTo(expectedIds)
    }

    @Test
    @DisplayName("Redis 조회에 실패하면 누적 조회수 순부터 반환한다")
    fun t6() {
        saveBook("isbn-views-cumulative-7", viewCount = 2_000_000)
        givenRecentBookIds(null)

        val expectedIds = (rankedIdsByViewCount() + fallbackIds()).take(5)
        val actualIds = bookService.getBooksOrderByRank("views", 5).map { it.id }

        assertThat(actualIds).isEqualTo(expectedIds)
    }
}
