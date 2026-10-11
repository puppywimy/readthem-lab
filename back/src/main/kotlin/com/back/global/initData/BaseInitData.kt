package com.back.global.initData

import com.back.domain.book.repository.BookRepository
import com.back.domain.book.service.BookService
import com.back.domain.member.service.MemberService
import com.back.domain.review.service.ReviewService
import com.back.domain.wish.service.WishService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Transactional

@Profile("dev", "test")
@Configuration

class BaseInitData(
    private val memberService: MemberService,
    private val bookRepository: BookRepository,
    private val wishService: WishService,
    private val reviewService: ReviewService,
    private val bookService: BookService
){

    @Lazy
    @Autowired
    private lateinit var self: BaseInitData

    @Bean
    fun baseInitDataApplicationRunner(): ApplicationRunner {
        return ApplicationRunner { args: ApplicationArguments ->
            self.work1()
        }
    }

    @Transactional
    fun work1() {
        if (memberService.count() > 0) return

        val memberSystem = memberService.join("system", "1234", null, "시스템", null)
        memberSystem.modifyRefreshToken("system")
        memberSystem.grantAdmin()

        val memberAdmin = memberService.join("admin", "1234", null, "관리자", null)
        memberAdmin.modifyRefreshToken("admin")
        memberAdmin.grantAdmin()

        val memberUser1 = memberService.join("user1", "1234", "githubuser1", null)
        memberUser1.modifyRefreshToken("user1")

        val memberUser2 = memberService.join("user2", "1234", "githubuser2", null)
        memberUser2.modifyRefreshToken("user2")

        val memberUser3 = memberService.join("user3", "1234", "githubuser3", null)
        memberUser3.modifyRefreshToken("user3")

        val book1 = bookRepository.save(
            com.back.domain.book.entity.Book(
                "책제목", "책설명", "isbn1", "작가",
                java.time.LocalDateTime.now(), "출판사", ""
            )
        )
        val book2 = bookRepository.save(
            com.back.domain.book.entity.Book(
                "책제목2", "책설명", "isbn2", "작가",
                java.time.LocalDateTime.now(), "출판사", ""
            )
        )

        val book3 = bookRepository.save(
            com.back.domain.book.entity.Book(
                "책제목3", "책설명", "isbn3", "작가",
                java.time.LocalDateTime.now(), "출판사", ""
            )
        )

        val book4 = bookRepository.save(
            com.back.domain.book.entity.Book(
                "책제목4", "책설명", "isbn4", "작가",
                java.time.LocalDateTime.now(), "출판사", ""
            )
        )

        wishService.createWish(memberUser1, book1.id)
        wishService.createWish(memberUser2, book2.id)
        wishService.createWish(memberUser3, book3.id)

        reviewService.createReview(
            memberUser1,
            book1.id,
            4.0f,
            "comment",
            listOf("a", "b")
        )
        reviewService.createReview(
            memberUser1,
            book2.id,
            3.5f,
            "",
            listOf("Java", "Spring", "신입 개발자")
        )
        reviewService.createReview(
            memberUser1,
            book3.id,
            1.5f,
            "많이 아쉬운 책입니다..",
            listOf("소설", "감자")
        )
        reviewService.createReview(
            memberUser1,
            book4.id,
            4.5f,
            "",
            listOf("소설")
        )

        reviewService.createReview(
            memberUser2,
            book2.id,
            3.5f,
            "",
            listOf("Java", "Spring", "신입 개발자")
        )
        reviewService.createReview(
            memberUser2,
            book3.id,
            1.5f,
            "많이 아쉬운 책입니다..",
            listOf("소설", "감자")
        )
        reviewService.createReview(
            memberUser2,
            book4.id,
            4.5f,
            "",
            listOf("소설")
        )

        reviewService.createReview(
            memberUser3,
            book2.id,
            3.5f,
            "",
            listOf("Java", "Spring", "신입 개발자")
        )
        reviewService.createReview(
            memberUser3,
            book3.id,
            1.5f,
            "많이 아쉬운 책입니다..",
            listOf("소설", "감자")
        )
    }
}