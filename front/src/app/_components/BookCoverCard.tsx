"use client";

import Link from "next/link";

import BookThumbnail from "@/app/_components/BookThumbnail";
import RoughFrame from "@/app/_components/RoughFrame";

type BookCoverCardProps = {
  bookId?: number;
  imgUrl?: string | null;
  title?: string | null;
  href: string;
  ariaLabel: string;
  className?: string;
  coverClassName?: string;
  placeholderClassName?: string;
  placeholderText?: string;
  children?: React.ReactNode;
};

export default function BookCoverCard({
  bookId,
  imgUrl,
  title,
  href,
  ariaLabel,
  className = "",
  coverClassName = "",
  placeholderClassName,
  placeholderText,
  children,
}: BookCoverCardProps) {
  return (
    <Link
      href={href}
      className={`rough-book-card group relative block overflow-hidden rounded-xl bg-white ${className}`}
      aria-label={ariaLabel}
    >
      <RoughFrame
        className="rough-overlay rough-card-line rough-book-cover-line !z-30"
        variant="card"
      />
      <div className={`relative h-full w-full ${coverClassName}`}>
        <BookThumbnail
          bookId={bookId}
          imgUrl={imgUrl}
          title={title}
          className="absolute inset-0 z-0 h-full w-full object-cover"
          placeholderClassName={`absolute inset-x-0 top-0 ${
            children != null ? "bottom-[4.25rem]" : "bottom-0"
          } flex items-center justify-center text-center ${placeholderClassName ?? ""}`}
          placeholderText={placeholderText}
          altClassName="text-xs text-gray-500"
        />
      </div>
      {children != null && (
        <div className="absolute inset-x-0 bottom-0 z-10">{children}</div>
      )}
    </Link>
  );
}
