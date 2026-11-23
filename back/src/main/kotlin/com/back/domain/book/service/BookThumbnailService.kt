package com.back.domain.book.service

import com.back.domain.book.client.KakaoBookClient
import com.back.domain.book.client.KakaoThumbnailResult
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

        return when (val result = kakaoBookClient.getThumbnailByIsbn(book.isbn)) {
            is KakaoThumbnailResult.Found -> {
                bookThumbnailWriter.updateImgUrl(bookId, result.url)
                ThumbnailDto(result.url)
            }

            KakaoThumbnailResult.NotFound -> {
                bookThumbnailWriter.markFetchAttempted(bookId)
                ThumbnailDto("")
            }

            // 일시적인 실패일 수 있으므로 이력을 남기지 않아 다음 조회 때 다시 시도한다
            KakaoThumbnailResult.Failed -> ThumbnailDto("")
        }
    }
}
