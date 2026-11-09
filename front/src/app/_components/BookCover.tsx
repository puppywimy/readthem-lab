"use client";

import { type CSSProperties, useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

const MISSING_THUMBNAIL_CACHE_TTL_MS = 5 * 60 * 1000;
const missingThumbnailRequestedAt = new Map<number, number>();
const thumbnailRequestByBookId = new Map<number, Promise<string | null>>();

type BookCoverProps = {
  bookId?: number;
  imgUrl?: string | null;
  title?: string | null;
  loading?: "lazy" | "eager";
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
}: BookCoverProps) {
  const [failedUrl, setFailedUrl] = useState<string | null>(null);
  const normalizedImgUrl = imgUrl?.trim() || null;
  const [fetchedThumbnail, setFetchedThumbnail] = useState<{
    bookId: number;
    imgUrl: string;
  } | null>(null);
  const fetchedThumbnailUrl =
    fetchedThumbnail != null && fetchedThumbnail.bookId === bookId
      ? fetchedThumbnail.imgUrl
      : null;
  const thumbnailUrl = normalizedImgUrl ?? fetchedThumbnailUrl;

  useEffect(() => {
    if (normalizedImgUrl != null) {
      return;
    }

    if (bookId == null) {
      return;
    }

    if (hasRecentMissingThumbnail(bookId)) {
      return;
    }

    let isMounted = true;

    fetchBookThumbnail(bookId)
      .then((nextImgUrl) => {
        if (isMounted && nextImgUrl != null) {
          setFetchedThumbnail({ bookId, imgUrl: nextImgUrl });
        }
      })
      .catch(() => {
        // 실패 시 아무 동작도 하지 않는다 (placeholder 유지)
      });

    return () => {
      isMounted = false;
    };
  }, [bookId, normalizedImgUrl]);

  const hue = ((bookId ?? 0) * 47) % 360;

  return (
    <span className="cover" style={{ "--h": hue } as CSSProperties}>
      <span>{title}</span>
      {thumbnailUrl != null && thumbnailUrl !== failedUrl && (
        // eslint-disable-next-line @next/next/no-img-element
        <img
          loading={loading}
          src={thumbnailUrl}
          alt={`${title ?? "도서"} 표지`}
          onError={() => setFailedUrl(thumbnailUrl)}
        />
      )}
    </span>
  );
}
