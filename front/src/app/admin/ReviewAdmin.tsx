"use client";

import { useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { useToast } from "@/lib/toast/ToastProvider";

import Pager from "@/app/admin/Pager";

type PageAdminReviewDto = components["schemas"]["PageAdminReviewDto"];

export default function ReviewAdmin() {
  const { showToast, showErrorToast, confirmAction } = useToast();
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

  const handleDelete = async (reviewId: number) => {
    if (!(await confirmAction("이 리뷰를 삭제하시겠습니까?"))) return;

    apiFetch(`/api/v1/reviews/${reviewId}`, { method: "DELETE" })
      .then((data) => {
        showToast(data?.message ?? "리뷰를 삭제했습니다.");
        loadReviews(pageNumber);
      })
      .catch(showErrorToast);
  };

  if (pageData == null)
    return (
      <div aria-busy="true" style={{ marginTop: 28 }}>
        <span className="sk line" style={{ height: 44 }} />
      </div>
    );

  const reviews = pageData.content ?? [];

  return (
    <>
      {reviews.length === 0 ? (
        <div className="state">
          <strong>리뷰가 없습니다.</strong>
        </div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>도서</th>
                <th>작성자</th>
                <th className="num">별점</th>
                <th>내용</th>
                <th className="num">좋아요</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {reviews.map((review, index) => (
                <tr key={review.id ?? index}>
                  <td>
                    <span className="cell-book">{review.bookTitle}</span>
                  </td>
                  <td className="nowrap">
                    {review.reviewer?.githubId ?? "탈퇴한 사용자"}
                  </td>
                  <td className="num">{(review.rating ?? 0).toFixed(1)}</td>
                  <td className="clip">{review.content}</td>
                  <td className="num">{review.likeCount ?? 0}</td>
                  <td className="fit">
                    <button
                      type="button"
                      className="text-btn"
                      onClick={() => {
                        if (review.id != null) handleDelete(review.id);
                      }}
                    >
                      삭제
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <Pager
        page={pageData.number ?? 0}
        totalPages={pageData.totalPages ?? 1}
        onChange={setPageNumber}
      />
    </>
  );
}
