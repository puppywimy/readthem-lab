import SvgIcon from "@/app/_components/SvgIcon";

export default function LikeButton({
  count,
  liked,
  onClick,
}: {
  count: number | undefined;
  liked: boolean;
  onClick?: () => void;
}) {
  return (
    <button
      type="button"
      className="like"
      aria-pressed={liked}
      aria-label="좋아요"
      onClick={onClick}
    >
      <SvgIcon name={liked ? "heart" : "heart-outline"} />
      <span>{count ?? 0}</span>
    </button>
  );
}
