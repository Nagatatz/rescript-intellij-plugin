import { describe, expect, it } from "vitest";
import { existsSync } from "node:fs";

describe("App module", () => {
  it("compiles to a .res.mjs file", () => {
    expect(existsSync("src/App.res.mjs")).toBe(true);
  });
});
