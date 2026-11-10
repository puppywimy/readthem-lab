"use client";

import { useSearchParams } from "next/navigation";

import { Suspense, useEffect, useState } from "react";

import { API_BASE_URL, apiFetch } from "@/lib/backend/client";

import { resolveAvatarUrl } from "@/lib/avatar";
import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { formatDate } from "@/lib/formatDate";

import { LibraryHeader, LibraryReviewRow } from "@/app/_components/Library";
import RatingValue from "@/app/_components/RatingValue";

type MemberDto = components["schemas"]["MemberDto"];
type ReviewsByMemberDto = components["schemas"]["ReviewsByMemberDto"];
function MemberDetailSkeleton() {
  return (
    <div className="wrap">
      {Array.from({ length: 4 }).map((_, index) => (
        <span
          key={index}
          className="sk line"
          style={{ height: 96, marginTop: 20 }}
        />
      ))}
    </div>
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
  const averageNumber = typeof average === "number" ? average : 0;
  const reviewResults = reviewData.results ?? [];

  return (
    <>
      <LibraryHeader
        githubId={member.githubId}
        githubLink={member.githubLink}
        imageUrl={resolveAvatarUrl(member)}
        stats={[
          ["작성한 리뷰", reviewResults.length],
          ["평균 별점", averageNumber.toFixed(1)],
        ]}
      />

      <section
        className="wrap"
        style={{ paddingBlock: "clamp(40px,5vw,64px)" }}
      >
        <h2>작성한 리뷰</h2>
        <div className="tab-body">
          {reviewResults.length === 0 ? (
            <div className="state">
              <strong>작성한 리뷰가 없습니다.</strong>
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
                />
              ))}
            </ul>
          )}
        </div>
      </section>

      {member.githubId && (
        <section className="band">
          <div className="wrap widget">
            <h2>{member.githubId}의 책장</h2>
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={`${API_BASE_URL}/api/v2/widgets/${member.githubId}`}
              alt="서재 위젯"
            />
          </div>
        </section>
      )}
    </>
  );
}

export default function Page() {
  return (
    <Suspense fallback={<MemberDetailSkeleton />}>
      <MemberDetail />
    </Suspense>
  );
}
