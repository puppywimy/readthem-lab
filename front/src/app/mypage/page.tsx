"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { useEffect, useRef, useState } from "react";

import { API_BASE_URL, apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import { resolveAvatarUrl } from "@/lib/avatar";
import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { formatDate } from "@/lib/formatDate";
import { useToast } from "@/lib/toast/ToastProvider";

import BookCover from "@/app/_components/BookCover";
import { LibraryHeader, LibraryReviewRow } from "@/app/_components/Library";
import LikeButton from "@/app/_components/LikeButton";
import { GitHubLoginButton } from "@/app/_components/LoginRequiredModal";
import RatingValue from "@/app/_components/RatingValue";
import ReviewFormModal from "@/app/_components/ReviewFormModal";

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
    <>
      <div className="band">
        <div className="wrap me" aria-busy="true">
          <span
            className="sk"
            style={{ width: 96, height: 96, borderRadius: 999 }}
          />
          <div>
            <span
              className="sk line"
              style={{ height: "2.4rem", maxWidth: 280 }}
            />
            <span className="sk line" style={{ maxWidth: 160 }} />
          </div>
        </div>
      </div>
      <div className="wrap">
        {Array.from({ length: 3 }).map((_, index) => (
          <span
            key={index}
            className="sk line"
            style={{ height: 96, marginTop: 20 }}
          />
        ))}
      </div>
    </>
  );
}

