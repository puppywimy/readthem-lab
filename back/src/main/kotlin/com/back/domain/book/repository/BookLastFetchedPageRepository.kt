package com.back.domain.book.repository

import com.back.domain.book.entity.BookLastFetchedPage
import org.springframework.data.jpa.repository.JpaRepository

interface BookLastFetchedPageRepository : JpaRepository<BookLastFetchedPage, Long>