"use client";

import type { InputHTMLAttributes, TextareaHTMLAttributes } from "react";
import { forwardRef } from "react";

import RoughFrame from "@/app/_components/RoughFrame";

type RoughInputSize = "sm" | "md" | "compact";
type RoughInputTone = "neutral" | "paper" | "history" | "wish";

const inputSizeClass: Record<RoughInputSize, string> = {
  sm: "rough-input-sm",
  md: "",
  compact: "rough-input-compact",
};

const inputToneClass: Record<RoughInputTone, string> = {
  neutral: "",
  paper: "rough-input-paper",
  history: "rough-input-history",
  wish: "rough-input-wish",
};

type RoughInputProps = InputHTMLAttributes<HTMLInputElement> & {
  wrapperClassName?: string;
  inputClassName?: string;
  roughSize?: RoughInputSize;
  tone?: RoughInputTone;
};

export const RoughInput = forwardRef<HTMLInputElement, RoughInputProps>(
  function RoughInput(
    {
      wrapperClassName = "",
      inputClassName = "",
      roughSize = "md",
      tone = "neutral",
      ...props
    },
    ref,
  ) {
    return (
      <span
        className={[
          "rough-input-border",
          inputSizeClass[roughSize],
          inputToneClass[tone],
          wrapperClassName,
        ]
          .filter(Boolean)
          .join(" ")}
      >
        <RoughFrame className="rough-overlay" variant="search" />
        <input
          ref={ref}
          className={[
            "relative z-10 h-full w-full border-0 bg-transparent px-3 py-1 text-sm outline-none",
            inputClassName,
          ]
            .filter(Boolean)
            .join(" ")}
          {...props}
        />
      </span>
    );
  },
);

type RoughTextareaProps = TextareaHTMLAttributes<HTMLTextAreaElement> & {
  wrapperClassName?: string;
  textareaClassName?: string;
  tone?: RoughInputTone;
};

export function RoughTextarea({
  wrapperClassName = "",
  textareaClassName = "",
  tone = "neutral",
  ...props
}: RoughTextareaProps) {
  return (
    <span
      className={[
        "rough-textarea-border",
        inputToneClass[tone],
        wrapperClassName,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <RoughFrame className="rough-overlay" variant="search" />
      <textarea
        className={[
          "relative z-10 w-full resize-none border-0 bg-transparent p-2 text-sm outline-none",
          textareaClassName,
        ]
          .filter(Boolean)
          .join(" ")}
        {...props}
      />
    </span>
  );
}
