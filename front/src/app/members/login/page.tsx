"use client";

import { useRouter } from "next/navigation";

import { apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import { consumeAuthReturnPath } from "@/lib/auth/authReturnPath";
import { useToast } from "@/lib/toast/ToastProvider";

export default function Page() {
  const router = useRouter();
  const { refresh } = useAuth();
  const { showToast, showErrorToast } = useToast();

  const handleSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    const form = e.target as HTMLFormElement;

    const usernameInput = form.elements.namedItem(
      "username",
    ) as HTMLInputElement;
    const passwordInput = form.elements.namedItem(
      "password",
    ) as HTMLTextAreaElement;

    usernameInput.value = usernameInput.value.trim();

    if (usernameInput.value.length === 0) {
      showToast("아이디를 입력해주세요.");
      usernameInput.focus();
      return;
    }

    if (usernameInput.value.length < 2) {
      showToast("아이디를 2자 이상 입력해주세요.");
      usernameInput.focus();
      return;
    }

    passwordInput.value = passwordInput.value.trim();

    if (passwordInput.value.length === 0) {
      showToast("비밀번호를 입력해주세요.");
      passwordInput.focus();
      return;
    }

    if (passwordInput.value.length < 2) {
      showToast("비밀번호를 2자 이상 입력해주세요.");
      passwordInput.focus();
      return;
    }

    apiFetch(`/api/v1/members/login`, {
      method: "POST",
      body: JSON.stringify({
        username: usernameInput.value,
        password: passwordInput.value,
      }),
    })
      .then((data) => {
        showToast(data.message);
        return refresh();
      })
      .then(() => {
        router.replace(consumeAuthReturnPath());
      })
      .catch(showErrorToast);
  };

  return (
    <div className="wrap gate">
      <h1 className="page-title">로그인</h1>
      <form onSubmit={handleSubmit}>
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
            autoComplete="current-password"
          />
        </label>
        <button className="btn">로그인</button>
      </form>
    </div>
  );
}
