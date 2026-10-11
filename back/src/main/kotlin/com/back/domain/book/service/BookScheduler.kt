package com.back.domain.book.service

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Profile("prod")
class BookScheduler(
    private val bookService: BookService,
    private val bookFetchService: BookFetchServiceV2
) {
    private val logger = LoggerFactory.getLogger(BookScheduler::class.java)

    @Scheduled(cron = "0 */5 * * * *")
    fun syncViewCountsToDb() {
        bookService.updateBooksViewCountInDb()
    }

//    @Scheduled(fixedDelay = 10000, initialDelay = 3000)
    fun fetchBooks() {
        try {
            val books = bookFetchService.fetchBooksFromLastFetchedPage()
            bookFetchService.updateBooks(books)
        } catch (exception: Exception) {
            logger.error(exception.message)
        }
    }
}