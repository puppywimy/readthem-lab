package com.back.global.initData

import com.back.domain.book.entity.Book
import com.back.domain.book.entity.BookOperational
import com.back.domain.book.repository.BookOperationalRepository
import com.back.domain.book.repository.BookRepository
import com.back.domain.book.service.BookService
import com.back.domain.member.entity.Member
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
import org.springframework.core.env.Environment
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private class SampleBook(
    val isbn: String, val title: String, val authors: String, val publisher: String, val year: Int,
    val description: String, val rating: Double, val reviewCount: Int, val viewCount: Int
)

private class SampleReview(
    val username: String, val isbn: String, val rating: Float, val likes: Int, val tags: List<String>, val content: String
)

// docs/design/index.html의 샘플 데이터
private val SAMPLE_BOOKS = listOf(
    SampleBook("9781449373320", "데이터 중심 애플리케이션 설계", "마틴 클레프만", "위키북스", 2018, "복제, 파티셔닝, 트랜잭션, 일괄 처리와 스트림 처리를 다루며 데이터 시스템의 트레이드오프를 설명합니다.", 4.8, 312, 18420),
    SampleBook("9780134685991", "이펙티브 자바", "조슈아 블로크", "인사이트", 2018, "자바 언어를 제대로 쓰기 위한 90개 아이템을 규칙과 근거로 정리한 책입니다.", 4.7, 287, 15873),
    SampleBook("9780135957059", "실용주의 프로그래머", "데이비드 토머스 외", "인사이트", 2022, "도구, 설계, 협업까지 개발자의 일하는 방식을 실용적인 조언으로 풀어냅니다.", 4.6, 241, 13208),
    SampleBook("9780134757599", "리팩터링 2판", "마틴 파울러", "한빛미디어", 2020, "동작을 유지하면서 코드 구조를 개선하는 절차를 예제와 카탈로그로 보여 줍니다.", 4.5, 198, 12761),
    SampleBook("9780132350884", "클린 코드", "로버트 C. 마틴", "인사이트", 2013, "이름, 함수, 주석, 테스트까지 읽기 좋은 코드의 기준을 사례와 함께 다룹니다.", 4.1, 406, 12094),
    SampleBook("9780321125217", "도메인 주도 설계", "에릭 에반스", "위키북스", 2011, "복잡한 도메인을 모델로 표현하고 코드와 일치시키는 방법을 설명합니다.", 4.3, 134, 9317),
    SampleBook("9781732102200", "A Philosophy of Software Design", "존 오스터하우트", "Yaknyam Press", 2021, "복잡성을 줄이는 설계 원칙을 깊은 모듈이라는 개념을 중심으로 정리합니다.", 4.9, 76, 6240),
    SampleBook("9780596007126", "헤드 퍼스트 디자인 패턴", "에릭 프리먼 외", "한빛미디어", 2022, "자주 쓰는 디자인 패턴을 그림과 대화체 예제로 익히는 입문서입니다.", 4.4, 223, 8955),
    SampleBook("9780735619678", "코드 컴플리트 2판", "스티브 맥코넬", "위키북스", 2020, "변수 이름부터 구조 설계까지 소프트웨어 구현 전반의 실무 지식을 모았습니다.", 4.5, 91, 5482),
    SampleBook("9781736417911", "Staff Engineer", "윌 라슨", "Staff Eng", 2021, "시니어 이후의 기술 리더가 어떤 일을 하고 어떻게 영향력을 만드는지 다룹니다.", 4.6, 58, 4176),
    SampleBook("9780201835953", "맨먼스 미신", "프레드릭 브룩스", "인사이트", 2015, "대규모 소프트웨어 프로젝트의 일정과 조직 문제를 다룬 고전입니다.", 4.4, 83, 3890),
    SampleBook("9780134092669", "컴퓨터 시스템", "랜달 브라이언트 외", "피어슨", 2015, "프로그래머 관점에서 메모리, 프로세스, 네트워크가 동작하는 원리를 설명합니다.", 4.8, 112, 7023),
    SampleBook("9780262510875", "컴퓨터 프로그램의 구조와 해석", "해럴드 에이블슨 외", "인사이트", 2016, "프로그램을 추상화하고 조합하는 사고방식을 Scheme으로 배웁니다.", 4.7, 64, 3312),
    SampleBook("9780134494166", "클린 아키텍처", "로버트 C. 마틴", "인사이트", 2019, "의존성 규칙을 중심으로 시스템 경계를 나누는 아키텍처 원칙을 설명합니다.", 4.2, 176, 9640),
    SampleBook("9781680502398", "Release It! 2판", "마이클 나이가드", "위키북스", 2018, "운영 환경에서 시스템이 무너지는 패턴과 이를 막는 안정성 패턴을 정리합니다.", 4.6, 47, 2765),
)

private const val DDIA = "9781449373320"
private const val CLEAN_ARCHITECTURE = "9780134494166"

