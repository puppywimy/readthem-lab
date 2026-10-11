package com.back.domain.widget.service

import com.back.domain.member.repository.MemberRepository
import com.back.domain.review.repository.ReviewRepository
import com.back.domain.widget.dto.WidgetBookDto
import com.back.domain.widget.dto.WidgetInformationDto
import com.back.domain.wish.repository.WishRepository
import com.back.global.exception.ServiceException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WidgetInformationService(
    private val memberRepository: MemberRepository,
    private val reviewRepository: ReviewRepository,
    private val wishRepository: WishRepository,
) {

    private companion object {
        const val VISIBLE_BOOK_MAX_COUNT = 5
    }

    fun getWidgetInformation(githubId: String) : WidgetInformationDto {

        val member = memberRepository.findByGithubId(githubId)
            ?: throw ServiceException("404-1", "존재하지 않는 회원입니다.")

        return WidgetInformationDto(
            reviewRepository.findByReviewer(
                member,
            PageRequest.of(
                0,
                VISIBLE_BOOK_MAX_COUNT))
                .toList()
                .map { book ->
                    WidgetBookDto(
                        book.book.title,
                        book.content.isNotBlank())
                     },
            reviewRepository.countByReviewer(member),
            reviewRepository.countByReviewerAndContentNot(member, ""),
            wishRepository.countByMember(member)
        )

    }
}