package com.back.global.rq

import com.back.domain.member.entity.Member
import com.back.domain.member.entity.Role
import com.back.domain.member.service.MemberService
import com.back.global.app.AppConfig
import com.back.global.exception.ServiceException
import com.back.global.security.SecurityUser
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class Rq(
    private val req: HttpServletRequest,
    private val resp: HttpServletResponse,
    private val memberService: MemberService
) {

    val actorOrNull: Member?
        get() {
            val principal = SecurityContextHolder
                .getContext()
                .authentication
                ?.principal

            if (principal == null || principal !is SecurityUser)
                return null

            return Member(
                id = principal.id,
                username = principal.username,
                name = principal.name,
                role = if (hasAdminAuthority(principal.authorities)) Role.ADMIN else Role.USER
            )
        }

    val actor: Member
        get() {
            return actorOrNull ?: throw ServiceException("401-1", "로그인이 필요합니다.")
        }

    private fun hasAdminAuthority(authorities: Collection<GrantedAuthority>): Boolean {
        return authorities.any { authority -> "ROLE_ADMIN" == authority.authority }
    }

    fun getHeader(name: String, defaultValue: String): String {
        return req.getHeader(name) ?: defaultValue
    }

    fun setHeader(name: String?, value: String?) {
        val value = value ?: ""

        if (value.isBlank()) {
            req.removeAttribute(name)
        } else {
            resp.setHeader(name, value)
        }
    }

    fun getCookieValue(name: String, defaultValue: String): String =
        req.cookies
            ?.firstOrNull { it.name == name }
            ?.value
            ?.takeIf { it.isNotBlank() }
            ?: defaultValue

    fun setCookie(name: String?, value: String?, maxAge: Int) {
        var value = value
        if (value == null) value = ""

        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.domain = AppConfig.cookieDomain
        cookie.secure = AppConfig.cookieSecure
        cookie.setAttribute("SameSite", "Strict")

        if (value.isBlank()) cookie.maxAge = 0
        else cookie.maxAge = maxAge

        resp.addCookie(cookie)
    }

    fun setCookie(name: String?, value: String?) {
        setCookie(name, value, 60 * 60 * 24 * 365)
    }

    fun deleteCookie(name: String?) {
        setCookie(name, null)
    }

    fun sendRedirect(url: String?) {
        resp.sendRedirect(url)
    }

    val actorFromDb: Member
        get() = memberService.getMemberById(actor!!.id)
}