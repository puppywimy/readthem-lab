package com.back.domain.member.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class MemberLoginRequest(
    @NotBlank
    @Size(min = 2, max = 30)
    val username: String,
    @NotBlank
    @Size(min = 2, max = 30)
    val password: String
)