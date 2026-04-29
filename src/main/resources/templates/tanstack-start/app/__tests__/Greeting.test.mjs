import { describe, it, expect } from "vitest";
import { make as Greeting } from "../components/Greeting.res.mjs";

describe("Greeting", () => {
  it("renders a function component", () => {
    expect(typeof Greeting).toBe("function");
  });
});
