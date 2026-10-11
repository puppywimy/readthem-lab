"use client";

import type { ReactNode } from "react";

import RoughBar from "@/app/_components/RoughBar";

type BookTapeProps = {
  title?: string;
  className?: string;
  children?: ReactNode;
  mode?: "overlay" | "inline";
};

export default function BookTape({
  title,
  className = "",
  children,
  mode = "overlay",
}: BookTapeProps) {
  const positionClassName =
    mode === "overlay"
      ? "!absolute !bottom-0"
      : "relative !bottom-auto !left-auto !right-auto !top-auto";

  return (
    <div
      className={`rough-review-tape z-0 w-full overflow-hidden bg-[var(--surface-soft)] text-[var(--foreground)] ${positionClassName} ${className}`}
    >
      <RoughBar
        className={`w-full ${mode === "overlay" ? "h-4" : "h-5"}`}
        fill="transparent"
        variant="line"
        lineInset={0}
      />
      <div className="px-2 py-2">{children ?? title}</div>
    </div>
  );
}
