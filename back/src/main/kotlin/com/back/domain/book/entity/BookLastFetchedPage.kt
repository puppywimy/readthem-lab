package com.back.domain.book.entity

import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Entity

@Entity
class BookLastFetchedPage : BaseEntity() {
    var number = 0
        protected set

    fun increaseNumber() {
        number++
    }
}
