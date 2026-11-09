"use client";

import { useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { useToast } from "@/lib/toast/ToastProvider";

import BookCover from "@/app/_components/BookCover";
import Dialog from "@/app/_components/Dialog";
import Pager from "@/app/admin/Pager";

type PageBookDto = components["schemas"]["PageBookDto"];
type BookDetailDto = components["schemas"]["BookDetailDto"];

export default function BookAdmin() {
  const { showToast, showErrorToast, confirmAction } = useToast();
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

  const handleDelete = async (id: number) => {
    if (!(await confirmAction("이 도서를 삭제하시겠습니까?"))) return;

    apiFetch(`/api/v1/books/${id}`, { method: "DELETE" })
      .then((data) => {
        showToast(data?.message ?? "도서를 삭제했습니다.");
        loadBooks(pageNumber);
      })
      .catch(showErrorToast);
  };

  if (pageData == null)
    return (
      <div aria-busy="true" style={{ marginTop: 28 }}>
        <span className="sk line" style={{ height: 44 }} />
      </div>
    );

  const books = pageData.content ?? [];

  return (
    <>
      {books.length === 0 ? (
        <div className="state">
          <strong>도서가 없습니다.</strong>
        </div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>제목</th>
                <th className="num">평점</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {books.map((book) => (
                <tr key={book.id}>
                  <td>
                    <span className="cell-book">
                      <BookCover
                        bookId={book.id}
                        imgUrl={book.imgUrl}
                        title={book.title}
                      />
                      {book.title}
                    </span>
                  </td>
                  <td className="num">
                    {(book.averageRating ?? 0).toFixed(1)}
                  </td>
                  <td className="fit">
                    <button
                      type="button"
                      className="text-btn"
                      onClick={() => {
                        if (book.id != null) startEdit(book.id);
                      }}
                    >
                      수정
                    </button>
                    <button
                      type="button"
                      className="text-btn"
                      onClick={() => {
                        if (book.id != null) handleDelete(book.id);
                      }}
                    >
                      삭제
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <Pager
        page={pageData.number ?? 0}
        totalPages={pageData.totalPages ?? 1}
        onChange={setPageNumber}
      />

      {editingId != null && editingBook != null && (
        <Dialog onClose={cancelEdit}>
          <form
            className="dlg"
            style={{ padding: 0 }}
            onSubmit={handleEditSubmit}
          >
            <h2>도서 수정</h2>
            <label className="field">
              제목
              <input name="title" defaultValue={editingBook.title} />
            </label>
            <label className="field">
              설명
              <textarea
                name="description"
                defaultValue={editingBook.description ?? ""}
              />
            </label>
            <label className="field">
              저자 (쉼표로 구분)
              <input
                name="authors"
                defaultValue={(editingBook.authors ?? []).join(", ")}
              />
            </label>
            <label className="field">
              출판사
              <input
                name="publisher"
                defaultValue={editingBook.publisher ?? ""}
              />
            </label>
            <label className="field">
              표지 이미지 URL
              <input name="imgUrl" defaultValue={editingBook.imgUrl ?? ""} />
            </label>
            <div className="dlg-actions">
              <button type="button" className="btn line" onClick={cancelEdit}>
                취소
              </button>
              <button className="btn">저장</button>
            </div>
          </form>
        </Dialog>
      )}
    </>
  );
}
