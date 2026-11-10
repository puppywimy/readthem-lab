const SCORES = [5, 4, 3, 2, 1];

// API의 0.5 단위 구간을 시안의 5단계로 묶는다 (4.5, 5.0 → 5)
export default function RatingHistogram({
  rating,
}: {
  rating: Record<string, unknown>;
}) {
  const counts = SCORES.map(
    (score) =>
      Number(rating[score.toFixed(1)] ?? 0) +
      Number(rating[(score - 0.5).toFixed(1)] ?? 0),
  );
  const total = counts.reduce((sum, count) => sum + count, 0);
  if (total === 0) return null;

  return (
    <div className="hist" aria-hidden="true">
      {SCORES.map((score, index) => (
        <div key={score}>
          <span>{score}</span>
          <i
            style={{ width: `${(counts[index] / total) * 100}%` }}
            title={`${counts[index]}개`}
          />
        </div>
      ))}
    </div>
  );
}
