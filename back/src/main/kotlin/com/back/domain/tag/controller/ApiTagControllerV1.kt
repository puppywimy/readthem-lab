package com.back.domain.tag.controller

import com.back.domain.tag.controller.request.TagCreateRequest
import com.back.domain.tag.service.TagService
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "ApiTagControllerV1", description = "API 태그 컨트롤러 V1")
class ApiTagControllerV1(
    private val tagService: TagService
) {

    @PostMapping
    @Operation(summary = "태그 생성")
    fun createTag(
        @RequestBody @Valid req: TagCreateRequest
    ): RsData<Unit> {
        tagService.createTag(req.name)

        return RsData(
            "201-1", "태그 생성을 성공했습니다."
        )
    }
}