export default function Page() {
  const router = useRouter();
  const { loginMember, isLogin, isLoginMemberPending, refresh } = useAuth();
  const { showToast, showErrorToast, confirmAction } = useToast();

  const [reviewData, setReviewData] = useState<ReviewsByMemberDto | null>(null);
  const [wishes, setWishes] = useState<BookWithWishIdAndTagsDto[] | null>(null);
  const [likedReviews, setLikedReviews] = useState<LikedReview[] | null>(null);
  const [tab, setTab] = useState<"reviews" | "liked" | "wishes">("reviews");
  const [editingReview, setEditingReview] =
    useState<ReviewWithBookImgUrl | null>(null);
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

    if (!isLogin) return;

    loadReviews();
    loadWishes();
    loadLikedReviews();
  }, [isLoginMemberPending, isLogin, router]);

  const handleDeleteReview = async (reviewId: number) => {
    if (!(await confirmAction("리뷰를 삭제하시겠습니까?"))) return;

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

  const handleWithdraw = async () => {
    if (!(await confirmAction("정말 탈퇴하시겠습니까? 되돌릴 수 없습니다.")))
      return;

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

  const handleCopyWidgetLink = async (code: string) => {
    try {
      await navigator.clipboard.writeText(code);
      setCopiedWidgetLink(true);
      window.setTimeout(() => setCopiedWidgetLink(false), 2000);
    } catch {
      showToast(
        "위젯 코드 복사에 실패했습니다. 코드를 직접 선택해 복사해 주세요.",
      );
    }
  };

  if (!isLoginMemberPending && !isLogin) {
    return (
      <div className="wrap">
        <div className="state">
          <strong>로그인이 필요합니다.</strong>
          로그인하면 리뷰와 찜한 책을 한곳에서 볼 수 있습니다.
          <div>
            <GitHubLoginButton />
          </div>
        </div>
      </div>
    );
  }

  if (isLoginMemberPending || reviewData == null || wishes == null) {
    return <MyPageSkeleton />;
  }

  const average = reviewData.rating?.["average"];
  const averageNumber = typeof average === "number" ? average : 0;
  const reviewResults = reviewData.results ?? [];
  const likedReviewResults = likedReviews ?? [];
  const githubId = loginMember?.githubId;
  const widgetLink = githubId
    ? `${API_BASE_URL}/api/v2/widgets/${githubId}`
    : "";
  const widgetCodeSnippet = `<img src="${widgetLink}" alt="내 서재 위젯" />`;

  return (
    <>
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

      <LibraryHeader
        githubId={githubId}
        githubLink={loginMember?.githubLink}
        imageUrl={resolveAvatarUrl(loginMember)}
        stats={[
          ["작성한 리뷰", reviewResults.length],
          ["보고 싶어요", wishes.length],
          ["내 평균 별점", averageNumber.toFixed(1)],
        ]}
      />

      <section
        className="wrap"
        style={{ paddingBlock: "clamp(40px,5vw,64px)" }}
      >
        <div className="tabs" role="tablist">
          {(
            [
              ["reviews", "작성한 리뷰"],
              ["liked", "좋아요한 리뷰"],
              ["wishes", "보고 싶어요"],
            ] as const
          ).map(([value, label]) => (
            <button
              key={value}
              type="button"
              className="tab"
              role="tab"
              aria-selected={tab === value}
              onClick={() => setTab(value)}
            >
              {label}
            </button>
          ))}
        </div>

        <div className="tab-body swap" role="tabpanel" key={tab}>
          {tab === "reviews" &&
            (reviewResults.length === 0 ? (
              <div className="state">
                <strong>작성한 리뷰가 없습니다.</strong>
                읽은 책을 찾아 첫 리뷰를 남겨 보세요.
                <div>
                  <Link className="btn" href="/books/search">
                    도서 찾기
                  </Link>
                </div>
              </div>
            ) : (
              <ul className="rv-list">
                {reviewResults.map((review) => (
                  <LibraryReviewRow
                    key={review.id ?? review.bookId}
                    review={review}
                    meta={
                      <>
                        {formatDate(review.createdDate)}
                        <RatingValue rating={review.rating} inline />
                      </>
                    }
                    actions={
                      <>
                        <button
                          type="button"
                          className="text-btn"
                          onClick={() => setEditingReview(review)}
                        >
                          수정
                        </button>
                        <button
                          type="button"
                          className="text-btn"
                          onClick={() => {
                            if (review.id != null)
                              handleDeleteReview(review.id);
                          }}
                        >
                          삭제
                        </button>
                      </>
                    }
                  />
                ))}
              </ul>
            ))}

          {tab === "liked" &&
            (likedReviewResults.length === 0 ? (
              <div className="state">
                <strong>좋아요한 리뷰가 없습니다.</strong>
                마음에 드는 리뷰에 좋아요를 눌러 모아 보세요.
              </div>
            ) : (
              <ul className="rv-list">
                {likedReviewResults.map((review) => (
                  <LibraryReviewRow
                    key={review.id ?? review.bookId}
                    review={review}
                    meta={
                      <>
                        {review.reviewer?.githubId ?? "탈퇴한 사용자"}
                        <RatingValue rating={review.rating} inline />
                      </>
                    }
                    actions={
                      <LikeButton
                        count={review.likeCount}
                        liked
                        onClick={() => handleToggleLikedReview(review)}
                      />
                    }
                  />
                ))}
              </ul>
            ))}

          {tab === "wishes" &&
            (wishes.length === 0 ? (
              <div className="state">
                <strong>보고 싶어요에 담은 책이 없습니다.</strong>
                관심 있는 책을 담으면 위젯에도 표시됩니다.
              </div>
            ) : (
              <div className="book-grid">
                {wishes.map((book) => (
                  <div className="wish-item" key={book.wishId ?? book.id}>
                    <Link href={`/books/detail?id=${book.id}`}>
                      <BookCover
                        bookId={book.id}
                        imgUrl={book.imgUrl}
                        title={book.title}
                      />
                      <h2>{book.title}</h2>
                      <p className="stats">
                        <RatingValue rating={book.averageRating} />
                      </p>
                    </Link>
                    <button
                      type="button"
                      className="text-btn"
                      disabled={book.wishId == null}
                      onClick={() => {
                        if (book.wishId != null) handleRemoveWish(book.wishId);
                      }}
                    >
                      찜 해제
                    </button>
                  </div>
                ))}
              </div>
            ))}
        </div>
      </section>

      {widgetLink && (
        <section className="band">
          <div className="wrap widget">
            <div>
              <h2>GitHub 프로필에 책장 붙이기</h2>
              <ol className="steps">
                <li>1. 프로필 저장소의 README.md를 엽니다.</li>
                <li>2. 아래 코드를 복사해 붙여 넣습니다.</li>
              </ol>
              <div className="code">
                <code>{widgetCodeSnippet}</code>
                <button
                  type="button"
                  className="btn"
                  onClick={() => handleCopyWidgetLink(widgetCodeSnippet)}
                >
                  {copiedWidgetLink ? "복사 완료" : "코드 복사"}
                </button>
              </div>
            </div>
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src={widgetLink} alt="내 서재 위젯 미리보기" />
          </div>
        </section>
      )}

      <div className="wrap account">
        <button type="button" className="text-btn" onClick={handleWithdraw}>
          회원 탈퇴
        </button>
      </div>
    </>
  );
}
