"use client";

import Link from "next/link";

import RoughButton from "@/app/_components/RoughButton";
import RoughFrame from "@/app/_components/RoughFrame";

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
    <div className="mx-auto flex min-h-[60vh] w-full max-w-xl items-center justify-center p-4">
      <div className="relative flex w-full flex-col gap-5 p-6">
        <RoughFrame className="rough-overlay" variant="card" />
        <div className="flex flex-col gap-2">
          <p className="text-sm font-semibold theme-muted">ERROR</p>
          <h1 className="text-2xl font-bold">오류가 발생했습니다</h1>
          <p className="theme-muted">
            요청을 처리하는 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.
          </p>
        </div>

        <div className="theme-surface relative flex flex-col gap-2 p-4 text-sm">
          <RoughFrame className="rough-overlay" variant="card" />
          {resultCode != null && (
            <div>
              <span className="font-semibold">코드: </span>
              <span>{resultCode}</span>
            </div>
          )}
          <div>
            <span className="font-semibold">메시지: </span>
            <span>{message}</span>
          </div>
        </div>

        <div className="flex flex-wrap gap-2">
          {onRetry != null ? (
            <RoughButton type="button" onClick={onRetry}>
              다시 시도
            </RoughButton>
          ) : (
            <RoughButton type="button" onClick={() => window.history.back()}>
              이전으로
            </RoughButton>
          )}
          <Link href="/">
            <RoughButton type="button" tone="submit">
              홈으로
            </RoughButton>
          </Link>
        </div>
      </div>
    </div>
  );
}
