"use client";

import RoughFrame from "@/app/_components/RoughFrame";

type AdminRoughDividerProps = {
  className?: string;
};

export default function AdminRoughDivider({
  className = "",
}: AdminRoughDividerProps) {
  return (
    <div className={`relative h-2 w-full opacity-70 ${className}`}>
      <RoughFrame className="rough-overlay" variant="softDivider" />
    </div>
  );
}
