import RoughFrame from "@/app/_components/RoughFrame";

const SIZE_CLASSES = {
  sm: "w-10 h-10 text-base",
  lg: "w-24 h-24 text-3xl",
};

export default function Avatar({
  label,
  imageUrl,
  size = "sm",
}: {
  label: string | null;
  imageUrl?: string | null;
  size?: "sm" | "lg";
}) {
  return (
    <div
      className={`${SIZE_CLASSES[size]} relative shrink-0 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-600`}
    >
      <RoughFrame className="rough-overlay" variant="circle" />
      {imageUrl ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src={imageUrl}
          alt=""
          className="absolute inset-0 h-full w-full rounded-full object-cover"
        />
      ) : (
        <span className="relative z-10">
          {label ? label.charAt(0).toUpperCase() : "?"}
        </span>
      )}
    </div>
  );
}
