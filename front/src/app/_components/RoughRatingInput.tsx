"use client";

import { useRef, useState } from "react";

import { ratingColor, ratingFillColor } from "@/lib/ratingColor";

import { RoughStarIcon } from "@/app/_components/RatingValue";

function StarPreview({
  fillPercent,
  fill,
}: {
  fillPercent: 0 | 50 | 100;
  fill: string;
}) {
  return (
    <span className="relative inline-block h-7 w-7 shrink-0">
      <RoughStarIcon fill="#e5e7eb" className="rough-overlay" />
      {fillPercent > 0 && (
        <span
          className="absolute inset-y-0 left-0 overflow-hidden"
          style={{ width: `${fillPercent}%` }}
        >
          <RoughStarIcon fill={fill} className="rough-overlay" />
        </span>
      )}
    </span>
  );
}

export default function RoughRatingInput({
  name,
  defaultValue,
  label = "평점",
}: {
  name: string;
  defaultValue?: number;
  label?: string;
}) {
  const [selectedValue, setSelectedValue] = useState(
    defaultValue != null ? Math.round(defaultValue * 2) : 0,
  );
  const [previewValue, setPreviewValue] = useState<number | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const wrapRef = useRef<HTMLDivElement | null>(null);

  const activeValue = previewValue ?? selectedValue;
  const ratingValue = selectedValue === 0 ? "" : `${selectedValue / 2}`;
  const activeRating = activeValue / 2;
  const activeFill =
    activeValue === 0 ? "#e5e7eb" : ratingFillColor(activeRating);
  const activeTextColor =
    activeValue === 0 ? "text-gray-600" : ratingColor(activeRating);

  const ratingFromPointer = (clientX: number) => {
    const wrap = wrapRef.current;
    if (wrap == null) return 0;

    const rect = wrap.getBoundingClientRect();
    const x = Math.min(Math.max(clientX - rect.left, 0), rect.width);
    if (x === 0) return 0;

    const ratio = x / rect.width;
    return Math.min(10, Math.max(1, Math.ceil(ratio * 10)));
  };

  const commitFromPointer = (clientX: number) => {
    const nextValue = ratingFromPointer(clientX);
    setSelectedValue(nextValue);
    setPreviewValue(nextValue);
  };

  return (
    <div className="flex min-w-0 flex-col gap-2">
      <input type="hidden" name={name} value={ratingValue} />
      <div className="flex flex-wrap items-center gap-2 text-sm">
        <span>{label}</span>
        <span className={`min-w-12 ${activeTextColor}`}>
          {selectedValue === 0 ? "미선택" : `${selectedValue / 2}점`}
        </span>
        <button
          className="text-xs text-gray-500 underline"
          type="button"
          onClick={() => {
            setSelectedValue(0);
            setPreviewValue(null);
          }}
        >
          선택 해제
        </button>
      </div>
      <div
        ref={wrapRef}
        className="rough-rating-input-wrap"
        role="slider"
        aria-label={label}
        aria-valuemin={0}
        aria-valuemax={5}
        aria-valuenow={selectedValue / 2}
        aria-valuetext={
          selectedValue === 0 ? "미선택" : `${selectedValue / 2}점`
        }
        tabIndex={0}
        onPointerDown={(e) => {
          e.preventDefault();
          setIsDragging(true);
          commitFromPointer(e.clientX);
        }}
        onPointerMove={(e) => {
          if (isDragging) {
            commitFromPointer(e.clientX);
          }
        }}
        onPointerUp={(e) => {
          commitFromPointer(e.clientX);
          setIsDragging(false);
          setPreviewValue(null);
        }}
        onPointerCancel={() => {
          setIsDragging(false);
          setPreviewValue(null);
        }}
        onKeyDown={(e) => {
          if (e.key === "ArrowRight" || e.key === "ArrowUp") {
            e.preventDefault();
            setSelectedValue((prev) => Math.min(10, prev + 1));
            return;
          }
          if (e.key === "ArrowLeft" || e.key === "ArrowDown") {
            e.preventDefault();
            setSelectedValue((prev) => Math.max(0, prev - 1));
            return;
          }
          if (e.key === "Backspace" || e.key === "Delete") {
            e.preventDefault();
            setSelectedValue(0);
          }
        }}
      >
        <div className="rough-rating-input-stars" aria-hidden="true">
          {Array.from({ length: 5 }, (_, index) => {
            const threshold = (index + 1) * 2;
            const fillPercent =
              activeValue >= threshold
                ? 100
                : activeValue === threshold - 1
                  ? 50
                  : 0;

            return (
              <StarPreview
                key={index}
                fill={activeFill}
                fillPercent={fillPercent}
              />
            );
          })}
        </div>
      </div>
    </div>
  );
}
