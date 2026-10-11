package com.back.domain.book.service

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BookViewsServiceTest {
    @Autowired
    private lateinit var bookService: BookService

    @Test
    @DisplayName("도서 인기순(조회수) 다건 조회")
    fun t1() {
        val viewsCount = listOf(3, 5, 1)
        val baseViewCounts = bookService.getBooks(0, 100)
            .associate { bookDto ->
                val book = bookService.getBookById(bookDto.id)
                bookDto.id to bookService.getBookViewCount(book)
            }

        for (i in viewsCount.indices)
            (0..<viewsCount[i]).forEach { _ ->
                val book = bookService.getBookById((i + 1).toLong())
                bookService.incrementViewCount(book)
            }

        val expectedViewCounts = baseViewCounts.toMutableMap()

        for (i in viewsCount.indices) {
            val bookId = (i + 1).toLong()
            expectedViewCounts[bookId] =
                (expectedViewCounts[bookId] ?: 0) + viewsCount[i]
        }

        val bookRank = bookService.getBooksOrderByRank("views", 0, 10)

        var upperCnt = Int.MAX_VALUE

        for (book in bookRank) {
            val b = bookService.getBookById(book.id)
            val nowCnt = bookService.getBookViewCount(b)

            assertThat(nowCnt).isEqualTo(expectedViewCounts[book.id] ?: 0)
            assertThat(upperCnt).isGreaterThanOrEqualTo(nowCnt)

            upperCnt = nowCnt
            
        }
    }
}
