package com.back.domain.book.service

import com.back.domain.book.entity.Book
import com.back.domain.book.entity.BookLastFetchedPage
import com.back.domain.book.repository.BookLastFetchedPageRepository
import com.back.domain.book.repository.BookRepository
import com.back.standard.util.Ut
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.String
import org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED

@Service
@Transactional(readOnly = true)
class BookFetchServiceV2(
    @Value("\${custom.book-fetch.api-key}")
    private val apiKey: String,

    private val bookLastFetchedPageRepository: BookLastFetchedPageRepository,
    private val bookRepository: BookRepository
) {
    private fun fetchBooks(pageNumber: Int) =
        WebClient
            .create("https://www.nl.go.kr/seoji/SearchApi.do")
            .get()
            .uri {
                it
                    .queryParam("cert_key", apiKey)
                    .queryParam("result_style", "json")
                    .queryParam("page_no", pageNumber)
                    .queryParam("page_size", 2)
                    .queryParam("sort", "INPUT_DATE")
                    .queryParam("order_by", "ASC")
                    .build()
            }

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
        val docs: List<DocumentDto> // ? = null,

//        val RESULT: String? = null,
//        val ERR_CODE: String? = null,
//        val ERR_MESSAGE: String? = null,
    )

    @Transactional(propagation = NOT_SUPPORTED)
    fun fetchBooksFromLastFetchedPage(): List<Book> {
        val bookLastFetchedPage = bookLastFetchedPageRepository.findByIdOrNull(1)
            ?: bookLastFetchedPageRepository.save(BookLastFetchedPage())

        val currentPageNumber = bookLastFetchedPage.number + 1

        val responseBody = fetchBooks(currentPageNumber)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: ""
        if (responseBody.isBlank())
            throw RuntimeException("Response body is null or blank.")

        println(responseBody)

        val responseBodyDto = Ut.json.objectMapper!!.readValue(
            responseBody,
            ResponseBodyDto::class.java
        )
//        if (responseBodyDto.RESULT == "ERROR")
//            throw RuntimeException("${responseBodyDto.ERR_CODE}: ${responseBodyDto.ERR_MESSAGE}")

        val documents = responseBodyDto.docs // ?: listOf()
//        if (documents.isEmpty())
//            throw RuntimeException("documents is null or empty.")

        return documents.map {
            val publishedDate = it.PUBLISH_PREDATE.takeIf(String::isNotBlank)?.let { date ->
                LocalDate.parse(
                    date,
                    DateTimeFormatter.ofPattern("yyyyMMdd"),
                ).atStartOfDay()
            }

            Book(
                title = it.TITLE,
                description = it.BOOK_INTRODUCTION,
                isbn = it.EA_ISBN,
                authors = it.AUTHOR,
                publishedDate = publishedDate,
                publisher = it.PUBLISHER,
                imgUrl = it.TITLE_URL,
            )
        }
    }

    @Transactional
    fun updateBooks(books: List<Book>) {
        books.forEach {
            bookRepository.findByIsbn(it.isbn)
                ?: bookRepository.save(it)
        }

        val bookLastFetchedPage = bookLastFetchedPageRepository.findByIdOrNull(1)
            ?: throw RuntimeException("Cannot find last fetched page.")

        bookLastFetchedPage.increaseNumber()

        bookLastFetchedPageRepository.save(bookLastFetchedPage)
    }
}