"use client";

import { createContext, useContext, useEffect, useMemo, useState } from "react";

type Theme = "light" | "dark";

type ThemeContextType = {
  theme: Theme;
  toggleTheme: () => void;
};

const THEME_STORAGE_KEY = "readthem-theme";
const ThemeContext = createContext<ThemeContextType | null>(null);

function applyTheme(theme: Theme) {
  document.documentElement.dataset.theme = theme;
}

function getPreferredTheme(): Theme {
  if (typeof window === "undefined") {
    return "light";
  }

  const storedTheme = window.localStorage.getItem(THEME_STORAGE_KEY);
  if (storedTheme === "light" || storedTheme === "dark") {
    return storedTheme;
  }

  const currentTheme = document.documentElement.dataset.theme;
  if (currentTheme === "light" || currentTheme === "dark") {
    return currentTheme;
  }

  return window.matchMedia("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light";
}

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [theme, setTheme] = useState<Theme>(() => getPreferredTheme());

  useEffect(() => {
    const nextTheme = getPreferredTheme();
    applyTheme(nextTheme);

    const handleThemeChange = () => {
      const changedTheme = getPreferredTheme();
      setTheme(changedTheme);
      applyTheme(changedTheme);
    };

    window.addEventListener("storage", handleThemeChange);

    const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
    mediaQuery.addEventListener("change", handleThemeChange);

    return () => {
      window.removeEventListener("storage", handleThemeChange);
      mediaQuery.removeEventListener("change", handleThemeChange);
    };
  }, []);

  const value = useMemo(
    () => ({
      theme,
      toggleTheme: () => {
        const nextTheme = theme === "light" ? "dark" : "light";
        window.localStorage.setItem(THEME_STORAGE_KEY, nextTheme);
        applyTheme(nextTheme);
        setTheme(nextTheme);
      },
    }),
    [theme],
  );

  return (
    <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);

  if (context == null) {
    throw new Error("useTheme는 ThemeProvider 내부에서만 사용할 수 있습니다.");
  }

  return context;
}
