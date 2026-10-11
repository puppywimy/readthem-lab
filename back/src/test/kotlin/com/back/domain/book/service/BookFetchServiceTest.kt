package com.back.domain.book.service

import com.back.domain.book.repository.BookRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class BookFetchServiceTest {
    @Autowired
    private lateinit var bookFetchService: BookFetchService

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Test
    @DisplayName("국립중앙도서관 API에서 실제로 도서 데이터를 가져와 저장한다")
    fun t1() {
        bookFetchService.fetch()

        val books = bookRepository.findAll()
        println("저장된 도서 수: " + books.size)
        books.forEach{ b -> println("${b?.title} / ${b?.isbn} / ${b?.authors}") }

        assertThat(books).isNotEmpty()
    }
}