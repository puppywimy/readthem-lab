"use client";

import Link from "next/link";

import { type RefObject, useEffect, useMemo, useRef, useState } from "react";

import type { components } from "@/lib/backend/apiV1/schema";
import { ratingColor } from "@/lib/ratingColor";

import BookThumbnail from "@/app/_components/BookThumbnail";
import RatingValue from "@/app/_components/RatingValue";
import RoughFrame from "@/app/_components/RoughFrame";

type BookDto = components["schemas"]["BookDto"];
const skeletonClassName = "book-skeleton";

type BookGridProps = {
  books?: BookDto[];
  isLoading?: boolean;
  layout?: "grid" | "horizontal";
  scrollRef?: RefObject<HTMLUListElement | null>;
  horizontalItemClassName?: string;
};

type BookGridItemProps = {
  book: BookDto;
  coverClassName: string;
  index: number;
  layout: "grid" | "horizontal";
};

function SkeletonCard({ coverClassName }: { coverClassName: string }) {
  return (
    <div className="flex h-full flex-col gap-2">
      <div className={coverClassName}>
        <div className={`h-full w-full rounded-xl ${skeletonClassName}`} />
      </div>
      <span className="book-title">
        <span className={`block h-5 w-full rounded ${skeletonClassName}`} />
        <span className={`mt-2 block h-5 w-3/4 rounded ${skeletonClassName}`} />
      </span>
      <span className="book-rating block h-6">
        <span className={`block h-5 w-16 rounded ${skeletonClassName}`} />
      </span>
    </div>
  );
}

function BookGridItem({
  book,
  coverClassName,
  index,
  layout,
}: BookGridItemProps) {
  const [isImageLoaded, setIsImageLoaded] = useState(false);
  const imgRef = useRef<HTMLImageElement>(null);

  useEffect(() => {
    if (imgRef.current?.complete) {
      setIsImageLoaded(true);
    }
  });

  const averageRating =
    typeof book.averageRating === "number" ? book.averageRating : 0;

  return (
    <Link
      href={`/books/detail?id=${book.id}`}
      className="book-link flex h-full flex-col gap-2"
    >
      <div className="rough-book-card rounded-xl bg-white">
        <RoughFrame
          className="rough-overlay rough-card-line rough-book-cover-line"
          variant="card"
        />
        <div
          className={`${coverClassName} ${book.imgUrl ? "" : "book-cover-placeholder"}`}
        >
          <BookThumbnail
            bookId={book.id}
            imgUrl={book.imgUrl}
            title={book.title}
            imgRef={imgRef}
            onLoad={() => setIsImageLoaded(true)}
            placeholderClassName="text-sm text-gray-400"
            altClassName="text-sm text-gray-400"
            imgClassName={`relative z-0 h-full w-full object-cover transition-opacity duration-500 ease-out ${
              isImageLoaded ? "opacity-100" : "opacity-0"
            }`}
          />
          {layout === "horizontal" && (
            <>
              <span className="book-rank-overlay" />
              <span className="book-rank-number">{index + 1}</span>
            </>
          )}
        </div>
      </div>
      <span className="book-title">{book.title}</span>
      <span
        className={`book-rating font-bold ${ratingColor(averageRating)}`}
      >
        <RatingValue rating={averageRating} />
      </span>
    </Link>
  );
}

export default function BookGrid({
  books = [],
  isLoading = false,
  layout = "grid",
  scrollRef,
  horizontalItemClassName,
}: BookGridProps) {
  const [visibleContentKey, setVisibleContentKey] = useState("");

  const contentKey = useMemo(
    () => books.map((book) => `${book.id}:${book.imgUrl ?? ""}`).join("\n"),
    [books],
  );

  const isContentVisible =
    !isLoading && books.length > 0 && visibleContentKey === contentKey;

  useEffect(() => {
    if (isLoading || books.length === 0) {
      return;
    }

    const frameId = window.requestAnimationFrame(() => {
      setVisibleContentKey(contentKey);
    });

    return () => {
      window.cancelAnimationFrame(frameId);
    };
  }, [books.length, contentKey, isLoading]);

  const listClassName =
    layout === "horizontal"
      ? "book-scroll-list flex snap-x snap-mandatory gap-5 overflow-x-auto py-2 pb-4"
      : "grid grid-cols-2 gap-4 p-2 sm:grid-cols-4";

  const itemClassName =
    layout === "horizontal"
      ? `book-horizontal-item group flex shrink-0 flex-col ${
          horizontalItemClassName ?? "w-40 sm:w-44"
        }`
      : "group flex flex-col";

  const coverClassName =
    "relative flex aspect-[2/3] w-full items-center justify-center overflow-hidden rounded-xl";

  const skeletonItems = Array.from({ length: books.length || 10 });

  if (!isLoading && books.length === 0) {
    return <div>도서가 없습니다.</div>;
  }

  const list = (
    <ul className={listClassName} ref={scrollRef}>
      {isLoading
        ? skeletonItems.map((_, index) => (
            <li
              key={index}
              className={`${itemClassName} ${
                index === skeletonItems.length - 1 ? "snap-end" : "snap-start"
              }`}
              aria-hidden="true"
            >
              <SkeletonCard coverClassName={coverClassName} />
            </li>
          ))
        : books.map((book, index) => (
            <li
              key={`${book.id}:${book.imgUrl ?? ""}`}
              className={`${itemClassName} relative ${
                index === books.length - 1 ? "snap-end" : "snap-start"
              }`}
              data-book-item="true"
            >
              <div
                className={`pointer-events-none absolute inset-0 transition-opacity duration-300 ease-out ${
                  isContentVisible ? "opacity-0" : "opacity-100"
                }`}
                aria-hidden="true"
              >
                <SkeletonCard coverClassName={coverClassName} />
              </div>
              <div
                className={`transition-opacity duration-300 ease-out ${
                  isContentVisible ? "opacity-100" : "opacity-0"
                }`}
              >
                <BookGridItem
                  book={book}
                  coverClassName={coverClassName}
                  index={index}
                  layout={layout}
                />
              </div>
            </li>
          ))}
    </ul>
  );

  if (layout === "horizontal") {
    return <div className="book-scroll-fade">{list}</div>;
  }

  return list;
}
