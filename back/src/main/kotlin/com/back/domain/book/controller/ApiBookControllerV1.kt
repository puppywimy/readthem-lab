package com.back.domain.book.controller

import com.back.domain.book.controller.request.BookUpdateRequest
import com.back.domain.book.dto.BookDetailDto
import com.back.domain.book.dto.BookDto
import com.back.domain.book.dto.ThumbnailDto
import com.back.domain.book.service.BookRecommendService
import com.back.domain.book.service.BookService
import com.back.domain.book.service.BookThumbnailService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "ApiBookControllerV1", description = "API 도서 컨트롤러 V1")
class ApiBookControllerV1(
    private val rq: Rq,
    private val bookService: BookService,
    private val bookRecommendService: BookRecommendService,
    private val bookThumbnailService: BookThumbnailService
) {

    @GetMapping("/admin")
    @Operation(summary = "도서 다건 조회 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    fun getBooks(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ) = bookService.getBooks(page, size)

    @GetMapping("/{id}")
    @Operation(summary = "도서 단건 조회")
    fun getBookDetail(@PathVariable id: Long): BookDetailDto {
        val viewCookieName = "viewed-%d".format(id)
        val alreadyViewed = rq.getCookieValue(viewCookieName, "") == "true"
        val result = bookService.getBookDetail(id, rq.actorOrNull, alreadyViewed)

        if (result.viewCountIncreased) {
            rq.setCookie(viewCookieName, "true", 60)
        }

        return result.bookDetail
    }

    @GetMapping("/{id}/thumbnail")
    @Operation(summary = "도서 썸네일 지연 조회")
    fun getBookThumbnail(@PathVariable id: Long) =
        bookThumbnailService.getOrFetchThumbnail(id)

    @GetMapping("/search")
    @Operation(summary = "도서 검색 다건 조회")
    fun getBooksBySearch(
        @RequestParam @NotBlank searchTerm: @NotBlank String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ) = bookService.getBooksBySearch(searchTerm, page, size)

    @GetMapping("/recommend")
    @Operation(summary = "도서 추천 다건 조회")
    fun getBooksByRecommend(
        @RequestParam(defaultValue = "10") maxCount: Int
    ) = bookRecommendService
            .getBooksByRecommend(rq.actorOrNull, maxCount)

    @GetMapping("/rank")
    @Operation(summary = "도서 순위 다건 조회")
    fun getBooksOrderByRank(
        @RequestParam(defaultValue = "") type: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ) = bookService.getBooksOrderByRank(type, page, size)

    @PutMapping("/{id}")
    @Operation(summary = "도서 수정 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    fun updateBook(
        @PathVariable id: Long,
        @RequestBody @Valid req: BookUpdateRequest
    ): RsData<BookDto> {
        val book = bookService.updateBook(
            id, req.title, req.description, req.authors, req.publisher, req.imgUrl
        )

        return RsData("200-1", "도서 수정을 성공했습니다.", book)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "도서 삭제 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    fun deleteBook(
        @PathVariable id: Long
    ): RsData<Unit> {
        bookService.deleteBook(id)

        return RsData("200-1", "도서 삭제를 성공했습니다.")
    }
}
