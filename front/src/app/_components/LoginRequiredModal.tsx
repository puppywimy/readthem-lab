"use client";

import type { ReactNode } from "react";

import { buildGitHubLoginUrl } from "@/lib/auth/authReturnPath";

import Dialog from "@/app/_components/Dialog";
import SvgIcon from "@/app/_components/SvgIcon";

export function GitHubLoginButton({
  className = "btn",
  children = "GitHub 로그인",
}: {
  className?: string;
  children?: ReactNode;
}) {
  return (
    <button
      type="button"
      className={className}
      onClick={() => {
        window.location.href = buildGitHubLoginUrl();
      }}
    >
      <SvgIcon name="github" />
      {children}
    </button>
  );
}

export default function LoginRequiredModal({
  onCancel,
}: {
  onCancel: () => void;
}) {
  return (
    <Dialog onClose={onCancel}>
      <h2>로그인이 필요합니다</h2>
      <p>GitHub 계정으로 로그인하면 리뷰를 쓰고 책을 찜할 수 있습니다.</p>
      <div className="dlg-actions">
        <button type="button" className="btn line" onClick={onCancel}>
          닫기
        </button>
        <GitHubLoginButton />
      </div>
    </Dialog>
  );
}
