"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { useEffect, useRef, useState } from "react";

import { API_BASE_URL, apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import {
  consumeAuthReturnPath,
  saveCurrentAuthReturnPath,
} from "@/lib/auth/authReturnPath";
import { resolveAvatarUrl } from "@/lib/avatar";
import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { formatDateTime } from "@/lib/formatDate";
import { ratingColor } from "@/lib/ratingColor";
import { useToast } from "@/lib/toast/ToastProvider";

import BookTape from "@/app/_components/BookTape";
import BookThumbnail from "@/app/_components/BookThumbnail";
import LibraryPageLayout from "@/app/_components/LibraryPageLayout";
import LibraryProfilePanel from "@/app/_components/LibraryProfilePanel";
import LibraryWidgetPreview from "@/app/_components/LibraryWidgetPreview";
import RatingValue from "@/app/_components/RatingValue";
import ReviewDetailCard from "@/app/_components/ReviewDetailCard";
import ReviewFormModal from "@/app/_components/ReviewFormModal";
import RoughButton from "@/app/_components/RoughButton";
import RoughDivider from "@/app/_components/RoughDivider";
import RoughFrame from "@/app/_components/RoughFrame";
import SvgIcon from "@/app/_components/SvgIcon";
import WidgetGuideModal from "@/app/_components/WidgetGuideModal";

type ReviewsByMemberDto = components["schemas"]["ReviewsByMemberDto"];
type BookWithWishIdAndTagsDto =
  components["schemas"]["BookWithWishIdAndTagsDto"];
type ReviewWithBookImgUrl = NonNullable<
  ReviewsByMemberDto["results"]
>[number] & {
  bookImgUrl?: string | null;
};
type LikedReview = ReviewWithBookImgUrl;

function MyPageSkeleton() {
  return (
    <div className="flex gap-8">
      <aside className="flex w-48 shrink-0 flex-col items-center gap-1 pt-6">
        <div className="book-skeleton h-24 w-24 rounded-full" />
        <div className="mt-6 w-full space-y-2">
          <div className="book-skeleton h-5 w-28 rounded" />
          <div className="book-skeleton h-5 w-24 rounded" />
          <div className="book-skeleton h-5 w-32 rounded" />
        </div>
        <div className="mt-3 w-full space-y-2">
          <div className="book-skeleton h-4 w-24 rounded" />
          <div className="book-skeleton h-24 w-full rounded" />
        </div>
        <div className="mt-4 flex w-full gap-2">
          <div className="book-skeleton h-9 flex-1 rounded" />
          <div className="book-skeleton h-9 flex-1 rounded" />
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col gap-4">
        <div>
          <div className="flex items-center justify-between gap-2">
            <div className="book-skeleton h-6 w-28 rounded" />
            <div className="flex gap-2">
              <div className="book-skeleton h-8 w-20 rounded" />
              <div className="book-skeleton h-8 w-28 rounded" />
            </div>
          </div>
          <div className="rough-panel-border relative mt-1 min-h-24 p-2">
            <div className="book-skeleton h-20 w-full rounded" />
          </div>
          <div className="mt-1 book-skeleton h-9 w-full rounded" />
        </div>

        <div className="flex gap-2">
          <div className="book-skeleton h-10 w-28 rounded" />
          <div className="book-skeleton h-10 w-28 rounded" />
        </div>

        <ul className="flex w-full flex-col">
          {Array.from({ length: 3 }).map((_, index) => (
            <li key={index} className="relative flex items-start gap-3 py-3">
              {index < 2 && <RoughDivider fullWidth />}
              <div className="book-skeleton h-20 w-14 shrink-0 rounded-lg" />
              <div className="min-w-0 flex-1 space-y-2">
                <div className="book-skeleton h-5 w-40 rounded" />
                <div className="book-skeleton h-4 w-24 rounded" />
                <div className="book-skeleton h-4 w-full rounded" />
                <div className="book-skeleton h-4 w-28 rounded" />
                <div className="book-skeleton h-8 w-16 rounded" />
              </div>
              <div className="book-skeleton h-6 w-14 shrink-0 rounded" />
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

function WishIcon() {
  return <SvgIcon name="bookmark" className="h-4 w-4 align-[-0.125em]" />;
}

export default function Page() {
  const router = useRouter();
  const { loginMember, isLogin, isLoginMemberPending, refresh } = useAuth();
  const { showToast, showErrorToast } = useToast();

  const [reviewData, setReviewData] = useState<ReviewsByMemberDto | null>(null);
  const [wishes, setWishes] = useState<BookWithWishIdAndTagsDto[] | null>(null);
  const [likedReviews, setLikedReviews] = useState<LikedReview[] | null>(null);
  const [tab, setTab] = useState<"reviews" | "liked" | "wishes">("reviews");
  const [editingReview, setEditingReview] =
    useState<ReviewWithBookImgUrl | null>(null);
  const [isWidgetGuideOpen, setIsWidgetGuideOpen] = useState(false);
  const [copiedWidgetLink, setCopiedWidgetLink] = useState(false);
  const pageCacheRef = useRef<{
    reviews?: ReviewsByMemberDto;
    wishes?: BookWithWishIdAndTagsDto[];
    likedReviews?: LikedReview[];
    memberId?: number | null;
  }>({});

  useEffect(() => {
    if (isLoginMemberPending) return;

    if (pageCacheRef.current.memberId !== loginMember?.id) {
      pageCacheRef.current = {
        memberId: loginMember?.id ?? null,
      };
      setReviewData(null);
      setWishes(null);
      setLikedReviews(null);
      setEditingReview(null);
      setTab("reviews");
      setCopiedWidgetLink(false);
      setIsWidgetGuideOpen(false);
    }
  }, [isLoginMemberPending, loginMember?.id]);

  const loadReviews = () => {
    if (pageCacheRef.current.reviews != null) {
      setReviewData(pageCacheRef.current.reviews);
      return;
    }
    apiFetch(`/api/v1/reviews/member/mine`)
      .then((data: ReviewsByMemberDto) => {
        pageCacheRef.current.reviews = data;
        setReviewData(data);
      })
      .catch(goToErrorPage);
  };

  const loadWishes = () => {
    if (pageCacheRef.current.wishes != null) {
      setWishes(pageCacheRef.current.wishes);
      return;
    }
    apiFetch(`/api/v1/wishes/mine`)
      .then((data) => {
        pageCacheRef.current.wishes = data;
        setWishes(data);
      })
      .catch(goToErrorPage);
  };

  const loadLikedReviews = () => {
    if (pageCacheRef.current.likedReviews != null) {
      setLikedReviews(pageCacheRef.current.likedReviews);
      return;
    }
    apiFetch(`/api/v1/reviews/member/mine/liked`)
      .then((data: LikedReview[]) => {
        pageCacheRef.current.likedReviews = data;
        setLikedReviews(data);
      })
      .catch(goToErrorPage);
  };

  useEffect(() => {
    if (isLoginMemberPending) return;

    if (!isLogin) {
      router.replace(`/`);
      return;
    }

    loadReviews();
    loadWishes();
    loadLikedReviews();
  }, [isLoginMemberPending, isLogin, router]);

  const handleDeleteReview = (reviewId: number) => {
    if (!confirm("리뷰를 삭제하시겠습니까?")) return;

    apiFetch(`/api/v1/reviews/${reviewId}`, { method: "DELETE" })
      .then((data) => {
        showToast(data?.message ?? "리뷰를 삭제했습니다.");
        const nextReviews =
          reviewData?.results?.filter((review) => review.id !== reviewId) ?? [];
        const nextReviewData =
          reviewData == null ? null : { ...reviewData, results: nextReviews };
        pageCacheRef.current.reviews = nextReviewData ?? undefined;
        setReviewData(nextReviewData);

        const nextLikedReviews = (likedReviews ?? []).filter(
          (review) => review.id !== reviewId,
        );
        pageCacheRef.current.likedReviews = nextLikedReviews;
        setLikedReviews(nextLikedReviews);
      })
      .catch(showErrorToast);
  };

  const handleToggleLikedReview = (review: LikedReview) => {
    const reviewId = review.id;
    if (reviewId == null) return;

    apiFetch(`/api/v1/reviews/${reviewId}/like`, { method: "DELETE" })
      .then((data) => {
        showToast(data?.message ?? "좋아요를 취소했습니다.");
        const nextLikedReviews = (likedReviews ?? []).filter(
          (item) => item.id !== reviewId,
        );
        pageCacheRef.current.likedReviews = nextLikedReviews;
        setLikedReviews(nextLikedReviews);
      })
      .catch(showErrorToast);
  };

  const handleRemoveWish = (wishId: number) => {
    apiFetch(`/api/v1/wishes/${wishId}`, { method: "DELETE" })
      .then((data) => {
        showToast(data?.message ?? "보고 싶어요를 취소했습니다.");
        const next = (wishes ?? []).filter((wish) => wish.wishId !== wishId);
        pageCacheRef.current.wishes = next;
        setWishes(next);
      })
      .catch(showErrorToast);
  };

  const handleEditReviewSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (editingReview == null || editingReview.id == null) return;

    const form = e.currentTarget;
    const formData = new FormData(form);
    const ratingValue = String(formData.get("rating") ?? "").trim();
    const contentValue = String(formData.get("content") ?? "").trim();
    const tagsValue = String(formData.get("tags") ?? "");
    const parsedRating =
      ratingValue === "" ? null : Number.parseFloat(ratingValue);

    if (parsedRating == null || Number.isNaN(parsedRating)) {
      showToast("평점을 선택해주세요.");
      return;
    }

    if (parsedRating * 2 !== Math.round(parsedRating * 2)) {
      showToast("평점은 0.5 단위로만 입력할 수 있습니다.");
      return;
    }

    const body = {
      rating: parsedRating,
      content: contentValue,
      tags: tagsValue
        .split(",")
        .map((tag) => tag.trim())
        .filter((tag) => tag.length > 0),
    };

    apiFetch(`/api/v1/reviews/${editingReview.id}`, {
      method: "PUT",
      body: JSON.stringify(body),
    })
      .then((data) => {
        showToast(data?.message ?? "리뷰를 수정했습니다.");
        const updateReview = (review: ReviewWithBookImgUrl) =>
          review.id === editingReview.id ? { ...review, ...body } : review;

        if (reviewData != null) {
          const nextReviews = (reviewData.results ?? []).map(updateReview);
          const nextReviewData = { ...reviewData, results: nextReviews };
          pageCacheRef.current.reviews = nextReviewData;
          setReviewData(nextReviewData);
        }
        if (likedReviews != null) {
          const nextLikedReviews = likedReviews.map(updateReview);
          pageCacheRef.current.likedReviews = nextLikedReviews;
          setLikedReviews(nextLikedReviews);
        }

        setEditingReview(null);
      })
      .catch(showErrorToast);
  };

  const handleWithdraw = () => {
    if (!confirm("정말 탈퇴하시겠습니까? 되돌릴 수 없습니다.")) return;

    apiFetch(`/api/v1/members`, { method: "DELETE" })
      .then((data) => {
        showToast(data?.message ?? "회원 탈퇴가 완료되었습니다.");
        return refresh();
      })
      .then(() => {
        router.replace(`/`);
      })
      .catch(showErrorToast);
  };

  const handleLogout = () => {
    saveCurrentAuthReturnPath();

    apiFetch(`/api/v1/members/logout`, { method: "DELETE" })
      .then(() => {
        showToast("로그아웃되었습니다.");
        return refresh().then(() => router.replace(consumeAuthReturnPath()));
      })
      .catch(showErrorToast);
  };

  const handleCopyWidgetLink = async (code: string) => {
    try {
      await navigator.clipboard.writeText(code);
      setCopiedWidgetLink(true);
      window.setTimeout(() => setCopiedWidgetLink(false), 2000);
    } catch {
      alert("위젯 링크 복사에 실패했습니다. 주소를 직접 선택해 복사해 주세요.");
    }
  };

  const handleOpenWidgetGuide = () => {
    setIsWidgetGuideOpen((current) => !current);
  };

  if (
    isLoginMemberPending ||
    !isLogin ||
    reviewData == null ||
    wishes == null
  ) {
    return <MyPageSkeleton />;
  }

  const average = reviewData.rating?.["average"];
  const averageNumber = typeof average === "number" ? average : null;
  const reviewResults = reviewData.results ?? [];
  const likedReviewResults = likedReviews ?? [];
  const widgetLink = loginMember?.githubId
    ? `${API_BASE_URL}/api/v2/widgets/${loginMember.githubId}`
    : "";
  const widgetCodeSnippet = `<img src="${widgetLink}" alt="내 서재 위젯" />`;

  return (
    <LibraryPageLayout
      sidebar={
        <LibraryProfilePanel
          avatarLabel={loginMember?.githubId ?? loginMember?.username}
          username={loginMember?.username}
          githubId={loginMember?.githubId}
          githubLink={loginMember?.githubLink}
          avatarUrl={resolveAvatarUrl(loginMember) ?? undefined}
          averageLabel="내가 준 평균 별점"
          averageRating={averageNumber}
          rating={reviewData.rating}
          mobileWidget={
            <div className="max-sm:block sm:hidden">
              <LibraryWidgetPreview
                githubId={loginMember?.githubId}
                widgetSrc={`${API_BASE_URL}/api/v2/widgets/${loginMember?.githubId ?? ""}`}
                widgetLink={loginMember?.widgetLink}
                actions={
                  <div className="flex flex-wrap justify-end gap-2">
                    <RoughButton
                      roughSize="sm"
                      tone="history"
                      type="button"
                      disabled={!widgetLink}
                      onClick={() => handleCopyWidgetLink(widgetCodeSnippet)}
                    >
                      {copiedWidgetLink ? "복사 완료" : "코드 복사"}
                    </RoughButton>
                    <RoughButton
                      roughSize="sm"
                      type="button"
                      onClick={handleOpenWidgetGuide}
                    >
                      README 가이드 보기
                    </RoughButton>
                  </div>
                }
              />
            </div>
          }
          actions={
            <div className="mt-4 flex w-full gap-2 max-sm:w-auto max-sm:flex-wrap">
              <RoughButton
                className="flex-1 max-sm:flex-none"
                roughSize="sm"
                type="button"
                onClick={handleLogout}
              >
                로그아웃
              </RoughButton>
              <RoughButton
                className="flex-1 max-sm:flex-none"
                roughSize="sm"
                tone="cancel"
                type="button"
                onClick={handleWithdraw}
              >
                회원 탈퇴
              </RoughButton>
            </div>
          }
        />
      }
    >
      {editingReview != null && (
        <ReviewFormModal
          defaultContent={editingReview.content ?? ""}
          defaultRating={editingReview.rating ?? undefined}
          defaultTags={(editingReview.tags ?? []).join(", ")}
          onCancel={() => setEditingReview(null)}
          onSubmit={handleEditReviewSubmit}
          submitLabel="수정 완료"
          title="리뷰 수정"
        />
      )}

      <div className="max-sm:hidden">
        <LibraryWidgetPreview
          githubId={loginMember?.githubId}
          widgetSrc={`${API_BASE_URL}/api/v2/widgets/${loginMember?.githubId ?? ""}`}
          widgetLink={loginMember?.widgetLink}
          actions={
            <div className="flex flex-wrap justify-end gap-2">
              <RoughButton
                roughSize="sm"
                tone="history"
                type="button"
                disabled={!widgetLink}
                onClick={() => handleCopyWidgetLink(widgetCodeSnippet)}
              >
                {copiedWidgetLink ? "복사 완료" : "코드 복사"}
              </RoughButton>
              <RoughButton
                roughSize="sm"
                type="button"
                onClick={handleOpenWidgetGuide}
              >
                README 가이드 보기
              </RoughButton>
            </div>
          }
        />
      </div>

      <section className="flex flex-col gap-1">
        <div className="flex gap-2">
          <div className="theme-tab">
            <button
              type="button"
              className={`px-3 py-2 text-sm ${
                tab === "reviews" ? "theme-tab-active" : "theme-tab-inactive"
              }`}
              onClick={() => setTab("reviews")}
            >
              작성한 리뷰 {reviewResults.length}
            </button>
          </div>
          <div className="theme-tab">
            <button
              type="button"
              className={`px-3 py-2 text-sm ${
                tab === "liked" ? "theme-tab-active" : "theme-tab-inactive"
              }`}
              onClick={() => setTab("liked")}
            >
              좋아요한 리뷰 {likedReviewResults.length}
            </button>
          </div>
          <div className="theme-tab">
            <button
              type="button"
              className={`px-3 py-2 text-sm ${
                tab === "wishes" ? "theme-tab-active" : "theme-tab-inactive"
              }`}
              onClick={() => setTab("wishes")}
            >
              보고 싶어요 {wishes?.length ?? 0}
            </button>
          </div>
        </div>

        {tab === "reviews" && (
          <>
            {reviewResults.length === 0 && (
              <div className="text-sm theme-muted">작성한 리뷰가 없습니다.</div>
            )}

            <ul className="flex w-full flex-col">
              {reviewResults.map((review, index) => (
                <li
                  key={review.id ?? review.bookId}
                  className="relative flex items-start gap-3 py-3"
                >
                  {index < reviewResults.length - 1 && (
                    <RoughDivider fullWidth />
                  )}
                  <Link
                    href={`/books/detail?id=${review.bookId}`}
                    className="rough-book-card review-book-thumbnail relative flex h-20 w-14 shrink-0 items-center justify-center overflow-hidden rounded-lg bg-white"
                    aria-label={`${review.bookTitle ?? `책 #${review.bookId}`} 상세 보기`}
                  >
                    <RoughFrame
                      className="rough-overlay rough-card-line rough-book-cover-line"
                      variant="card"
                    />
                    <BookThumbnail
                      bookId={review.bookId}
                      imgUrl={(review as ReviewWithBookImgUrl).bookImgUrl}
                      title={review.bookTitle}
                      className="h-full w-full object-cover"
                      placeholderClassName="flex h-full w-full items-center justify-center"
                      placeholderText="표지 없음"
                      altClassName="text-xs text-gray-400"
                    />
                  </Link>
                  <div className="flex-1 min-w-0">
                    <Link
                      className="font-semibold hover:underline"
                      href={`/books/detail?id=${review.bookId}`}
                    >
                      {review.bookTitle ?? `책 #${review.bookId}`}
                    </Link>

                    <div className="flex flex-wrap gap-1 text-xs theme-tag">
                      {(review.tags ?? []).map((tag) => (
                        <span key={tag}>#{tag}</span>
                      ))}
                    </div>

                    <div className="mt-1 text-sm">{review.content}</div>
                    <div className="mt-1 text-xs theme-subtle">
                      {formatDateTime(review.createdDate)}
                    </div>
                  </div>

                  <div className="flex shrink-0 flex-col items-end gap-2">
                    <span
                      className={`font-bold ${
                        typeof review.rating === "number"
                          ? ratingColor(review.rating)
                          : ""
                      }`}
                    >
                      <RatingValue rating={review.rating ?? 0} />
                    </span>
                    <div className="flex flex-wrap items-center justify-end gap-2">
                      <RoughButton
                        className="px-2"
                        roughSize="sm"
                        type="button"
                        onClick={() => {
                          if (review.id == null) return;
                          setEditingReview(review);
                        }}
                      >
                        수정
                      </RoughButton>
                      <RoughButton
                        className="px-2"
                        roughSize="sm"
                        tone="cancel"
                        type="button"
                        onClick={() => {
                          if (review.id == null) return;
                          handleDeleteReview(review.id);
                        }}
                      >
                        삭제
                      </RoughButton>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </>
        )}

        {tab === "liked" && (
          <>
            {likedReviewResults.length === 0 && (
              <div className="text-sm theme-muted">
                좋아요한 리뷰가 없습니다.
              </div>
            )}

            <ul className="flex w-full flex-col">
              {likedReviewResults.map((review, index) => (
                <li key={review.id ?? review.bookId} className="relative py-3">
                  {index < likedReviewResults.length - 1 && (
                    <RoughDivider fullWidth />
                  )}
                  <div className="flex gap-3">
                    <div className="relative w-24 shrink-0">
                      <Link
                        href={`/books/detail?id=${review.bookId}`}
                        className="rough-book-card review-book-thumbnail relative flex h-36 w-24 items-center justify-center overflow-hidden rounded-lg bg-white"
                        aria-label={`${review.bookTitle ?? `책 #${review.bookId}`} 상세 보기`}
                      >
                        <RoughFrame
                          className="rough-overlay rough-card-line rough-book-cover-line"
                          variant="card"
                        />
                        <BookThumbnail
                          bookId={review.bookId}
                          imgUrl={review.bookImgUrl}
                          title={review.bookTitle}
                          className="h-full w-full object-cover"
                          placeholderClassName="flex h-full w-full items-center justify-center"
                          placeholderText="표지 없음"
                          altClassName="text-xs text-gray-400"
                        />
                        <BookTape>
                          <div className="flex flex-col gap-1">
                            <div className="line-clamp-1 text-xs font-bold">
                              {review.bookTitle ?? `책 #${review.bookId}`}
                            </div>
                          </div>
                        </BookTape>
                      </Link>
                    </div>
                    <div className="min-w-0 flex-1">
                      <ReviewDetailCard
                        review={review}
                        memberLink={
                          review.reviewer?.id != null
                            ? `/members/detail?id=${review.reviewer.id}`
                            : null
                        }
                        showActions
                        liked
                        likeLabel="좋아요 취소"
                        onToggleLike={() => handleToggleLikedReview(review)}
                        onEdit={
                          loginMember?.id != null &&
                          loginMember.id === review.reviewer?.id
                            ? () => {
                                if (review.id == null) return;
                                setEditingReview(review);
                              }
                            : undefined
                        }
                        onDelete={
                          loginMember?.id != null &&
                          loginMember.id === review.reviewer?.id
                            ? () => {
                                if (review.id == null) return;
                                handleDeleteReview(review.id);
                              }
                            : undefined
                        }
                      />
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </>
        )}

        {tab === "wishes" && (
          <>
            {wishes == null ? (
              <div className="text-sm theme-muted">로딩 중...</div>
            ) : (
              wishes.length === 0 && (
                <div className="text-sm theme-muted">
                  보고 싶어요 한 도서가 없습니다.
                </div>
              )
            )}

            <ul className="flex w-full flex-col">
              {wishes.map((book, index) => {
                const averageRating =
                  typeof book.averageRating === "number"
                    ? book.averageRating
                    : null;
                const title = book.title ?? "제목 없음";

                return (
                  <li
                    key={book.wishId ?? book.id ?? index}
                    className="relative flex items-center justify-between gap-3 py-3"
                  >
                    {index < wishes.length - 1 && <RoughDivider fullWidth />}
                    {book.id != null ? (
                      <Link
                        href={`/books/detail?id=${book.id}`}
                        className="font-semibold"
                      >
                        {title}
                      </Link>
                    ) : (
                      <span className="font-semibold">{title}</span>
                    )}
                    <div className="flex items-center gap-3">
                      <span
                        className={`font-bold ${
                          averageRating != null
                            ? ratingColor(averageRating)
                            : ""
                        }`}
                      >
                        {averageRating != null ? (
                          <RatingValue rating={averageRating} />
                        ) : (
                          "-"
                        )}
                      </span>
                      <RoughButton
                        className="px-2"
                        roughSize="sm"
                        tone="wishActive"
                        type="button"
                        disabled={book.wishId == null}
                        onClick={() => {
                          if (book.wishId == null) return;
                          handleRemoveWish(book.wishId);
                        }}
                      >
                        <WishIcon />
                        보고 싶어요 취소
                      </RoughButton>
                    </div>
                  </li>
                );
              })}
            </ul>
          </>
        )}
      </section>

      {isWidgetGuideOpen && (
        <WidgetGuideModal
          copiedWidgetLink={copiedWidgetLink}
          onCancel={() => setIsWidgetGuideOpen(false)}
          onCopyWidgetLink={handleCopyWidgetLink}
          widgetLink={widgetLink}
        />
      )}
    </LibraryPageLayout>
  );
}
