package com.back.domain.review.repository

import com.back.domain.member.repository.MemberRepository
import com.back.domain.review.entity.ReviewLike
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ReviewLikeRepositoryTest {
    @Autowired
    private lateinit var reviewLikeRepository: ReviewLikeRepository

    @Autowired
    private lateinit var reviewRepository: ReviewRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    @DisplayName("동일한 리뷰-회원 조합으로 좋아요를 두 번 저장하면 유니크 제약조건 위반이 즉시 발생한다")
    fun t1() {
        val review = reviewRepository.findByIdOrNull(1L)!!
        val member = memberRepository.findByIdOrNull(2L)!!

        reviewLikeRepository.save(ReviewLike(review, member))

        assertThrows<DataIntegrityViolationException> {
            reviewLikeRepository.save(ReviewLike(review, member))
        }
    }
}
