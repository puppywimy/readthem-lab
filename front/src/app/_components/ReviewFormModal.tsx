"use client";

import type { FormEventHandler } from "react";

import RoughButton from "@/app/_components/RoughButton";
import RoughFrame from "@/app/_components/RoughFrame";
import { RoughInput, RoughTextarea } from "@/app/_components/RoughInput";
import RoughRatingInput from "@/app/_components/RoughRatingInput";

type ReviewFormModalProps = {
  defaultContent?: string;
  defaultRating?: number;
  defaultTags?: string;
  onSubmit: FormEventHandler<HTMLFormElement>;
  onCancel: () => void;
  submitLabel: string;
  title: string;
};

export default function ReviewFormModal({
  defaultContent,
  defaultRating,
  defaultTags,
  onCancel,
  onSubmit,
  submitLabel,
  title,
}: ReviewFormModalProps) {
  return (
    <div
      className="rough-modal-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="review-form-title"
    >
      <form
        className="rough-modal-card flex w-full max-w-md flex-col gap-3"
        onSubmit={onSubmit}
      >
        <RoughFrame className="rough-overlay" variant="card" />
        <h2 id="review-form-title" className="text-xl font-bold">
          {title}
        </h2>
        <RoughRatingInput
          name="rating"
          defaultValue={defaultRating}
          label="평점"
        />
        <RoughTextarea
          name="content"
          defaultValue={defaultContent}
          maxLength={30}
          placeholder="리뷰 내용 (2~30자)"
          rows={2}
        />
        <RoughInput
          inputClassName="px-2"
          type="text"
          name="tags"
          defaultValue={defaultTags}
          placeholder="태그 (쉼표로 구분)"
        />
        <div className="flex justify-end gap-2">
          <RoughButton
            roughSize="sm"
            tone="cancel"
            type="button"
            onClick={onCancel}
          >
            취소
          </RoughButton>
          <RoughButton roughSize="sm" tone="submit" type="submit">
            {submitLabel}
          </RoughButton>
        </div>
      </form>
    </div>
  );
}
