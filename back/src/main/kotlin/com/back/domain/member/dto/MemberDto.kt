package com.back.domain.member.dto

import com.back.domain.member.entity.Member
import jakarta.validation.constraints.NotNull

open class MemberDto(
    @field:NotNull val id: Long,
    val githubId: String?,
    val githubLink: String?,
    val imgUrl: String?
) {
    constructor(member: Member) : this(
        member.id,
        member.githubId,
        member.githubLink,
        member.imgUrl
    )
}