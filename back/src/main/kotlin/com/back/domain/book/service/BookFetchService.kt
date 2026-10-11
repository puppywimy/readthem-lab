package com.back.domain.book.service

import com.back.domain.book.entity.BookFetchProgress
import com.back.domain.book.repository.BookFetchProgressRepository
import com.back.domain.book.repository.BookRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.util.UriComponentsBuilder

@Service
class BookFetchService(
    private val bookRepository: BookRepository,
    private val bookFetchProgressRepository: BookFetchProgressRepository,
    private val restTemplate: RestTemplate,
    private val objectMapper: tools.jackson.databind.ObjectMapper? = null,

) {

    @Autowired
    @Lazy
    private lateinit var self: BookFetchService

    @Value($$"${custom.book-fetch.api-keys}")
    private val apiKeys: List<String>? = null

    @Transactional
    fun fetch() {
        val progress: BookFetchProgress = bookFetchProgressRepository.findFirstByOrderByIdAsc()
            ?: run {
                val newProgress = BookFetchProgress()
                newProgress.nextPage()
                bookFetchProgressRepository.save(newProgress)
            }

        if (progress.currentApiKeyIndex >= apiKeys!!.size) {
            log.info("오늘 모든 API 키 호출 한도 소진. 내일 다시 시작합니다.")
            return
        }

        val apiKey = apiKeys[progress.currentApiKeyIndex]

        // 국립중앙도서관 api 요청변수에 맞게 변경
        val uri: java.net.URI = UriComponentsBuilder.fromUriString(API_URL)
            .queryParam("cert_key", apiKey)
            .queryParam("result_style", "json")
            .queryParam("page_no", progress.currentPage)
            .queryParam("page_size", PAGE_SIZE)
            .queryParam("form", "종이책")
            .queryParam("sort", "INPUT_DATE")
            .queryParam("order_by", "DESC")
            .encode()
            .build()
            .toUri()

        try {
            val response: String? = restTemplate.getForObject<String>(uri)
            val root = objectMapper!!.readTree(response)

            if ("ERROR" == root.path("RESULT").asText(null)) {
                log.error(
                    "국립중앙도서관 API 오류 - errCode: {}, errMessage: {}, page: {}",
                    root.path("ERR_CODE").asText(), root.path("ERR_MESSAGE").asText(), progress.currentPage
                )
                return
            }

            val items = root.path("docs")

            if (items.isMissingNode || items.isEmpty) {
                log.info("더 이상 수집할 데이터가 없습니다. page: {}", progress.currentPage)
                return
            }

            if (items.isArray) {
                for (item in items) {
                    trySaveBook(item)
                }
            } else {
                trySaveBook(items)
            }

            progress.incrementCallCount()
            progress.nextPage()
            bookFetchProgressRepository.save(progress)

            log.info(
                "수집 완료 - apiKeyIndex: {}, page: {}, callCount: {}",
                progress.currentApiKeyIndex, progress.currentPage, progress.dailyCallCount
            )

            if (progress.dailyCallCount >= DAILY_LIMIT) {
                log.info("API 키 {} 한도 소진. 다음 키로 전환합니다.", progress.currentApiKeyIndex)
                progress.nextApiKeyIndex()
                bookFetchProgressRepository.save(progress)
            }
        } catch (e: java.lang.Exception) {
            log.error("도서 수집 중 오류 발생 - page: {}", progress.currentPage, e)
        }
    }

    private fun trySaveBook(item: tools.jackson.databind.JsonNode) {
        try {
            self.saveBook(item)
        } catch (e: java.lang.Exception) {
            log.error("도서 저장 실패 - isbn: {}, title: {}", item.path("EA_ISBN").asText(), item.path("TITLE").asText(), e)
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    fun saveBook(item: tools.jackson.databind.JsonNode) {
        val isbn = item.path("EA_ISBN").asText()

        if (isbn.isBlank() || bookRepository.existsByIsbn(isbn)) {
            return
        }

        val publishPredate = item.path("PUBLISH_PREDATE").asText()
        var publishedDate: java.time.LocalDateTime? = null
        if (!publishPredate.isBlank()) {
            try {
                if (publishPredate.length == 8) {
                    val date = java.time.LocalDate.parse(
                        publishPredate,
                        java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
                    )
                    publishedDate = date.atStartOfDay()
                } else if (publishPredate.length >= 4) {
                    publishedDate = java.time.LocalDateTime.of(publishPredate.substring(0, 4).toInt(), 1, 1, 0, 0)
                }
            } catch (_: java.lang.Exception) {
                log.warn("발행일 파싱 실패: {}", publishPredate)
            }
        }

        val book = com.back.domain.book.entity.Book(
            item.path("TITLE").asText(),
            item.path("BOOK_INTRODUCTION").asText(null),
            isbn,
            item.path("AUTHOR").asText(null),
            publishedDate!!,
            item.path("PUBLISHER").asText(null),
            item.path("TITLE_URL").asText(null)
        )

        bookRepository.save(book)
    }

    companion object {
        private const val API_URL = "https://www.nl.go.kr/seoji/SearchApi.do"
        private const val PAGE_SIZE = 1000
        private const val DAILY_LIMIT = 10000
        private val log = LoggerFactory.getLogger(BookFetchService::class.java)
    }
}