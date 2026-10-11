package com.back.domain.member.dto

class TokenDto(
    val accessToken: String,
    val refreshToken: String?
)