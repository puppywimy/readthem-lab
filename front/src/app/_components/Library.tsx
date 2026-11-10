import Link from "next/link";

import type { ReactNode } from "react";

import type { components } from "@/lib/backend/apiV1/schema";

import Avatar from "@/app/_components/Avatar";
import BookCover from "@/app/_components/BookCover";
import SvgIcon from "@/app/_components/SvgIcon";

type LibraryReview = NonNullable<
  components["schemas"]["ReviewsByMemberDto"]["results"]
>[number];

export function LibraryHeader({
  githubId,
  githubLink,
  imageUrl,
  stats,
}: {
  githubId: string | null | undefined;
  githubLink?: string | null;
  imageUrl?: string | null;
  stats: [string, string | number][];
}) {
  return (
    <div className="band">
      <div className="wrap me">
        <Avatar label={githubId} imageUrl={imageUrl} size={96} />
        <div>
          <h1 className="page-title">{githubId}의 서재</h1>
          {githubLink && (
            <a
              className="gh"
              href={githubLink}
              target="_blank"
              rel="noreferrer"
            >
              <SvgIcon name="github" />
              {githubId}
            </a>
          )}
        </div>
        <dl className="me-stats">
          {stats.map(([label, value]) => (
            <div key={label}>
              <dt>{label}</dt>
              <dd>{value}</dd>
            </div>
          ))}
        </dl>
      </div>
    </div>
  );
}

export function LibraryReviewRow({
  review,
  meta,
  actions,
}: {
  review: LibraryReview;
  meta: ReactNode;
  actions?: ReactNode;
}) {
  const href = `/books/detail?id=${review.bookId}`;
  const title = review.bookTitle ?? `책 #${review.bookId}`;

  return (
    <li className="my-row">
      <Link href={href} aria-label={`${title} 상세 보기`}>
        <BookCover
          bookId={review.bookId}
          imgUrl={review.bookImgUrl}
          title={title}
        />
      </Link>
      <div>
        <h2>
          <Link href={href}>{title}</Link>
        </h2>
        <p className="meta">{meta}</p>
        <q>{review.content}</q>
      </div>
      {actions && <div className="row-actions">{actions}</div>}
    </li>
  );
}
