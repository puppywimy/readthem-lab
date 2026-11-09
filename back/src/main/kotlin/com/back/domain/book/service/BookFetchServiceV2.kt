package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.book.entity.BookLastFetchedPage
import com.back.domain.book.repository.BookLastFetchedPageRepository
import com.back.domain.book.repository.BookRepository
import com.back.standard.util.Ut
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.String
import org.springframework.transaction.annotation.Propagation.NEVER

@Service
@Transactional(readOnly = true)
class BookFetchServiceV2(
    @Value("\${custom.book-fetch.api-key}")
    private val apiKey: String,

    private val bookLastFetchedPageRepository: BookLastFetchedPageRepository,
    private val bookRepository: BookRepository,
) {
    companion object {
        private const val PAGE_SIZE = 1000
    }

    @Lazy
    @Autowired
    private lateinit var self: BookFetchServiceV2

    private fun fetchBooks(pageNumber: Int) =
        WebClient
            .builder()
            .baseUrl("https://www.nl.go.kr/seoji/SearchApi.do")
            .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(4 * 1024 * 1024) }
            .build()
            .get()
            .uri {
                it
                    .queryParam("cert_key", apiKey)
                    .queryParam("result_style", "json")
                    .queryParam("page_no", pageNumber)
                    .queryParam("page_size", PAGE_SIZE)
                    .queryParam("sort", "INPUT_DATE")
                    .queryParam("order_by", "ASC")
                    .build()
            }

    private val logger = LoggerFactory.getLogger(BookFetchServiceV2::class.java)

    private data class DocumentDto(
        val PUBLISHER: String,
        val DDC: String,
        val UPDATE_DATE: String,
        val BOOK_TB_CNT: String,
        val BOOK_SUMMARY: String,
        val EA_ADD_CODE: String,
        val PUBLISHER_URL: String,
        val AUTHOR: String,
        val SERIES_TITLE: String,
        val KDC: String,
        val EDITION_STMT: String,
        val BOOK_TB_CNT_URL: String,
        val SET_ISBN: String,
        val REAL_PUBLISH_DATE: String,
        val TITLE_URL: String,
        val PRE_PRICE: String,
        val BOOK_INTRODUCTION_URL: String,
        val DEPOSIT_YN: String,
        val BOOK_SIZE: String,
        val BOOK_SUMMARY_URL: String,
        val EBOOK_YN: String,
        val REAL_PRICE: String,
        val FORM: String,
        val FORM_DETAIL: String,
        val PAGE: String,
        val CONTROL_NO: String,
        val SERIES_NO: String,
        val EA_ISBN: String,
        val INPUT_DATE: String,
        val BOOK_INTRODUCTION: String,
        val SET_EXPRESSION: String,
        val VOL: String,
        val CIP_YN: String,
        val SUBJECT: String,
        val BIB_YN: String,
        val TITLE: String,
        val PUBLISH_PREDATE: String,
        val RELATED_ISBN: String,
        val SET_ADD_CODE: String
    )

    private class ResponseBodyDto(
        val TOTAL_COUNT: String? = null,
        val PAGE_NO: String? = null,
        val docs: List<DocumentDto>? = null,

        val resultCode: String? = null,
        val resultMsg: String? = null,
    )

    @Transactional(propagation = NEVER)
    fun fetchBooksFromLastFetchedPage() {
        while (true) {
            val bookLastFetchedPage = bookLastFetchedPageRepository.findByIdOrNull(1)
                ?: bookLastFetchedPageRepository.save(BookLastFetchedPage())
            val currentPageNumber = bookLastFetchedPage.number + 1

            val responseBody = try {
                fetchBooks(currentPageNumber)
                    .retrieve()
                    .bodyToMono<String>()
                    .block() ?: ""
            } catch (e: WebClientResponseException) {
                logger.error("page=$currentPageNumber 외부 API 오류 응답 status=${e.statusCode} body=${e.responseBodyAsString}")
                break
            } catch (e: Exception) {
                logger.error("page=$currentPageNumber 외부 API 요청 실패", e)
                break
            }

            if (responseBody.isBlank()) {
                logger.error("page=$currentPageNumber 응답 본문이 비어 있음")
                break
            }

            val responseBodyDto = try {
                Ut.json.objectMapper!!.readValue(
                    responseBody,
                    ResponseBodyDto::class.java
                )
            } catch (e: Exception) {
                logger.error("page=$currentPageNumber 응답 본문 파싱 실패", e)
                break
            }

            if (responseBodyDto.docs == null) {
                val resultCode = responseBodyDto.resultCode
                val resultMsg = responseBodyDto.resultMsg
                when (resultCode) {
                    "105" -> {
                        logger.error("page=$currentPageNumber API 오류 resultCode=105 resultMsg=$resultMsg body=$responseBody")
                        bookLastFetchedPage.increaseNumber()
                        bookLastFetchedPageRepository.save(bookLastFetchedPage)
                        continue
                    }

                    else -> {
                        logger.error("page=$currentPageNumber API 오류 resultCode=$resultCode resultMsg=$resultMsg body=$responseBody")
                        break
                    }
                }
            }

            val totalCount = try {
                Integer.parseInt(responseBodyDto.TOTAL_COUNT)
            } catch (e: Exception) {
                logger.error("page=$currentPageNumber TOTAL_COUNT가 숫자가 아님 TOTAL_COUNT=${responseBodyDto.TOTAL_COUNT}")
                break
            }

            val documents = responseBodyDto.docs
            if (documents.isEmpty()) {
                logger.error("page=$currentPageNumber docs가 빈 배열임")
                break
            }

            val books = documents
                .mapIndexedNotNull { i, it ->
                    val currentDocumentNumber = (currentPageNumber - 1) * PAGE_SIZE + i + 1;

                    val isbn = it.EA_ISBN.uppercase()
                    if (isbn.isBlank()) {
                        logger.debug("page=$currentPageNumber document=$currentDocumentNumber controlNo=${it.CONTROL_NO} ISBN 없음, 건너뜀")
                        return@mapIndexedNotNull null
                    }

                    val publishedDate = try {
                        LocalDate.parse(
                            it.PUBLISH_PREDATE,
                            DateTimeFormatter.ofPattern("yyyyMMdd"),
                        ).atStartOfDay()
                    } catch (e: DateTimeParseException) {
                        logger.debug("page=$currentPageNumber document=$currentDocumentNumber controlNo=${it.CONTROL_NO} 출판일 형식 오류 PUBLISH_PREDATE=${it.PUBLISH_PREDATE}, null로 저장")
                        null
                    }

                    Book(
                        title = it.TITLE,
                        description = it.BOOK_INTRODUCTION,
                        isbn = isbn,
                        authors = it.AUTHOR,
                        publishedDate = publishedDate,
                        publisher = it.PUBLISHER,
                        imgUrl = it.TITLE_URL,
                        fetchedPage = currentPageNumber,
                    )
                }
                .reversed()

            val savedCount = try {
                self.updateBooksAndProgress(books)
            } catch (e: Exception) {
                logger.error("page=$currentPageNumber 저장 실패", e)
                break
            }

            val lastPageNumber = (totalCount + PAGE_SIZE - 1) / PAGE_SIZE
            logger.info("page=$currentPageNumber/$lastPageNumber 처리 완료 fetched=${documents.size} savedCount=$savedCount")

            if (totalCount <= currentPageNumber * PAGE_SIZE) {
                logger.info("마지막 페이지 도달, 수집 완료")
                break
            }
        }
    }

    @Transactional
    fun updateBooksAndProgress(books: List<Book>): Int {
        val bookLastFetchedPage = bookLastFetchedPageRepository.findByIdOrNull(1)
            ?: error("Cannot find BookLastFetchedPage.")

        val newBooks = mutableListOf<Book>()

        if (books.isNotEmpty()){
            val duplicatedIsbns = bookRepository
                .findByIsbnIn(books.map { it.isbn })
                .map { it.isbn }
                .toMutableSet()

            books.forEach {
                if (duplicatedIsbns.add(it.isbn)) newBooks.add(it)
            }

            bookRepository.saveBulk(newBooks)
        }

        bookLastFetchedPage.increaseNumber()

        return newBooks.size
    }
}