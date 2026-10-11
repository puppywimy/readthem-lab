package com.back.domain.review.controller

import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.review.service.ReviewService
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ApiReviewControllerV1PostTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var reviewService: ReviewService

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var bookOperationalRepository: BookOperationalRepository

    @Throws(Exception::class)
    private fun postReview(bookId: Long, rating: Float, content: String, tags: List<String>): ResultActions {
        return mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/reviews/book/$bookId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "rating": ${String.format(java.util.Locale.US, "%.1f", rating)},
                                            "content": "$content",
                                            "tags": ${tags.joinToString(
                                                separator = ", ",
                                                prefix = "[",
                                                postfix = "]",
                                                transform = { "\"$it\"" } // Wraps each string in quotes
                                            )}
                                        }

                                        """.trimIndent()
                    )
            )
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("리뷰 작성")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t1() {
        val bookId = 1L

        val rating = 3.5f
        val content = "책 좋네요 ㅎㅎ"
        val tags = listOf("a", "b")

        val resultActions = postReview(bookId, rating, content, tags)

        val review = reviewService.getLatestReview()

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.message").value("리뷰 생성을 성공했습니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.id").value(review!!.id))
            .andExpect(jsonPath("$.data.bookId").value(bookId))
            .andExpect(jsonPath("$.data.rating").value(rating))
            .andExpect(jsonPath("$.data.content").value(content))
            .andExpect(
                jsonPath("$.data.createdDate")
                    .value(Matchers.startsWith(review.createdDate.toString().take(20)))
            )
            .andExpect(jsonPath("$.data.tags").exists())

        for (i in tags.indices) {
            resultActions
                .andExpect(jsonPath("$.data.tags[$i]").value(tags[i]))
        }
    }

    @Test
    @DisplayName("리뷰 작성 시 운영 정보가 없는 도서의 운영 정보가 생성된다")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t8() {
        val book = bookRepository.save(
            Book(
                "리뷰로 운영 정보가 생기는 책",
                "리뷰 생성 시 운영 정보가 함께 만들어져야 한다",
                "isbn-review-creates-operational",
                "작가",
                LocalDateTime.now(),
                "출판사",
                ""
            )
        )

        assertThat(bookOperationalRepository.findByIsbn(book.isbn)).isNull()

        postReview(book.id, 4.5f, "운영 정보 생성 확인", listOf("운영"))
            .andExpect(status().isCreated())

        val bookOperational = bookOperationalRepository.findByIsbn(book.isbn)

        assertThat(bookOperational).isNotNull
        assertThat(bookOperational!!.reviewCount).isEqualTo(1)
        assertThat(bookOperational.averageRating).isEqualTo(4.5)
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 이미 작성한 리뷰 존재")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t2() {
        val bookId = 1L

        val rating = 3.5f
        val content = "책 좋네요 ㅎㅎ"
        val tags = listOf("a", "b")

        val resultActions = postReview(bookId, rating, content, tags)

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.resultCode").value("409-1"))
            .andExpect(jsonPath("$.message").value("이미 존재하는 리뷰입니다."))
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: bookId의 책을 찾을 수 없음")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t3() {
        val bookId = 1111111111L

        val rating = 3.5f
        val content = "책 좋네요 ㅎㅎ"
        val tags = listOf("a", "b")

        val resultActions = postReview(bookId, rating, content, tags)

        resultActions
            .andExpect(handler().handlerType(ApiReviewControllerV1::class.java))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-1"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 도서입니다."))
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 로그인 하지 않은 사용자의 리뷰 작성 시도")
    @Throws(Exception::class)
    fun t4() {
        val bookId = 1L

        val rating = 3.5f
        val content = "책 좋네요 ㅎㅎ"
        val tags = listOf("a", "b")

        val resultActions = postReview(bookId, rating, content, tags)

        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.resultCode").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."))
    }

    @Throws(Exception::class)
    private fun checkRatingFieldError(rating: Float) {
        val bookId = 1L
        val content = "책 좋네요 ㅎㅎ"
        val tags = listOf("a", "b")

        val resultActions = postReview(bookId, rating, content, tags)

        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-1"))
            .andExpect(
                jsonPath("$.message").value(
                    "rating-ValidRating-rating은 0~5, 0.5 단위여야 합니다."
                )
            )
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 평점이 0.5 단위의 숫자가 아님")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t5() {
        checkRatingFieldError(3.2f)
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 평점이 5 초과")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t6() {
        checkRatingFieldError(6f)
    }

    @Test
    @DisplayName("리뷰 작성 - 실패: 평점이 0 미만")
    @WithUserDetails("user2")
    @Throws(Exception::class)
    fun t7() {
        checkRatingFieldError(-1f)
    }
}
