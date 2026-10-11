package com.back.global.security

import com.back.domain.member.service.MemberService
import com.back.global.rq.Rq
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class CustomOAuth2LoginSuccessHandler(
    private val memberService: MemberService,
    private val rq: Rq
) : AuthenticationSuccessHandler {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val actor = rq.actorFromDb

        val accessToken = memberService.genAccessToken(actor)

        rq.setCookie("refreshToken", actor.refreshToken)
        rq.setCookie("accessToken", accessToken)

        var redirectUrl = "/"

        // ✅ state 파라미터 확인
        val stateParam = request.getParameter("state")

        if (stateParam != null) {
            // 1️⃣ Base64 URL-safe 디코딩
            val decodedStateParam = String(
                Base64.getUrlDecoder().decode(stateParam),
                StandardCharsets.UTF_8
            )

            // 2️⃣ '#' 앞은 redirectUrl, 뒤는 originState
            redirectUrl = decodedStateParam.split("#".toRegex(), limit = 2).toTypedArray()[0]
        }

        // getSession이 null을 반환할 수 있어 세이프콜을 해야 합니다.
        request.getSession(false)?.invalidate()

        // ✅ 최종 리다이렉트
        rq.sendRedirect(redirectUrl)
    }
}
