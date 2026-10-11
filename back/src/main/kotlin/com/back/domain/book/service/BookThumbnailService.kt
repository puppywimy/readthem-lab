package com.back.domain.book.service

import com.back.domain.book.client.KakaoBookClient
import com.back.domain.book.dto.ThumbnailDto
import com.back.domain.book.repository.BookRepository
import com.back.global.exception.ServiceException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BookThumbnailService(
    private val kakaoBookClient: KakaoBookClient,
    private val bookThumbnailWriter: BookThumbnailWriter,
    private val bookRepository: BookRepository,
) {

    fun getOrFetchThumbnail(bookId: Long): ThumbnailDto {
        val book = bookRepository.findByIdOrNull(bookId)
            ?: throw ServiceException("404-1", "존재하지 않는 도서입니다.")

        if (!book.imgUrl.isNullOrEmpty())
            throw ServiceException("409-1", "이미 존재하는 썸네일입니다.")

        book.imgUrlFetchedAt?.let {
            throw ServiceException("404-2", "존재하지 않는 썸네일입니다.")
        }

        val thumbnail = kakaoBookClient.getThumbnailByIsbn(book.isbn)
        if (thumbnail == null) {
            bookThumbnailWriter.markFetchAttempted(bookId)
            return ThumbnailDto("")
        } else {
            bookThumbnailWriter.updateImgUrl(bookId, thumbnail)
            return ThumbnailDto(thumbnail)
        }
    }
}
