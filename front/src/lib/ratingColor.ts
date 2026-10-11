const RATING_STYLES = [
  { min: 4, textClassName: "text-green-600", fill: "#4ade80" },
  { min: 2.5, textClassName: "text-sky-500", fill: "#38bdf8" },
  { min: 1.5, textClassName: "text-orange-500", fill: "#fb923c" },
  { min: 0, textClassName: "text-red-500", fill: "#f87171" },
] as const;

export function ratingStyle(rating: number) {
  return (
    RATING_STYLES.find(({ min }) => rating >= min) ??
    RATING_STYLES[RATING_STYLES.length - 1]
  );
}

export function ratingColor(rating: number) {
  return ratingStyle(rating).textClassName;
}

export function ratingFillColor(rating: number) {
  return ratingStyle(rating).fill;
}
