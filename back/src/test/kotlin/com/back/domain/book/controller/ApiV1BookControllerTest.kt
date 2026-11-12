package com.back.domain.book.controller

import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1BookControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var bookRepository: BookRepository
    @Autowired
    private lateinit var bookOperationalRepository: BookOperationalRepository

    @Test
    @DisplayName("도서 단건 조회 - 비인증 사용자")
    @Throws(Exception::class)
    fun t2() {
        val book = bookRepository.findAll()[0]

        val resultActions = mvc
            .perform(
                get("/api/v1/books/${book.id}")
            )
            .andDo(print())

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(book.id))
            .andExpect(jsonPath("$.title").value(book.title))
            .andExpect(jsonPath("$.description").value(book.description))
            .andExpect(jsonPath("$.isbn").value(book.isbn))
            .andExpect(jsonPath("$.publishedDate").exists())
            .andExpect(jsonPath("$.publisher").value(book.publisher))
            .andExpect(jsonPath("$.imgUrl").value(book.imgUrl))
            .andExpect(jsonPath("$.authors").isArray())
            .andExpect(jsonPath("$.reviewCount").exists())
            .andExpect(jsonPath("$.rating").exists())
            .andExpect(jsonPath("$.rating.average").exists())
            .andExpect(jsonPath("$.tags").isArray())
            .andExpect(jsonPath("$.wishId").doesNotExist()) // 비인증 시 wishId 없음
    }

    @Test
    @DisplayName("도서 단건 조회 - 인증 사용자 (isWished 포함)")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t3() {
        val book = bookRepository.findAll()[0]

        val resultActions = mvc
            .perform(
                get("/api/v1/books/${book.id}")
            )
            .andDo(print())

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(book.id))
            .andExpect(jsonPath("$.title").value(book.title))
            .andExpect(jsonPath("$.wishId").exists()) // 인증 시 wishId 있음
    }

    @Test
    @DisplayName("도서 검색 다건 조회")
    @Throws(Exception::class)
    fun t4() {
        // BaseInitData의 도서: 제목 완전 일치가 먼저, 나머지 앞부분 일치는 관련도가 같아 id 내림차순
        val expectedTitles = listOf("책제목", "책제목4", "책제목3", "책제목2")
        val expectedBooks = expectedTitles.map { title -> bookRepository.findAll().first { it.title == title } }

        val resultActions = mvc
            .perform(
                get("/api/v1/books/search")
                    .param("searchTerm", "책 제목")
            )
            .andDo(print())

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedBooks.size))

        for (i in expectedBooks.indices) {
            val expected = expectedBooks[i]
            val expectedAvgRating =
                bookOperationalRepository.findByIsbn(expected.isbn)?.averageRating ?: 0
            resultActions
                .andExpect(jsonPath("$[$i].id").value(expected.id))
                .andExpect(jsonPath("$[$i].title").value(expected.title))
                .andExpect(jsonPath("$[$i].imgUrl").value(expected.imgUrl))
                .andExpect(jsonPath("$[$i].averageRating").value(expectedAvgRating))
        }
    }

    @Test
    @DisplayName("도서 검색 다건 조회 - page, size 지정")
    @Throws(Exception::class)
    fun t5() {
        val expected = bookRepository.findAll().first { it.title == "책제목3" }

        mvc
            .perform(
                get("/api/v1/books/search")
                    .param("searchTerm", "책제목")
                    .param("page", "1")
                    .param("size", "2")
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(expected.id))
            .andExpect(jsonPath("$[1].title").value("책제목2"))
    }
}
