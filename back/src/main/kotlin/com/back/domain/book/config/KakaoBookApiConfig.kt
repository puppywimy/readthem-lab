package com.back.domain.book.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class KakaoBookApiConfig {

    @Bean
    fun kakaoBookApiWebClient(
        @Value("\${custom.kakao.base-url}") baseUrl: String,
        @Value("\${custom.kakao.api-key}") apiKey: String,
    ): WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK $apiKey")
        .build()
}
