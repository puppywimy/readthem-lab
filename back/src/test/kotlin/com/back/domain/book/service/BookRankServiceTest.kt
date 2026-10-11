package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.review.service.ReviewService
import com.back.global.exception.ServiceException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BookRankServiceTest(@Autowired private val bookRepository: BookRepository) {
    @Autowired
    private lateinit var reviewService: ReviewService

    @Autowired
    private lateinit var bookService: BookService

    @Autowired
    private lateinit var bookOperationalRepository: BookOperationalRepository

    @Test
    @DisplayName("도서 순위(리뷰수) 다건 조회")
    fun t1() {
        val bookRank = bookService.getBooksOrderByRank("reviewCnt", 0, 100)

        if (bookRank.isEmpty()) return

        var upperCnt = reviewService.getReviewsByBookId(bookRank[0].id, null).size

        for (i in 1..<bookRank.size) {
            val nowCnt = reviewService.getReviewsByBookId(bookRank[i].id, null).size

            assertThat(upperCnt).isGreaterThanOrEqualTo(nowCnt)

            upperCnt = nowCnt
        }
    }

    fun getAverageRating(id: Long)
        = bookRepository.findByIdOrNull(id)?.let {
            bookOperationalRepository
                .findByIsbn(it.isbn)?.averageRating
                ?: throw ServiceException("404-1", "오류")
    }

    @Test
    @DisplayName("도서 순위(평점) 다건 조회")
    fun t2() {
        val bookRank = bookService.getBooksOrderByRank("rating", 0, 100)

        if (bookRank.isEmpty()) return

        var upperRating = getAverageRating(bookRank[0].id)

        for (i in 1..<bookRank.size) {
            val nowRating = getAverageRating(bookRank[i].id)

            AssertionsForClassTypes.assertThat(upperRating).isGreaterThanOrEqualTo(nowRating)

            upperRating = nowRating
        }
    }

    @Test
    @DisplayName("도서 순위는 운영 정보가 없는 도서도 0값으로 포함한다")
    fun t3() {
        val bookWithoutOperational = bookRepository.save(
            Book(
                "운영 정보 없는 책",
                "운영 정보가 없어도 랭킹에 포함되어야 한다",
                "isbn-rank-without-operational",
                "작가",
                LocalDateTime.now(),
                "출판사",
                ""
            )
        )

        val bookRank = bookService.getBooksOrderByRank("reviewCnt", 0, 100)

        assertThat(bookOperationalRepository.findByIsbn(bookWithoutOperational.isbn)).isNull()
        assertThat(bookRank.map { it.id }).contains(bookWithoutOperational.id)
    }

    @Test
    @DisplayName("도서 순위는 운영 정보가 없는 도서를 뒤에서 id 내림차순으로 채운다")
    fun t4() {
        val first = bookRepository.save(
            Book(
                "운영 정보 없는 책 1",
                "설명",
                "isbn-rank-without-operational-1",
                "작가",
                LocalDateTime.now(),
                "출판사",
                ""
            )
        )
        val second = bookRepository.save(
            Book(
                "운영 정보 없는 책 2",
                "설명",
                "isbn-rank-without-operational-2",
                "작가",
                LocalDateTime.now(),
                "출판사",
                ""
            )
        )

        val expectedFallbackIds = bookRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
            .filter { bookOperationalRepository.findByIsbn(it.isbn) == null }
            .map { it.id }

        val actualIds = bookService.getBooksOrderByRank("reviewCnt", 0, 200)
            .map { it.id }

        assertThat(actualIds.takeLast(expectedFallbackIds.size)).isEqualTo(expectedFallbackIds)
        assertThat(actualIds.indexOf(second.id)).isLessThan(actualIds.indexOf(first.id))
    }

    @Test
    @DisplayName("도서 순위는 운영 정보 도서가 끝난 다음 페이지부터 운영 정보 없는 도서만 id 내림차순으로 조회한다")
    fun t5() {
        val first = bookRepository.save(
            Book(
                "운영 정보 없는 책 3",
                "설명",
                "isbn-rank-without-operational-3",
                "작가",
                LocalDateTime.now(),
                "출판사",
                ""
            )
        )
        val second = bookRepository.save(
            Book(
                "운영 정보 없는 책 4",
                "설명",
                "isbn-rank-without-operational-4",
                "작가",
                LocalDateTime.now(),
                "출판사",
                ""
            )
        )

        val rankedCount = bookOperationalRepository.count().toInt()
        val pageSize = 2
        val page = rankedCount / pageSize

        val expectedFallbackIds = bookRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
            .filter { bookOperationalRepository.findByIsbn(it.isbn) == null }
            .take(pageSize)
            .map { it.id }

        val actualIds = bookService.getBooksOrderByRank("reviewCnt", page, pageSize)
            .map { it.id }

        assertThat(actualIds).containsOnly(first.id, second.id)
        assertThat(actualIds).isEqualTo(expectedFallbackIds)
    }

    @Test
    @DisplayName("도서 순위 fallback은 중간 오프셋에서도 누락 없이 다음 페이지를 이어서 채운다")
    fun t6() {
        bookRepository.save(
            Book(
                "운영 정보 없는 책 5",
                "설명",
                "isbn-rank-without-operational-5",
                "작가",
                LocalDateTime.now(),
                "출판사",
                ""
            )
        )
        bookRepository.save(
            Book(
                "운영 정보 없는 책 6",
                "설명",
                "isbn-rank-without-operational-6",
                "작가",
                LocalDateTime.now(),
                "출판사",
                ""
            )
        )
        bookRepository.save(
            Book(
                "운영 정보 없는 책 7",
                "설명",
                "isbn-rank-without-operational-7",
                "작가",
                LocalDateTime.now(),
                "출판사",
                ""
            )
        )

        val rankedCount = bookOperationalRepository.count().toInt()
        val pageSize = rankedCount + 1
        val page = 1

        val expectedFallbackIds = bookRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
            .filter { bookOperationalRepository.findByIsbn(it.isbn) == null }
            .drop(1)
            .take(pageSize)
            .map { it.id }

        val actualIds = bookService.getBooksOrderByRank("reviewCnt", page, pageSize)
            .map { it.id }

        assertThat(actualIds).isEqualTo(expectedFallbackIds)
    }
}
