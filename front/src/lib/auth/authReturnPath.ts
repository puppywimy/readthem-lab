"use client";

const AUTH_RETURN_PATH_KEY = "readthem-auth-return-path";

const FALLBACK_PATH = "/";

const AUTH_ONLY_PATHS = ["/members/login", "/members/join"];
const LOGIN_REQUIRED_PATHS = ["/mypage", "/admin"];

const normalizePath = (path: string) => {
  if (!path.startsWith("/")) return FALLBACK_PATH;

  const isAuthOnlyPath = AUTH_ONLY_PATHS.some(
    (authPath) => path === authPath || path.startsWith(`${authPath}/`),
  );
  const isLoginRequiredPath = LOGIN_REQUIRED_PATHS.some(
    (loginRequiredPath) =>
      path === loginRequiredPath || path.startsWith(`${loginRequiredPath}/`),
  );

  if (isAuthOnlyPath || isLoginRequiredPath) {
    return FALLBACK_PATH;
  }

  return path;
};

export const getCurrentAuthReturnPath = () => {
  if (typeof window === "undefined") return FALLBACK_PATH;

  return normalizePath(
    `${window.location.pathname}${window.location.search}${window.location.hash}`,
  );
};

export const saveCurrentAuthReturnPath = () => {
  if (typeof window === "undefined") return FALLBACK_PATH;

  const returnPath = getCurrentAuthReturnPath();
  window.localStorage.setItem(AUTH_RETURN_PATH_KEY, returnPath);

  return returnPath;
};

export const consumeAuthReturnPath = () => {
  if (typeof window === "undefined") return FALLBACK_PATH;

  const storedPath = window.localStorage.getItem(AUTH_RETURN_PATH_KEY);
  window.localStorage.removeItem(AUTH_RETURN_PATH_KEY);

  return normalizePath(storedPath ?? FALLBACK_PATH);
};

export const getGitHubLoginUrlForReturnPath = (returnPath: string) => {
  if (typeof window === "undefined") return "#";

  const redirectUrl = new URL(normalizePath(returnPath), window.location.origin)
    .toString();

  return `${process.env.NEXT_PUBLIC_API_BASE_URL}/oauth2/authorization/github?redirectUrl=${encodeURIComponent(
    redirectUrl,
  )}`;
};

export const buildGitHubLoginUrl = () => {
  if (typeof window === "undefined") return "#";

  const returnPath = saveCurrentAuthReturnPath();
  return getGitHubLoginUrlForReturnPath(returnPath);
};
