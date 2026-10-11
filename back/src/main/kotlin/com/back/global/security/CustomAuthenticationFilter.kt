package com.back.global.security

import com.back.domain.member.entity.Member
import com.back.domain.member.entity.Role
import com.back.domain.member.service.MemberService
import com.back.global.exception.ServiceException
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import com.back.standard.util.Ut
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class CustomAuthenticationFilter(
    private val memberService: MemberService,
    private val rq: Rq
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        logger.debug("Processing request for " + request.requestURI)

        try {
            work(request, response, filterChain)
        } catch (e: ServiceException) {
            val rsData: RsData<Void> = e.rsData
            response.contentType = "application/json;charset=UTF-8"
            response.status = rsData.statusCode
            response.writer.write(
                Ut.json.toString(rsData)
            )
        }
    }

    @Throws(ServletException::class, IOException::class)
    private fun work(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // API 요청이 아니라면 패스
        if (!request.requestURI.startsWith("/api/")) {
            filterChain.doFilter(request, response)
            return
        }

        // 인증, 인가가 필요없는 API 요청이라면 패스
        if (mutableListOf<String?>(
                "/api/v1/members/login",
                "/api/v1/members/logout",
                "/api/v1/members/join"
            ).contains(request.requestURI)
        ) {
            filterChain.doFilter(request, response)
            return
        }

        var refreshToken: String
        var accessToken: String

        val headerAuthorization = rq.getHeader("Authorization", "")

        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer ")) throw ServiceException(
                "401-2",
                "Authorization 헤더가 Bearer 형식이 아닙니다."
            )

            val headerAuthorizationBits = headerAuthorization.split(" ".toRegex(), limit = 3).toTypedArray()

            refreshToken = headerAuthorizationBits[1]
            accessToken = if (headerAuthorizationBits.size == 3) headerAuthorizationBits[2] else ""
        } else {
            refreshToken = rq.getCookieValue("refreshToken", "")
            accessToken = rq.getCookieValue("accessToken", "")
        }

        logger.debug("refreshToken : $refreshToken")
        logger.debug("accessToken : $accessToken")

        val isRefreshTokenExists = !refreshToken.isBlank()
        val isAccessTokenExists = !accessToken.isBlank()

        if (!isRefreshTokenExists && !isAccessTokenExists) {
            filterChain.doFilter(request, response)
            return
        }

        var member: Member? = null
        var isAccessTokenValid = false

        if (isAccessTokenExists) {
            val payload = memberService.payload(accessToken)

            if (payload != null) {
                val id = payload["id"] as Int
                val username = payload["username"] as String
                val name = payload["name"] as String
                val role = Role.valueOf(payload["role"] as String)
                member = Member(id.toLong(), username, name, role)

                isAccessTokenValid = true
            }
        }

        if (member == null) {
            member = memberService
                .getMemberByRefreshToken(refreshToken)
                ?: throw ServiceException("401-3", "API 키가 유효하지 않습니다.")
        }

        if (isAccessTokenExists && !isAccessTokenValid) {
            val actorAccessToken = memberService.genAccessToken(member)

            rq.setCookie("accessToken", actorAccessToken)
            rq.setHeader("Authorization", actorAccessToken)
        }

        val user: UserDetails = SecurityUser(
            member.id,
            member.username ?: "",
            member.name ?: "",
            member.authorities
        )

        val authentication: Authentication = UsernamePasswordAuthenticationToken(
            user,
            user.password,
            user.authorities
        )

        // 이 시점 이후부터는 시큐리티가 이 요청을 인증된 사용자의 요청이다.
        SecurityContextHolder
            .getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }
}
