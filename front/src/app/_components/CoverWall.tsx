import type { components } from "@/lib/backend/apiV1/schema";

import BookCover from "@/app/_components/BookCover";

type BookDto = components["schemas"]["BookDto"];

// public/covers/1.jpg ~ 15.jpg
const STATIC_COVERS = 15;

// 세 칸을 두 번씩 이어 붙여 끊김 없이 흐르게 한다. 책이 적어도 칸이 비지 않게 반복한다.
// 처음에는 정적 표지를 보여 주고, 실제 표지는 불러와지는 대로 그 위에 덮인다.
export default function CoverWall({ books }: { books: BookDto[] }) {
  const slots = Array.from(
    { length: Math.max(STATIC_COVERS, books.length) },
    (_, index) => ({
      book: books.length > 0 ? books[index % books.length] : undefined,
      fallbackSrc: `/covers/${(index % STATIC_COVERS) + 1}.jpg`,
    }),
  );

  return (
    <div className="wall" aria-hidden="true">
      {[0, 1, 2].map((column) => {
        const items = slots.filter((_, index) => index % 3 === column);

        return (
          <div className="col" key={column}>
            {[...items, ...items].map(({ book, fallbackSrc }, index) => (
              <BookCover
                key={index}
                bookId={book?.id}
                imgUrl={book?.imgUrl}
                title={book?.title}
                fallbackSrc={fallbackSrc}
                loading="eager"
              />
            ))}
          </div>
        );
      })}
    </div>
  );
}
