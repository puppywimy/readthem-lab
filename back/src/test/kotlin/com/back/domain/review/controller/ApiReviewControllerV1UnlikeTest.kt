package com.back.domain.review.controller

import com.back.domain.review.service.ReviewService
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ApiReviewControllerV1UnlikeTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var reviewService: ReviewService

    @Throws(Exception::class)
    private fun likeReview(id: Long) {
        mvc.perform(post("/api/v1/reviews/$id/like")).andDo(print())
    }

    @Throws(Exception::class)
    private fun unlikeReview(id: Long): ResultActions {
        return mvc
            .perform(
                delete("/api/v1/reviews/$id/like")
            )
            .andDo(print())
    }

    @Test
    @DisplayName("좋아요 삭제")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t1() {
        val reviewId = 1L

        likeReview(reviewId)
        val resultActions = unlikeReview(reviewId)

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("좋아요 삭제를 성공했습니다."))

        assertEquals(0, reviewService.getReview(reviewId).likeCount)
    }

    @Test
    @DisplayName("좋아요 삭제 - 실패: 좋아요하지 않은 리뷰의 좋아요 삭제 시도")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t2() {
        val reviewId = 1L

        val resultActions = unlikeReview(reviewId)

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-1"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 좋아요입니다."))
    }

    @Test
    @DisplayName("좋아요 삭제 - 실패: id의 리뷰가 존재하지 않음")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t3() {
        val reviewId = 1111111L

        val resultActions = unlikeReview(reviewId)

        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-1"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 리뷰입니다."))
    }

    @Test
    @DisplayName("좋아요 삭제 - 실패: 로그인 하지 않은 사용자의 좋아요 삭제 시도")
    @Throws(Exception::class)
    fun t4() {
        val reviewId = 1L

        val resultActions = unlikeReview(reviewId)

        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."))
    }
}
