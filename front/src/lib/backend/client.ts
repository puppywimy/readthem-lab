const NEXT_PUBLIC_API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

export const API_BASE_URL = NEXT_PUBLIC_API_BASE_URL;

export const isLoginCheckRequest = (url: string) => {
  try {
    return (
      new URL(url, NEXT_PUBLIC_API_BASE_URL).pathname === "/api/v1/members/me"
    );
  } catch {
    return url === "/api/v1/members/me";
  }
};

export const apiFetchRaw = (url: string, options?: RequestInit) => {
  options = options || {};

  options.credentials = "include";

  if (options.body) {
    const headers = new Headers(options.headers || {});

    if (!headers.has("Content-Type")) {
      headers.set("Content-Type", "application/json; charset=utf-8");
    }

    options.headers = headers;
  }

  return fetch(`${NEXT_PUBLIC_API_BASE_URL}${url}`, options);
};

const parseJsonBody = (res: Response) => {
  return res.text().then((text) => {
    if (text.length === 0) {
      return null;
    }

    try {
      return JSON.parse(text);
    } catch {
      return {
        message: res.ok
          ? "서버 응답을 해석할 수 없습니다."
          : "서버에서 올바르지 않은 오류 응답을 받았습니다.",
        status: res.status,
        statusText: res.statusText,
      };
    }
  });
};

export const apiFetch = (url: string, options?: RequestInit) => {
  return apiFetchRaw(url, options).then((res) => {
    if (!res.ok) {
      if (isLoginCheckRequest(url)) {
        return null;
      }

      return parseJsonBody(res).then((errorData) => {
        throw { ...(errorData ?? {}), requestUrl: url };
      });
    }

    return parseJsonBody(res);
  });
};
