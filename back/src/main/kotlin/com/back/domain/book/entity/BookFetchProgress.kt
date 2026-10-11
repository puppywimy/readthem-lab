package com.back.domain.book.entity

import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Entity
import java.time.LocalDate

@Entity
class BookFetchProgress : BaseEntity() {

    var currentPage = 1
    var currentApiKeyIndex = 0

    var dailyCallCount = 0
    var lastCallDate: LocalDate = LocalDate.now()

    fun nextPage() {
        this.currentPage++
    }

    fun nextApiKeyIndex() {
        this.currentApiKeyIndex++
        this.dailyCallCount = 0
    }

    fun incrementCallCount() {
        val today = LocalDate.now()
        if (!today.isEqual(this.lastCallDate)) {
            // 자정이 지나 다음 날이 되면 모든 상태 초기화
            this.lastCallDate = today
            this.dailyCallCount = 0
            this.currentApiKeyIndex = 0
        }
        this.dailyCallCount++
    }
}
