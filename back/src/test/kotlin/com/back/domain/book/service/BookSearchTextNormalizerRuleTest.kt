package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

// book.search_text 생성 컬럼(SQL)과 BookSearchTextNormalizer(Kotlin)의 전처리 규칙이 같은지 확인한다.
// 두 규칙이 어긋나면 검색 결과가 조용히 빠진다.
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BookSearchTextNormalizerRuleTest {
    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val inputs = listOf(
        // 공백
        "열혈 C 프로그래밍",
        "  앞뒤\t공백\n줄바꿈  ",
        "전각　공백",
        // 기호 7개
        "열혈 C++ 프로그래밍",
        "C# 교과서",
        "M&A 실무",
        "Node.js 교과서",
        "Vue: 입문",
        "Spring-Boot",
        "TCP/IP",
        "+#&.:-/",
        // 그 밖의 기호
        "Clean Code!",
        "(개정판) [한빛] {중괄호} <꺾쇠>",
        "\"따옴표\" '작은따옴표' `백틱`",
        "a_b*c~d@e\$f%g^h=i|j\\k,l;m?n",
        "가·나…다「라」『마』《바》",
        "이모지 📚 책",
        // 한글·영문·숫자 혼합
        "자바의 정석 3판",
        "Effective Java 3rd Edition",
        "파이썬2025",
        "ABCabc가나다123",
        "Ünïcödé Café",
        "日本語の本",
        "",
    )

    @Test
    @DisplayName("생성 컬럼 값이 Kotlin 전처리 결과와 같다")
    fun t1() {
        val books = inputs.mapIndexed { i, title ->
            Book(
                title, "설명", "isbn-normalizer-rule-$i", inputs[inputs.size - 1 - i],
                LocalDateTime.now(), "출판사", ""
            )
        }
        bookRepository.saveAllAndFlush(books)

        for (book in books) {
            val searchText = jdbcTemplate.queryForObject(
                "SELECT search_text FROM book WHERE id = ?", String::class.java, book.id
            )
            val expected = BookSearchTextNormalizer.normalize(book.title) + " " +
                    BookSearchTextNormalizer.normalize(book.authors!!)

            assertThat(searchText)
                .`as`("title=%s, authors=%s", book.title, book.authors)
                .isEqualTo(expected)
        }
    }

    @Test
    @DisplayName("저자가 없으면 '전처리한 제목 + 공백'이다")
    fun t2() {
        val book = bookRepository.saveAndFlush(
            Book("Node.js 교과서", "설명", "isbn-normalizer-rule-null", null, LocalDateTime.now(), "출판사", "")
        )

        val searchText = jdbcTemplate.queryForObject(
            "SELECT search_text FROM book WHERE id = ?", String::class.java, book.id
        )

        assertThat(searchText).isEqualTo(BookSearchTextNormalizer.normalize("Node.js 교과서") + " ")
    }

    @Test
    @DisplayName("기호 7개는 대체 문자로 바꾸고 나머지 기호와 공백은 지운다")
    fun t3() {
        assertThat(BookSearchTextNormalizer.normalize("열혈 C++ 프로그래밍")).isEqualTo("열혈Cǂǂ프로그래밍")
        assertThat(BookSearchTextNormalizer.normalize("Node.js 교과서")).isEqualTo("Nodeǃjs교과서")
        assertThat(BookSearchTextNormalizer.normalize("+#&.:-/")).isEqualTo("ǂǁǀǃƻʔƾ")
        assertThat(BookSearchTextNormalizer.normalize("Clean Code!")).isEqualTo("CleanCode")
        assertThat(BookSearchTextNormalizer.normalize("\"(*)\"")).isEmpty()
    }
}
