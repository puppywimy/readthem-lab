package com.back.domain.book.service

import com.back.domain.book.client.KakaoBookClient
import com.back.domain.book.entity.Book
import com.back.domain.book.repository.BookRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional

class BookThumbnailServiceTest {
    private val bookRepository: BookRepository = mock(BookRepository::class.java)
    private val kakaoBookClient: KakaoBookClient = mock(KakaoBookClient::class.java)
    private val bookThumbnailWriter: BookThumbnailWriter = mock(BookThumbnailWriter::class.java)
    private val bookThumbnailService =
        BookThumbnailService(kakaoBookClient, bookThumbnailWriter, bookRepository)

    @Test
    fun `imgUrl이 없고 조회 이력도 없으면 카카오 API에서 썸네일을 가져와 저장한다`() {
        val book = Book(
            title = "썸네일 미조회 도서",
            description = null,
            isbn = "9790000000001",
            authors = "작가",
            publishedDate = null,
            publisher = "출판사",
            imgUrl = null,
        )

        `when`(bookRepository.findById(1L)).thenReturn(Optional.of(book))
        `when`(kakaoBookClient.getThumbnailByIsbn(book.isbn))
            .thenReturn("https://example.com/new-cover.png")

        val thumbnail = bookThumbnailService.getOrFetchThumbnail(1L)

        assertThat(thumbnail.imgUrl).isEqualTo("https://example.com/new-cover.png")
        verify(bookThumbnailWriter).updateImgUrl(1L, "https://example.com/new-cover.png")
    }

    @Test
    fun `카카오 API에서 썸네일을 찾지 못하면 빈 문자열을 반환한다`() {
        val book = Book(
            title = "썸네일 조회 실패 도서",
            description = null,
            isbn = "9790000000002",
            authors = "작가",
            publishedDate = null,
            publisher = "출판사",
            imgUrl = null,
        )

        `when`(bookRepository.findById(1L)).thenReturn(Optional.of(book))
        `when`(kakaoBookClient.getThumbnailByIsbn(book.isbn)).thenReturn(null)

        val thumbnail = bookThumbnailService.getOrFetchThumbnail(1L)

        assertThat(thumbnail.imgUrl).isEqualTo("")
        verify(bookThumbnailWriter).markFetchAttempted(1L)
    }
}
