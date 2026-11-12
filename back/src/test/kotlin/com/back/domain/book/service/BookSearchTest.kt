package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

// InnoDB는 FULLTEXT 인덱스를 커밋 시점에 갱신해서, 같은 트랜잭션에서 넣은 행은 MATCH로 검색되지 않는다.
// 그래서 @Transactional 없이 저장을 바로 커밋하고, 롤백이 없으니 넣은 데이터는 @AfterAll에서 직접 지운다.
// 예외가 LIKE 검색으로 넘어가지 않고 드러나도록 BookService가 아니라 BookQueryService를 호출한다.
@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookSearchTest {
    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var bookQueryService: BookQueryService

    private val books = mutableListOf<Book>()

    @BeforeAll
    fun setUp() {
        listOf(
            "열혈 C" to "윤성우",
            "열혈 C 프로그래밍" to "윤성우",
            "열혈 C++ 프로그래밍" to "윤성우",
            "열혈C 입문" to "홍길동",
            "다시 읽는 열혈 C" to "홍길동",
            "열혈강호" to "전극진",
            "C++ Primer" to "Stanley Lippman",
            "C# 교과서" to "박용준",
            "Concurrency in C#" to "Stephen Cleary",
            "M&A 실무" to "김실무",
            "Clean Code" to "Robert C. Martin",
            "Node.js 교과서" to "조현영",
        ).forEachIndexed { i, (title, authors) ->
            books += bookRepository.save(
                Book(title, "설명", "isbn-search-$i", authors, LocalDateTime.now(), "오렌지미디어", "")
            )
        }
    }

    @AfterAll
    fun tearDown() {
        bookRepository.deleteAll(books)
    }

    private fun search(searchTerm: String, page: Int = 0, size: Int = 100): List<String> =
        bookQueryService.getBooksByFullTextSearch(searchTerm, page, size).map { it.title }

    @Test
    @DisplayName("띄어쓰기를 무시한다")
    fun t1() {
        assertThat(search("열혈 C")).isEqualTo(search("열혈C"))
        assertThat(search("열 혈 C")).isEqualTo(search("열혈C"))
    }

    @Test
    @DisplayName("한 글자 단어가 포함된 검색어도 검색어 전체로 찾는다")
    fun t2() {
        assertThat(search("열혈 C")).containsExactlyInAnyOrder(
            "열혈 C", "열혈 C 프로그래밍", "열혈 C++ 프로그래밍", "열혈C 입문", "다시 읽는 열혈 C"
        )
        assertThat(search("열혈 C 프로그래밍")).containsExactly("열혈 C 프로그래밍")
    }

    @Test
    @DisplayName("기호 7개를 구분한다")
    fun t3() {
        assertThat(search("C++")).containsExactlyInAnyOrder("열혈 C++ 프로그래밍", "C++ Primer")
        assertThat(search("C#")).containsExactlyInAnyOrder("C# 교과서", "Concurrency in C#")
        assertThat(search("열혈 C++ 프로그래밍")).containsExactly("열혈 C++ 프로그래밍")
        assertThat(search("M&A")).containsExactly("M&A 실무")
        assertThat(search("Node.js")).containsExactly("Node.js 교과서")
        assertThat(search("nodejs")).isEmpty()
    }

    @Test
    @DisplayName("대소문자를 무시한다")
    fun t4() {
        assertThat(search("node.JS")).containsExactly("Node.js 교과서")
        assertThat(search("CLEAN code")).containsExactly("Clean Code")
    }

    @Test
    @DisplayName("목록에 없는 기호는 무시한다")
    fun t5() {
        assertThat(search("Clean, Code!")).containsExactly("Clean Code")
        assertThat(search("(열혈강호)")).containsExactly("열혈강호")
    }

    @Test
    @DisplayName("저자로 검색된다")
    fun t6() {
        assertThat(search("윤성우")).containsExactlyInAnyOrder(
            "열혈 C", "열혈 C 프로그래밍", "열혈 C++ 프로그래밍"
        )
        assertThat(search("Robert C. Martin")).containsExactly("Clean Code")
    }

    @Test
    @DisplayName("출판사로는 검색되지 않는다")
    fun t7() {
        assertThat(search("오렌지미디어")).isEmpty()
    }

    @Test
    @DisplayName("제목 끝과 저자 앞에 걸친 검색어는 일치하지 않는다")
    fun t8() {
        assertThat(search("교과서 박용준")).isEmpty()
        assertThat(search("교과서박용")).isEmpty()
    }

    @Test
    @DisplayName("연산자 문자가 든 검색어를 예외 없이 처리한다")
    fun t9() {
        assertThat(search("\"")).isEmpty()
        assertThat(search("(")).isEmpty()
        assertThat(search("-자바")).isEmpty()
        assertThat(search("\"열혈강호")).containsExactly("열혈강호")
        assertThat(search("+열혈강호 -C*")).isEmpty()
    }

    @Test
    @DisplayName("기호만 있는 검색어는 빈 결과를 돌려준다")
    fun t10() {
        assertThat(search("!?~")).isEmpty()
        assertThat(search("   ")).isEmpty()
    }

    @Test
    @DisplayName("제목 완전 일치, 제목 앞부분 일치, 나머지 순으로 정렬한다")
    fun t11() {
        val found = search("열혈C")

        assertThat(found).hasSize(5)
        assertThat(found[0]).isEqualTo("열혈 C")
        assertThat(found.subList(1, 4)).containsExactlyInAnyOrder(
            "열혈 C 프로그래밍", "열혈 C++ 프로그래밍", "열혈C 입문"
        )
        assertThat(found[4]).isEqualTo("다시 읽는 열혈 C")
    }

    @Test
    @DisplayName("page, size로 나눠 조회한다")
    fun t12() {
        val all = search("열혈C")

        assertThat(search("열혈C", page = 0, size = 2)).isEqualTo(all.subList(0, 2))
        assertThat(search("열혈C", page = 1, size = 2)).isEqualTo(all.subList(2, 4))
        assertThat(search("열혈C", page = 2, size = 2)).isEqualTo(all.subList(4, 5))
        assertThat(search("열혈C", page = 3, size = 2)).isEmpty()
    }
}
