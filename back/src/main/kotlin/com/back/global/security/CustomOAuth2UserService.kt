package com.back.global.security

import com.back.domain.member.service.MemberService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CustomOAuth2UserService(
    private val memberService: MemberService
) : DefaultOAuth2UserService() {

    // 깃허브 로그인이 성공할 때 마다 이 함수가 실행된다.
    @Transactional
    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val oauthUserId = oAuth2User.name
        val providerTypeCode = userRequest.clientRegistration.getRegistrationId().uppercase(Locale.getDefault())

        val attributes: MutableMap<String, Any> = oAuth2User.attributes

        val nickname = attributes["login"] as String
        val profileImgUrl = attributes["avatar_url"] as String?
        val username = providerTypeCode + "__%s".format(oauthUserId)
        val password = ""
        val member = memberService.modifyOrJoin(username, password, nickname, profileImgUrl)

        return SecurityUser(
            member.id,
            member.username!!,
            member.name!!,
            member.authorities
        )
    }
}