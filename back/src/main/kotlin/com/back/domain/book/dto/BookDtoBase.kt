package com.back.domain.book.dto

abstract class BookDtoBase(
    @JvmField
    val id: Long,
    val title: String,
    val imgUrl: String?
)