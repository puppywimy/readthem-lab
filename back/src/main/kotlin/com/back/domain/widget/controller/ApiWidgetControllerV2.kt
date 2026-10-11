package com.back.domain.widget.controller

import com.back.domain.widget.service.WidgetByWidgetServerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/v2/widgets")
@Tag(name = "ApiWidgetControllerV2", description = "API 위젯 컨트롤러 V2")
class ApiWidgetControllerV2(
    private val widgetByWidgetServerService: WidgetByWidgetServerService
) {

    @GetMapping("{githubId}")
    @Operation(summary = "위젯 단건 조회")
    fun getWidget(
        @PathVariable githubId: String
    ): ResponseEntity<String> {
        val widgetString = widgetByWidgetServerService.getWidget(githubId)
        val httpHeaders = HttpHeaders().apply {
            contentType = MediaType.parseMediaType("image/svg+xml")
        }

        return ResponseEntity(widgetString, httpHeaders, OK)
    }
}
