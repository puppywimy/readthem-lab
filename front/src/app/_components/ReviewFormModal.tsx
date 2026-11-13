"use client";

import {
  type KeyboardEvent,
  type PointerEvent,
  type SyntheticEvent,
  useEffect,
  useRef,
  useState,
} from "react";

import Dialog from "@/app/_components/Dialog";
import { StarLayers } from "@/app/_components/RatingStars";

export type ReviewFormValues = {
  rating: number;
  content: string;
  tags: string[];
};

type ReviewFormModalProps = {
  defaultContent?: string;
  defaultRating?: number;
  defaultTags?: string[];
  onSubmit: (values: ReviewFormValues) => void;
  onCancel: () => void;
  submitLabel: string;
  title: string;
};

// 백엔드 검증(ValidTags)과 같은 한도
const MAX_TAGS = 5;
const MAX_TAG_LENGTH = 30;

const TAG_HELP = "Enter로 태그를 추가합니다.";
const TAG_DUP = "이미 입력한 태그입니다.";
const TAG_FULL = `태그는 ${MAX_TAGS}개까지 입력할 수 있습니다.`;

export default function ReviewFormModal({
  defaultContent = "",
  defaultRating = 0,
  defaultTags = [],
  onCancel,
  onSubmit,
  submitLabel,
  title,
}: ReviewFormModalProps) {
  const [rating, setRating] = useState(defaultRating);
  const [content, setContent] = useState(defaultContent);
  const [tags, setTags] = useState(defaultTags);
  const [tagText, setTagText] = useState("");
  const [tagError, setTagError] = useState<string | null>(null);
  const ratingRef = useRef<HTMLInputElement | null>(null);
  const tagInputRef = useRef<HTMLInputElement | null>(null);

  // 0은 미선택: 제출을 막는다
  useEffect(() => {
    ratingRef.current?.setCustomValidity(rating ? "" : "별점을 선택해 주세요.");
  }, [rating]);

  // 별의 왼쪽 절반은 .5, 오른쪽 절반은 정수로 올림한다. 누른 채 끌어도 바뀐다.
  const pickStar = (e: PointerEvent<HTMLDivElement>) => {
    if (e.type === "pointermove" && !e.buttons) return;
    const { left, width } = e.currentTarget.getBoundingClientRect();
    const ratio = (e.clientX - left) / width;
    setRating(Math.min(5, Math.max(0.5, Math.ceil(ratio * 10) / 2)));
    ratingRef.current?.focus();
  };

  const rejectTag = (message: string) => {
    setTagError(message);
    // 연달아 거절돼도 진동이 다시 돈다
    tagInputRef.current?.animate(
      [
        { translate: "0" },
        { translate: "-4px 0" },
        { translate: "4px 0" },
        { translate: "-4px 0" },
        { translate: "0" },
      ],
      { duration: 300 },
    );
  };

  // 입력한 글자를 태그로 넣는다. 넣을 수 없으면 입력창에 남긴 채 알리고 null을 돌려준다.
  const commitTag = () => {
    const tag = tagText.trim();
    if (!tag) {
      setTagText("");
      return tags;
    }
    if (tags.includes(tag)) {
      setTagText(tag);
      rejectTag(TAG_DUP);
      return null;
    }
    if (tags.length >= MAX_TAGS) {
      rejectTag(TAG_FULL);
      return null;
    }
    const nextTags = [...tags, tag];
    setTags(nextTags);
    setTagText("");
    setTagError(null);
    return nextTags;
  };

  const handleTagKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      e.preventDefault(); // 태그 확정이지 폼 제출이 아니다
      if (!e.nativeEvent.isComposing) commitTag();
    } else if (e.key === "Backspace" && !tagText && tags.length > 0) {
      setTags(tags.slice(0, -1));
      setTagError(null);
    }
  };

  const handleSubmit = (e: SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    // Enter를 누르지 않고 남겨 둔 글자도 태그로 넣는다
    const nextTags = commitTag();
    if (nextTags == null) {
      tagInputRef.current?.focus();
      return;
    }
    onSubmit({ rating, content: content.trim(), tags: nextTags });
  };

  return (
    <Dialog onClose={onCancel}>
      <form className="dlg" style={{ padding: 0 }} onSubmit={handleSubmit}>
        <h2>{title}</h2>
        <div className="field">
          <label htmlFor="review-rating">
            별점<em className="mark req">필수</em>
          </label>
          <div className="rating-row">
            <div
              className="stars"
              style={{ "--v": rating } as React.CSSProperties}
              onPointerDown={pickStar}
              onPointerMove={pickStar}
              // 기본 mousedown이 포커스를 range 밖으로 옮기지 않게 막는다 (누른 뒤 방향키로 이어서 조절)
              onMouseDown={(e) => e.preventDefault()}
            >
              <StarLayers />
              <input
                ref={ratingRef}
                id="review-rating"
                type="range"
                min="0"
                max="5"
                step="0.5"
                value={rating}
                onChange={(e) => setRating(Number(e.target.value))}
              />
            </div>
            <output htmlFor="review-rating">{rating.toFixed(1)}</output>
          </div>
        </div>
        <label className="field">
          <span className="field-head">
            <span>
              리뷰<em className="mark">선택</em>
            </span>
            <small>{content.length}/500</small>
          </span>
          <textarea
            maxLength={500}
            value={content}
            onChange={(e) => setContent(e.target.value)}
          />
        </label>
        <div className="field">
          <label htmlFor="review-tag-input">
            태그<em className="mark">선택</em>
          </label>
          <div
            className={tagError ? "tag-box dup" : "tag-box"}
            onClick={() => tagInputRef.current?.focus()}
          >
            {tags.map((tag) => (
              <span className="tag-chip" key={tag}>
                {tag}
                <button
                  type="button"
                  aria-label={`${tag} 태그 삭제`}
                  onClick={() => {
                    setTags(tags.filter((t) => t !== tag));
                    setTagError(null);
                  }}
                >
                  ×
                </button>
              </span>
            ))}
            <input
              ref={tagInputRef}
              id="review-tag-input"
              maxLength={MAX_TAG_LENGTH}
              autoComplete="off"
              aria-describedby="review-tag-help"
              value={tagText}
              onChange={(e) => {
                setTagText(e.target.value);
                setTagError(null);
              }}
              onKeyDown={handleTagKeyDown}
            />
          </div>
          <small id="review-tag-help" role="status">
            {tagError ?? TAG_HELP}
          </small>
        </div>
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
