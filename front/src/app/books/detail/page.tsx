"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";

import { Suspense, useCallback, useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { useToast } from "@/lib/toast/ToastProvider";

import BookCover from "@/app/_components/BookCover";
import BookTrack from "@/app/_components/BookTrack";
import LoginRequiredModal from "@/app/_components/LoginRequiredModal";
import RatingHistogram from "@/app/_components/RatingHistogram";
import RatingValue from "@/app/_components/RatingValue";
import ReviewDetailCard, { TagList } from "@/app/_components/ReviewDetailCard";
import ReviewFormModal from "@/app/_components/ReviewFormModal";
import SvgIcon from "@/app/_components/SvgIcon";

type BookDetailDto = components["schemas"]["BookDetailDto"];
type BookDetailWithWishId = BookDetailDto & {
  isWished?: boolean;
  wished?: boolean;
  wishId?: number | null;
};
type BookDto = components["schemas"]["BookDto"];
type ReviewDto = components["schemas"]["ReviewDto"];

type ReviewSort = "latest" | "popular";

function BookDetailSkeleton() {
  return (
    <div className="band">
      <div className="wrap book-hero" aria-busy="true">
        <span className="sk cover-sk" />
        <div>
          <span
            className="sk line"
            style={{ height: "3rem", maxWidth: "70%" }}
          />
          <span className="sk line" style={{ maxWidth: "40%" }} />
          <span className="sk line" />
          <span className="sk line" style={{ maxWidth: "85%" }} />
        </div>
      </div>
    </div>
  );
}

function BookDetail() {
  const searchParams = useSearchParams();
  const id = searchParams.get("id");
  const { loginMember, isLogin } = useAuth();
  const { showToast, showErrorToast, confirmAction } = useToast();

  const [book, setBook] = useState<BookDetailWithWishId | null>(null);
  const [reviews, setReviews] = useState<ReviewDto[] | null>(null);
  const [recommendBooks, setRecommendBooks] = useState<BookDto[] | null>(null);
  const [editingReviewId, setEditingReviewId] = useState<number | null>(null);
  const [likedReviewIds, setLikedReviewIds] = useState<Set<number>>(new Set());
  const [showWriteForm, setShowWriteForm] = useState(false);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [reviewSort, setReviewSort] = useState<ReviewSort>("latest");

  const loadBook = useCallback(() => {
    if (id == null) return;

    apiFetch(`/api/v1/books/${id}`)
      .then((data) => {
        setBook(data);
      })
      .catch(goToErrorPage);
  }, [id]);

  const loadReviews = useCallback(() => {
    if (id == null) return;

    apiFetch(`/api/v1/reviews/book/${id}`)
      .then((data: ReviewDto[]) => {
        setReviews(data);
        setLikedReviewIds(
          new Set(
            data
              .filter((review) => review.likedByMe && review.id != null)
              .map((review) => review.id as number),
          ),
        );
      })
      .catch(goToErrorPage);
  }, [id]);

  useEffect(() => {
    if (id == null) {
      goToErrorPage({ message: "책 ID가 없습니다." });
      return;
    }

    loadBook();
    loadReviews();
  }, [id, loadBook, loadReviews]);

  useEffect(() => {
    if (!isLogin) return;

    apiFetch("/api/v1/books/recommend")
      .then((data) => {
        setRecommendBooks(data);
      })
      .catch(goToErrorPage);
  }, [isLogin]);

  const extractReviewFields = (form: HTMLFormElement) => {
    const formData = new FormData(form);
    const ratingValue = String(formData.get("rating") ?? "").trim();
    const contentValue = String(formData.get("content") ?? "").trim();
    const tagsValue = String(formData.get("tags") ?? "");

    /*
    if (contentValue.length < 2) {
      alert("리뷰 내용을 2자 이상 입력해주세요.");
      return null;
    }
    */

    if (contentValue.length > 500) {
      showToast("리뷰 내용은 500자 이하로 입력해주세요.");
      return null;
    }

    const tags = tagsValue
      .split(",")
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);

    const rating = ratingValue === "" ? undefined : Number(ratingValue);

    return {
      content: contentValue,
      tags,
      ...(rating != null ? { rating } : {}),
    };
  };

  const handleWriteSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (id == null) return;

    const form = e.currentTarget;
    const body = extractReviewFields(form);
    if (body == null) return;

    apiFetch(`/api/v1/reviews/book/${id}`, {
      method: "POST",
      body: JSON.stringify(body),
    })
      .then((data) => {
        showToast(data?.message ?? "리뷰를 작성했습니다.");
        form.reset();
        setShowWriteForm(false);
        loadReviews();
        loadBook();
      })
      .catch(showErrorToast);
  };

  const handleEditSubmit = (
    e: React.SyntheticEvent<HTMLFormElement>,
    reviewId: number,
  ) => {
    e.preventDefault();

    const form = e.currentTarget;
    const body = extractReviewFields(form);
    if (body == null) return;

    apiFetch(`/api/v1/reviews/${reviewId}`, {
      method: "PUT",
      body: JSON.stringify(body),
    })
      .then((data) => {
        showToast(data?.message ?? "리뷰를 수정했습니다.");
        setEditingReviewId(null);
        loadReviews();
        loadBook();
      })
      .catch(showErrorToast);
  };

  const handleToggleWish = () => {
    if (book == null || id == null) return;

    const wishId = book.wishId;

    if (wishId != null) {
      apiFetch(`/api/v1/wishes/${wishId}`, { method: "DELETE" })
        .then((data) => {
          showToast(data?.message ?? "보고 싶어요를 취소했습니다.");
          setBook((currentBook) =>
            currentBook == null
              ? currentBook
              : { ...currentBook, wishId: null },
          );
          loadBook();
        })
        .catch(showErrorToast);
      return;
    }

    apiFetch(`/api/v1/wishes/book/${id}`, { method: "POST" })
      .then((data) => {
        showToast(data?.message ?? "보고 싶어요에 추가했습니다.");
        const createdWishId = data?.data?.id;

        if (typeof createdWishId === "number") {
          setBook((currentBook) =>
            currentBook == null
              ? currentBook
              : { ...currentBook, wishId: createdWishId },
          );
        }

        loadBook();
      })
      .catch(showErrorToast);
  };

  const openLoginModal = () => {
    setShowLoginModal(true);
  };

  const handleOpenReviewAction = () => {
    if (isLogin) {
      setShowWriteForm(true);
      return;
    }
    openLoginModal();
  };

  const closeLoginModal = () => {
    setShowLoginModal(false);
  };

  const handleToggleLike = (review: ReviewDto) => {
    if (!isLogin) {
      openLoginModal();
      return;
    }

    const reviewId = review.id;
    if (reviewId == null) return;

    const isLiked = likedReviewIds.has(reviewId);

    apiFetch(`/api/v1/reviews/${reviewId}/like`, {
      method: isLiked ? "DELETE" : "POST",
    })
      .then((data) => {
        showToast(
          data?.message ??
            (isLiked ? "좋아요를 취소했습니다." : "좋아요를 눌렀습니다."),
        );
        setLikedReviewIds((current) => {
          const next = new Set(current);
          if (isLiked) {
            next.delete(reviewId);
          } else {
            next.add(reviewId);
          }
          return next;
        });
        setReviews((current) =>
          current == null
            ? current
            : current.map((r) =>
                r.id === reviewId
                  ? { ...r, likeCount: (r.likeCount ?? 0) + (isLiked ? -1 : 1) }
                  : r,
              ),
        );
      })
      .catch(showErrorToast);
  };

  const handleDelete = async (reviewId: number) => {
    if (!(await confirmAction("리뷰를 삭제하시겠습니까?"))) return;

    apiFetch(`/api/v1/reviews/${reviewId}`, {
      method: "DELETE",
    })
      .then((data) => {
        showToast(data?.message ?? "리뷰를 삭제했습니다.");
        loadReviews();
        loadBook();
      })
      .catch(showErrorToast);
  };

  if (id == null) {
    return <BookDetailSkeleton />;
  }

  if (book == null || reviews == null) return <BookDetailSkeleton />;

  const isBookWished = book.wishId != null;
  const average = book.rating?.["average"];
  const averageNumber = typeof average === "number" ? average : 0;
  const authors = book.authors ?? [];
  const bookMeta = [
    authors.length > 0 ? `${authors.join(", ")} 지음` : null,
    book.publisher,
    book.publishedDate ? `${book.publishedDate.slice(0, 4)}년` : null,
  ]
    .filter(Boolean)
    .join(", ");
  const editingReview =
    editingReviewId == null
      ? null
      : (reviews.find((review) => review.id === editingReviewId) ?? null);

  // 베스트 리뷰는 고정하고, 정렬은 그 아래 목록에만 적용한다
  const [bestReview, ...otherReviews] = [...reviews].sort(
    (a, b) => (b.likeCount ?? 0) - (a.likeCount ?? 0),
  );
  const sortedReviews = otherReviews.sort((a, b) =>
    reviewSort === "popular"
      ? (b.likeCount ?? 0) - (a.likeCount ?? 0)
      : (b.createdDate ?? "").localeCompare(a.createdDate ?? ""),
  );

  const renderReview = (review: ReviewDto, best = false) => {
    const isMine =
      loginMember?.id != null && loginMember.id === review.reviewer?.id;

    return (
      <ReviewDetailCard
        review={review}
        best={best}
        liked={review.id != null && likedReviewIds.has(review.id)}
        onToggleLike={() => handleToggleLike(review)}
        onEdit={
          isMine && review.id != null
            ? () => setEditingReviewId(review.id as number)
            : undefined
        }
        onDelete={
          isMine && review.id != null
            ? () => handleDelete(review.id as number)
            : undefined
        }
      />
    );
  };

  return (
    <>
      {showLoginModal && <LoginRequiredModal onCancel={closeLoginModal} />}
      {showWriteForm && (
        <ReviewFormModal
          onCancel={() => setShowWriteForm(false)}
          onSubmit={handleWriteSubmit}
          submitLabel="등록"
          title="리뷰 쓰기"
        />
      )}
      {editingReview?.id != null && (
        <ReviewFormModal
          defaultContent={editingReview.content ?? ""}
          defaultRating={editingReview.rating}
          defaultTags={(editingReview.tags ?? []).join(", ")}
          onCancel={() => setEditingReviewId(null)}
          onSubmit={(e) => handleEditSubmit(e, editingReview.id as number)}
          submitLabel="수정 완료"
          title="리뷰 수정"
        />
      )}

      <div className="band">
        <div className="wrap book-hero">
          <BookCover
            bookId={book.id}
            imgUrl={book.imgUrl}
            title={book.title}
            loading="eager"
          />
          <div>
            <h1>{book.title}</h1>
            <p className="meta" style={{ marginTop: 12 }}>
              {bookMeta}
            </p>
            {book.description && <p className="desc">{book.description}</p>}
            <TagList tags={book.tags} />
            <button
              type="button"
              className={`btn ${isBookWished ? "" : "line"}`}
              aria-pressed={isBookWished}
              onClick={isLogin ? handleToggleWish : openLoginModal}
            >
              <SvgIcon name={isBookWished ? "bookmark" : "bookmark-outline"} />
              보고 싶어요
            </button>
          </div>
        </div>
      </div>

      <section className="wrap book-body">
        <div className="sticky-col">
          <h2 className="sr">평점</h2>
          <div className="score">{averageNumber.toFixed(1)}</div>
          <p className="meta" style={{ marginTop: 10 }}>
            리뷰 {book.reviewCount ?? 0}개
          </p>
          {book.rating && <RatingHistogram rating={book.rating} />}
          <button
            type="button"
            className="btn"
            style={reviews.length === 0 ? { marginTop: 24 } : undefined}
            onClick={handleOpenReviewAction}
          >
            <SvgIcon name="review" />
            리뷰 쓰기
          </button>
        </div>
        <div>
          <h2
            className={reviews.length === 0 ? "sr" : ""}
            style={{ marginBottom: 20 }}
          >
            리뷰
          </h2>
          {bestReview == null ? (
            <div className="state">
              <strong>아직 리뷰가 없습니다.</strong>이 책을 읽었다면 첫 리뷰를
              남겨 보세요.
            </div>
          ) : (
            <>
              <article className="feature-review">
                {renderReview(bestReview, true)}
              </article>
              {sortedReviews.length > 1 && (
                <div className="sort" role="group" aria-label="리뷰 정렬">
                  {(
                    [
                      ["latest", "최신순"],
                      ["popular", "인기순"],
                    ] as const
                  ).map(([value, label]) => (
                    <button
                      key={value}
                      type="button"
                      className="text-btn"
                      aria-pressed={value === reviewSort}
                      onClick={() => setReviewSort(value)}
                    >
                      {label}
                    </button>
                  ))}
                </div>
              )}
              <ul className="rv-list">
                {sortedReviews.map((review) => (
                  <li className="rv-row" key={review.id}>
                    {renderReview(review)}
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>
      </section>

      {isLogin && recommendBooks != null && recommendBooks.length > 0 && (
        <section className="wrap" style={{ paddingTop: 0 }}>
          <BookTrack title="추천 도서">
            {recommendBooks.map((recommendBook) => (
              <Link
                key={recommendBook.id}
                href={`/books/detail?id=${recommendBook.id}`}
              >
                <BookCover
                  bookId={recommendBook.id}
                  imgUrl={recommendBook.imgUrl}
                  title={recommendBook.title}
                />
                <h3>{recommendBook.title}</h3>
                <p className="meta">
                  <RatingValue rating={recommendBook.averageRating} />
                </p>
              </Link>
            ))}
          </BookTrack>
        </section>
      )}
    </>
  );
}

export default function Page() {
  return (
    <Suspense fallback={<BookDetailSkeleton />}>
      <BookDetail />
    </Suspense>
  );
}
