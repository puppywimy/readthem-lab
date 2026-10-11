package com.back.domain.book.repository

import com.back.domain.book.entity.BookFetchProgress
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BookFetchProgressRepository : JpaRepository<BookFetchProgress, Long> {
    fun findFirstByOrderByIdAsc(): BookFetchProgress?
}