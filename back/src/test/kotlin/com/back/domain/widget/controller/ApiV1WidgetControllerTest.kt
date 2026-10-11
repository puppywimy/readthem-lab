package com.back.domain.widget.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1WidgetControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("위젯 조회")
    fun t1() {
        val githubId = "githubuser1"

        val resultActions = mvc
            .perform(
                get("/api/v1/widgets/$githubId")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiWidgetControllerV1::class.java))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith("image/svg+xml"))
    }
}
