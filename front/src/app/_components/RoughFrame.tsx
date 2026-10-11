"use client";

import { useEffect, useRef } from "react";

import rough from "roughjs/bin/rough";

import { useTheme } from "@/lib/theme/ThemeProvider";

type RoughFrameProps = {
  className?: string;
  variant?:
    | "frame"
    | "highlight"
    | "divider"
    | "softDivider"
    | "search"
    | "card"
    | "circle";
};

export default function RoughFrame({
  className = "",
  variant = "frame",
}: RoughFrameProps) {
  const svgRef = useRef<SVGSVGElement | null>(null);
  const { theme } = useTheme();

  useEffect(() => {
    const svg = svgRef.current;
    if (svg == null) return;
    const target = svg.parentElement ?? svg;

    const draw = () => {
      const rc = rough.svg(svg);
      const { width, height } = target.getBoundingClientRect();
      if (width === 0 || height === 0) return;

      svg.setAttribute("width", `${width}`);
      svg.setAttribute("height", `${height}`);
      svg.setAttribute("viewBox", `0 0 ${width} ${height}`);
      svg.replaceChildren();

      if (variant === "highlight") {
        const stroke = rc.path(
          `M 16 ${height - 18} Q ${width * 0.35} ${height - 4}, ${
            width * 0.68
          } ${height - 20} T ${width - 16} ${height - 14}`,
          {
            stroke: "#ffb347",
            strokeWidth: 6,
            roughness: 1.8,
            bowing: 3,
          },
        );
        svg.append(stroke);
        return;
      }

      if (variant === "divider") {
        const overrun = Math.max(8, width * 0.02);
        svg.setAttribute(
          "viewBox",
          `${-overrun} 0 ${width + overrun * 2} ${height}`,
        );
        const firstStroke = rc.line(
          -overrun,
          height / 2,
          width + overrun,
          height / 2 - 1,
          {
            stroke: theme === "dark" ? "#f4f1ea" : "#1f1f1f",
            strokeWidth: 1.25,
            roughness: 1.45,
            bowing: 1.4,
          },
        );
        const secondStroke = rc.line(
          -overrun,
          height / 2 + 2,
          width + overrun,
          height / 2 + 1,
          {
            stroke: theme === "dark" ? "#f4f1ea" : "#1f1f1f",
            strokeWidth: 0.75,
            roughness: 1.35,
            bowing: 1.2,
          },
        );
        svg.append(firstStroke, secondStroke);
        return;
      }

      if (variant === "softDivider") {
        const overrun = Math.max(6, width * 0.015);
        svg.setAttribute(
          "viewBox",
          `${-overrun} 0 ${width + overrun * 2} ${height}`,
        );
        const strokeColor = theme === "dark" ? "#a8a29e" : "#9ca3af";
        const firstStroke = rc.line(
          -overrun,
          height / 2,
          width + overrun,
          height / 2 - 0.5,
          {
            stroke: strokeColor,
            strokeWidth: 0.85,
            roughness: 0.8,
            bowing: 0.65,
          },
        );
        const secondStroke = rc.line(
          -overrun,
          height / 2 + 1.25,
          width + overrun,
          height / 2 + 0.75,
          {
            stroke: strokeColor,
            strokeWidth: 0.4,
            roughness: 0.7,
            bowing: 0.55,
          },
        );
        svg.append(firstStroke, secondStroke);
        return;
      }

      if (variant === "search") {
        const inset = 3;
        const radius = Math.min(10, height / 2 - 4);
        const left = inset;
        const top = inset;
        const right = width - inset;
        const bottom = height - inset;
        const roundedRect = rc.path(
          [
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
          ].join(" "),
          {
            stroke: theme === "dark" ? "#f4f1ea" : "#1f1f1f",
            strokeWidth: 1.4,
            roughness: 1.25,
            bowing: 1.3,
            fill: "transparent",
            preserveVertices: true,
          },
        );
        svg.append(roundedRect);
        return;
      }

      if (variant === "card") {
        const inset = 4;
        const radius = 12;
        const left = inset;
        const top = inset;
        const right = width - inset;
        const bottom = height - inset;
        const roundedRect = rc.path(
          [
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
          ].join(" "),
          {
            stroke: theme === "dark" ? "#f4f1ea" : "#1f1f1f",
            strokeWidth: 1.4,
            roughness: 1.2,
            bowing: 1.2,
            fill: "transparent",
            preserveVertices: true,
          },
        );
        svg.append(roundedRect);
        return;
      }

      if (variant === "circle") {
        const inset = 1.5;
        const diameter = Math.max(0, Math.min(width, height) - inset * 2);
        const circle = rc.ellipse(width / 2, height / 2, diameter, diameter, {
          stroke: theme === "dark" ? "#f4f1ea" : "#1f1f1f",
          strokeWidth: 1.7,
          roughness: 1.35,
          bowing: 1.1,
          fill: "transparent",
        });
        svg.append(circle);
        return;
      }

      const rect = rc.rectangle(3, 3, width - 6, height - 6, {
        stroke: theme === "dark" ? "#f4f1ea" : "#1f1f1f",
        strokeWidth: 1.3,
        roughness: 1.1,
        bowing: 1.2,
        fill: "transparent",
      });
      svg.append(rect);
    };

    draw();

    const resizeObserver = new ResizeObserver(draw);
    resizeObserver.observe(target);

    return () => {
      resizeObserver.disconnect();
    };
  }, [theme, variant]);

  return <svg ref={svgRef} aria-hidden="true" className={className} />;
}
