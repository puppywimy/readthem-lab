package com.back.domain.tag.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class TagCreateRequest (
        @field:NotBlank
        @field:Size(min = 1, max = 20, message = "태그명은 1~20자여야 합니다")
        val name: String
    )