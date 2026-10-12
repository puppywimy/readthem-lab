package com.back.domain.book.repository

import com.back.domain.book.entity.Book
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Types
import java.time.LocalDateTime

@Repository
class BookRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : BookRepositoryCustom {

    override fun saveBulk(books: List<Book>) {
        val sql = """
            insert into book (
                title,
                description,
                isbn,
                authors,
                published_date,
                publisher,
                img_url,
                created_date,
                modified_date
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?);
            """.trimIndent()

        val now = LocalDateTime.now()

        jdbcTemplate.batchUpdate(
            sql,
            books,
            books.size,
        ) { ps, book ->
            ps.setString(1, book.title)
            ps.setString(2, book.description)
            ps.setString(3, book.isbn)
            ps.setString(4, book.authors)

            if (book.publishedDate != null) ps.setObject(5, book.publishedDate)
            else ps.setNull(5, Types.TIMESTAMP)

            ps.setString(6, book.publisher)
            ps.setString(7, book.imgUrl)
            ps.setObject(8, now)
            ps.setObject(9, now)
        }
    }
}