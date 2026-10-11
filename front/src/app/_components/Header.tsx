"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { useEffect, useRef, useState } from "react";

import { useAuth } from "@/lib/auth/AuthProvider";
import {
  buildGitHubLoginUrl,
  consumeAuthReturnPath,
  getCurrentAuthReturnPath,
  getGitHubLoginUrlForReturnPath,
  saveCurrentAuthReturnPath,
} from "@/lib/auth/authReturnPath";
import { useTheme } from "@/lib/theme/ThemeProvider";
import { useToast } from "@/lib/toast/ToastProvider";

import RoughBar from "@/app/_components/RoughBar";
import RoughButton from "@/app/_components/RoughButton";
import { RoughInput } from "@/app/_components/RoughInput";
import SvgIcon from "@/app/_components/SvgIcon";

export default function Header() {
  const router = useRouter();
  const { isLogin, isLoginMemberPending, isAdmin, logout } = useAuth();
  const { showToast, showErrorToast } = useToast();
  const { toggleTheme } = useTheme();
  const [searchTerm, setSearchTerm] = useState("");
  const [isSearchPending, setIsSearchPending] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const searchInputRef = useRef<HTMLInputElement | null>(null);
  const isSearchSubmitPointerDownRef = useRef(false);
  const githubLoginHref = !isLogin
    ? getGitHubLoginUrlForReturnPath(getCurrentAuthReturnPath())
    : "#";

  useEffect(() => {
    const handleSearchLoading = (event: Event) => {
      setIsSearchPending(Boolean((event as CustomEvent<boolean>).detail));
    };

    window.addEventListener("readthem-search-loading", handleSearchLoading);

    return () => {
      window.removeEventListener(
        "readthem-search-loading",
        handleSearchLoading,
      );
    };
  }, []);

  useEffect(() => {
    if (!isSearchOpen) return;

    const frameId = window.requestAnimationFrame(() => {
      searchInputRef.current?.focus();
    });

    return () => {
      window.cancelAnimationFrame(frameId);
    };
  }, [isSearchOpen]);

  useEffect(() => {
    const mediaQuery = window.matchMedia("(min-width: 640px)");
    const closeMobileSearch = () => {
      if (mediaQuery.matches) {
        setIsSearchOpen(false);
      }
    };

    closeMobileSearch();
    mediaQuery.addEventListener("change", closeMobileSearch);

    return () => {
      mediaQuery.removeEventListener("change", closeMobileSearch);
    };
  }, []);

  const handleLogout = () => {
    saveCurrentAuthReturnPath();

    logout()
      .then(() => {
        showToast("로그아웃되었습니다.");
        router.replace(consumeAuthReturnPath());
      })
      .catch(showErrorToast);
  };

  const handleGitHubLogin = (
    e: React.MouseEvent<HTMLAnchorElement, MouseEvent>,
  ) => {
    e.preventDefault();
    window.location.href = buildGitHubLoginUrl();
  };

  const handleSearch = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    const term = searchTerm.trim();
    if (isSearchPending) return;
    if (term.length < 2) {
      showToast("검색어는 2글자 이상 입력해주세요.");
      return;
    }

    const currentUrl = new URL(window.location.href);
    const currentSearchTerm = currentUrl.searchParams.get("searchTerm") ?? "";
    if (
      currentUrl.pathname === "/books/search" &&
      currentSearchTerm.trim() === term
    ) {
      return;
    }

    setIsSearchPending(true);
    router.push(`/books/search?searchTerm=${encodeURIComponent(term)}`);
  };

  const closeMobileSearchOnBlur = (e: React.FocusEvent<HTMLFormElement>) => {
    if (!isSearchOpen) return;
    if (e.currentTarget.contains(e.relatedTarget)) return;
    if (isSearchSubmitPointerDownRef.current) return;
    if (!window.matchMedia("(max-width: 639px)").matches) return;

    setIsSearchOpen(false);
  };

  const submitMobileSearchBeforeBlur = (
    e: React.PointerEvent<HTMLButtonElement>,
  ) => {
    if (!window.matchMedia("(max-width: 639px)").matches) return;
    if (isSearchPending) return;

    isSearchSubmitPointerDownRef.current = true;
    e.preventDefault();
    e.currentTarget.form?.requestSubmit();

    window.requestAnimationFrame(() => {
      isSearchSubmitPointerDownRef.current = false;
    });
  };

  return (
    <header className="theme-site-header relative">
      <nav className="mx-auto flex w-full max-w-4xl items-center gap-3 px-4 py-2">
        <div className={`shrink-0 ${isSearchOpen ? "max-sm:hidden" : ""}`}>
          <Link
            href="/"
            className="shrink-0 text-xl font-bold whitespace-nowrap"
          >
            READTHEM.md
          </Link>
        </div>

        {!isSearchOpen && (
          <button
            type="button"
            className="mobile-search-toggle theme-nav-link theme-nav-control shrink-0"
            onClick={() => setIsSearchOpen(true)}
            aria-label="검색창 열기"
          >
            <SvgIcon name="search" />
            <span className="theme-nav-label">검색</span>
          </button>
        )}

        <form
          className={`min-w-0 items-center gap-1 ${
            isSearchOpen
              ? "flex flex-1"
              : "hidden w-72 shrink-0 sm:flex lg:w-80"
          }`}
          onSubmit={handleSearch}
          onBlur={closeMobileSearchOnBlur}
        >
          <label className="min-w-0 flex-1">
            <RoughInput
              ref={searchInputRef}
              wrapperClassName="w-full"
              type="text"
              placeholder="제목/저자 검색"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              maxLength={100}
              disabled={isSearchPending}
            />
          </label>
          <RoughButton
            className={`search-submit-button theme-nav-control shrink-0 whitespace-nowrap px-2 ${
              isSearchOpen ? "search-submit-button-open" : ""
            }`}
            roughSize="sm"
            type="submit"
            disabled={isSearchPending}
            onPointerDown={submitMobileSearchBeforeBlur}
          >
            <SvgIcon name="search" />
            <span className="search-submit-label">
              {isSearchPending ? "검색중" : "검색"}
            </span>
          </RoughButton>
        </form>

        <div
          className={`ml-auto shrink-0 items-center justify-end gap-2 whitespace-nowrap ${
            isSearchOpen ? "hidden sm:flex" : "flex"
          }`}
        >
          <button
            type="button"
            className="theme-nav-link theme-nav-control shrink-0"
            onClick={toggleTheme}
            aria-label="테마 전환"
          >
            <SvgIcon name="moon" className="theme-light-only h-4 w-4" />
            <SvgIcon name="sun" className="theme-dark-only h-4 w-4" />
            <span className="theme-nav-label theme-toggle-label" />
          </button>

          {isLoginMemberPending && (
            <span
              className="theme-nav-link theme-nav-control pointer-events-none invisible gap-1.5"
              aria-hidden="true"
            >
              <SvgIcon name="github" />
              <span className="theme-nav-label">GitHub로 로그인</span>
            </span>
          )}

          {!isLoginMemberPending && !isLogin && (
            <div className="auth-action-enter">
              <a
                href={githubLoginHref}
                className="theme-nav-link theme-nav-control gap-1.5"
                onClick={handleGitHubLogin}
                aria-label="GitHub로 로그인"
              >
                <SvgIcon name="github" />
                <span className="theme-nav-label">GitHub로 로그인</span>
              </a>
            </div>
          )}

          {!isLoginMemberPending && isLogin && (
            <div className="auth-action-enter flex min-w-0 items-center gap-2 whitespace-nowrap">
              <Link href="/mypage" className="theme-nav-link theme-nav-control">
                <SvgIcon name="library" />
                <span className="theme-nav-label">내 서재</span>
              </Link>
              {isAdmin && (
                <Link
                  href="/admin"
                  className="theme-nav-link theme-nav-control"
                >
                  <SvgIcon name="admin" />
                  <span className="theme-nav-label">관리자</span>
                </Link>
              )}
              <button
                type="button"
                className="theme-nav-link theme-nav-control"
                onClick={handleLogout}
                aria-label="로그아웃"
              >
                <SvgIcon name="logout" />
                <span className="theme-nav-label">로그아웃</span>
              </button>
            </div>
          )}
        </div>
      </nav>
      <div className="theme-rough-divider-bar">
        <RoughBar
          className="h-full w-full"
          fill="transparent"
          lineInset={0}
          variant="line"
        />
      </div>
    </header>
  );
}
