"use client";

import Link from "next/link";

import type { components } from "@/lib/backend/apiV1/schema";

import BookThumbnail from "@/app/_components/BookThumbnail";
import RoughFrame from "@/app/_components/RoughFrame";
import BookTape from "@/app/_components/BookTape";
import ReviewDetailCard from "@/app/_components/ReviewDetailCard";

type ReviewDto = components["schemas"]["ReviewDto"];

type Props = {
  review: ReviewDto & { bookImgUrl?: string | null };
  className?: string;
  reviewCardClassName?: string;
  onToggleLike?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
  liked?: boolean;
  showActions?: boolean;
  likeLabel?: string;
};

export default function LikedReviewCard({
  review,
  className = "",
  reviewCardClassName = "",
  onToggleLike,
  onEdit,
  onDelete,
  liked = false,
  showActions = false,
  likeLabel = "좋아요 취소",
}: Props) {
  return (
    <div className={`relative flex min-w-0 gap-3 ${className}`}>
      <RoughFrame className="rough-overlay" variant="card" />
      <div className="relative flex w-24 shrink-0 flex-col">
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
        </Link>
        <BookTape className="mt-0">
          <div className="line-clamp-1 text-xs font-bold">
            {review.bookTitle ?? `책 #${review.bookId}`}
          </div>
        </BookTape>
      </div>
      <div className={`min-w-0 flex-1 ${reviewCardClassName}`}>
        <ReviewDetailCard
          review={review}
          showActions={showActions}
          liked={liked}
          likeLabel={likeLabel}
          onToggleLike={onToggleLike}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      </div>
    </div>
  );
}
