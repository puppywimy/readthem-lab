package com.back.domain.book.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
class KakaoBookClient(
    private val kakaoBookApiWebClient: WebClient,
    @Value("\${custom.kakao.api-key:}") private val apiKey: String,
) {

    fun getThumbnailByIsbn(isbn: String): KakaoThumbnailResult {
        if (isbn.isBlank()) return KakaoThumbnailResult.NotFound

        // API 키가 설정되지 않은 환경(로컬 등)에서는 호출 자체를 건너뛰어 불필요한 대기 없이 fallback.
        // 키가 없어서 못 찾은 것이므로 표지 없음으로 기록하지 않는다.
        if (apiKey.isBlank()) return KakaoThumbnailResult.Failed

        val response = try {
            kakaoBookApiWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/v3/search/book")
                        .queryParam("query", isbn)
                        .queryParam("target", "isbn")
                        .build()
                }
                .retrieve()
                .bodyToMono(KakaoBookSearchResponse::class.java)
                .block(Duration.ofSeconds(2))
        } catch (_: Exception) {
            // 카카오 API 실패는 표지 보강 실패로만 취급하고 도서 조회 자체는 정상 진행되어야 함
            return KakaoThumbnailResult.Failed
        } ?: return KakaoThumbnailResult.Failed

        val thumbnail = response.documents
            .firstOrNull()
            ?.thumbnail
            ?.takeIf { it.isNotBlank() }
            ?: return KakaoThumbnailResult.NotFound

        return KakaoThumbnailResult.Found(thumbnail)
    }
}
