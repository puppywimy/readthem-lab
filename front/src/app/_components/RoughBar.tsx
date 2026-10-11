"use client";

import { useEffect, useRef } from "react";

import rough from "roughjs/bin/rough";

import { useTheme } from "@/lib/theme/ThemeProvider";

export default function RoughBar({
  className = "",
  fill,
  variant = "bar",
  orientation = "horizontal",
  lineInset,
}: {
  className?: string;
  fill: string;
  variant?: "bar" | "line";
  orientation?: "horizontal" | "vertical";
  lineInset?: number;
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
      const inset = 1;
      svg.setAttribute("width", `${width}`);
      svg.setAttribute("height", `${height}`);
      svg.setAttribute("preserveAspectRatio", "none");
      svg.replaceChildren();
      const stroke = theme === "dark" ? "#f4f1ea" : "#1f1f1f";

      if (variant === "line") {
        svg.setAttribute("viewBox", `0 0 ${width} ${height}`);
        svg.style.overflow = "visible";

        const resolvedLineInset =
          lineInset ?? Math.max(2, Math.min(width, height) * 0.25);
        const line =
          orientation === "vertical"
            ? rc.line(
                width / 2,
                resolvedLineInset,
                width / 2,
                height - resolvedLineInset,
                {
                  stroke,
                  strokeWidth: 1,
                  roughness: 1.8,
                  bowing: 1.4,
                },
              )
            : rc.line(
                resolvedLineInset,
                height / 2,
                width - resolvedLineInset,
                height / 2,
                {
                  stroke,
                  strokeWidth: 1,
                  roughness: 1.8,
                  bowing: 1.4,
                },
              );

        svg.append(line);
        return;
      }

      svg.setAttribute("viewBox", `0 0 ${width} ${height}`);
      svg.style.overflow = "";

      const left = inset;
      const top = inset;
      const right = width - inset;
      const bottom = height - inset;
      const radius = Math.min(5, (right - left) / 2, (bottom - top) / 2);
      const roundedRect = [
        `M ${left + radius} ${top}`,
        `L ${right - radius} ${top}`,
        `Q ${right} ${top} ${right} ${top + radius}`,
        `L ${right} ${bottom - radius}`,
        `Q ${right} ${bottom} ${right - radius} ${bottom}`,
        `L ${left + radius} ${bottom}`,
        `Q ${left} ${bottom} ${left} ${bottom - radius}`,
        `L ${left} ${top + radius}`,
        `Q ${left} ${top} ${left + radius} ${top}`,
        "Z",
      ].join(" ");

      svg.append(
        rc.path(roundedRect, {
          stroke,
          strokeWidth: 1,
          roughness: 1.25,
          bowing: 1.1,
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
  }, [fill, lineInset, orientation, theme, variant]);

  return (
    <svg
      ref={svgRef}
      aria-hidden="true"
      className={className}
      preserveAspectRatio="none"
    />
  );
}
