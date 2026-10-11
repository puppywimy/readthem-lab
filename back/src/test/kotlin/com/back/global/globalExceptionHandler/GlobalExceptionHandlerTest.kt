package com.back.global.globalExceptionHandler

import com.back.domain.book.repository.BookRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Test
    @DisplayName("ConstraintViolationException - searchTerm 공백 시 400-1 응답")
    @Throws(Exception::class)
    fun t1() {
        val resultActions = mvc
            .perform(
                get("/api/v1/books/search")
                    .param("searchTerm", "")
            )
            .andDo(print())

        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-1"))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("MethodArgumentNotValidException - 리뷰 작성 시 필드 누락 시 400-1 응답")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t2() {
        val bookId = bookRepository.findAll()[0].id

        val resultActions = mvc
            .perform(
                post("/api/v1/reviews/book/$bookId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "content": "리뷰 내용",
                            "tags": []
                        }
                        """.trimIndent()
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-1"))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("HttpMessageNotReadableException - 잘못된 JSON 요청 시 400-2 응답")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t3() {
        val bookId = bookRepository.findAll().first().id

        val resultActions = mvc
            .perform(
                post("/api/v1/reviews/book/$bookId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json")
            )
            .andDo(print())

        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-2"))
            .andExpect(jsonPath("$.message").value("요청 본문이 올바르지 않습니다."))
    }

    @Test
    @DisplayName("ServiceException - 존재하지 않는 도서 조회 시 404-1 응답")
    @Throws(Exception::class)
    fun t4() {
        val resultActions = mvc
            .perform(get("/api/v1/books/99999"))
            .andDo(print())

        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-1"))
            .andExpect(jsonPath("$.message").exists())
    }
}
