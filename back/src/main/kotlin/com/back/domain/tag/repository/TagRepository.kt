package com.back.domain.tag.repository

import com.back.domain.tag.entity.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface TagRepository : JpaRepository<Tag, Long> {
    fun findByName(@NotBlank name: String): Tag?
}
