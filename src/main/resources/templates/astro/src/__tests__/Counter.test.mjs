import { describe, it, expect } from "vitest";
import { make as Counter } from "../components/Counter.res.mjs";
import { make as StaticGreeting } from "../components/StaticGreeting.res.mjs";

describe("Counter Island", () => {
  it("is a function component", () => {
    expect(typeof Counter).toBe("function");
  });
});

describe("StaticGreeting", () => {
  it("is a function component", () => {
    expect(typeof StaticGreeting).toBe("function");
  });
});
