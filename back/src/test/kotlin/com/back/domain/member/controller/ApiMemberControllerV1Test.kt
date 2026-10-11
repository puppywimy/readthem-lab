package com.back.domain.member.controller

import com.back.domain.member.repository.MemberRepository
import com.back.domain.member.service.MemberService
import org.assertj.core.api.AssertionsForClassTypes.assertThat
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
class ApiMemberControllerV1Test {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    @DisplayName("내 정보 단건 조회")
    @WithUserDetails("user1")
    fun t1() {
        val resultActions = mvc
            .perform(get("/api/v1/members/me"))
            .andDo(print())

        val member = memberService.getMemberByUsername("user1")

        resultActions
            .andExpect(handler().handlerType(ApiMemberControllerV1::class.java))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(member.id))
            .andExpect(jsonPath("$.username").value(member.username))
            .andExpect(jsonPath("$.githubId").value(member.githubId))
            .andExpect(jsonPath("$.githubLink").value(member.githubLink))
            .andExpect(jsonPath("$.widgetLink").value(member.widgetLink))
    }

    @Test
    @DisplayName("회원 단건 조회")
    fun t2() {
        val id = 1L

        val resultActions = mvc
            .perform(get("/api/v1/members/$id"))
            .andDo(print())

        val member = memberService.getMemberById(id)

        resultActions
            .andExpect(handler().handlerType(ApiMemberControllerV1::class.java))
            .andExpect(handler().methodName("getMember"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(member.id))
            .andExpect(jsonPath("$.githubId").value(member.githubId))
            .andExpect(jsonPath("$.githubLink").value(member.githubLink))
    }

    @Test
    @DisplayName("회원 탈퇴")
    @WithUserDetails("user1")
    fun t3() {
        val resultActions = mvc
            .perform(delete("/api/v1/members"))
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiMemberControllerV1::class.java))
            .andExpect(handler().methodName("delete"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("회원 탈퇴를 성공했습니다."))

        resultActions.andExpect { result ->
            val refreshTokenCookie = result.response.getCookie("refreshToken")!!

            assertThat(refreshTokenCookie.value).isEmpty()
            assertThat(refreshTokenCookie.maxAge).isEqualTo(0)
            assertThat(refreshTokenCookie.path).isEqualTo("/")
            assertThat(refreshTokenCookie.isHttpOnly).isTrue()

            val accessTokenCookie = result.response.getCookie("accessToken")!!

            assertThat(accessTokenCookie.value).isEmpty()
            assertThat(accessTokenCookie.maxAge).isEqualTo(0)
            assertThat(accessTokenCookie.path).isEqualTo("/")
            assertThat(accessTokenCookie.isHttpOnly).isTrue()
        }
    }

    @Test
    @DisplayName("로그인")
    fun t4() {
        val resultActions = mvc
            .perform(
                post("/api/v1/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "username": "user1",
                            "password": "1234"
                        }
                        """.trimIndent()
                    )
            )
            .andDo(print())

        val member = memberService.getMemberByUsername("user1")

        resultActions
            .andExpect(handler().handlerType(ApiMemberControllerV1::class.java))
            .andExpect(handler().methodName("login"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("로그인을 성공했습니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.refreshToken").value(member.refreshToken))

        resultActions.andExpect { result ->
            val refreshTokenCookie = result.response.getCookie("refreshToken")!!

            assertThat(refreshTokenCookie.value).isEqualTo(member.refreshToken)
            assertThat(refreshTokenCookie.path).isEqualTo("/")
            assertThat(refreshTokenCookie.isHttpOnly).isTrue()

            val accessTokenCookie = result.response.getCookie("accessToken")!!

            assertThat(accessTokenCookie.value).isNotBlank()
            assertThat(accessTokenCookie.path).isEqualTo("/")
            assertThat(accessTokenCookie.isHttpOnly).isTrue()
        }
    }

    @Test
    @DisplayName("회원가입")
    fun t5() {
        val resultActions = mvc
            .perform(
                post("/api/v1/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "username": "user7",
                            "password": "1234",
                            "githubId": "easy-h"
                        }
                        """.trimIndent()
                    )
            )
            .andDo(print())

        val member = memberService.getMemberByUsername("user7")

        resultActions
            .andExpect(handler().handlerType(ApiMemberControllerV1::class.java))
            .andExpect(handler().methodName("join"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("회원가입을 성공했습니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.refreshToken").value(member.refreshToken))

        resultActions.andExpect { result ->
            val refreshTokenCookie = result.response.getCookie("refreshToken")!!

            assertThat(refreshTokenCookie.value).isEqualTo(member.refreshToken)
            assertThat(refreshTokenCookie.path).isEqualTo("/")
            assertThat(refreshTokenCookie.isHttpOnly).isTrue()

            val accessTokenCookie = result.response.getCookie("accessToken")!!

            assertThat(accessTokenCookie.value).isNotBlank()
            assertThat(accessTokenCookie.path).isEqualTo("/")
            assertThat(accessTokenCookie.isHttpOnly).isTrue()
        }
    }

    @Test
    @DisplayName("회원 다건 조회 - 관리자")
    @WithUserDetails("admin")
    fun t7() {
        val resultActions = mvc
            .perform(
                get("/api/v1/members/admin")
                    .param("page", "0")
                    .param("size", "10")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiMemberControllerV1::class.java))
            .andExpect(handler().methodName("getMembers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].username").exists())
            .andExpect(jsonPath("$.content[0].isAdmin").exists())
            .andExpect(jsonPath("$.content[0].isDeleted").exists())
    }

    @Test
    @DisplayName("회원 다건 조회 - 실패: 관리자가 아님")
    @WithUserDetails("user1")
    fun t8() {
        val resultActions = mvc
            .perform(get("/api/v1/members/admin"))
            .andDo(print())

        resultActions
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.message").value("권한이 없습니다."))
    }

    @Test
    @DisplayName("회원 삭제 - 관리자")
    @WithUserDetails("admin")
    fun t9() {
        val member = memberService.getMemberByUsername("user2")

        val resultActions = mvc
            .perform(delete("/api/v1/members/admin/${member.id}"))
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiMemberControllerV1::class.java))
            .andExpect(handler().methodName("deleteMember"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("회원 강제 탈퇴를 성공했습니다."))

        val found = memberRepository.findById(member.id)

        assertThat(found).isPresent
        assertThat(found.get().isDeleted).isTrue()
    }

    @Test
    @DisplayName("회원 삭제 - 실패: 관리자가 아님")
    @WithUserDetails("user1")
    fun t10() {
        val member = memberService.getMemberByUsername("user2")

        val resultActions = mvc
            .perform(delete("/api/v1/members/admin/${member.id}"))
            .andDo(print())

        resultActions
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.message").value("권한이 없습니다."))
    }

    @Test
    @DisplayName("로그아웃")
    @WithUserDetails("user1")
    fun t6() {
        val resultActions = mvc
            .perform(delete("/api/v1/members/logout"))
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiMemberControllerV1::class.java))
            .andExpect(handler().methodName("logout"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.message").value("로그아웃을 성공했습니다."))

        resultActions.andExpect { result ->
            val refreshTokenCookie = result.response.getCookie("refreshToken")!!

            assertThat(refreshTokenCookie.value).isEmpty()
            assertThat(refreshTokenCookie.maxAge).isEqualTo(0)
            assertThat(refreshTokenCookie.path).isEqualTo("/")
            assertThat(refreshTokenCookie.isHttpOnly).isTrue()

            val accessTokenCookie = result.response.getCookie("accessToken")!!

            assertThat(accessTokenCookie.value).isEmpty()
            assertThat(accessTokenCookie.maxAge).isEqualTo(0)
            assertThat(accessTokenCookie.path).isEqualTo("/")
            assertThat(accessTokenCookie.isHttpOnly).isTrue()
        }
    }
}