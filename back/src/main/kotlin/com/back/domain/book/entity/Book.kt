package com.back.domain.book.entity

import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
class Book(
    @field:Column(nullable = false, columnDefinition = "LONGTEXT")
    var title: String,

    @field:Column(columnDefinition = "LONGTEXT")
    var description: String?,

    @field:Column(unique = true)
    var isbn: String,

    @field:Column(columnDefinition = "LONGTEXT")
    var authors: String?,

    val publishedDate: LocalDateTime?,
    var publisher: String?,
    var imgUrl: String?

) : BaseEntity() {

    var imgUrlFetchedAt: LocalDateTime? = null
        protected set

    fun update(title: String, description: String?, authors: String?, publisher: String?, imgUrl: String?) {
        this.title = title
        this.description = description
        this.authors = authors
        this.publisher = publisher
        this.imgUrl = imgUrl
    }

    fun updateImgUrl(imgUrl: String) {
        this.imgUrl = imgUrl
        this.imgUrlFetchedAt = LocalDateTime.now()
    }

    fun markImgUrlFetchAttempted() {
        this.imgUrlFetchedAt = LocalDateTime.now()
    }
}
