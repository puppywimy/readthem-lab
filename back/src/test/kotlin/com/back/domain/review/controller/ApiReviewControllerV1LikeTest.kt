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
class ApiReviewControllerV1LikeTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var reviewService: ReviewService

    @Throws(Exception::class)
    private fun likeReview(id: Long): ResultActions {
        return mvc
            .perform(
                post("/api/v1/reviews/$id/like")
            )
            .andDo(print())
    }

    @Test
    @DisplayName("좋아요 생성")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t1() {
        val reviewId = 1L

        val resultActions = likeReview(reviewId)

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.message").value("좋아요 생성을 성공했습니다."))

        assertEquals(1, reviewService.getReview(reviewId).likeCount)
    }

    @Test
    @DisplayName("좋아요 생성 - 실패: 이미 좋아요한 리뷰")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t2() {
        val reviewId = 1L

        likeReview(reviewId)
        val resultActions = likeReview(reviewId)

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.resultCode").value("409-1"))
            .andExpect(jsonPath("$.message").value("이미 존재하는 좋아요입니다."))

        assertEquals(1, reviewService.getReview(reviewId).likeCount)
    }

    @Test
    @DisplayName("좋아요 생성 - 실패: id의 리뷰가 존재하지 않음")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t3() {
        val reviewId = 1111111L

        val resultActions = likeReview(reviewId)

        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-1"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 리뷰입니다."))
    }

    @Test
    @DisplayName("좋아요 생성 - 실패: 로그인 하지 않은 사용자의 좋아요 시도")
    @Throws(Exception::class)
    fun t4() {
        val reviewId = 1L

        val resultActions = likeReview(reviewId)

        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."))
    }
}
