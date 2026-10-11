package com.back.global.security

import com.back.global.rq.Rq
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class CustomOAuth2LoginFailureHandler(
    private val rq: Rq
) : AuthenticationFailureHandler {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
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

        // ✅ 최종 리다이렉트
        rq.sendRedirect(redirectUrl)
    }
}
