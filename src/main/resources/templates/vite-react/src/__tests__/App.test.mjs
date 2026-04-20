import { describe, expect, it } from "vitest";
import { make as App } from "../App.res.mjs";

describe("App", () => {
  it("is a function component", () => {
    expect(typeof App).toBe("function");
  });
});
