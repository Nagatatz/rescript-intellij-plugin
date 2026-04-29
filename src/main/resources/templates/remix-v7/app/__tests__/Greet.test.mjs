import { describe, it, expect } from "vitest";
import { make as Greet } from "../components/Greet.res.mjs";
import { homeLoader } from "../loaders/HomeLoader.res.mjs";

describe("Greet component", () => {
  it("is a function component", () => {
    expect(typeof Greet).toBe("function");
  });
});

describe("homeLoader", () => {
  it("echoes the project name", () => {
    expect(homeLoader({ project: "demo" })).toEqual({ name: "demo" });
  });
});
