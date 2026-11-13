// 채운 별과 빈 별을 같은 모양으로 겹쳐야 해서 마스크 아이콘(SvgIcon) 대신 인라인 SVG를 쓴다
const STAR_ICONS = [0, 1, 2, 3, 4].map((index) => (
  <svg
    key={index}
    className="i"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <path d="m12 3 2.7 5.5 6 .9-4.3 4.2 1 6-5.4-2.9-5.4 2.9 1-6-4.3-4.2 6-.9L12 3Z" />
  </svg>
));

// 빈 별 위에 채운 별을 겹치고, 채운 별은 CSS 변수 --v(0~5)만큼만 잘라 보여 준다
export function StarLayers() {
  return (
    <>
      <span>{STAR_ICONS}</span>
      <span className="on">{STAR_ICONS}</span>
    </>
  );
}

export default function RatingStars({
  rating,
}: {
  rating: number | null | undefined;
}) {
  const value = rating ?? 0;

  return (
    <>
      <span
        className="stars sm"
        style={{ "--v": value } as React.CSSProperties}
        aria-hidden="true"
      >
        <StarLayers />
      </span>
      <strong>
        <span className="sr">별점 </span>
        {value.toFixed(1)}
      </strong>
    </>
  );
}
