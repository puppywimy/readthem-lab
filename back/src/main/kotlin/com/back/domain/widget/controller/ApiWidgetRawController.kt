package com.back.domain.widget.controller

import com.back.domain.widget.service.WidgetInformationService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/*/widgets")
@Tag(name = "ApiWidgetRawController", description = "API 위젯 기초 정보 컨트롤러")
class ApiWidgetRawController(
    private val widgetInformationService: WidgetInformationService
) {

    @GetMapping("/{githubId}/raw")
    fun getWidgetInformation(
        @PathVariable githubId: String
    ) = widgetInformationService.getWidgetInformation(githubId)

}