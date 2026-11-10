"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";

import { useAuth } from "@/lib/auth/AuthProvider";
import {
  consumeAuthReturnPath,
  saveCurrentAuthReturnPath,
} from "@/lib/auth/authReturnPath";
import { useTheme } from "@/lib/theme/ThemeProvider";
import { useToast } from "@/lib/toast/ToastProvider";

import { GitHubLoginButton } from "@/app/_components/LoginRequiredModal";
import SvgIcon from "@/app/_components/SvgIcon";

export default function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const { isLogin, isLoginMemberPending, isAdmin, logout } = useAuth();
  const { showToast, showErrorToast } = useToast();
  const { toggleTheme } = useTheme();
  const current = (path: string) =>
    pathname.startsWith(path) ? ("page" as const) : undefined;

  const handleLogout = () => {
    saveCurrentAuthReturnPath();

    logout()
      .then(() => {
        showToast("로그아웃되었습니다.");
        router.replace(consumeAuthReturnPath());
      })
      .catch(showErrorToast);
  };

  return (
    <header className="nav">
      <div className="wrap">
        <Link className="logo" href="/">
          Readthem <em>Lab</em>
        </Link>
        <nav className="links" aria-label="주요 메뉴">
          <Link
            href={pathname === "/" ? "/" : "/books/search"}
            aria-current={current("/books/search")}
          >
            도서 검색
          </Link>
          <Link href="/#home-rank">독서 트렌드</Link>
          <Link href="/#home-reviews">리뷰</Link>
          {isLogin && (
            <Link href="/mypage" aria-current={current("/mypage")}>
              내 서재
            </Link>
          )}
          {isAdmin && (
            <Link
              className="nav-admin"
              href="/admin"
              aria-current={current("/admin")}
            >
              <SvgIcon name="admin" />
              관리자
            </Link>
          )}
        </nav>
        <div className="nav-end">
          <button
            type="button"
            className="icon-btn"
            onClick={toggleTheme}
            aria-label="테마 전환"
          >
            <SvgIcon name="moon" className="i h-5 theme-light-only" />
            <SvgIcon name="sun" className="i h-5 theme-dark-only" />
          </button>
          {isLoginMemberPending ? (
            // 비로그인 버튼과 같은 폭으로 자리를 잡아 둔다
            <span className="btn line invisible" aria-hidden="true">
              <SvgIcon name="github" />
              <span>
                <span className="long">GitHub </span>로그인
              </span>
            </span>
          ) : isLogin ? (
            <button type="button" className="btn line" onClick={handleLogout}>
              <SvgIcon name="logout" />
              로그아웃
            </button>
          ) : (
            <GitHubLoginButton className="btn line">
              <span>
                <span className="long">GitHub </span>로그인
              </span>
            </GitHubLoginButton>
          )}
        </div>
      </div>
    </header>
  );
}
