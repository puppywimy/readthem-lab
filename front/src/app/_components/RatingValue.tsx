"use client";

import { useEffect, useRef } from "react";

import rough from "roughjs/bin/rough";

import { ratingFillColor } from "@/lib/ratingColor";
import { useTheme } from "@/lib/theme/ThemeProvider";

function starPoints(width: number, height: number) {
  const centerX = width / 2;
  const centerY = height / 2;
  const outerRadius = Math.min(width, height) * 0.42;
  const innerRadius = outerRadius * 0.48;

  return Array.from({ length: 10 }, (_, index) => {
    const angle = -Math.PI / 2 + (index * Math.PI) / 5;
    const radius = index % 2 === 0 ? outerRadius : innerRadius;
    return [
      centerX + Math.cos(angle) * radius,
      centerY + Math.sin(angle) * radius,
    ] as [number, number];
  });
}

export function RoughStarIcon({
  fill,
  className = "",
}: {
  fill: string;
  className?: string;
}) {
  const svgRef = useRef<SVGSVGElement | null>(null);
  const { theme } = useTheme();

  useEffect(() => {
    const svg = svgRef.current;
    if (svg == null) return;
    const target = svg.parentElement ?? svg;

    const draw = () => {
      const { width, height } = target.getBoundingClientRect();
      if (width === 0 || height === 0) return;

      const rc = rough.svg(svg);
      svg.setAttribute("width", `${width}`);
      svg.setAttribute("height", `${height}`);
      svg.setAttribute("viewBox", `0 0 ${width} ${height}`);
      svg.replaceChildren();

      svg.append(
        rc.polygon(starPoints(width, height), {
          stroke: theme === "dark" ? "#f4f1ea" : "#1f1f1f",
          strokeWidth: 1.15,
          roughness: 1.15,
          bowing: 1,
          fill,
          fillStyle: "solid",
        }),
      );
    };

    draw();

    const resizeObserver = new ResizeObserver(draw);
    resizeObserver.observe(target);

    return () => {
      resizeObserver.disconnect();
    };
  }, [fill, theme]);

  return <svg ref={svgRef} aria-hidden="true" className={className} />;
}

export default function RatingValue({
  rating,
  className = "",
  starClassName = "h-5 w-5",
}: {
  rating: number | string;
  className?: string;
  starClassName?: string;
}) {
  const numericRating = typeof rating === "number" ? rating : Number(rating);
  const safeRating = Number.isFinite(numericRating) ? numericRating : 0;

  return (
    <span className={`inline-flex items-center gap-1 ${className}`}>
      <span className={`relative inline-block shrink-0 ${starClassName}`}>
        <RoughStarIcon
          fill={ratingFillColor(safeRating)}
          className="rough-overlay"
        />
      </span>
      <span>{rating}</span>
    </span>
  );
}
