package com.back.domain.wish.entity

import com.back.domain.book.entity.Book
import com.back.domain.member.entity.Member
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class Wish(
    @field:JoinColumn(name = "member_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    val member: Member,

    @field:JoinColumn(name = "book_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    val book: Book,
) : BaseEntity()
