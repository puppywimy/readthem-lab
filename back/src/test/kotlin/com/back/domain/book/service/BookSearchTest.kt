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
// 예외가 1~3단계 폴백으로 넘어가지 않고 드러나도록 BookService가 아니라 BookQueryService를 호출한다.
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
            "하얼빈" to "빈센트",
            // 단계 정렬 확인용 (t13~t15). 주석은 '단계검색'으로 검색했을 때 걸리는 단계다.
            "단계검색" to "가저자", // 1 제목 완전 일치
            "단계검색 입문" to "나저자", // 2 제목 앞부분 일치
            "단계검색" to "단계검색연구회", // 1 (저자 앞부분 일치이기도 함)
            "알기 쉬운 책" to "단계검색 연구회", // 3 저자 앞부분 일치
            "다시 보는 단계검색" to "다저자", // 4 나머지
            "단계검색 실전" to "단계검색팀", // 2 (저자 앞부분 일치이기도 함)
            "쉬운 책" to "단계검색팀", // 3
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

    private fun searchWithAuthors(searchTerm: String, page: Int = 0, size: Int = 100): List<String> =
        bookQueryService.getBooksByFullTextSearch(searchTerm, page, size)
            .map { dto -> books.first { it.id == dto.id } }
            .map { "${it.title}/${it.authors}" }

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
        // 제목 끝 글자와 저자 첫 글자가 같은 경우: 토큰 "얼빈"(제목 끝), "빈센"(저자 앞)이 둘 다 있다.
        assertThat(search("얼빈센")).isEmpty()
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
    @DisplayName("제목 완전 일치, 제목 앞부분 일치, 나머지 순으로 정렬한다 (띄어쓰기 무시)")
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

    @Test
    @DisplayName("제목 완전 일치, 제목 앞부분 일치, 저자 앞부분 일치, 나머지 순으로, 단계 안에서는 최신순으로 정렬한다")
    fun t13() {
        assertThat(searchWithAuthors("단계검색")).containsExactly(
            "단계검색/단계검색연구회", "단계검색/가저자",
            "단계검색 실전/단계검색팀", "단계검색 입문/나저자",
            "쉬운 책/단계검색팀", "알기 쉬운 책/단계검색 연구회",
            "다시 보는 단계검색/다저자",
        )
    }

    @Test
    @DisplayName("여러 단계에 걸리는 도서는 가장 앞 단계에서 한 번만 나온다")
    fun t14() {
        val found = searchWithAuthors("단계검색")

        assertThat(found).doesNotHaveDuplicates()
        assertThat(found.indexOf("단계검색/단계검색연구회")).isLessThan(found.indexOf("단계검색 입문/나저자"))
        assertThat(found.indexOf("단계검색 실전/단계검색팀")).isLessThan(found.indexOf("쉬운 책/단계검색팀"))
    }

    @Test
    @DisplayName("단계 경계를 넘는 페이지에서 누락이나 중복이 없다")
    fun t15() {
        val all = searchWithAuthors("단계검색")

        for (size in 1..4) {
            val paged = (0..all.size / size).flatMap { searchWithAuthors("단계검색", page = it, size = size) }
            assertThat(paged).`as`("size=$size").isEqualTo(all)
        }
    }

    @Test
    @DisplayName("1~3단계 검색은 전체 검색에서 4단계(나머지)만 뺀 결과를 같은 순서로 돌려준다")
    fun t16() {
        val prefix = bookQueryService.getBooksByPrefixSearch("단계검색", 0, 100)
            .map { dto -> books.first { it.id == dto.id } }
            .map { "${it.title}/${it.authors}" }

        assertThat(prefix).isEqualTo(searchWithAuthors("단계검색").filterNot { it == "다시 보는 단계검색/다저자" })
    }

    @Test
    @DisplayName("1~3단계 검색도 page, size로 나눠 조회하고, 기호만 있는 검색어는 빈 결과를 돌려준다")
    fun t17() {
        fun prefixIds(page: Int, size: Int) =
            bookQueryService.getBooksByPrefixSearch("단계검색", page, size).map { it.id }

        val all = prefixIds(0, 100)
        for (size in 1..4) {
            val paged = (0..all.size / size).flatMap { prefixIds(it, size) }
            assertThat(paged).`as`("size=$size").isEqualTo(all)
        }
        assertThat(bookQueryService.getBooksByPrefixSearch("!?~", 0, 10)).isEmpty()
    }
}
