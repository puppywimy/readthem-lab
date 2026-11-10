"use client";

import { type FormEventHandler, useState } from "react";

import Dialog from "@/app/_components/Dialog";

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
  defaultContent = "",
  defaultRating = 4.5,
  defaultTags,
  onCancel,
  onSubmit,
  submitLabel,
  title,
}: ReviewFormModalProps) {
  const [rating, setRating] = useState(defaultRating);
  const [contentLength, setContentLength] = useState(defaultContent.length);

  return (
    <Dialog onClose={onCancel}>
      <form className="dlg" style={{ padding: 0 }} onSubmit={onSubmit}>
        <h2>{title}</h2>
        <label className="field">
          <span className="field-head">
            별점<output>{rating.toFixed(1)}</output>
          </span>
          <input
            type="range"
            name="rating"
            min="0.5"
            max="5"
            step="0.5"
            value={rating}
            onChange={(e) => setRating(Number(e.target.value))}
          />
        </label>
        <label className="field">
          <span className="field-head">
            리뷰<small>{contentLength}/500</small>
          </span>
          <textarea
            name="content"
            required
            maxLength={500}
            defaultValue={defaultContent}
            onChange={(e) => setContentLength(e.target.value.length)}
          />
        </label>
        <label className="field">
          태그
          <input
            name="tags"
            maxLength={100}
            defaultValue={defaultTags}
            aria-describedby="tag-help"
          />
          <small id="tag-help">쉼표로 구분해 입력합니다.</small>
        </label>
        <div className="dlg-actions">
          <button type="button" className="btn line" onClick={onCancel}>
            취소
          </button>
          <button className="btn">{submitLabel}</button>
        </div>
      </form>
    </Dialog>
  );
}
