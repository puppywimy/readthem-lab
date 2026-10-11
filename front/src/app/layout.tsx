import type { Metadata } from "next";

import { AuthProvider } from "@/lib/auth/AuthProvider";
import { ThemeProvider } from "@/lib/theme/ThemeProvider";
import { ToastProvider } from "@/lib/toast/ToastProvider";

import AuthReturnPathRestorer from "@/app/_components/AuthReturnPathRestorer";
import Header from "@/app/_components/Header";
import RoughBar from "@/app/_components/RoughBar";

import "./globals.css";

export const metadata: Metadata = {
  title: "READTHEM.md",
  description: "개발자의 도서 리뷰 공유 플랫폼",
};

const themeScript = `
(() => {
  try {
    const storedTheme = window.localStorage.getItem("readthem-theme");
    const theme =
      storedTheme === "light" || storedTheme === "dark"
        ? storedTheme
        : window.matchMedia("(prefers-color-scheme: dark)").matches
          ? "dark"
          : "light";

    document.documentElement.dataset.theme = theme;
  } catch {
    document.documentElement.dataset.theme = "light";
  }
})();
`;

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: themeScript }} />
        <link rel="preconnect" href="https://cdn.jsdelivr.net" />
      </head>
      <body className="flex min-h-screen flex-col antialiased">
        <ThemeProvider>
          <ToastProvider>
            <AuthProvider>
              <AuthReturnPathRestorer />
              <Header />
              <main className="relative z-0 mx-auto flex w-full max-w-4xl flex-grow flex-col p-4">
                <div className="relative flex flex-1 flex-col">{children}</div>
              </main>
              <footer className="theme-site-footer text-center text-sm theme-muted">
                <div className="theme-rough-divider-bar">
                  <RoughBar
                    className="h-full w-full"
                    fill="transparent"
                    lineInset={0}
                    variant="line"
                  />
                </div>
                <div className="px-2 py-6">
                  <a
                    href="https://github.com/prgrms-be-devcourse/NBE10-12-3-ADP"
                    target="_blank"
                    rel="noreferrer"
                    className="theme-link"
                  >
                    GitHub Repository
                  </a>
                </div>
              </footer>
            </AuthProvider>
          </ToastProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
