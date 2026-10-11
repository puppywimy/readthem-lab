import { SVG_NS } from "./constants.js";
import { roundedRectPath, rotatePoint, starPoints } from "./utils.js";

export function appendText(doc, parent, attrs) {
  const text = doc.createElementNS(SVG_NS, "text");
  const value = attrs.text ?? "";

  const safeAttrs = { ...attrs };
  delete safeAttrs.text;

  setAttrs(text, safeAttrs);
  text.appendChild(doc.createTextNode(value));
  parent.appendChild(text);
}

export function appendRoughPath(parent, rc, d, options) {
  const node = rc.draw(rc.gen.path(d, options));
  parent.appendChild(node);
}

export function appendRoughLine(parent, rc, x1, y1, x2, y2, options) {
  parent.appendChild(rc.draw(rc.gen.line(x1, y1, x2, y2, options)));
}

export function setAttrs(node, attrs) {
  Object.entries(attrs).forEach(([key, value]) => {
    node.setAttribute(key, String(value));
  });
}

export function appendOuterBorderOverlay(svg, rc, x, y, w, h, r) {
  const path = roundedRectPath(x, y, w, h, r);

  const layers = [
    { dx: 0, dy: 0, strokeWidth: 2.2, roughness: 1.15, bowing: 1.1 },
    { dx: 0.8, dy: -0.6, strokeWidth: 1.2, roughness: 1.15, bowing: 1.1 },
  ];

  layers.forEach(({ dx, dy, strokeWidth, roughness, bowing }) => {
    const node = rc.draw(rc.gen.path(path, {
      fill: "transparent",
      stroke: "#1f1f1f",
      strokeWidth,
      roughness,
      bowing,
      preserveVertices: true,
      fillStyle: "solid",
    }));

    if (dx !== 0 || dy !== 0) {
      node.setAttribute("transform", `translate(${dx} ${dy})`);
    }

    svg.appendChild(node);
  });
}

export function appendFramedPanel(
  svg,
  rc,
  x,
  y,
  w,
  h,
  r,
  fill,
  stroke = "#1f1f1f",
) {
  const path = roundedRectPath(x, y, w, h, r);

  const layers = [
    { dx: 0, dy: 0, fill, stroke: fill, strokeWidth: 0.6, roughness: 0.5, bowing: 0.4 },
    { dx: 1.2, dy: 0.4, fill, stroke: fill, strokeWidth: 0.4, roughness: 0.5, bowing: 0.4 },
    { dx: 0, dy: 0, fill: "transparent", stroke, strokeWidth: 1.8, roughness: 1.2, bowing: 1.1 },
    { dx: 0.8, dy: -0.6, fill: "transparent", stroke, strokeWidth: 1.2, roughness: 1.2, bowing: 1.1 },
  ];

  layers.forEach(({ dx, dy, fill: layerFill, stroke: layerStroke, strokeWidth, roughness, bowing }) => {
    const node = rc.draw(rc.gen.path(path, {
      fill: layerFill,
      stroke: layerStroke,
      strokeWidth,
      roughness,
      bowing,
      preserveVertices: true,
      fillStyle: "solid",
    }));

    if (dx !== 0 || dy !== 0) {
      node.setAttribute("transform", `translate(${dx} ${dy})`);
    }

    svg.appendChild(node);
  });
}

export function appendRoughStar(svg, rc, x, y, size, fill, rotation = 0) {
  const points = starPoints(size, size).map((point) =>
    rotatePoint(point, size / 2, size / 2, rotation),
  );

  const star = rc.draw(rc.gen.polygon(points, {
    stroke: "#1f1f1f",
    strokeWidth: 1.15,
    roughness: 1.15,
    bowing: 1,
    fill,
    fillStyle: "solid",
  }));

  star.setAttribute("transform", `translate(${x} ${y})`);
  svg.appendChild(star);
}
