"use client";

import { useSearchParams } from "next/navigation";

import { Suspense, useCallback, useEffect, useRef, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";

import BookGrid from "@/app/_components/BookGrid";

type BookDto = components["schemas"]["BookDto"];
type PageBookDto = components["schemas"]["PageBookDto"];

const SEARCH_PAGE_SIZE = 20;

function isPageBookDto(data: BookDto[] | PageBookDto): data is PageBookDto {
  return data != null && !Array.isArray(data) && "content" in data;
}

function searchLoadingChanged(isLoading: boolean) {
  window.dispatchEvent(
    new CustomEvent("readthem-search-loading", { detail: isLoading }),
  );
}

function SearchResultsSkeleton({ searchTerm }: { searchTerm?: string }) {
  return (
    <>
      <div className="px-1">
        <div className="book-skeleton h-9 w-56 max-w-full rounded" />
        {searchTerm && (
          <div className="mt-2 text-sm theme-muted">
            &apos;{searchTerm}&apos; 검색 결과를 불러오는 중...
          </div>
        )}
      </div>
      <div className="mt-4">
        <BookGrid isLoading />
      </div>
    </>
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

  const requestSearchPage = useCallback(
    (page: number, signal?: AbortSignal, onStart?: () => void) => {
      if (searchTerm.length === 0 || loadingRef.current) {
        return;
      }

      loadingRef.current = true;
      searchLoadingChanged(true);
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
        })
        .catch((error) => {
          if (signal?.aborted) return;
          goToErrorPage(error);
        })
        .finally(() => {
          if (signal?.aborted) return;

          loadingRef.current = false;
          searchLoadingChanged(false);
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
      searchLoadingChanged(false);
      return;
    }

    requestSearchPage(0, abortController.signal);

    return () => {
      abortController.abort();
      loadingRef.current = false;
      searchLoadingChanged(false);
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

  if (searchTerm.length === 0) {
    return <div>검색어를 입력해주세요.</div>;
  }

  return (
    <>
      <h1>&apos;{searchTerm}&apos; 검색 결과</h1>
      {isInitialLoading ? (
        <div className="mt-4">
          <BookGrid isLoading />
        </div>
      ) : (
        <>
          <BookGrid books={books} />
          <div
            ref={sentinelRef}
            className="py-6 text-center text-sm theme-muted"
          >
            {isLoadingMore
              ? "더 불러오는 중..."
              : hasNextPage
                ? " "
                : books.length > 0
                  ? "마지막 검색 결과입니다."
                  : ""}
          </div>
        </>
      )}
    </>
  );
}

function SearchResults() {
  const searchParams = useSearchParams();
  const searchTerm = (searchParams.get("searchTerm") ?? "").trim();

  return <SearchResultsContent key={searchTerm} searchTerm={searchTerm} />;
}

export default function Page() {
  return (
    <Suspense fallback={<SearchResultsSkeleton />}>
      <SearchResults />
    </Suspense>
  );
}
