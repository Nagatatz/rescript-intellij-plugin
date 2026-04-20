import { describe, expect, it } from "vitest";
import { hasFlag, namedValue } from "../Args.res.mjs";

describe("hasFlag", () => {
  it("returns true when the flag is present", () => {
    expect(hasFlag(["--shout", "Alice"], "--shout")).toBe(true);
  });
  it("returns false when the flag is absent", () => {
    expect(hasFlag(["Alice"], "--shout")).toBe(false);
  });
});

describe("namedValue", () => {
  it("returns the value following the flag", () => {
    expect(namedValue(["--out", "dist"], "--out")).toEqual("dist");
  });
  it("returns undefined when the flag is missing", () => {
    expect(namedValue(["Alice"], "--out")).toBeUndefined();
  });
});
