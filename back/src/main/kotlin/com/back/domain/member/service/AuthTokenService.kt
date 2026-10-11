package com.back.domain.member.service

import com.back.domain.member.entity.Member
import com.back.standard.util.Ut
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthTokenService {
    @Value("\${custom.accessToken.expirationSeconds}")
    private var expireSeconds: Int = 0

    @Value("\${custom.jwt.secretKey}")
    private lateinit var secret: String

    fun genAccessToken(member: Member): String {
        val id = member.id
        val username = member.username
        val name = member.name
        val role = member.role.name

        return Ut.jwt.toString(
            secret,
            expireSeconds,
            mapOf("id" to id, "username" to username, "name" to name, "role" to role)
        )
    }

    fun payload(accessToken: String): Map<String, Any>? {
        val parsedPayload = Ut.jwt.payload(secret, accessToken) ?: return null

        val id = parsedPayload["id"] as Int
        val username = parsedPayload["username"] as String
        val name = parsedPayload["name"] as String
        val role = parsedPayload["role"] as String

        return mapOf("id" to id, "username" to username, "name" to name, "role" to role)
    }
}