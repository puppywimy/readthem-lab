const WINDOW = 2;

export default function Pager({
  page,
  totalPages,
  onChange,
}: {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}) {
  if (totalPages <= 1) return null;

  const start = Math.max(0, page - WINDOW);
  const end = Math.min(totalPages - 1, page + WINDOW);

  return (
    <nav className="pager" aria-label="페이지">
      {Array.from({ length: end - start + 1 }, (_, index) => start + index).map(
        (value) => (
          <button
            key={value}
            type="button"
            aria-current={value === page ? "page" : undefined}
            onClick={() => onChange(value)}
          >
            {value + 1}
          </button>
        ),
      )}
    </nav>
  );
}
