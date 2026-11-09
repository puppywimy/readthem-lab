import type { CSSProperties } from "react";

export default function Avatar({
  label,
  imageUrl,
  size = 44,
}: {
  label: string | null | undefined;
  imageUrl?: string | null;
  size?: number;
}) {
  return (
    <span
      className="avatar"
      style={{ "--s": `${size}px` } as CSSProperties}
      aria-hidden="true"
    >
      {label ? label.charAt(0).toUpperCase() : "?"}
      {imageUrl && (
        // eslint-disable-next-line @next/next/no-img-element
        <img src={imageUrl} alt="" loading="lazy" />
      )}
    </span>
  );
}
