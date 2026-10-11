package com.back.domain.tag.entity

import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Tag(
    @field:Column(unique = true)
    val name: String
) : BaseEntity()
