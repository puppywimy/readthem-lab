"use client";

import { buildGitHubLoginUrl } from "@/lib/auth/authReturnPath";

import RoughButton from "@/app/_components/RoughButton";
import RoughFrame from "@/app/_components/RoughFrame";

type LoginRequiredModalProps = {
  onCancel: () => void;
};

export default function LoginRequiredModal({
  onCancel,
}: LoginRequiredModalProps) {
  const handleGitHubLogin = () => {
    window.location.href = buildGitHubLoginUrl();
  };

  return (
    <div
      className="rough-modal-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="login-required-title"
    >
      <div className="rough-modal-card">
        <RoughFrame className="rough-overlay" variant="card" />
        <h2 id="login-required-title" className="text-xl font-bold">
          로그인이 필요한 기능입니다
        </h2>
        <p className="mt-2 text-sm text-gray-600">
          이 기능을 사용하려면 로그인이 필요합니다. 로그인하시겠습니까?
        </p>
        <div className="mt-5 flex justify-end gap-2">
          <RoughButton
            roughSize="sm"
            tone="cancel"
            type="button"
            onClick={onCancel}
          >
            취소
          </RoughButton>
          <RoughButton
            roughSize="sm"
            tone="history"
            className="flex items-center gap-1.5 whitespace-nowrap"
            type="button"
            onClick={handleGitHubLogin}
            aria-label="GitHub로 로그인"
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              className="rough-github-button-icon shrink-0 !inline-block"
              src="/github.svg"
              alt=""
            />
            <span> GitHub로 로그인</span>
          </RoughButton>
        </div>
      </div>
    </div>
  );
}
