"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";

import { Suspense, useEffect, useState } from "react";

import { API_BASE_URL, apiFetch } from "@/lib/backend/client";

import { resolveAvatarUrl } from "@/lib/avatar";
import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { formatDateTime } from "@/lib/formatDate";
import { ratingColor } from "@/lib/ratingColor";

import BookThumbnail from "@/app/_components/BookThumbnail";
import LibraryPageLayout from "@/app/_components/LibraryPageLayout";
import LibraryProfilePanel from "@/app/_components/LibraryProfilePanel";
import LibraryWidgetPreview from "@/app/_components/LibraryWidgetPreview";
import RatingValue from "@/app/_components/RatingValue";
import RoughDivider from "@/app/_components/RoughDivider";
import RoughFrame from "@/app/_components/RoughFrame";

type MemberDto = components["schemas"]["MemberDto"];
type ReviewsByMemberDto = components["schemas"]["ReviewsByMemberDto"];
type ReviewWithBookImgUrl = NonNullable<
  ReviewsByMemberDto["results"]
>[number] & {
  bookImgUrl?: string | null;
};

function MemberDetailSkeleton() {
  return (
    <LibraryPageLayout
      sidebar={
        <aside className="flex w-48 shrink-0 flex-col items-center gap-1 pt-6">
          <div className="book-skeleton h-24 w-24 rounded-full" />
          <div className="mt-6 w-full space-y-2">
            <div className="book-skeleton h-5 w-28 rounded" />
            <div className="book-skeleton h-5 w-24 rounded" />
            <div className="book-skeleton h-5 w-32 rounded" />
          </div>
          <div className="mt-3 w-full space-y-2">
            <div className="book-skeleton h-4 w-20 rounded" />
            <div className="book-skeleton h-24 w-full rounded" />
          </div>
        </aside>
      }
    >
      <div>
        <div className="flex items-center justify-between gap-2">
          <div className="book-skeleton h-6 w-28 rounded" />
        </div>
        <div className="rough-panel-border relative mt-1 min-h-24 p-2">
          <div className="book-skeleton h-20 w-full rounded" />
        </div>
      </div>

      <div className="flex gap-2">
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
            </div>
            <div className="book-skeleton h-6 w-14 shrink-0 rounded" />
          </li>
        ))}
      </ul>
    </LibraryPageLayout>
  );
}

function MemberDetail() {
  const searchParams = useSearchParams();
  const id = searchParams.get("id");

  const [member, setMember] = useState<MemberDto | null>(null);
  const [reviewData, setReviewData] = useState<ReviewsByMemberDto | null>(null);

  useEffect(() => {
    if (id == null) {
      goToErrorPage({ message: "회원 ID가 없습니다." });
      return;
    }

    apiFetch(`/api/v1/members/${id}`)
      .then((data) => {
        setMember(data);
      })
      .catch(goToErrorPage);

    apiFetch(`/api/v1/reviews/member/${id}`)
      .then((data: ReviewsByMemberDto) => {
        setReviewData(data);
      })
      .catch(goToErrorPage);
  }, [id]);

  if (id == null) {
    return <MemberDetailSkeleton />;
  }

  if (member == null || reviewData == null) return <MemberDetailSkeleton />;

  const average = reviewData.rating?.["average"];
  const averageNumber = typeof average === "number" ? average : null;
  const reviewResults = reviewData.results ?? [];
  const widgetPreview = member.githubId ? (
    <LibraryWidgetPreview
      githubId={member.githubId}
      widgetSrc={`${API_BASE_URL}/api/v2/widgets/${member.githubId}`}
    />
  ) : null;

  return (
    <LibraryPageLayout
      sidebar={
        <LibraryProfilePanel
          avatarLabel={member.githubId}
          username={member.githubId}
          githubId={member.githubId}
          githubLink={member.githubLink}
          avatarUrl={resolveAvatarUrl(member) ?? undefined}
          averageLabel="평균 별점"
          averageRating={averageNumber}
          rating={reviewData.rating}
          mobileWidget={
            widgetPreview && (
              <div className="max-sm:block sm:hidden">{widgetPreview}</div>
            )
          }
        />
      }
    >
      {widgetPreview && <div className="max-sm:hidden">{widgetPreview}</div>}

      <div>
        <div className="flex gap-2">
          <div className="theme-tab">
            <div className="theme-tab-active px-3 py-2 text-sm">
              작성한 리뷰 {reviewResults.length}
            </div>
          </div>
        </div>

        {reviewResults.length === 0 && (
          <div className="mt-2 text-sm theme-muted">
            작성한 리뷰가 없습니다.
          </div>
        )}

        <ul className="flex w-full flex-col">
          {reviewResults.map((review, index) => (
            <li
              key={review.id ?? review.bookId}
              className="relative flex items-start gap-3 py-3"
            >
              {index < reviewResults.length - 1 && <RoughDivider fullWidth />}
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

                <div className="text-sm mt-1">{review.content}</div>
                <div className="mt-1 text-xs theme-subtle">
                  {formatDateTime(review.createdDate)}
                </div>
              </div>

              <span
                className={`font-bold shrink-0 ${
                  typeof review.rating === "number"
                    ? ratingColor(review.rating)
                    : ""
                }`}
              >
                <RatingValue rating={review.rating ?? 0} />
              </span>
            </li>
          ))}
        </ul>
      </div>
    </LibraryPageLayout>
  );
}
export default function Page() {
  return (
    <Suspense fallback={<MemberDetailSkeleton />}>
      <MemberDetail />
    </Suspense>
  );
}
