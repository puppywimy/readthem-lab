package com.back.domain.member.controller

import com.back.domain.member.dto.AdminMemberDto
import com.back.domain.member.dto.MemberDto
import com.back.domain.member.controller.request.MemberJoinRequest
import com.back.domain.member.controller.request.MemberLoginRequest
import com.back.domain.member.dto.MemberWithUsernameAndWidgetLinkDto
import com.back.domain.member.dto.TokenDto
import com.back.domain.member.service.MemberService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "ApiMemberControllerV1", description = "API 회원 컨트롤러 V1")
class ApiMemberControllerV1(
    private val memberService: MemberService,
    private val rq: Rq
) {
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    @SecurityRequirement(name = "bearerAuth")
    fun getMy(): MemberWithUsernameAndWidgetLinkDto {
        return memberService.getMy(rq.actor)
    }

    @GetMapping("/{id}")
    @Operation(summary = "회원 단건 조회")
    fun getMember(
        @PathVariable @Valid id: Long
    ): MemberDto {
        return memberService.getMember(id)
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴")
    @SecurityRequirement(name = "bearerAuth")
    fun delete(): RsData<Unit> {
        memberService.deleteMember(rq.actor.id)

        rq.deleteCookie("refreshToken")
        rq.deleteCookie("accessToken")

        return RsData("200-1", "회원 탈퇴를 성공했습니다.")
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    fun login(
        @RequestBody @Valid reqBody: MemberLoginRequest
    ): RsData<TokenDto> {
        val member = memberService.getMemberByUsername(reqBody.username)

        memberService.checkPassword(member, reqBody.password)

        val accessToken = memberService.genAccessToken(member)

        rq.setCookie("refreshToken", member.refreshToken)
        rq.setCookie("accessToken", accessToken)

        return RsData(
            "200-1",
            "로그인을 성공했습니다.",
            TokenDto(
                accessToken,
                member.refreshToken
            )
        )
    }

    @PostMapping
    @Operation(summary = "회원가입")
    fun join(
        @RequestBody @Valid reqBody: MemberJoinRequest
    ): RsData<TokenDto> {
        val member = memberService.join(
            reqBody.username,
            reqBody.password,
            reqBody.githubId,
            null
        )

        memberService.checkPassword(member, reqBody.password)

        val accessToken = memberService.genAccessToken(member)

        rq.setCookie("refreshToken", member.refreshToken)
        rq.setCookie("accessToken", accessToken)

        return RsData(
            "200-1",
            "회원가입을 성공했습니다.",
            TokenDto(
                accessToken,
                member.refreshToken
            )
        )
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "bearerAuth")
    fun logout(): RsData<Unit> {
        rq.deleteCookie("refreshToken")
        rq.deleteCookie("accessToken")

        return RsData("200-1", "로그아웃을 성공했습니다.")
    }

    @GetMapping("/admin")
    @Operation(summary = "회원 다건 조회 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    fun getMembers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<AdminMemberDto> {
        return memberService.getMembers(page, size)
    }

    @DeleteMapping("/admin/{id}")
    @Operation(summary = "회원 강제 탈퇴 (관리자)")
    @SecurityRequirement(name = "bearerAuth")
    fun deleteMember(
        @PathVariable id: Long
    ): RsData<Unit> {
        memberService.deleteMember(id)

        return RsData("200-1", "회원 강제 탈퇴를 성공했습니다.")
    }
}