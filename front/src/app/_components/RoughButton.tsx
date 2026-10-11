"use client";

import type { ButtonHTMLAttributes, ReactNode } from "react";

import RoughFrame from "@/app/_components/RoughFrame";

type RoughButtonTone =
  | "neutral"
  | "submit"
  | "history"
  | "wish"
  | "wishActive"
  | "cancel";

type RoughButtonSize = "sm" | "md" | "lg";

const toneClass: Record<RoughButtonTone, string> = {
  neutral: "rough-action-neutral",
  submit: "rough-action-submit",
  history: "rough-action-history",
  wish: "rough-action-wish",
  wishActive: "rough-action-wish-active",
  cancel: "rough-action-cancel",
};

const sizeClass: Record<RoughButtonSize, string> = {
  sm: "rough-action-sm",
  md: "",
  lg: "rough-action-lg",
};

type RoughButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode;
  tone?: RoughButtonTone;
  roughSize?: RoughButtonSize;
  fullWidth?: boolean;
};

export default function RoughButton({
  children,
  className = "",
  tone = "neutral",
  roughSize = "md",
  fullWidth = false,
  ...props
}: RoughButtonProps) {
  return (
    <button
      className={[
        "rough-action-button",
        toneClass[tone],
        sizeClass[roughSize],
        fullWidth ? "w-full" : "",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      {...props}
    >
      <span className="rough-action-button-frame">
        <RoughFrame className="rough-overlay" variant="search" />
      </span>
      <span className="relative z-10 inline-flex items-center justify-center gap-1.5 whitespace-nowrap">
        {children}
      </span>
    </button>
  );
}
