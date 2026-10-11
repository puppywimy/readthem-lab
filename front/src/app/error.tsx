"use client";

import ErrorView from "@/app/_components/ErrorView";

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return <ErrorView message={error.message} onRetry={reset} />;
}
