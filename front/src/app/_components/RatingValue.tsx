import SvgIcon from "@/app/_components/SvgIcon";

export default function RatingValue({
  rating,
  inline = false,
}: {
  rating: number | null | undefined;
  inline?: boolean;
}) {
  return (
    <span className={inline ? "rating" : "metric"}>
      <span className="sr">별점 </span>
      <SvgIcon name="star" className="i h-3" />
      <span>{(rating ?? 0).toFixed(1)}</span>
    </span>
  );
}
