"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";

import { Suspense, useCallback, useEffect, useRef, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";

import BookCover from "@/app/_components/BookCover";
import RatingValue from "@/app/_components/RatingValue";
import SearchForm from "@/app/_components/SearchForm";

type BookDto = components["schemas"]["BookDto"];
type PageBookDto = components["schemas"]["PageBookDto"];

const SEARCH_PAGE_SIZE = 20;

function isPageBookDto(data: BookDto[] | PageBookDto): data is PageBookDto {
  return data != null && !Array.isArray(data) && "content" in data;
}

function BookGridSkeleton() {
  return (
    <div className="book-grid">
      {Array.from({ length: 12 }).map((_, index) => (
        <div key={index}>
          <span className="sk cover-sk" />
          <span className="sk line" />
        </div>
      ))}
    </div>
  );
}

function SearchResultsContent({ searchTerm }: { searchTerm: string }) {
  const sentinelRef = useRef<HTMLDivElement | null>(null);
  const loadingRef = useRef(false);

  const [books, setBooks] = useState<BookDto[]>([]);
  const [pageNumber, setPageNumber] = useState(0);
  const [hasNextPage, setHasNextPage] = useState(false);
  const [isInitialLoading, setIsInitialLoading] = useState(
    searchTerm.length > 0,
  );
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [totalElements, setTotalElements] = useState<number | null>(null);

  const requestSearchPage = useCallback(
    (page: number, signal?: AbortSignal, onStart?: () => void) => {
      if (searchTerm.length === 0 || loadingRef.current) {
        return;
      }

      loadingRef.current = true;
      onStart?.();

      apiFetch(
        `/api/v1/books/search?searchTerm=${encodeURIComponent(
          searchTerm,
        )}&page=${page}&size=${SEARCH_PAGE_SIZE}`,
        { signal },
      )
        .then((data: BookDto[] | PageBookDto) => {
          if (signal?.aborted) return;

          const nextBooks = isPageBookDto(data) ? (data.content ?? []) : data;
          const nextPageNumber = isPageBookDto(data)
            ? (data.number ?? page)
            : page;
          const isLastPage = isPageBookDto(data)
            ? (data.last ?? true)
            : nextBooks.length < SEARCH_PAGE_SIZE;

          setBooks((prev) =>
            page === 0 ? nextBooks : [...prev, ...nextBooks],
          );
          setPageNumber(nextPageNumber);
          setHasNextPage(!isLastPage);
          if (isPageBookDto(data)) setTotalElements(data.totalElements ?? null);
        })
        .catch((error) => {
          if (signal?.aborted) return;
          goToErrorPage(error);
        })
        .finally(() => {
          if (signal?.aborted) return;

          loadingRef.current = false;
          setIsInitialLoading(false);
          setIsLoadingMore(false);
        });
    },
    [searchTerm],
  );

  const loadMorePage = useCallback(() => {
    requestSearchPage(pageNumber + 1, undefined, () => {
      setIsLoadingMore(true);
    });
  }, [pageNumber, requestSearchPage]);

  useEffect(() => {
    const abortController = new AbortController();

    if (searchTerm.length === 0) {
      return;
    }

    requestSearchPage(0, abortController.signal);

    return () => {
      abortController.abort();
      loadingRef.current = false;
    };
  }, [requestSearchPage, searchTerm]);

  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (sentinel == null) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && hasNextPage && !loadingRef.current) {
          loadMorePage();
        }
      },
      { rootMargin: "240px 0px" },
    );

    observer.observe(sentinel);

    return () => {
      observer.disconnect();
    };
  }, [hasNextPage, loadMorePage]);

  const count = isInitialLoading
    ? "불러오는 중"
    : `${(totalElements ?? books.length).toLocaleString()}권`;

  return (
    <div className="wrap split">
      <div className="sticky-col search-col">
        <h1 className="page-title">
          {searchTerm ? `'${searchTerm}' 검색 결과` : "도서 검색"}
        </h1>
        {searchTerm && <p className="count">{count}</p>}
        <SearchForm id="search-q" initialTerm={searchTerm} />
      </div>
      <div aria-busy={isInitialLoading}>
        {searchTerm.length === 0 ? (
          <div className="state">
            <strong>검색어를 입력해 주세요.</strong>
            제목이나 저자 이름으로 찾을 수 있습니다.
          </div>
        ) : isInitialLoading ? (
          <BookGridSkeleton />
        ) : books.length === 0 ? (
          <div className="state">
            <strong>찾는 도서가 없습니다.</strong>
            다른 제목이나 저자 이름으로 검색해 보세요.
          </div>
        ) : (
          <>
            <div className="book-grid swap">
              {books.map((book) => (
                <Link key={book.id} href={`/books/detail?id=${book.id}`}>
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
              ))}
            </div>
            <div ref={sentinelRef} className="end">
              {isLoadingMore
                ? "더 불러오는 중..."
                : hasNextPage
                  ? " "
                  : "마지막 검색 결과입니다."}
            </div>
          </>
        )}
      </div>
    </div>
  );
}

function SearchResults() {
  const searchParams = useSearchParams();
  const searchTerm = (searchParams.get("searchTerm") ?? "").trim();

  return <SearchResultsContent key={searchTerm} searchTerm={searchTerm} />;
}

export default function Page() {
  return (
    <Suspense
      fallback={
        <div className="wrap split">
          <div />
          <BookGridSkeleton />
        </div>
      }
    >
      <SearchResults />
    </Suspense>
  );
}
