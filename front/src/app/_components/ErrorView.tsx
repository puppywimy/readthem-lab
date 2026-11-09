"use client";

import Link from "next/link";

type ErrorViewProps = {
  message: string;
  resultCode?: string | null;
  onRetry?: () => void;
};

export default function ErrorView({
  message,
  resultCode,
  onRetry,
}: ErrorViewProps) {
  return (
    <div className="wrap">
      <div className="state">
        <strong>오류가 발생했습니다</strong>
        {resultCode != null && `[${resultCode}] `}
        {message}
        <div className="flex gap-2">
          <button
            type="button"
            className="btn line"
            onClick={onRetry ?? (() => window.history.back())}
          >
            {onRetry != null ? "다시 시도" : "이전으로"}
          </button>
          <Link className="btn" href="/">
            홈으로
          </Link>
        </div>
      </div>
    </div>
  );
}
