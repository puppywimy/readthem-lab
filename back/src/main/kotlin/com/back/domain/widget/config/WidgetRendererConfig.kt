package com.back.domain.widget.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WidgetRendererConfig {

    @Bean
    fun widgetRendererWebClient(
        @Value($$"${custom.widget.renderer.base-url}")
        baseUrl: String,
    ): WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build()
}
