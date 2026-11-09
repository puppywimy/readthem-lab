import type { Metadata } from "next";

import { AuthProvider } from "@/lib/auth/AuthProvider";
import { ThemeProvider } from "@/lib/theme/ThemeProvider";
import { ToastProvider } from "@/lib/toast/ToastProvider";

import AuthReturnPathRestorer from "@/app/_components/AuthReturnPathRestorer";
import Header from "@/app/_components/Header";

import "./globals.css";

export const metadata: Metadata = {
  title: "Readthem Lab",
  description: "개발자의 도서 리뷰 플랫폼",
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
        <link
          rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/pretendard@1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.css"
        />
      </head>
      <body className="home-a">
        <ThemeProvider>
          <ToastProvider>
            <AuthProvider>
              <AuthReturnPathRestorer />
              <Header />
              <main className="page">{children}</main>
              <footer className="foot">
                <div className="wrap">
                  <span>Readthem Lab</span>
                  <a
                    href="https://github.com/prgrms-be-devcourse/NBE10-12-3-ADP"
                    target="_blank"
                    rel="noreferrer"
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
