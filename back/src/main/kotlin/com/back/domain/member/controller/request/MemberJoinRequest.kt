package com.back.domain.member.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

class MemberJoinRequest(
    @NotBlank
    @Size(min = 2, max = 30)
    val username: String,
    @NotBlank
    @Size(min = 2, max = 30)
    val password: String,
    @NotBlank
    @Size(max = 39)
    @Pattern(
        regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
        message = "githubId는 영문 소문자, 숫자, 하이픈(-)만 사용할 수 있으며 하이픈은 처음/끝/연속으로 올 수 없습니다."
    )
    val githubId: String
)