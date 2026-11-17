"use client";

import { usePathname } from "next/navigation";

import { useState } from "react";

import { goToBookSearch } from "@/lib/navigation/goToBookSearch";
import { useToast } from "@/lib/toast/ToastProvider";

export default function SearchForm({
  id,
  initialTerm = "",
  autoFocus = false,
}: {
  id: string;
  initialTerm?: string;
  autoFocus?: boolean;
}) {
  const pathname = usePathname();
  const { showToast } = useToast();
  const [term, setTerm] = useState(initialTerm);

  const handleSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    const trimmed = term.trim();
    if (trimmed.length < 2) {
      showToast("검색어는 2글자 이상 입력해주세요.");
      return;
    }

    goToBookSearch(pathname, trimmed);
  };

  return (
    <form className="search" onSubmit={handleSubmit}>
      <label htmlFor={id}>도서 검색</label>
      <div>
        <input
          id={id}
          type="search"
          maxLength={100}
          placeholder="제목이나 저자를 입력하세요"
          autoFocus={autoFocus}
          value={term}
          onChange={(e) => setTerm(e.target.value)}
        />
        <button className="btn">검색</button>
      </div>
    </form>
  );
}
