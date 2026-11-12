"use client";

import { useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

const MISSING_THUMBNAIL_CACHE_TTL_MS = 5 * 60 * 1000;
const missingThumbnailRequestedAt = new Map<number, number>();
const thumbnailRequestByBookId = new Map<number, Promise<string | null>>();

type BookCoverProps = {
  bookId?: number;
  imgUrl?: string | null;
  title?: string | null;
  loading?: "lazy" | "eager";
  // 실제 표지가 불러와지기 전(또는 없을 때) 스켈레톤·안내 문구 대신 보여 줄 정적 이미지
  fallbackSrc?: string;
};

function hasRecentMissingThumbnail(bookId: number) {
  const requestedAt = missingThumbnailRequestedAt.get(bookId);

  if (requestedAt == null) {
    return false;
  }

  if (Date.now() - requestedAt <= MISSING_THUMBNAIL_CACHE_TTL_MS) {
    return true;
  }

  missingThumbnailRequestedAt.delete(bookId);
  return false;
}

function fetchBookThumbnail(bookId: number) {
  const currentRequest = thumbnailRequestByBookId.get(bookId);

  if (currentRequest != null) {
    return currentRequest;
  }

  const request = apiFetch(`/api/v1/books/${bookId}/thumbnail`)
    .then((data) => {
      const nextImgUrl =
        typeof data?.imgUrl === "string" && data.imgUrl.trim() !== ""
          ? data.imgUrl
          : null;

      if (nextImgUrl == null) {
        missingThumbnailRequestedAt.set(bookId, Date.now());
      }

      return nextImgUrl;
    })
    .finally(() => {
      thumbnailRequestByBookId.delete(bookId);
    });

  thumbnailRequestByBookId.set(bookId, request);
  return request;
}

export default function BookCover({
  bookId,
  imgUrl,
  title,
  loading = "lazy",
  fallbackSrc,
}: BookCoverProps) {
  const [failedUrl, setFailedUrl] = useState<string | null>(null);
  const [loadedUrl, setLoadedUrl] = useState<string | null>(null);
  const normalizedImgUrl = imgUrl?.trim() || null;
  // 썸네일 조회가 끝난 결과. 없거나 조회에 실패했으면 imgUrl이 null이다.
  const [fetchedThumbnail, setFetchedThumbnail] = useState<{
    bookId: number;
    imgUrl: string | null;
  } | null>(null);
  const isThumbnailFetched =
    fetchedThumbnail != null && fetchedThumbnail.bookId === bookId;
  const thumbnailUrl =
    normalizedImgUrl ?? (isThumbnailFetched ? fetchedThumbnail.imgUrl : null);

  useEffect(() => {
    if (normalizedImgUrl != null) {
      return;
    }

    if (bookId == null) {
      return;
    }

    let isMounted = true;

    // 최근에 썸네일이 없다고 확인한 도서는 다시 조회하지 않는다
    const request = hasRecentMissingThumbnail(bookId)
      ? Promise.resolve(null)
      : fetchBookThumbnail(bookId);

    request
      // 조회 실패는 썸네일 없음과 같게 다룬다
      .catch(() => null)
      .then((nextImgUrl) => {
        if (isMounted) {
          setFetchedThumbnail({ bookId, imgUrl: nextImgUrl });
        }
      });

    return () => {
      isMounted = false;
    };
  }, [bookId, normalizedImgUrl]);

  const hasImage = thumbnailUrl != null && thumbnailUrl !== failedUrl;
  // 썸네일 주소를 조회하는 중이거나, 이미지를 아직 다 불러오지 못한 상태
  const isLoading = hasImage
    ? thumbnailUrl !== loadedUrl
    : normalizedImgUrl == null && bookId != null && !isThumbnailFetched;
  // 정적 표지가 있으면 스켈레톤·안내 문구를 쓰지 않는다
  const stateClass =
    fallbackSrc != null
      ? ""
      : isLoading
        ? " loading"
        : hasImage
          ? ""
          : " empty";

  return (
    <span className={`cover${stateClass}`}>
      {fallbackSrc != null ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img src={fallbackSrc} alt="" />
      ) : (
        !isLoading && !hasImage && <span>썸네일이 없습니다</span>
      )}
      {hasImage && (
        // eslint-disable-next-line @next/next/no-img-element
        <img
          loading={loading}
          src={thumbnailUrl}
          // 정적 표지 위에 겹칠 때는 불러오는 동안 대체 글자가 비치지 않게 비운다
          alt={fallbackSrc != null ? "" : `${title ?? "도서"} 표지`}
          // 정적 표지 위에서는 다 불러온 뒤에 서서히 나타나게 한다
          className={
            fallbackSrc == null
              ? undefined
              : thumbnailUrl === loadedUrl
                ? "swap in"
                : "swap"
          }
          onLoad={() => setLoadedUrl(thumbnailUrl)}
          onError={() => setFailedUrl(thumbnailUrl)}
        />
      )}
    </span>
  );
}
