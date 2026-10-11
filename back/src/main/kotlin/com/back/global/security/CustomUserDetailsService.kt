package com.back.global.security

import com.back.domain.member.entity.Member
import com.back.domain.member.service.MemberService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val memberService: MemberService
) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val member: Member = memberService.getMemberByUsername(username)

        return SecurityUser(
            member.id,
            member.username!!,
            member.nickname,
            member.authorities
        )
    }
}