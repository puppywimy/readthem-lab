"use client";

import { useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import { useToast } from "@/lib/toast/ToastProvider";

import RoughButton from "@/app/_components/RoughButton";
import RoughFrame from "@/app/_components/RoughFrame";
import { RoughInput } from "@/app/_components/RoughInput";
import BookAdmin from "@/app/admin/BookAdmin";
import MemberAdmin from "@/app/admin/MemberAdmin";
import ReviewAdmin from "@/app/admin/ReviewAdmin";

type Tab = "members" | "books" | "reviews";

export default function Page() {
  const { isLogin, isLoginMemberPending, isAdmin, refresh } = useAuth();
  const { showErrorToast } = useToast();

  const [tab, setTab] = useState<Tab>("members");

  const handleLoginSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    const form = e.currentTarget;

    const usernameInput = form.elements.namedItem(
      "username",
    ) as HTMLInputElement;
    const passwordInput = form.elements.namedItem(
      "password",
    ) as HTMLInputElement;

    apiFetch(`/api/v1/members/login`, {
      method: "POST",
      body: JSON.stringify({
        username: usernameInput.value,
        password: passwordInput.value,
      }),
    })
      .then(() => refresh())
      .catch(showErrorToast);
  };

  if (isLoginMemberPending) {
    return <div>로딩중...</div>;
  }

  if (!isLogin) {
    return (
      <div className="mx-auto flex min-h-[60vh] w-full max-w-xl items-center justify-center p-4">
        <div className="relative flex w-full max-w-sm flex-col gap-4 p-6">
          <RoughFrame className="rough-overlay" variant="card" />
          <h1 className="mb-4 text-xl font-bold">관리자 로그인</h1>
          <form className="flex flex-col gap-2" onSubmit={handleLoginSubmit}>
            <RoughInput
              type="text"
              name="username"
              placeholder="아이디"
              autoFocus
              maxLength={30}
            />
            <RoughInput
              type="password"
              name="password"
              placeholder="비밀번호"
              maxLength={30}
            />
            <div className="pt-1">
              <RoughButton fullWidth tone="submit" type="submit">
                <span className="py-1">로그인</span>
              </RoughButton>
            </div>
          </form>
        </div>
      </div>
    );
  }

  if (!isAdmin) {
    return <div>관리자 권한이 없는 계정입니다.</div>;
  }

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-xl font-bold">관리자 페이지</h1>

      <div className="theme-tab-row flex gap-2">
        {(
          [
            ["members", "회원 관리"],
            ["books", "도서 관리"],
            ["reviews", "리뷰 관리"],
          ] as [Tab, string][]
        ).map(([value, label]) => (
          <button
            key={value}
            type="button"
            className={`px-3 py-2 text-sm ${
              tab === value ? "theme-tab-active" : "theme-tab-inactive"
            }`}
            onClick={() => setTab(value)}
          >
            {label}
          </button>
        ))}
      </div>

      {tab === "members" && <MemberAdmin />}
      {tab === "books" && <BookAdmin />}
      {tab === "reviews" && <ReviewAdmin />}
    </div>
  );
}
