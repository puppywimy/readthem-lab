package com.back.domain.book.service

import com.back.domain.book.dto.BookDto
import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.member.entity.Member
import com.back.domain.review.entity.Review
import com.back.domain.review.repository.ReviewRepository
import com.back.standard.recommend.byRating.SimilarityRecommendByRating
import com.back.standard.recommend.byRating.SimilarityRecommendByRating.Rating
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookRecommendService(
    private val reviewRepository: ReviewRepository,
    private val bookRepository: BookRepository,
    private val bookOperationalRepository: BookOperationalRepository
) {

    private fun reviewToRecommendReview(review: Review): Rating<Long, String> {
        return Rating(
            review.reviewer.id,
            review.book.isbn,
            review.rating
        )
    }

    private fun recommendReviewsByReviewer(reviewer: Member): List<Rating<Long, String>> {
        return reviewRepository.findByReviewer(reviewer, PageRequest.of(0, 5))
            .toList()
            .map { review: Review -> this.reviewToRecommendReview(review) }
    }

    fun getBooksByRecommend(actor: Member?, maxCount: Int): List<BookDto> {
        if (actor == null) return listOf()

        val recommendSystem = SimilarityRecommendByRating<Long, String>()

        val recentReviews: List<Review> = reviewRepository
            .findByReviewer(
                actor,
                PageRequest.of(0, 5)
            )
            .toList()

        recommendSystem.setData(
            recommendReviewsByReviewer(actor)
        )

        val members: MutableSet<Member> = mutableSetOf()

        for (review in recentReviews) {
            reviewRepository
                .findByBook(review.book, PageRequest.of(0, 10))
                .forEach { r -> members.add(r.reviewer) }
        }

        for (reviewer in members) {
            recommendSystem.setData(
                recommendReviewsByReviewer(reviewer)
            )
        }

        val recommendedIsbns = recommendSystem.getRecommendList(actor.id, 5, maxCount)
        val booksByIsbn = bookRepository.findByIsbnIn(recommendedIsbns)
            .associateBy { it.isbn }
        val ratingsByIsbn = bookOperationalRepository.findByIsbnIn(recommendedIsbns)
            .associate { it.isbn to it.averageRating }

        return recommendedIsbns.mapNotNull { isbn ->
            booksByIsbn[isbn]?.let { BookDto(it, ratingsByIsbn[isbn] ?: 0.0) }
        }
    }
}
