"use client";

import { Fragment, useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { useToast } from "@/lib/toast/ToastProvider";

import type { components } from "@/lib/backend/apiV1/schema";

import AdminRoughDivider from "@/app/admin/AdminRoughDivider";
import RoughButton from "@/app/_components/RoughButton";
import { RoughInput, RoughTextarea } from "@/app/_components/RoughInput";

type PageBookDto = components["schemas"]["PageBookDto"];
type BookDetailDto = components["schemas"]["BookDetailDto"];

export default function BookAdmin() {
  const { showToast, showErrorToast } = useToast();
  const [pageData, setPageData] = useState<PageBookDto | null>(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingBook, setEditingBook] = useState<BookDetailDto | null>(null);

  const loadBooks = (page: number) => {
    apiFetch(`/api/v1/books/admin?page=${page}&size=10`)
      .then(setPageData)
      .catch(goToErrorPage);
  };

  useEffect(() => {
    loadBooks(pageNumber);
  }, [pageNumber]);

  const startEdit = (id: number) => {
    apiFetch(`/api/v1/books/${id}`)
      .then((data) => {
        setEditingId(id);
        setEditingBook(data);
      })
      .catch(showErrorToast);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditingBook(null);
  };

  const handleEditSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (editingId == null) return;

    const form = e.currentTarget;

    const titleInput = form.elements.namedItem("title") as HTMLInputElement;
    const descriptionInput = form.elements.namedItem(
      "description",
    ) as HTMLTextAreaElement;
    const authorsInput = form.elements.namedItem("authors") as HTMLInputElement;
    const publisherInput = form.elements.namedItem(
      "publisher",
    ) as HTMLInputElement;
    const imgUrlInput = form.elements.namedItem("imgUrl") as HTMLInputElement;

    apiFetch(`/api/v1/books/${editingId}`, {
      method: "PUT",
      body: JSON.stringify({
        title: titleInput.value,
        description: descriptionInput.value,
        authors: authorsInput.value,
        publisher: publisherInput.value,
        imgUrl: imgUrlInput.value,
      }),
    })
      .then((data) => {
        showToast(data?.message ?? "도서 정보를 수정했습니다.");
        cancelEdit();
        loadBooks(pageNumber);
      })
      .catch(showErrorToast);
  };

  const handleDelete = (id: number) => {
    if (!confirm("이 도서를 삭제하시겠습니까?")) return;

    apiFetch(`/api/v1/books/${id}`, { method: "DELETE" })
      .then((data) => {
        showToast(data?.message ?? "도서를 삭제했습니다.");
        loadBooks(pageNumber);
      })
      .catch(showErrorToast);
  };

  if (pageData == null) return <div>로딩중...</div>;

  const books = pageData.content ?? [];

  return (
    <div className="flex flex-col gap-4">
      <div className="overflow-x-auto">
        <AdminRoughDivider />
        <table className="w-full text-left text-sm">
          <thead>
            <tr>
              <th className="p-2">ID</th>
              <th className="p-2">제목</th>
              <th className="p-2">평점</th>
              <th className="p-2"></th>
            </tr>
            <tr aria-hidden="true">
              <td colSpan={4} className="p-0">
                <AdminRoughDivider />
              </td>
            </tr>
          </thead>
          <tbody>
            {books.map((book, index) => (
              <Fragment key={book.id}>
                <tr>
                  <td className="p-2">{book.id}</td>
                  <td className="p-2">{book.title}</td>
                  <td className="p-2">{book.averageRating}</td>
                  <td className="flex gap-2 p-2">
                      <RoughButton
                      roughSize="sm"
                      type="button"
                      onClick={() => {
                        if (book.id == null) return;
                        startEdit(book.id);
                      }}
                    >
                      수정
                    </RoughButton>
                    <RoughButton
                      roughSize="sm"
                      tone="cancel"
                      type="button"
                      onClick={() => {
                        if (book.id == null) return;
                        handleDelete(book.id);
                      }}
                    >
                      삭제
                    </RoughButton>
                  </td>
                </tr>
                {index < books.length - 1 && (
                  <tr aria-hidden="true">
                    <td colSpan={4} className="p-0">
                      <AdminRoughDivider />
                    </td>
                  </tr>
                )}
              </Fragment>
            ))}
          </tbody>
        </table>
        <AdminRoughDivider />
      </div>

      <div className="flex items-center gap-2">
        <RoughButton
          roughSize="sm"
          type="button"
          disabled={pageData.first}
          onClick={() => setPageNumber((p) => Math.max(0, p - 1))}
        >
          이전
        </RoughButton>
        <span className="text-sm">
          {(pageData.number ?? 0) + 1} / {pageData.totalPages ?? 1}
        </span>
        <RoughButton
          roughSize="sm"
          type="button"
          disabled={pageData.last}
          onClick={() => setPageNumber((p) => p + 1)}
        >
          다음
        </RoughButton>
      </div>

      {editingId != null && editingBook != null && (
        <form
          key={editingBook.id ?? editingId}
          className="sketch-panel flex max-w-md flex-col gap-2 p-3"
          onSubmit={handleEditSubmit}
        >
          <h2 className="font-bold">도서 정보 수정 (#{editingId})</h2>
          <RoughInput
            type="text"
            name="title"
            placeholder="제목"
            defaultValue={editingBook.title}
          />
          <RoughTextarea
            name="description"
            placeholder="설명"
            defaultValue={editingBook.description ?? ""}
            rows={3}
          />
          <RoughInput
            type="text"
            name="authors"
            placeholder="저자 (쉼표로 구분)"
            defaultValue={(editingBook.authors ?? []).join(", ")}
          />
          <RoughInput
            type="text"
            name="publisher"
            placeholder="출판사"
            defaultValue={editingBook.publisher ?? ""}
          />
          <RoughInput
            type="text"
            name="imgUrl"
            placeholder="표지 이미지 URL"
            defaultValue={editingBook.imgUrl ?? ""}
          />
          <div className="flex gap-2">
            <RoughButton tone="submit" type="submit">
              저장
            </RoughButton>
            <RoughButton tone="cancel" type="button" onClick={cancelEdit}>
              취소
            </RoughButton>
          </div>
        </form>
      )}
    </div>
  );
}
