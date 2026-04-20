import { describe, expect, it } from "vitest";
import { greet } from "../Index.res.mjs";

describe("greet", () => {
  it("returns a greeting containing the supplied name", () => {
    expect(greet("world")).toContain("world");
  });
});
