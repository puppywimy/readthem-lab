import { isLoginCheckRequest } from "@/lib/backend/client";

type AppError = {
  resultCode?: string;
  message?: string;
  requestUrl?: string;
};

const FALLBACK_MESSAGE = "알 수 없는 오류가 발생했습니다.";

export function getErrorMessage(error: unknown) {
  if (error instanceof Error) {
    return error.message;
  }

  if (typeof error === "object" && error != null && "message" in error) {
    const message = (error as AppError).message;

    if (typeof message === "string" && message.length > 0) {
      return message;
    }
  }

  return FALLBACK_MESSAGE;
}

export function getErrorResultCode(error: unknown) {
  if (typeof error === "object" && error != null && "resultCode" in error) {
    const resultCode = (error as AppError).resultCode;

    if (typeof resultCode === "string" && resultCode.length > 0) {
      return resultCode;
    }
  }

  return null;
}

export function goToErrorPage(error: unknown) {
  const requestUrl =
    typeof error === "object" &&
    error != null &&
    "requestUrl" in error &&
    typeof (error as AppError).requestUrl === "string"
      ? (error as AppError).requestUrl
      : null;

  if (requestUrl != null && isLoginCheckRequest(requestUrl)) {
    return;
  }

  const params = new URLSearchParams({
    message: getErrorMessage(error),
  });
  const resultCode = getErrorResultCode(error);

  if (resultCode != null) {
    params.set("resultCode", resultCode);
  }

  window.location.assign(`/error?${params.toString()}`);
}
