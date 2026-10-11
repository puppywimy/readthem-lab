"use client";

import { useRouter } from "next/navigation";

import { apiFetch } from "@/lib/backend/client";

import { useAuth } from "@/lib/auth/AuthProvider";
import { consumeAuthReturnPath } from "@/lib/auth/authReturnPath";
import { useToast } from "@/lib/toast/ToastProvider";

import RoughButton from "@/app/_components/RoughButton";
import { RoughInput } from "@/app/_components/RoughInput";

export default function Page() {
  const router = useRouter();
  const { refresh } = useAuth();
  const { showErrorToast } = useToast();

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
      alert("아이디를 입력해주세요.");
      usernameInput.focus();
      return;
    }

    if (usernameInput.value.length < 2) {
      alert("아이디를 2자 이상 입력해주세요.");
      usernameInput.focus();
      return;
    }

    passwordInput.value = passwordInput.value.trim();

    if (passwordInput.value.length === 0) {
      alert("비밀번호를 입력해주세요.");
      passwordInput.focus();
      return;
    }

    if (passwordInput.value.length < 2) {
      alert("비밀번호를 2자 이상 입력해주세요.");
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
        alert(data.message);
        return refresh();
      })
      .then(() => {
        router.replace(consumeAuthReturnPath());
      })
      .catch(showErrorToast);
  };

  return (
    <>
      <h1>로그인</h1>

      <form
        className="flex max-w-sm flex-col gap-2 p-2"
        onSubmit={handleSubmit}
      >
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
        <RoughButton fullWidth roughSize="lg" tone="submit" type="submit">
          로그인
        </RoughButton>
      </form>
    </>
  );
}
