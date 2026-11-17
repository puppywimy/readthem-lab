"use client";

import { usePathname } from "next/navigation";

import { goToBookSearch } from "@/lib/navigation/goToBookSearch";

/** 검색어 없는 도서 검색 페이지 링크. 이동 방식은 goToBookSearch 참고 */
export default function BookSearchLink(
  props: Omit<React.ComponentProps<"a">, "href" | "onClick">,
) {
  const pathname = usePathname();

  const handleClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    // 새 탭·새 창으로 열기는 브라우저에 맡긴다
    if (e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) {
      return;
    }

    e.preventDefault();
    goToBookSearch(pathname);
  };

  return <a href="/books/search/" {...props} onClick={handleClick} />;
}
