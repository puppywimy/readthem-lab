import type { ReactNode } from "react";

type LibraryPageLayoutProps = {
  sidebar: ReactNode;
  children: ReactNode;
};

export default function LibraryPageLayout({
  sidebar,
  children,
}: LibraryPageLayoutProps) {
  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:gap-8">
      {sidebar}
      <div className="flex min-w-0 flex-1 flex-col gap-4 max-sm:gap-1">
        {children}
      </div>
    </div>
  );
}
