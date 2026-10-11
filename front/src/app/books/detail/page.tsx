"use client";

import { useSearchParams } from "next/navigation";

import { Suspense, useCallback, useEffect, useRef, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { ratingColor } from "@/lib/ratingColor";
import { ratingFillColor } from "@/lib/ratingColor";
import { useToast } from "@/lib/toast/ToastProvider";

import BookCoverCard from "@/app/_components/BookCoverCard";
import BookGrid from "@/app/_components/BookGrid";
import BookTape from "@/app/_components/BookTape";
import BookThumbnail from "@/app/_components/BookThumbnail";
import LoginRequiredModal from "@/app/_components/LoginRequiredModal";
import RatingHistogram from "@/app/_components/RatingHistogram";
import RatingValue from "@/app/_components/RatingValue";
import { RoughStarIcon } from "@/app/_components/RatingValue";
import ReviewDetailCard from "@/app/_components/ReviewDetailCard";
import ReviewFormModal from "@/app/_components/ReviewFormModal";
import RoughButton from "@/app/_components/RoughButton";
import RoughDivider from "@/app/_components/RoughDivider";
import RoughFrame from "@/app/_components/RoughFrame";
import SvgIcon from "@/app/_components/SvgIcon";

type BookDetailDto = components["schemas"]["BookDetailDto"];
type BookDetailWithWishId = BookDetailDto & {
  isWished?: boolean;
  wished?: boolean;
  wishId?: number | null;
};
type BookDto = components["schemas"]["BookDto"];
type ReviewDto = components["schemas"]["ReviewDto"];

function BookDetailSkeleton() {
  return (
    <div className="mx-auto flex w-full max-w-3xl flex-col gap-6 p-4">
      <div className="flex gap-6">
        <div className="flex w-40 shrink-0 flex-col gap-2">
          <div className="book-skeleton h-56 w-full rounded-xl" />
          <div className="book-skeleton h-9 w-full rounded" />
        </div>

        <div className="grid flex-1 gap-4 md:grid-cols-[minmax(0,1fr)_10rem]">
          <div className="flex min-w-0 flex-col gap-2">
            <div className="book-skeleton h-9 w-3/4 rounded" />
            <div className="book-skeleton h-5 w-2/3 rounded" />
            <div className="mt-1 space-y-2">
              <div className="book-skeleton h-4 w-full rounded" />
              <div className="book-skeleton h-4 w-11/12 rounded" />
              <div className="book-skeleton h-4 w-4/5 rounded" />
            </div>
            <div className="mt-2 flex flex-wrap gap-2">
              <div className="book-skeleton h-6 w-16 rounded" />
              <div className="book-skeleton h-6 w-20 rounded" />
              <div className="book-skeleton h-6 w-14 rounded" />
            </div>
          </div>

          <div className="flex min-w-0 flex-col gap-2">
            <div className="ml-auto book-skeleton h-12 w-24 rounded" />
            <div className="ml-auto book-skeleton h-4 w-20 rounded" />
            <div className="mt-1 book-skeleton h-24 w-full max-w-40 rounded" />
          </div>
        </div>
      </div>

      <div>
        <div className="flex items-center gap-2">
          <div className="book-skeleton h-7 w-24 rounded" />
          <div className="book-skeleton h-5 w-10 rounded" />
        </div>
        <div className="mt-3">
          <BookGrid isLoading layout="horizontal" />
        </div>
      </div>

      <div>
        <div className="flex items-center gap-2">
          <div className="book-skeleton h-7 w-24 rounded" />
          <div className="book-skeleton h-9 w-28 rounded" />
        </div>
        <ul className="mt-2 flex w-full flex-col">
          {Array.from({ length: 3 }).map((_, index) => (
            <li key={index} className="relative py-3">
              <div className="flex items-start gap-3">
                <div className="book-skeleton h-10 w-10 shrink-0 rounded-full" />
                <div className="min-w-0 flex-1 space-y-2">
                  <div className="book-skeleton h-5 w-32 rounded" />
                  <div className="book-skeleton h-4 w-24 rounded" />
                  <div className="book-skeleton h-4 w-full rounded" />
                  <div className="book-skeleton h-4 w-28 rounded" />
                </div>
                <div className="book-skeleton h-6 w-14 shrink-0 rounded" />
              </div>
              {index < 2 && <RoughDivider />}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

function WishIcon({ filled = false }: { filled?: boolean }) {
  return (
    <SvgIcon
      name={filled ? "bookmark" : "bookmark-outline"}
      className="h-4 w-4 align-[-0.125em]"
    />
  );
}

function ReviewIcon() {
  return (
    <SvgIcon name="review" className="h-4 w-4 align-[-0.125em]" />
  );
}

function CarouselArrow({ direction }: { direction: "left" | "right" }) {
  return <SvgIcon name={`chevron-${direction}`} />;
}

function BookDetail() {
  const searchParams = useSearchParams();
  const id = searchParams.get("id");
  const { loginMember, isLogin } = useAuth();
  const { showToast, showErrorToast } = useToast();

  const [book, setBook] = useState<BookDetailWithWishId | null>(null);
  const [reviews, setReviews] = useState<ReviewDto[] | null>(null);
  const [recommendBooks, setRecommendBooks] = useState<BookDto[] | null>(null);
  const [editingReviewId, setEditingReviewId] = useState<number | null>(null);
  const [likedReviewIds, setLikedReviewIds] = useState<Set<number>>(new Set());
  const [showWriteForm, setShowWriteForm] = useState(false);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const recommendScrollRef = useRef<HTMLUListElement | null>(null);

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

  const moveRecommendCarousel = (direction: -1 | 1) => {
    const scrollElement = recommendScrollRef.current;
    if (scrollElement == null) return;

    const items = scrollElement.querySelectorAll<HTMLElement>(
      "[data-recommend-item='true']",
    );
    const scrollDistance =
      items[1] != null
        ? items[1].offsetLeft - items[0].offsetLeft
        : (items[0]?.getBoundingClientRect().width ?? 0);

    scrollElement.scrollBy({
      left: scrollDistance * 2 * direction,
      behavior: "smooth",
    });
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

  const handleDelete = (reviewId: number) => {
    if (!confirm("리뷰를 삭제하시겠습니까?")) return;

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
  const averageNumber = typeof average === "number" ? average : null;
  const authors = book.authors ?? [];
  const tags = book.tags ?? [];
  const editingReview =
    editingReviewId == null
      ? null
      : reviews.find((review) => review.id === editingReviewId) ?? null;
  const editingReviewTargetId = editingReview?.id;
  const editingReviewContent = editingReview?.content ?? "";
  const editingReviewRating = editingReview?.rating ?? undefined;
  const editingReviewTags = (editingReview?.tags ?? []).join(", ");

  return (
    <div className="flex flex-col gap-6 p-4 max-w-3xl mx-auto w-full">
      {showLoginModal && <LoginRequiredModal onCancel={closeLoginModal} />}
      {showWriteForm && (
        <ReviewFormModal
          onCancel={() => setShowWriteForm(false)}
          onSubmit={handleWriteSubmit}
          submitLabel="리뷰 작성"
          title="리뷰 작성"
        />
      )}
      {editingReviewTargetId != null && (
        <ReviewFormModal
          defaultContent={editingReviewContent}
          defaultRating={editingReviewRating}
          defaultTags={editingReviewTags}
          onCancel={() => setEditingReviewId(null)}
          onSubmit={(e) => handleEditSubmit(e, editingReviewTargetId)}
          submitLabel="리뷰 수정"
          title="리뷰 수정"
        />
      )}

      <div className="flex gap-6">
        <div className="flex w-40 shrink-0 flex-col gap-2">
          <div
            className={`rough-cover flex h-56 items-center justify-center overflow-hidden ${
              book.imgUrl ? "" : "book-cover-placeholder"
            }`}
          >
            <RoughFrame className="rough-overlay" variant="card" />
            <BookThumbnail
              bookId={book.id}
              imgUrl={book.imgUrl}
              title={book.title}
              className="w-full h-full object-cover"
              placeholderClassName="flex h-full w-full items-center justify-center text-sm text-gray-400"
            />
          </div>

          {isLogin ? (
            <RoughButton
              fullWidth
              roughSize="sm"
              tone={isBookWished ? "wishActive" : "wish"}
              type="button"
              onClick={handleToggleWish}
            >
              <WishIcon filled={isBookWished} />
              {isBookWished ? "보고 싶어요 취소" : "보고 싶어요"}
            </RoughButton>
          ) : (
            <RoughButton
              fullWidth
              roughSize="sm"
              tone="wish"
              type="button"
              onClick={openLoginModal}
            >
              <WishIcon />
              보고 싶어요
            </RoughButton>
          )}
        </div>

        <div className="grid flex-1 gap-4 md:grid-cols-[minmax(0,1fr)_10rem]">
          <div className="flex min-w-0 flex-col gap-2">
            <h1 className="text-2xl font-bold">{book.title}</h1>
            <div className="text-sm theme-muted">
              {authors.join(", ") || "-"} · {book.publisher} ·{" "}
              {book.publishedDate}
            </div>

            <p className="mt-1 text-sm theme-description">{book.description}</p>

            <div className="flex flex-wrap gap-2 text-sm theme-tag">
              {tags.map((tag) => (
                <span key={tag}>#{tag}</span>
              ))}
            </div>
          </div>

          <div className="flex min-w-0 flex-col gap-2">
            <div className="text-right">
              <div
                className={`text-4xl font-bold leading-none ${
                  averageNumber != null ? ratingColor(averageNumber) : ""
                }`}
              >
                {averageNumber != null ? (
                  <RatingValue rating={averageNumber} starClassName="h-9 w-9" />
                ) : (
                  "-"
                )}
              </div>
              <div className="mt-1 text-sm theme-muted">
                리뷰 {book.reviewCount}개
              </div>
            </div>
            {book.rating && (
              <RatingHistogram
                rating={book.rating}
                className="mt-1 w-full max-w-40"
              />
            )}
          </div>
        </div>
      </div>

      {isLogin && (
        <div>
          <div className="flex h-8 items-center gap-2">
            <h2 className="text-lg font-bold">추천 도서</h2>
            <span className="text-sm theme-muted">
              {recommendBooks?.length ?? 0}권
            </span>
            <div className="ml-auto flex shrink-0 items-center gap-1">
              <RoughButton
                type="button"
                className="flex h-8 w-8 items-center justify-center px-0 text-xl leading-none"
                roughSize="sm"
                onClick={() => moveRecommendCarousel(-1)}
                aria-label="추천 도서 이전 목록"
              >
                <CarouselArrow direction="left" />
              </RoughButton>
              <RoughButton
                type="button"
                className="flex h-8 w-8 items-center justify-center px-0 text-xl leading-none"
                roughSize="sm"
                onClick={() => moveRecommendCarousel(1)}
                aria-label="추천 도서 다음 목록"
              >
                <CarouselArrow direction="right" />
              </RoughButton>
            </div>
          </div>

          {recommendBooks == null ? (
            <div className="mt-2 text-sm theme-muted">
              추천 도서를 불러오는 중...
            </div>
          ) : recommendBooks.length === 0 ? (
            <div className="mt-2 text-sm theme-muted">
              리뷰를 추가하여 추천을 받아보세요.
            </div>
          ) : (
            <ul
              ref={recommendScrollRef}
              className="book-scroll-list mt-3 flex gap-3 overflow-x-auto py-2 pb-4"
            >
              {recommendBooks.map((recommendBook) => (
                <li
                  key={recommendBook.id}
                  className="shrink-0"
                  data-recommend-item="true"
                >
                  <article className="relative h-full w-24 overflow-hidden rounded-xl bg-white">
                    <BookCoverCard
                      bookId={recommendBook.id}
                      imgUrl={recommendBook.imgUrl}
                      title={recommendBook.title}
                      href={`/books/detail?id=${recommendBook.id}`}
                      ariaLabel={`${recommendBook.title ?? `책 #${recommendBook.id}`} 상세 보기`}
                      className="aspect-[2/3] w-full rounded-xl"
                      placeholderClassName="text-sm text-gray-400"
                      placeholderText="표지 없음"
                    >
                      <BookTape>
                        <div className="flex flex-col gap-1">
                          <div className="truncate text-xs font-bold">
                            {recommendBook.title}
                          </div>
                          <div className="flex items-center gap-1 text-xs font-bold">
                            <span className="relative inline-block h-3.5 w-3.5 shrink-0">
                              <RoughStarIcon
                                fill={ratingFillColor(
                                  recommendBook.averageRating ?? 0,
                                )}
                                className="rough-overlay"
                              />
                            </span>
                            <span>
                              {typeof recommendBook.averageRating === "number"
                                ? recommendBook.averageRating.toFixed(1)
                                : "-"}
                            </span>
                          </div>
                        </div>
                      </BookTape>
                    </BookCoverCard>
                  </article>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      <div>
        <div className="flex flex-wrap items-center gap-2">
          <h2 className="text-lg font-bold">리뷰 {reviews.length}개</h2>
          <RoughButton
            roughSize="sm"
            tone="history"
            type="button"
            onClick={handleOpenReviewAction}
          >
            <ReviewIcon />
            리뷰 작성하기
          </RoughButton>
        </div>

        {reviews.length === 0 && (
          <div className="mt-2 text-sm theme-muted">아직 리뷰가 없습니다.</div>
        )}

        <ul className="mt-2 flex w-full flex-col">
          {reviews.map((review, index) => (
            <li key={review.id ?? review.createdDate ?? index} className="relative py-3">
              <ReviewDetailCard
                review={review}
                memberLink={
                  review.reviewer?.id != null
                    ? `/members/detail?id=${review.reviewer.id}`
                    : null
                }
                showActions
                liked={review.id != null && likedReviewIds.has(review.id)}
                likeLabel={
                  review.id != null && likedReviewIds.has(review.id)
                    ? "좋아요 취소"
                    : "좋아요"
                }
                onToggleLike={() => handleToggleLike(review)}
                onEdit={
                  loginMember?.id != null &&
                  loginMember.id === review.reviewer?.id
                    ? () => {
                        if (review.id == null) return;
                        setEditingReviewId(review.id);
                      }
                    : undefined
                }
                onDelete={
                  loginMember?.id != null &&
                  loginMember.id === review.reviewer?.id
                    ? () => {
                        if (review.id == null) return;
                        handleDelete(review.id);
                      }
                    : undefined
                }
              />
              {index < reviews.length - 1 && <RoughDivider />}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export default function Page() {
  return (
    <Suspense fallback={<BookDetailSkeleton />}>
      <BookDetail />
    </Suspense>
  );
}
