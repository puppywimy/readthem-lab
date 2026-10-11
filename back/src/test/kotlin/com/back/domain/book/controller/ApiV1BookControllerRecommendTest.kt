package com.back.domain.book.controller

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
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1BookControllerRecommendTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("도서 추천 다건 조회 - user2")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t1() {
        val resultActions = mvc
            .perform(
                get("/api/v1/books/recommend")
            )
            .andDo(print())

        resultActions
            .andExpect(jsonPath("$[0].id").value(1))
    }

    @Test
    @DisplayName("도서 추천 다건 조회 - user3")
    @WithUserDetails("user3")
    @Throws(Exception::class)
    fun t2() {
        val resultActions = mvc
            .perform(
                get("/api/v1/books/recommend")
            )
            .andDo(print())

        resultActions
            .andExpect(jsonPath("$[0].id").value(4))
            .andExpect(jsonPath("$[1].id").value(1))
    }
}