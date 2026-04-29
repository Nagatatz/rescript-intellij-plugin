"use client";

// Thin Client Component boundary. Waku (and the React Server Components
// runtime in general) requires a `"use client"` directive at the top of
// the file that owns the boundary. ReScript does not emit this directive
// in compiled output, so we re-export the ReScript `make` function from a
// TSX file that does.
import { make } from "./Counter.res.mjs";

export default make;
