package com.back.domain.review.entity

import com.back.domain.book.entity.Book
import com.back.domain.member.entity.Member
import com.back.domain.tag.entity.Tag
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.BatchSize

@Entity
@Table(
    indexes = [Index(name = "idx_like_count_id", columnList = "like_count DESC, id DESC")]
)
class Review(
    @field:JoinColumn(name = "book_id")
    @field:ManyToOne
    val book: Book,

    @field:JoinColumn(name = "reviewer_id")
    @field:ManyToOne
    val reviewer: Member,

    var rating: Float,

    @field:Column(length = 500)
    var content: String,

    tags: MutableList<Tag>
) : BaseEntity() {
    @OneToMany(mappedBy = "review", cascade = [CascadeType.ALL], orphanRemoval = true)
    @BatchSize(size = 100)
    private val reviewTags: MutableList<ReviewTag> = mutableListOf()

    var likeCount: Int = 0

    init {
        for (i in tags.indices) {
            this.reviewTags.add(ReviewTag(this, tags[i]))
        }
    }

    val tags: MutableList<String>
        get() = reviewTags.stream()
            .map { tag: ReviewTag -> tag.tag.name }
            .toList()

    fun modify(rating: Float, content: String, tags: MutableList<Tag>) {
        this.rating = rating
        this.content = content

        this.reviewTags.clear()
        for (i in tags.indices) {
            this.reviewTags.add(ReviewTag(this, tags[i]))
        }
    }
}
