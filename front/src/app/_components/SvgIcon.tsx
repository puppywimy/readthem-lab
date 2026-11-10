"use client";

type SvgIconProps = {
  name: string;
  className?: string;
};

export default function SvgIcon({ name, className = "i" }: SvgIconProps) {
  return (
    <span
      aria-hidden="true"
      className={`inline-block shrink-0 bg-current ${className}`}
      style={{
        WebkitMask: `url("/icons/${name}.svg") center / contain no-repeat`,
        mask: `url("/icons/${name}.svg") center / contain no-repeat`,
      }}
    />
  );
}
