import RoughBar from "@/app/_components/RoughBar";

import { ratingFillColor } from "@/lib/ratingColor";

const RATING_BUCKETS = [
  "5.0",
  "4.5",
  "4.0",
  "3.5",
  "3.0",
  "2.5",
  "2.0",
  "1.5",
  "1.0",
  "0.5",
];

export default function RatingHistogram({
  rating,
  className = "",
}: {
  rating: Record<string, unknown>;
  className?: string;
}) {
  const displayBuckets = [...RATING_BUCKETS].reverse();
  const counts = displayBuckets.map((bucket) => Number(rating[bucket] ?? 0));
  const maxCount = Math.max(1, ...counts);

  return (
    <div className={`flex flex-col gap-1 ${className}`}>
      <div className="rough-rating-chart">
        {displayBuckets.map((bucket, i) => (
          <div key={bucket} className="rough-rating-column">
            <div
              className="rough-rating-bar"
              style={{ height: `${(counts[i] / maxCount) * 100}%` }}
            >
              <RoughBar
                className="rough-overlay"
                fill={ratingFillColor(Number(bucket))}
              />
            </div>
          </div>
        ))}
      </div>
      <div className="flex justify-between text-xs text-gray-500">
        <span>0.5</span>
        <span>5</span>
      </div>
    </div>
  );
}
