"use client";

import { type ReactNode, useRef } from "react";

import SvgIcon from "@/app/_components/SvgIcon";

export default function BookTrack({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  const trackRef = useRef<HTMLDivElement | null>(null);
  const move = (direction: -1 | 1) =>
    trackRef.current?.scrollBy({ left: direction * 400, behavior: "smooth" });

  return (
    <>
      <div className="head">
        <h2>{title}</h2>
        <div className="arrows">
          <button
            type="button"
            className="icon-btn"
            onClick={() => move(-1)}
            aria-label="이전 도서"
          >
            <SvgIcon name="chevron-left" />
          </button>
          <button
            type="button"
            className="icon-btn"
            onClick={() => move(1)}
            aria-label="다음 도서"
          >
            <SvgIcon name="chevron-right" />
          </button>
        </div>
      </div>
      <div className="track" ref={trackRef}>
        {children}
      </div>
    </>
  );
}
