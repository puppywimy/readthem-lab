package com.back.domain.book.repository

import com.back.domain.book.entity.Book

interface BookRepositoryCustom {

    fun saveBulk(books: List<Book>)
}