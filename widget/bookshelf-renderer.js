import { DOMImplementation, XMLSerializer } from "xmldom";
import rough from "roughjs/bundled/rough.cjs.js";
import { SVG_NS, WIDGET_HEIGHT, WIDGET_WIDTH } from "./constants.js";
import {
  appendFramedPanel,
  appendOuterBorderOverlay,
  appendRoughLine,
  appendRoughPath,
  appendRoughStar,
  appendText,
} from "./svg.js";
import { hashString, lerp, mulberry32 } from "./utils.js";

export function renderBookshelfWidget(data) {
  const {
    reviewCount = 0,
    reviewWithContentCount = 0,
    wishCount = 0,
    books = [],
  } = data;

  const doc = new DOMImplementation().createDocument(SVG_NS, "svg", null);
  const svg = doc.documentElement;
  svg.setAttribute("viewBox", `0 0 ${WIDGET_WIDTH} ${WIDGET_HEIGHT}`);
  svg.setAttribute("xmlns", SVG_NS);

  const rc = rough.svg(svg);

  appendDefsAndStyle(doc, svg);
  appendFramedPanel(svg, rc, 8, 8, 1584, 704, 34, "#fffdf7");
  appendBookStack(doc, svg, rc, books);

  const infoX = 790;
  appendText(doc, svg, { x: infoX, y: 158, class: "main-title", text: "나의 작은 책장" });
  appendText(doc, svg, { x: infoX, y: 232, class: "subtitle", text: "오늘도 한 권, 차곡차곡 쌓는 중" });
  appendStatCard(doc, svg, rc, infoX, 304, "#1f1f1f", reviewCount, "읽은 책", "orange");
  appendStatCard(doc, svg, rc, infoX + 268, 304, "#1f1f1f", reviewWithContentCount, "쓴 리뷰", "green");
  appendText(doc, svg, { x: infoX, y: 632, class: "bottom", text: `+ ${wishCount}권이 더 책장에서 기다리는 중` });
  appendRoughStar(svg, rc, 1388, 322, 34, "#f472b6");
  appendRoughStar(svg, rc, 1460, 192, 42, "#f59e0b", lerp(-12, 12, mulberry32(hashString("wish-star"))()));
  appendExtraStars(svg, rc);

  appendOuterBorderOverlay(svg, rc, 8, 8, 1584, 704, 34);

  return new XMLSerializer().serializeToString(svg);
}

function appendBookStack(doc, svg, rc, books) {
  const visibleBooks = books.slice(-5);
  const renderItems = visibleBooks
    .map((book, index) => ({ book, originalIndex: index, visualSlot: visibleBooks.length - 1 - index }))
    .sort((a, b) => a.visualSlot - b.visualSlot);

  renderItems.forEach(({ book, originalIndex, visualSlot }) => {
    const seed = hashString(`${book.title ?? "book"}-${visualSlot}`);
    const rand = mulberry32(seed);
    const isBottomBook = visualSlot === 0;
    const tilt = isBottomBook ? 0 : lerp(-1.15, 1.15, rand());
    const wobbleX = lerp(-4, 4, rand());
    const wobbleY = isBottomBook ? 0 : lerp(-2, 2, rand());
    const bookHeight = 92;
    const bookGap = 96;
    const bookWidthBySlot = [474, 468, 482, 470, 462];
    const w = bookWidthBySlot[visualSlot] ?? 470;
    const h = bookHeight;
    const sideDepth = 22 + visualSlot * 1.5;
    const topDepth = 10 + visualSlot * 1.1;
    const bottomBookBottomY = 708;
    const x = 132 + visualSlot * 7 + wobbleX;
    const y = bottomBookBottomY - topDepth - h - visualSlot * bookGap + wobbleY;
    const bookFillPaletteBySlot = ["#e9d5ff", "#fecdd3", "#bae6fd", "#d9f99d", "#fde68a"];
    const bookFill = bookFillPaletteBySlot[visualSlot] ?? "#fde68a";

    const bookGroup = doc.createElementNS(SVG_NS, "g");
    bookGroup.setAttribute("transform", `translate(${x} ${y}) rotate(${tilt} ${w / 2} ${h / 2})`);
    svg.appendChild(bookGroup);

    appendRoughPath(bookGroup, rc, `
      M 0 ${topDepth}
      L ${sideDepth} 0
      L ${w + sideDepth} 0
      L ${w} ${topDepth}
      Z
    `, { fill: bookFill, fillStyle: "solid", stroke: "#1f1f1f", strokeWidth: 2, roughness: 1.15, bowing: 1 });

    appendRoughPath(bookGroup, rc, `
      M ${w} ${topDepth}
      L ${w + sideDepth} 0
      L ${w + sideDepth} ${h}
      L ${w} ${h + topDepth}
      Z
    `, { fill: "#ffffff", fillStyle: "solid", stroke: "#1f1f1f", strokeWidth: 2, roughness: 1.15, bowing: 1 });

    appendPageLines(bookGroup, rc, w, topDepth, sideDepth, h);

    appendFramedPanel(bookGroup, rc, 0, topDepth, w, h, 12, bookFill);
    appendText(doc, bookGroup, { x: 62, y: topDepth + 62, class: "book-title", text: book.title ?? `책제목${originalIndex + 1}` });

    if (book.hasContent) {
      appendBookmark(bookGroup, rc, w, sideDepth, topDepth);
    }
  });
}

