"use client";

import { createContext, useContext, useEffect, useRef, useState } from "react";

import { getErrorMessage } from "@/lib/error/goToErrorPage";

import Dialog from "@/app/_components/Dialog";

type ToastContextType = {
  showToast: (message: string) => void;
  showErrorToast: (error: unknown) => void;
  confirmAction: (message: string) => Promise<boolean>;
};

type PendingConfirm = {
  message: string;
  resolve: (confirmed: boolean) => void;
};

const ToastContext = createContext<ToastContextType | null>(null);

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [message, setMessage] = useState("");
  const [isToastVisible, setIsToastVisible] = useState(false);
  const [pendingConfirm, setPendingConfirm] = useState<PendingConfirm | null>(
    null,
  );
  const timerRef = useRef<number | null>(null);

  const showToast = (nextMessage: string) => {
    setMessage(nextMessage);
    setIsToastVisible(true);

    if (timerRef.current != null) {
      window.clearTimeout(timerRef.current);
    }

    timerRef.current = window.setTimeout(() => {
      setIsToastVisible(false);
      timerRef.current = null;
    }, 2200);
  };

  const settleConfirm = (confirmed: boolean) => {
    pendingConfirm?.resolve(confirmed);
    setPendingConfirm(null);
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
        confirmAction: (confirmMessage) =>
          new Promise((resolve) =>
            setPendingConfirm({ message: confirmMessage, resolve }),
          ),
      }}
    >
      {children}
      {pendingConfirm != null && (
        <Dialog onClose={() => settleConfirm(false)}>
          <form
            className="dlg"
            style={{ padding: 0 }}
            onSubmit={(e) => {
              e.preventDefault();
              settleConfirm(true);
            }}
          >
            <h2>{pendingConfirm.message}</h2>
            <div className="dlg-actions">
              <button
                type="button"
                className="btn line"
                onClick={() => settleConfirm(false)}
              >
                취소
              </button>
              <button className="btn">확인</button>
            </div>
          </form>
        </Dialog>
      )}
      <div
        className={`toast ${isToastVisible ? "show" : ""}`}
        role="status"
        aria-live="polite"
      >
        {message}
      </div>
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
