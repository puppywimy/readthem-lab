"use client";

import { useRouter } from "next/navigation";

import { useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import type { components } from "@/lib/backend/apiV1/schema";
import { useToast } from "@/lib/toast/ToastProvider";

import CoverWall from "@/app/_components/CoverWall";

export default function Page() {
  const router = useRouter();
  const { refresh } = useAuth();
  const { showToast, showErrorToast } = useToast();
  const [wallBooks, setWallBooks] = useState<
    components["schemas"]["BookDto"][]
  >([]);

  useEffect(() => {
    apiFetch(`/api/v1/books/rank?type=views&page=0&size=15`)
      .then((data) => setWallBooks(data?.content ?? data ?? []))
      .catch(() => setWallBooks([]));
  }, []);

  const handleSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    const form = e.currentTarget;

    const usernameInput = form.elements.namedItem(
      "username",
    ) as HTMLInputElement;
    const passwordInput = form.elements.namedItem(
      "password",
    ) as HTMLInputElement;
    const githubIdInput = form.elements.namedItem(
      "githubId",
    ) as HTMLInputElement;

    usernameInput.value = usernameInput.value.trim();

    if (usernameInput.value.length < 2) {
      showToast("아이디를 2자 이상 입력해주세요.");
      usernameInput.focus();
      return;
    }

    passwordInput.value = passwordInput.value.trim();

    if (passwordInput.value.length < 2) {
      showToast("비밀번호를 2자 이상 입력해주세요.");
      passwordInput.focus();
      return;
    }

    githubIdInput.value = githubIdInput.value.trim();

    if (githubIdInput.value.length < 2) {
      showToast("GitHub 아이디를 2자 이상 입력해주세요.");
      githubIdInput.focus();
      return;
    }

    apiFetch(`/api/v1/members`, {
      method: "POST",
      body: JSON.stringify({
        username: usernameInput.value,
        password: passwordInput.value,
        githubId: githubIdInput.value,
      }),
    })
      .then((data) => {
        showToast(data.message);
        return refresh();
      })
      .then(() => {
        router.replace(`/`);
      })
      .catch(showErrorToast);
  };

  return (
    <div className="hero join-hero">
      <div className="join-text">
        <h1>
          계정을 만들고
          <br />
          바로 시작해요
        </h1>
        <form className="terms grid gap-4" onSubmit={handleSubmit}>
          <label className="field">
            아이디
            <input
              name="username"
              type="text"
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
              autoComplete="new-password"
            />
          </label>
          <label className="field">
            GitHub 아이디
            <input
              name="githubId"
              type="text"
              required
              maxLength={30}
              autoComplete="off"
            />
          </label>
          <div className="join-actions">
            <button className="btn">가입하기</button>
          </div>
        </form>
      </div>
      <div className="join-side">
        <CoverWall books={wallBooks} />
      </div>
    </div>
  );
}
