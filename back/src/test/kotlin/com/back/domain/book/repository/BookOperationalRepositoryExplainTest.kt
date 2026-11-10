package com.back.domain.book.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BookOperationalRepositoryExplainTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    @DisplayName("BookOperational 정렬용 인덱스가 생성된다")
    fun t1() {
        val indexNames = jdbcTemplate.queryForList(
            """
                SELECT INDEX_NAME
                FROM INFORMATION_SCHEMA.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'book_operational'
            """.trimIndent(),
            String::class.java
        )

        assertThat(indexNames).contains(
            "idx_book_average_rating_id",
            "idx_book_review_count_id",
            "idx_book_view_count_id"
        )
    }
}
