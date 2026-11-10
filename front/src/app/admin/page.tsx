"use client";

import Link from "next/link";

import { useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import { useToast } from "@/lib/toast/ToastProvider";

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
    return (
      <div className="wrap admin" aria-busy="true">
        <span className="sk line" style={{ height: 44 }} />
      </div>
    );
  }

  if (!isLogin) {
    return (
      <div className="wrap gate">
        <h1 className="page-title">관리자 로그인</h1>
        <form onSubmit={handleLoginSubmit}>
          <label className="field">
            아이디
            <input
              name="username"
              required
              maxLength={30}
              autoComplete="username"
            />
          </label>
          <label className="field">
            비밀번호
            <input
              name="password"
              type="password"
              required
              maxLength={30}
              autoComplete="current-password"
            />
          </label>
          <button className="btn">로그인</button>
        </form>
      </div>
    );
  }

  if (!isAdmin) {
    return (
      <div className="wrap">
        <div className="state">
          <strong>관리자 권한이 없는 계정입니다.</strong>
          관리자 계정으로 다시 로그인해 주세요.
          <div>
            <Link className="btn line" href="/">
              홈으로
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="wrap admin">
      <h1 className="page-title">관리자 페이지</h1>
      <div className="tabs" role="tablist">
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
            className="tab"
            role="tab"
            aria-selected={tab === value}
            onClick={() => setTab(value)}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="swap" role="tabpanel" key={tab}>
        {tab === "members" && <MemberAdmin />}
        {tab === "books" && <BookAdmin />}
        {tab === "reviews" && <ReviewAdmin />}
      </div>
    </div>
  );
}