private val SAMPLE_REVIEWS = listOf(
    SampleReview("haneul-dev", DDIA, 4.5f, 96, listOf("실무 추천", "다시 읽는 책"), "한 번 읽고 끝낼 책은 아니에요. 팀에서 같은 문제가 생길 때마다 관련 장을 다시 펴 보고 있습니다."),
    SampleReview("seojun-404", DDIA, 5f, 27, listOf("실무 추천"), "신규 입사자에게 가장 먼저 권하는 책이 됐습니다. 예제가 지금 봐도 낡지 않았어요."),
    SampleReview("gyuri-kang", DDIA, 4f, 41, listOf("스터디 추천"), "혼자 읽으면 지루한 구간이 있지만 스터디로 한 장씩 토론하니 얻는 게 많았습니다."),
    SampleReview("dohyun-k", DDIA, 3.5f, 8, listOf("입문서 아님"), "경력 1년 차에게는 조금 일렀습니다. 실무에서 비슷한 문제를 겪어 본 뒤에 읽으면 좋겠어요."),
    SampleReview("dohyun-k", CLEAN_ARCHITECTURE, 3f, 2, emptyList(), "기대보다 사례가 적었습니다."),
    SampleReview("mun-seok", "9780134757599", 4.5f, 33, emptyList(), "테스트부터 만드는 첫 장 예제를 따라 하고 나니 작은 단위로 고치는 감각이 생겼습니다."),
    SampleReview("mun-seok", CLEAN_ARCHITECTURE, 4f, 19, emptyList(), "경계를 어디에 그을지 고민할 때 좋은 기준을 줍니다. 후반부 사례는 조금 오래된 느낌이에요."),
    SampleReview("yoonseul", "9781732102200", 4.5f, 128, listOf("실무 추천"), "얇아서 주말이면 끝납니다. 깊은 모듈이라는 개념 하나로 지난주에 쪼갠 클래스를 다시 합쳤어요."),
    SampleReview("byte-bori", "9780134685991", 5f, 167, listOf("실무 추천"), "순서대로 읽지 않아도 됩니다. 코드 리뷰에서 같은 지적을 세 번 받았다면 그 아이템부터 펴 보세요."),
    SampleReview("mun-seok", DDIA, 5f, 214, listOf("실무 추천"), "복제와 파티셔닝을 안다고 생각했는데, 장애 상황을 설명해 보라는 질문에 막혔어요. 이 책을 읽고 나서야 트레이드오프를 제 말로 설명할 수 있게 됐습니다."),
)

private val SAMPLE_WISH_ISBNS = listOf("9780321125217", "9781736417911", "9780262510875", "9781680502398")
private val SAMPLE_LIKED_ISBNS = listOf("9780134685991", "9781732102200")

@Profile("dev", "test")
@Configuration

class BaseInitData(
    private val memberService: MemberService,
    private val bookRepository: BookRepository,
    private val bookOperationalRepository: BookOperationalRepository,
    private val wishService: WishService,
    private val reviewService: ReviewService,
    private val bookService: BookService,
    private val environment: Environment
){

    @Lazy
    @Autowired
    private lateinit var self: BaseInitData

    @Bean
    fun baseInitDataApplicationRunner(): ApplicationRunner {
        return ApplicationRunner { args: ApplicationArguments ->
            // 테스트는 work1의 데이터에 의존한다.
            if (environment.matchesProfiles("test")) self.work1() else self.work2()
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

    @Transactional
    fun work2() {
        if (memberService.count() > 0) return

        fun join(username: String, nickname: String, githubId: String? = username): Member =
            memberService.join(username, "1234", githubId, nickname, null)
                .also { it.modifyRefreshToken(username) }

        join("system", "시스템", null).grantAdmin()
        join("admin", "관리자", null).grantAdmin()
        join("readthem-admin", "운영자").grantAdmin()

        val members = listOf(
            "mun-seok" to "문석", "byte-bori" to "보리", "yoonseul" to "윤슬", "dohyun-k" to "도현",
            "gyuri-kang" to "규리", "haneul-dev" to "haneul-dev", "seojun-404" to "seojun-404"
        ).associate { (username, nickname) -> username to join(username, nickname) }

        val books = SAMPLE_BOOKS.associate {
            it.isbn to bookRepository.save(
                Book(
                    it.title, it.description, it.isbn, it.authors,
                    LocalDateTime.of(it.year, 1, 1, 0, 0), it.publisher,
                    "https://covers.openlibrary.org/b/isbn/${it.isbn}-L.jpg"
                )
            )
        }

        val reviewIds = SAMPLE_REVIEWS.associate {
            (it.isbn to it.username) to reviewService.createReview(
                members.getValue(it.username), books.getValue(it.isbn).id, it.rating, it.content, it.tags
            ).id
        }

        val munSeok = members.getValue("mun-seok")
        SAMPLE_WISH_ISBNS.forEach { wishService.createWish(munSeok, books.getValue(it).id) }

        reviewService.createReviewLike(munSeok, reviewIds.getValue(SAMPLE_LIKED_ISBNS[0] to "byte-bori"))
        reviewService.createReviewLike(munSeok, reviewIds.getValue(SAMPLE_LIKED_ISBNS[1] to "yoonseul"))

        // 시안의 숫자는 실제 리뷰·좋아요 수보다 커서, 집계 값을 직접 맞춘다.
        SAMPLE_REVIEWS.forEach {
            reviewService.getReview(reviewIds.getValue(it.isbn to it.username)).likeCount = it.likes
        }
        SAMPLE_BOOKS.forEach {
            val operational = bookOperationalRepository.findByIsbn(it.isbn)
                ?: bookOperationalRepository.save(BookOperational(it.isbn))
            operational.updateRating(it.rating, it.reviewCount)
            operational.viewCount = it.viewCount
        }

        memberService.deleteMember(members.getValue("dohyun-k").id)
    }
}