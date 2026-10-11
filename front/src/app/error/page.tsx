"use client";

import { useSearchParams } from "next/navigation";

import { Suspense } from "react";

import ErrorView from "@/app/_components/ErrorView";

function ErrorPageContent() {
  const searchParams = useSearchParams();
  const resultCode = searchParams.get("resultCode");
  const message =
    searchParams.get("message") ?? "알 수 없는 오류가 발생했습니다.";

  return <ErrorView message={message} resultCode={resultCode} />;
}

export default function Page() {
  return (
    <Suspense fallback={<div>로딩중...</div>}>
      <ErrorPageContent />
    </Suspense>
  );
}
