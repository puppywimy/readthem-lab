"use client";

import { Fragment, useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { useToast } from "@/lib/toast/ToastProvider";

import type { components } from "@/lib/backend/apiV1/schema";
import { ratingColor } from "@/lib/ratingColor";

import AdminRoughDivider from "@/app/admin/AdminRoughDivider";
import RatingValue from "@/app/_components/RatingValue";
import RoughButton from "@/app/_components/RoughButton";

type PageAdminReviewDto = components["schemas"]["PageAdminReviewDto"];

export default function ReviewAdmin() {
  const { showToast, showErrorToast } = useToast();
  const [pageData, setPageData] = useState<PageAdminReviewDto | null>(null);
  const [pageNumber, setPageNumber] = useState(0);

  const loadReviews = (page: number) => {
    apiFetch(`/api/v1/reviews/admin?page=${page}&size=10`)
      .then(setPageData)
      .catch(goToErrorPage);
  };

  useEffect(() => {
    loadReviews(pageNumber);
  }, [pageNumber]);

  const handleDelete = (reviewId: number) => {
    if (!confirm("이 리뷰를 삭제하시겠습니까?")) return;

    apiFetch(`/api/v1/reviews/${reviewId}`, { method: "DELETE" })
      .then((data) => {
        showToast(data?.message ?? "리뷰를 삭제했습니다.");
        loadReviews(pageNumber);
      })
      .catch(showErrorToast);
  };

  if (pageData == null) return <div>로딩중...</div>;

  const reviews = pageData.content ?? [];

  return (
    <div className="flex flex-col gap-4">
      {reviews.length === 0 && (
        <div className="text-sm theme-muted">리뷰가 없습니다.</div>
      )}

      {reviews.length > 0 && (
        <ul className="flex flex-col">
          <li aria-hidden="true">
            <AdminRoughDivider />
          </li>
          {reviews.map((review, index) => (
            <Fragment key={review.id ?? index}>
              <li className="flex items-start gap-3 py-2">
                <div className="min-w-0 flex-1">
                  <div className="text-sm font-semibold">
                    {review.bookTitle}
                  </div>
                  <div className="text-xs theme-muted">
                    {review.reviewer?.githubId ?? "탈퇴한 사용자"}
                  </div>
                  <div className="text-sm">{review.content}</div>
                  <div className="flex flex-wrap gap-1 text-xs theme-tag">
                    {(review.tags ?? []).map((tag) => (
                      <span key={tag}>#{tag}</span>
                    ))}
                  </div>
                </div>
                <span
                  className={`shrink-0 font-bold ${
                    typeof review.rating === "number"
                      ? ratingColor(review.rating)
                      : ""
                  }`}
                >
                  <RatingValue rating={review.rating ?? 0} />
                </span>
                <RoughButton
                  roughSize="sm"
                  tone="cancel"
                  type="button"
                  onClick={() => {
                    if (review.id == null) return;
                    handleDelete(review.id);
                  }}
                >
                  삭제
                </RoughButton>
              </li>
              {index < reviews.length - 1 && (
                <li aria-hidden="true">
                  <AdminRoughDivider />
                </li>
              )}
            </Fragment>
          ))}
          <li aria-hidden="true">
            <AdminRoughDivider />
          </li>
        </ul>
      )}

      <div className="flex items-center gap-2">
        <RoughButton
          roughSize="sm"
          type="button"
          disabled={pageData.first}
          onClick={() => setPageNumber((p) => Math.max(0, p - 1))}
        >
          이전
        </RoughButton>
        <span className="text-sm">
          {(pageData.number ?? 0) + 1} / {pageData.totalPages ?? 1}
        </span>
        <RoughButton
          roughSize="sm"
          type="button"
          disabled={pageData.last}
          onClick={() => setPageNumber((p) => p + 1)}
        >
          다음
        </RoughButton>
      </div>
    </div>
  );
}
