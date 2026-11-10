"use client";

import Link from "next/link";

import { useEffect, useRef, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import type { components } from "@/lib/backend/apiV1/schema";
import { timeAgo } from "@/lib/formatDate";
import { useToast } from "@/lib/toast/ToastProvider";

import BookCover from "@/app/_components/BookCover";
import BookTrack from "@/app/_components/BookTrack";
import CoverWall from "@/app/_components/CoverWall";
import LikeButton from "@/app/_components/LikeButton";
import LoginRequiredModal, {
  GitHubLoginButton,
} from "@/app/_components/LoginRequiredModal";
import RatingValue from "@/app/_components/RatingValue";
import SearchForm from "@/app/_components/SearchForm";
import SvgIcon from "@/app/_components/SvgIcon";

type BookDto = components["schemas"]["BookDto"];
type ReviewDto = components["schemas"]["ReviewDto"] & {
  bookImgUrl?: string | null;
};

const RANK_TABS = [
  { label: "인기 도서", type: "views" },
  { label: "평점 좋은 도서", type: "rating" },
  { label: "리뷰 많은 도서", type: "reviewCount" },
] as const;
type RankType = (typeof RANK_TABS)[number]["type"];

const WALL_SIZE = 15;
const RANK_SIZE = 6;

const bookHref = (id?: number) => `/books/detail?id=${id}`;

function extractListData<T>(data: unknown): T[] {
  if (Array.isArray(data)) return data;

  if (
    data != null &&
    typeof data === "object" &&
    "content" in data &&
    Array.isArray(data.content)
  ) {
    return data.content as T[];
  }

  return [];
}

function fetchList<T>(url: string) {
  return apiFetch(url)
    .then((data) => extractListData<T>(data))
    .catch(() => [] as T[]);
}

function RankPanel({ books }: { books: BookDto[] | undefined }) {
  if (books == null) {
    return (
      <div className="panel" aria-busy="true">
        <span className="sk cover-sk" style={{ maxWidth: 220 }} />
        <div>
          {Array.from({ length: 5 }).map((_, index) => (
            <span key={index} className="sk line" style={{ height: 64 }} />
          ))}
        </div>
      </div>
    );
  }

  if (books.length === 0) {
    return (
      <div className="state">
        <strong>표시할 순위가 없습니다.</strong>
      </div>
    );
  }

  const [first, ...rest] = books;

  return (
    <div className="panel">
      <Link className="top" href={bookHref(first.id)}>
        <BookCover
          bookId={first.id}
          imgUrl={first.imgUrl}
          title={first.title}
        />
        <div>
          <div className="num">1</div>
          <h3>{first.title}</h3>
          <p style={{ marginTop: 8 }}>
            <RatingValue rating={first.averageRating} />
          </p>
        </div>
      </Link>
      <ol className="rows" start={2}>
        {rest.map((book, index) => (
          <li key={book.id}>
            <Link href={bookHref(book.id)}>
              <b>{index + 2}</b>
              <BookCover
                bookId={book.id}
                imgUrl={book.imgUrl}
                title={book.title}
              />
              <div>
                <div className="title">{book.title}</div>
              </div>
              <RatingValue rating={book.averageRating} />
            </Link>
          </li>
        ))}
      </ol>
    </div>
  );
}

export default function Page() {
  const { isLogin } = useAuth();
  const { showErrorToast } = useToast();
  const [ranks, setRanks] = useState<Partial<Record<RankType, BookDto[]>>>({});
  const [wallBooks, setWallBooks] = useState<BookDto[]>([]);
  const [rankType, setRankType] = useState<RankType>("views");
  const [popularReviews, setPopularReviews] = useState<ReviewDto[] | null>(
    null,
  );
  const [latestReviews, setLatestReviews] = useState<ReviewDto[] | null>(null);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const pageRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    fetchList<BookDto>(
      `/api/v1/books/rank?type=views&page=0&size=${WALL_SIZE}`,
    ).then((books) => {
      setWallBooks(books);
      setRanks((current) => ({
        ...current,
        views: books.slice(0, RANK_SIZE),
      }));
    });

    (["rating", "reviewCount"] as const).forEach((type) => {
      fetchList<BookDto>(
        `/api/v1/books/rank?type=${type}&page=0&size=${RANK_SIZE}`,
      ).then((books) => setRanks((current) => ({ ...current, [type]: books })));
    });

    fetchList<ReviewDto>(`/api/v1/reviews/likeCount?page=0&size=3`).then(
      setPopularReviews,
    );

    fetchList<ReviewDto>(`/api/v1/reviews/latest?page=0&size=20`).then(
      (reviews) => {
        const seenBookIds = new Set<number | undefined>();

        setLatestReviews(
          reviews
            .filter((review) => {
              if (seenBookIds.has(review.bookId)) return false;
              seenBookIds.add(review.bookId);
              return true;
            })
            .slice(0, 8),
        );
      },
    );
  }, []);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return;
          entry.target.classList.add("in");
          observer.unobserve(entry.target);
        });
      },
      { threshold: 0.12 },
    );

    pageRef.current
      ?.querySelectorAll(".reveal")
      .forEach((element) => observer.observe(element));

    return () => observer.disconnect();
  }, []);

  const handleToggleLike = (review: ReviewDto) => {
    if (!isLogin) {
      setShowLoginModal(true);
      return;
    }

    const liked = review.likedByMe ?? false;

    apiFetch(`/api/v1/reviews/${review.id}/like`, {
      method: liked ? "DELETE" : "POST",
    })
      .then(() => {
        setPopularReviews((current) =>
          (current ?? []).map((item) =>
            item.id === review.id
              ? {
                  ...item,
                  likedByMe: !liked,
                  likeCount: (item.likeCount ?? 0) + (liked ? -1 : 1),
                }
              : item,
          ),
        );
      })
      .catch(showErrorToast);
  };

  return (
    <div ref={pageRef}>
      {showLoginModal && (
        <LoginRequiredModal onCancel={() => setShowLoginModal(false)} />
      )}

      <div className="hero">
        <div className="hero-text">
          <h1>
            개발자의
            <br />
            도서 리뷰 플랫폼
          </h1>
          <p className="lede">
            개발자들의{" "}
            <Link className="trend-link" href="/#home-rank">
              독서 트렌드
              <SvgIcon name="arrow-up-right" className="i h-3" />
            </Link>
            를 확인하고 찜하세요.
            <br />
            나만의 찜 목록을 위젯으로 만들어보세요.
          </p>
          <SearchForm id="home-q" />
        </div>
        <CoverWall books={wallBooks} />
      </div>

      <section id="home-rank" className="wrap">
        <h2 className="sr">독서 트렌드</h2>
        <div className="tabs" role="tablist">
          {RANK_TABS.map(({ label, type }) => (
            <button
              key={type}
              type="button"
              className="tab"
              role="tab"
              aria-selected={type === rankType}
              onClick={() => setRankType(type)}
            >
              {label}
            </button>
          ))}
        </div>
        <div role="tabpanel">
          <RankPanel key={rankType} books={ranks[rankType]} />
        </div>
      </section>

      <section id="home-reviews" className="wrap reveal">
        <h2>인기 많은 리뷰</h2>
        {popularReviews?.length === 0 ? (
          <div className="state">
            <strong>표시할 리뷰가 없습니다.</strong>
          </div>
        ) : (
          <div className="reviews" aria-busy={popularReviews == null}>
            {(popularReviews ?? []).map((review) => (
              <article className="rv" key={review.id}>
                <q>{review.content}</q>
                <div className="rv-foot">
                  <Link
                    href={bookHref(review.bookId)}
                    aria-label={`${review.bookTitle} 상세 보기`}
                    className="contents"
                  >
                    <BookCover
                      bookId={review.bookId}
                      imgUrl={review.bookImgUrl}
                      title={review.bookTitle}
                    />
                  </Link>
                  <div>
                    <strong>{review.bookTitle}</strong>
                    <small>
                      {review.reviewer?.githubId ?? "탈퇴한 사용자"}
                      <RatingValue rating={review.rating} inline />
                    </small>
                  </div>
                  <LikeButton
                    count={review.likeCount}
                    liked={review.likedByMe ?? false}
                    onClick={() => handleToggleLike(review)}
                  />
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
      <section className="wrap reveal">
        {latestReviews?.length === 0 ? (
          <>
            <h2>최근 리뷰가 추가된 도서</h2>
            <div className="state">
              <strong>표시할 리뷰가 없습니다.</strong>
            </div>
          </>
        ) : (
          <BookTrack title="최근 리뷰가 추가된 도서">
            {(latestReviews ?? []).map((review) => (
              <Link key={review.id} href={bookHref(review.bookId)}>
                <BookCover
                  bookId={review.bookId}
                  imgUrl={review.bookImgUrl}
                  title={review.bookTitle}
                />
                <h3>{review.bookTitle}</h3>
                <p className="meta">{timeAgo(review.createdDate)} 리뷰</p>
              </Link>
            ))}
          </BookTrack>
        )}
      </section>

      <section className="reveal widget-section">
        <div className="wrap widget">
          <div>
            <h2>GitHub 프로필에 붙이는 나만의 책장</h2>
            <p className="lede">
              코드 한 줄을 README에 붙이면 프로필에 내 책장이 표시됩니다.
            </p>
            {isLogin ? (
              <Link className="btn" href="/mypage">
                내 서재로 이동
              </Link>
            ) : (
              <GitHubLoginButton>GitHub로 3초 만에 시작하기</GitHubLoginButton>
            )}
          </div>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src="/widget-sample.svg"
            alt="리뷰를 쓰거나 보고 싶어요한 책의 표지가 흐르는 위젯 예시"
          />
        </div>
      </section>
    </div>
  );
}
