"use client";

import Link from "next/link";

import { useAuth } from "@/lib/auth/AuthProvider";
import type { components } from "@/lib/backend/apiV1/schema";
import { formatDate } from "@/lib/formatDate";

import Avatar from "@/app/_components/Avatar";
import LikeButton from "@/app/_components/LikeButton";
import RatingValue from "@/app/_components/RatingValue";

type ReviewDto = components["schemas"]["ReviewDto"];

export function TagList({ tags }: { tags?: string[] }) {
  if (!tags?.length) return null;

  return (
    <ul className="tags">
      {tags.map((tag) => (
        <li key={tag}>{tag}</li>
      ))}
    </ul>
  );
}

type ReviewDetailCardProps = {
  review: ReviewDto;
  best?: boolean;
  liked: boolean;
  onToggleLike: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
};

export default function ReviewDetailCard({
  review,
  best = false,
  liked,
  onToggleLike,
  onEdit,
  onDelete,
}: ReviewDetailCardProps) {
  const { loginMember } = useAuth();
  const reviewer = review.reviewer;
  const name = reviewer?.githubId ?? "탈퇴한 사용자";
  const href =
    reviewer?.id == null
      ? null
      : loginMember?.id === reviewer.id
        ? "/mypage"
        : `/members/detail?id=${reviewer.id}`;
  const avatar = <Avatar label={name} imageUrl={reviewer?.imgUrl} size={36} />;

  return (
    <>
      <div className="rv-who">
        {href ? <Link href={href}>{avatar}</Link> : avatar}
        <div>
          <strong>
            {href ? <Link href={href}>{name}</Link> : name}
            {best && (review.likeCount ?? 0) > 0 && (
              <span className="badge on">베스트 리뷰</span>
            )}
          </strong>
          <small>
            {formatDate(review.createdDate)}
            <RatingValue rating={review.rating} inline />
          </small>
        </div>
        <LikeButton
          count={review.likeCount}
          liked={liked}
          onClick={onToggleLike}
        />
      </div>
      <q>{review.content}</q>
      <TagList tags={review.tags} />
      {(onEdit || onDelete) && (
        <div className="mt-2 -ml-2.5 flex gap-0.5">
          {onEdit && (
            <button type="button" className="text-btn" onClick={onEdit}>
              수정
            </button>
          )}
          {onDelete && (
            <button type="button" className="text-btn" onClick={onDelete}>
              삭제
            </button>
          )}
        </div>
      )}
    </>
  );
}
