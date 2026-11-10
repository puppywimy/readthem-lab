"use client";

import { useRouter } from "next/navigation";

import { useState } from "react";

import { useToast } from "@/lib/toast/ToastProvider";

export default function SearchForm({
  id,
  initialTerm = "",
}: {
  id: string;
  initialTerm?: string;
}) {
  const router = useRouter();
  const { showToast } = useToast();
  const [term, setTerm] = useState(initialTerm);

  const handleSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    const trimmed = term.trim();
    if (trimmed.length < 2) {
      showToast("검색어는 2글자 이상 입력해주세요.");
      return;
    }

    router.push(`/books/search?searchTerm=${encodeURIComponent(trimmed)}`);
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
          value={term}
          onChange={(e) => setTerm(e.target.value)}
        />
        <button className="btn">검색</button>
      </div>
    </form>
  );
}
