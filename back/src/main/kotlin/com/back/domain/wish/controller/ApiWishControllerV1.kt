package com.back.domain.wish.controller

import com.back.domain.book.dto.BookWithWishIdAndTagsDto
import com.back.domain.wish.dto.WishDto
import com.back.domain.wish.service.WishService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/wishes")
@Tag(name = "ApiWishControllerV1", description = "API 찜 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
class ApiWishControllerV1(
    private val wishService: WishService,
    private val rq: Rq,
) {

    @GetMapping("/mine")
    @Operation(summary = "내 찜 다건 조회")
    fun getMyWishes(): List<BookWithWishIdAndTagsDto> =
        wishService.getMyWishes(rq.actor)

    @PostMapping("/book/{bookId}")
    @Operation(summary = "찜 생성")
    fun createWish(
        @PathVariable bookId: Long
    ): RsData<WishDto> {

        return RsData(
            "201-1",
            "찜 생성을 성공했습니다.",
            wishService.createWish(rq.actor, bookId))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "찜 삭제")
    fun deleteWish(
        @PathVariable id: Long
    ): RsData<Unit> {
        wishService.deleteWish(rq.actor, id)

        return RsData("200-1", "찜 삭제를 성공했습니다.")
    }
}
