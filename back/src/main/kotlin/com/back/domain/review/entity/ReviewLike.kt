package com.back.domain.review.entity

import com.back.domain.member.entity.Member
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["review_id", "member_id"])])
class ReviewLike(
    @field:JoinColumn(name = "review_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    val review: Review,

    @field:JoinColumn(name = "member_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    val member: Member
) : BaseEntity()
