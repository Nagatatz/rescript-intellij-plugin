import { describe, it, expect } from "vitest";
import { make as Greet } from "../components/Greet.res.mjs";
import { make as Counter } from "../components/Counter.res.mjs";

describe("Server Component", () => {
  it("Greet exposes a function component", () => {
    expect(typeof Greet).toBe("function");
  });
});

describe("Client Component body", () => {
  it("Counter exposes a function component", () => {
    expect(typeof Counter).toBe("function");
  });
});
