package com.back.domain.book.repository

import com.back.domain.book.entity.Book
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

// 새 검색 인덱스(ft_book_search_text)도 불용어가 꺼진 채 만들어졌는지 확인한다.
// InnoDB는 FULLTEXT 인덱스를 커밋 시점에 갱신해서, 같은 트랜잭션에서 넣은 행은 MATCH로 검색되지 않는다.
// 그래서 @Transactional 없이 저장을 바로 커밋하고, 롤백이 없으니 넣은 데이터는 finally에서 직접 지운다.
@ActiveProfiles("test")
@SpringBootTest
class BookFullTextSearchTest {
    @Autowired
    private lateinit var bookRepository: BookRepository

    @Test
    @DisplayName("영문 검색어로 도서를 찾는다 (FULLTEXT 인덱스의 불용어가 꺼져 있어야 통과)")
    fun t1() {
        // 불용어가 켜진 채 인덱스가 만들어지면 'a'가 든 ngram 토큰(ja, av, va)이 전부 빠져 검색되지 않는다.
        val book = bookRepository.save(
            Book(
                "Effective Java", "설명", "isbn-fulltext-english", "Joshua Bloch",
                LocalDateTime.now(), "Addison-Wesley", ""
            )
        )

        try {
            val found = bookRepository.searchByKeyword("Java", PageRequest.of(0, 10)).toList()

            assertThat(found.map { it.id }).containsExactly(book.id)
        } finally {
            bookRepository.delete(book)
        }
    }
}
