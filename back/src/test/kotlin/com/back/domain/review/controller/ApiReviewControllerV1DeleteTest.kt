package com.back.domain.review.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ApiReviewControllerV1DeleteTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Throws(Exception::class)
    private fun deleteReview(id: Long): ResultActions {
        return mvc
            .perform(
                delete("/api/v1/reviews/$id")
            )
            .andDo(print())
    }

    @Test
    @DisplayName("리뷰 삭제")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t1() {
        val reviewId = 1L

        val resultActions = deleteReview(reviewId)

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("리뷰 삭제를 성공했습니다."))
    }

    @Test
    @DisplayName("리뷰 삭제 - 실패: 작성하지 않은 리뷰 삭제 시도")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t2() {
        val reviewId = 1L

        val resultActions = deleteReview(reviewId)

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.message").value("리뷰 삭제 권한이 없습니다."))
    }

    @Test
    @DisplayName("리뷰 삭제 - 관리자는 다른 사람의 리뷰도 삭제 가능")
    @WithUserDetails("admin")
    @Throws(Exception::class)
    fun t2_1() {
        val reviewId = 1L

        val resultActions = deleteReview(reviewId)

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("리뷰 삭제를 성공했습니다."))
    }

    @Test
    @DisplayName("리뷰 삭제 - 실패: id의 리뷰가 존재하지 않음")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t3() {
        val reviewId = 1111111L

        val resultActions = deleteReview(reviewId)

        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-1"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 리뷰입니다."))
    }

    @Test
    @DisplayName("리뷰 삭제 - 실패: 로그인 하지 않은 사용자의 리뷰 삭제 시도")
    @Throws(Exception::class)
    fun t4() {
        val reviewId = 1L

        val resultActions = deleteReview(reviewId)

        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."))
    }
}