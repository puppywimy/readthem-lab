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

    @Scheduled(initialDelay = 3000)
    fun fetchBooks() {
        while (!Thread.currentThread().isInterrupted) {
            try {
                val books = bookFetchService.fetchBooksFromLastFetchedPage()
                bookFetchService.updateBooks(books)
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (exception: Exception) {
                logger.error("도서 수집 실패", exception)
                Thread.sleep(3_000)
            }
        }
    }
}