function appendBookmark(bookGroup, rc, w, sideDepth, topDepth) {
  appendRoughPath(bookGroup, rc, `
    M ${w + 2} ${topDepth + 20}
    H ${w + sideDepth + 50}
    L ${w + sideDepth + 28} ${topDepth + 36}
    L ${w + sideDepth + 50} ${topDepth + 52}
    H ${w + 2}
    Z
  `, { fill: "#ffffff", fillStyle: "solid", stroke: "#1f1f1f", strokeWidth: 2.1, roughness: 1.6 });
}

function appendPageLines(bookGroup, rc, w, topDepth, sideDepth, h) {
  const x1 = w + 7;
  const x2 = w + sideDepth - 6;
  const slope = topDepth / sideDepth;
  const baseYs = [topDepth + 20, topDepth + 36, topDepth + 52, topDepth + 68];

  baseYs.forEach((y1, index) => {
    const y2 = y1 - slope * (x2 - x1);

    appendRoughLine(bookGroup, rc, x1, y1, x2, y2, {
      stroke: "#bdbdbd",
      strokeWidth: index === baseYs.length - 1 ? 1.02 : 0.98,
      roughness: 0.7,
      bowing: 0.15,
    });
  });
}

function appendExtraStars(svg, rc) {
  const stars = [
    { x: 1292, y: 98, size: 22, fill: "#fde68a", seed: "star-1" },
    { x: 1510, y: 286, size: 20, fill: "#bae6fd", seed: "star-2" },
    { x: 1332, y: 252, size: 18, fill: "#fbcfe8", seed: "star-3" },
  ];

  stars.forEach(({ x, y, size, fill, seed }) => {
    const rotation = lerp(-14, 14, mulberry32(hashString(seed))());
    appendRoughStar(svg, rc, x, y, size, fill, rotation);
  });
}

function appendDefsAndStyle(doc, svg) {
  const style = doc.createElementNS(SVG_NS, "style");
  style.appendChild(doc.createTextNode(`
    @font-face {
      font-family: 'GangwonEducationModuche';
      src: url('https://cdn.jsdelivr.net/gh/projectnoonnu/noonfonts_2201-2@1.0/GangwonEdu_OTFLightA.woff') format('woff');
      font-weight: 300;
      font-display: swap;
    }
    @font-face {
      font-family: 'GangwonEducationModuche';
      src: url('https://cdn.jsdelivr.net/gh/projectnoonnu/noonfonts_2201-2@1.0/GangwonEdu_OTFBoldA.woff') format('woff');
      font-weight: 700;
      font-display: swap;
    }
    svg {
      font-family: "GangwonEducationModuche", "Pretendard", "Noto Sans KR", "Apple SD Gothic Neo", sans-serif;
      font-synthesis: none;
    }
    text { paint-order: stroke fill; stroke-linejoin: round; stroke-linecap: round; }
    .main-title { font-size: 72px; font-weight: 700; fill: #171717; stroke: rgba(255, 253, 247, 0.45); stroke-width: 1.8px; }
    .subtitle { font-size: 38px; font-weight: 700; fill: #525252; letter-spacing: 2px; stroke: rgba(255, 253, 247, 0.35); stroke-width: 1px; }
    .book-title { font-size: 33px; font-weight: 700; fill: #171717; stroke: rgba(255, 253, 247, 0.35); stroke-width: 1px; }
    .num { font-size: 84px; font-weight: 700; fill: #171717; stroke: rgba(255, 253, 247, 0.35); stroke-width: 1px; }
    .orange { fill: #171717; }
    .green { fill: #171717; }
    .label { font-size: 32px; font-weight: 700; fill: #525252; stroke: rgba(255, 253, 247, 0.25); stroke-width: 0.8px; }
    .bottom { font-size: 34px; font-weight: 700; fill: #525252; letter-spacing: 1px; stroke: rgba(255, 253, 247, 0.25); stroke-width: 0.8px; }
  `));
  svg.appendChild(style);
}

function appendStatCard(doc, svg, rc, x, y, stroke, number, label, colorClass) {
  appendFramedPanel(svg, rc, x, y, 230, 232, 34, "#fff7ed", stroke);
  appendText(doc, svg, { x: x + 115, y: y + 126, "text-anchor": "middle", class: `num ${colorClass}`, text: String(number) });
  appendText(doc, svg, { x: x + 115, y: y + 180, "text-anchor": "middle", class: "label", text: label });
}
