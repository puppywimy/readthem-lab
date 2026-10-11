"use client";

import type { ReactNode } from "react";

import RoughFrame from "@/app/_components/RoughFrame";
import { RoughInput } from "@/app/_components/RoughInput";

type LibraryWidgetPreviewProps = {
  githubId: string | null | undefined;
  widgetSrc: string;
  widgetLink?: string | null;
  actions?: ReactNode;
};

export default function LibraryWidgetPreview({
  githubId,
  widgetSrc,
  widgetLink,
  actions,
}: LibraryWidgetPreviewProps) {
  return (
    <div className="min-w-0 w-full">
      <div className="flex min-w-0 items-center justify-between gap-2">
        <h2 className="font-bold">위젯 미리보기</h2>
        {actions}
      </div>
      <div className="rough-panel-border relative mt-1 flex min-h-24 items-center justify-center bg-transparent p-2">
        <RoughFrame className="rough-overlay" variant="card" />
        {githubId ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img className="max-w-full" src={widgetSrc} alt="위젯 미리보기" />
        ) : (
          <span className="text-sm theme-muted">위젯 정보가 없습니다</span>
        )}
      </div>
      {widgetLink && (
        <div className="mt-1">
          <RoughInput
            inputClassName="text-xs"
            roughSize="sm"
            readOnly
            value={widgetLink}
            onFocus={(e) => e.currentTarget.select()}
          />
        </div>
      )}
    </div>
  );
}
