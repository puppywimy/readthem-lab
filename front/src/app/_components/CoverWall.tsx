import type { components } from "@/lib/backend/apiV1/schema";

import BookCover from "@/app/_components/BookCover";

type BookDto = components["schemas"]["BookDto"];

const MIN_COVERS = 15;

// 세 칸을 두 번씩 이어 붙여 끊김 없이 흐르게 한다. 책이 적어도 칸이 비지 않게 반복한다.
export default function CoverWall({ books }: { books: BookDto[] }) {
  if (books.length === 0) return <div className="wall" aria-hidden="true" />;

  const covers = Array.from(
    { length: Math.max(MIN_COVERS, books.length) },
    (_, index) => books[index % books.length],
  );

  return (
    <div className="wall" aria-hidden="true">
      {[0, 1, 2].map((column) => {
        const items = covers.filter((_, index) => index % 3 === column);

        return (
          <div className="col" key={column}>
            {[...items, ...items].map((book, index) => (
              <BookCover
                key={index}
                bookId={book.id}
                imgUrl={book.imgUrl}
                title={book.title}
                loading="eager"
              />
            ))}
          </div>
        );
      })}
    </div>
  );
}
