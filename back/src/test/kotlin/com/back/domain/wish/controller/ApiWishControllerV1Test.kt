package com.back.domain.wish.controller

import com.back.domain.book.service.BookService
import com.back.domain.member.service.MemberService
import com.back.domain.wish.service.WishService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiWishControllerV1Test {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var wishService: WishService

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var bookService: BookService

    @Test
    @DisplayName("내 찜 다건 조회")
    @WithUserDetails("user1")
    fun t1() {
        val actor = memberService.getMemberByUsername("user1")
        val wishes = wishService.getMyWishes(actor)

        val resultActions = mvc
            .perform(
                get("/api/v1/wishes/mine")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiWishControllerV1::class.java))
            .andExpect(status().isOk())

        for (i in 0..<wishes.size) {
            val wish = wishes[i]
            val tags = bookService.getTagsByBookId(wish.id)

            resultActions
                .andExpect(jsonPath("$.[$i].id").value(wish.id))
                .andExpect(jsonPath("$.[$i].title").value(wish.title))
                .andExpect(jsonPath("$.[$i].imgUrl").value(wish.imgUrl))
                .andExpect(jsonPath("$.[$i].averageRating").value(wish.averageRating))

            for (j in 0..<tags.size) {
                val tag = tags[j]

                resultActions
                    .andExpect(jsonPath("$[$i].tags[$j]").value(tag))
            }
        }
    }

    @Test
    @DisplayName("찜 생성")
    @WithUserDetails("user1")
    fun t2() {
        val bookId = 2L

        val resultActions = mvc
            .perform(
                post("/api/v1/wishes/book/$bookId")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiWishControllerV1::class.java))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.message").value("찜 생성을 성공했습니다."))
    }

    @Test
    @DisplayName("찜 삭제")
    @WithUserDetails("user1")
    fun t4() {
        val id = 1L

        val resultActions = mvc
            .perform(
                delete("/api/v1/wishes/$id")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiWishControllerV1::class.java))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("찜 삭제를 성공했습니다."))
    }
}