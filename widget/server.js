import express from "express";
import cors from "cors";
import { renderBookshelfWidget } from "./bookshelf-renderer.js";

const app = express();
app.use(cors());
app.use(express.json({ limit: "1mb" }));

app.post("/widgets/bookshelf", (req, res) => {
  try {
    const svg = renderBookshelfWidget(req.body);
    res.setHeader("Content-Type", "image/svg+xml; charset=utf-8");
    res.send(svg);
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "SVG rendering failed" });
  }
});

const port = Number(process.env.PORT ?? 3001);

app.listen(port, "0.0.0.0", () => {
  console.log(`Widget renderer running on http://localhost:${port}`);
});
