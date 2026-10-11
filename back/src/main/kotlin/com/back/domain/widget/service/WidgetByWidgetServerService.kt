package com.back.domain.widget.service

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Duration

@Service
@Transactional(readOnly = true)
class WidgetByWidgetServerService(
    private val widgetInformationService: WidgetInformationService,
    private val widgetRendererWebClient: WebClient,
) {

    data class WidgetRenderRequest(
        val reviewCount: Int,
        val reviewWithContentCount: Int,
        val wishCount: Int,
        val books: List<BookItem>
    ) {
        class BookItem(
            val title: String,
            val hasContent: Boolean
        )
    }
    fun getWidget(githubId: String): String {

        val widgetInfo = widgetInformationService.getWidgetInformation(githubId)

        val books = widgetInfo.recentReadBooks.map { review ->
            WidgetRenderRequest.BookItem(
                review.title,
                review.withReview
            )
        }

        val request = WidgetRenderRequest(
            widgetInfo.readCount,
            widgetInfo.reviewCount,
            widgetInfo.wishCount,
            books = books
        )

        return widgetRendererWebClient.post()
            .uri("/widgets/bookshelf")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.valueOf("image/svg+xml"))
            .bodyValue(request)
            .retrieve()
            .bodyToMono<String>()
            .block(Duration.ofSeconds(3)) ?: ""
    }
}
