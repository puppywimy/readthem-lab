package com.back.domain.book.controller

import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.review.repository.ReviewRepository
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1BookControllerAdminTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var bookOperationalRepository: BookOperationalRepository

    @Autowired
    private lateinit var reviewRepository: ReviewRepository

    @Test
    @DisplayName("도서 수정 - 관리자")
    @WithUserDetails("admin")
    @Throws(Exception::class)
    fun t1() {
        val book = bookRepository.findAll()[0]

        val resultActions = mvc
            .perform(
                put("/api/v1/books/${book.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "수정된 제목",
                                            "description": "수정된 설명",
                                            "authors": "수정된 작가",
                                            "publisher": "수정된 출판사",
                                            "imgUrl": "https://example.com/img.png"
                                        }
                                        
                                        """.trimIndent()
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiBookControllerV1::class.java))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("도서 수정을 성공했습니다."))
            .andExpect(jsonPath("$.data.title").value("수정된 제목"))

        val modified = bookRepository.findById(book.id).orElseThrow()
        assertThat(modified.title).isEqualTo("수정된 제목")
        assertThat(modified.description).isEqualTo("수정된 설명")
        assertThat(modified.authors).isEqualTo("수정된 작가")
        assertThat(modified.publisher).isEqualTo("수정된 출판사")
        assertThat(modified.imgUrl).isEqualTo("https://example.com/img.png")
    }

    @Test
    @DisplayName("도서 수정 - 실패: 관리자가 아님")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t2() {
        val book = bookRepository.findAll()[0]

        val resultActions = mvc
            .perform(
                put("/api/v1/books/${book.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "수정된 제목"
                                        }
                                        
                                        """.trimIndent()
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.message").value("권한이 없습니다."))
    }

    @Test
    @DisplayName("도서 삭제 - 관리자 (리뷰/찜 함께 삭제)")
    @WithUserDetails("admin")
    @Throws(Exception::class)
    fun t3() {
        val book = bookRepository.findAll()[0]
        val bookId = book.id
        val isbn = book.isbn

        Assertions.assertThat(reviewRepository.findByBook(book)).isNotEmpty()
        assertThat(bookOperationalRepository.findByIsbn(isbn)).isNotNull()

        val resultActions = mvc
            .perform(
                delete("/api/v1/books/${book.id}")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiBookControllerV1::class.java))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("도서 삭제를 성공했습니다."))

        assertThat(bookRepository.findById(bookId)).isEmpty()
        assertThat(bookOperationalRepository.findByIsbn(isbn)).isNull()
    }

    @Test
    @DisplayName("도서 삭제 - 실패: 관리자가 아님")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t4() {
        val book = bookRepository.findAll()[0]

        val resultActions = mvc
            .perform(
                delete("/api/v1/books/${book.id}")
            )
            .andDo(print())

        resultActions
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.message").value("권한이 없습니다."))
    }
}
