"use client";

import { createContext, useContext, useEffect, useRef, useState } from "react";

import RoughFrame from "@/app/_components/RoughFrame";
import { getErrorMessage } from "@/lib/error/goToErrorPage";

type ToastContextType = {
  showToast: (message: string) => void;
  showErrorToast: (error: unknown) => void;
};

const ToastContext = createContext<ToastContextType | null>(null);

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [message, setMessage] = useState<string | null>(null);
  const timerRef = useRef<number | null>(null);

  const showToast = (nextMessage: string) => {
    setMessage(nextMessage);

    if (timerRef.current != null) {
      window.clearTimeout(timerRef.current);
    }

    timerRef.current = window.setTimeout(() => {
      setMessage(null);
      timerRef.current = null;
    }, 1800);
  };

  useEffect(() => {
    return () => {
      if (timerRef.current != null) {
        window.clearTimeout(timerRef.current);
      }
    };
  }, []);

  return (
    <ToastContext.Provider
      value={{
        showToast,
        showErrorToast: (error) => {
          console.error("API error", error);
          showToast(getErrorMessage(error));
        },
      }}
    >
      {children}
      {message != null && (
        <div className="rough-toast" role="status" aria-live="polite">
          <RoughFrame className="rough-overlay" variant="card" />
          <span className="relative z-10">{message}</span>
        </div>
      )}
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);

  if (context == null) {
    throw new Error("useToast는 ToastProvider 내부에서만 사용할 수 있습니다.");
  }

  return context;
}
