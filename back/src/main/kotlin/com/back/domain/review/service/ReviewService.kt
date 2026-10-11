package com.back.domain.review.service

import com.back.domain.book.entity.Book
import com.back.domain.book.entity.BookOperational
import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.member.entity.Member
import com.back.domain.member.repository.MemberRepository
import com.back.domain.review.dto.AdminReviewDto
import com.back.domain.review.dto.ReviewDto
import com.back.domain.review.dto.ReviewWithBookImgUrlDto
import com.back.domain.review.dto.ReviewsByMemberDto
import com.back.domain.review.entity.Review
import com.back.domain.review.entity.ReviewLike
import com.back.domain.review.repository.ReviewLikeRepository
import com.back.domain.review.repository.ReviewRepository
import com.back.domain.tag.entity.Tag
import com.back.domain.tag.repository.TagRepository
import com.back.global.exception.ServiceException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.roundToInt

@Service
@Transactional(readOnly = true)
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val reviewLikeRepository: ReviewLikeRepository,
    private val bookRepository: BookRepository,
    private val bookOperationalRepository: BookOperationalRepository,
    private val memberRepository: MemberRepository,
    private val tagRepository: TagRepository
) {

    private fun getBookById(bookId: Long): Book =
        bookRepository.findByIdOrNull(bookId)
            ?: throw ServiceException("404-1", "존재하지 않는 도서입니다.")

    private fun getMemberById(memberId: Long): Member =
        memberRepository.findByIdOrNull(memberId)
            ?: throw ServiceException("404-1", "존재하지 않는 회원입니다.")

    fun getReview(reviewId: Long): Review =
        reviewRepository.findByIdOrNull(reviewId)
            ?: throw ServiceException("404-1", "존재하지 않는 리뷰입니다.")

    private fun getOrCreateTag(tagName: String): Tag =
        tagRepository.findByName(tagName) ?: tagRepository.save(Tag(tagName))

    private fun refreshBookRating(book: Book) {
        val averageRating = reviewRepository.getAverageRatingByBook(book)
        val reviewCount = reviewRepository.countByBook(book)

        val bookOperational =
            bookOperationalRepository.findByIsbn(book.isbn)
                ?: bookOperationalRepository.save(BookOperational(book.isbn))

        bookOperational.updateRating(averageRating, reviewCount)
    }

    private fun getRatingMap(member: Member): Map<String, Any> {
        val ratings = mutableMapOf<String, Any>()

        ratings["average"] = (reviewRepository.getAverageRatingByMember(member) * 10.0f).roundToInt() / 10.0f

        for (i in 0..9) {
            val targetRating = (i + 1) * 0.5f
            ratings["%.1f".format(targetRating)] = reviewRepository.countByReviewerAndRating(member, targetRating)
        }

        return ratings
    }

    fun getReviewsOrderByLatest(page: Int, size: Int)
        = reviewRepository
            .findAllByOrderByIdDesc(
                PageRequest.of(page, size))
            .toList()
            .map { ReviewWithBookImgUrlDto(it) }

    fun getReviewsOrderByLikeCount(page: Int, size: Int)
            = reviewRepository
                .findAllByOrderByLikeCountDescIdDesc(
                    PageRequest.of(page, size))
                .toList()
                .map { ReviewWithBookImgUrlDto(it) }

    fun getReviewsByBookId(bookId: Long, actor: Member?): List<ReviewDto> {
        val reviews = reviewRepository.findByBook(getBookById(bookId))

        val likedReviewIds =
            if (actor != null && reviews.isNotEmpty())
                reviewLikeRepository.findReviewIdsByMemberAndReviewIn(actor, reviews).toSet()
            else emptySet()

        return reviews.map { ReviewDto(it, it.id in likedReviewIds) }
    }

    fun getReviewsWithMyLike(memberId: Long): List<ReviewWithBookImgUrlDto> {
        return reviewRepository.findLikedReviewsByMember(getMemberById(memberId))
            .map { ReviewWithBookImgUrlDto(it) }
    }

    fun getReviewsByMemberId(memberId: Long): ReviewsByMemberDto {
        val member = getMemberById(memberId)

        return ReviewsByMemberDto(
            getRatingMap(member),
            reviewRepository.findByReviewer(member).map { ReviewWithBookImgUrlDto(it) }
        )
    }

    fun getReviews(page: Int, size: Int): Page<AdminReviewDto> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        return reviewRepository.findAll(pageable).map { AdminReviewDto(it) }
    }

    fun getLatestReview(): Review? = reviewRepository.findFirstByOrderByIdDesc()

    @Transactional
    fun createReview(
        actor: Member, bookId: Long,
        rating: Float, content: String, tags: List<String>
    ): ReviewDto {
        val book = getBookById(bookId)

        if (reviewRepository.findFirstByBookAndReviewer(book, actor) != null)
            throw ServiceException("409-1", "이미 존재하는 리뷰입니다.")

        val review = reviewRepository.save(
            Review(book, actor, rating, content, tags.map { getOrCreateTag(it) }.toMutableList())
        )

        refreshBookRating(book)

        return ReviewDto(review)
    }

    @Transactional
    fun updateReview(
        actor: Member, reviewId: Long,
        rating: Float, content: String, tags: List<String>
    ): ReviewDto {
        val review = getReview(reviewId)

        if (review.reviewer != actor) {
            throw ServiceException("403-1", "리뷰 수정 권한이 없습니다.")
        }

        review.modify(rating, content, tags.map { getOrCreateTag(it) }.toMutableList())

        refreshBookRating(review.book)

        return ReviewDto(review)
    }

    @Transactional
    fun deleteReview(actor: Member, reviewId: Long) {
        val review = getReview(reviewId)

        if (review.reviewer != actor && !actor.isAdmin) {
            throw ServiceException("403-1", "리뷰 삭제 권한이 없습니다.")
        }

        reviewLikeRepository.deleteAllByReview(review)

        val book = review.book
        reviewRepository.delete(review)

        refreshBookRating(book)
    }

    @Transactional
    fun createReviewLike(actor: Member, reviewId: Long) {
        val review = getReview(reviewId)

        if (reviewLikeRepository.existsByReviewAndMember(review, actor))
            throw ServiceException("409-1", "이미 존재하는 좋아요입니다.")

        try {
            reviewLikeRepository.save(ReviewLike(review, actor))
        } catch (e: DataIntegrityViolationException) {
            throw ServiceException("409-1", "이미 존재하는 좋아요입니다.")
        }

        reviewRepository.increaseLikeCount(reviewId)
    }

    @Transactional
    fun deleteReviewLike(actor: Member, reviewId: Long) {
        val review = getReview(reviewId)

        if (reviewLikeRepository.deleteByReviewAndMember(review, actor) == 0)
            throw ServiceException("404-1", "존재하지 않는 좋아요입니다.")

        reviewRepository.decreaseLikeCount(reviewId)
    }
}
