package com.back.domain.review.entity


import com.back.domain.tag.entity.Tag
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class ReviewTag(
    @field:ManyToOne(fetch = FetchType.LAZY)
    val review: Review,
    @field:ManyToOne(fetch = FetchType.LAZY)
    val tag: Tag
) : BaseEntity()
