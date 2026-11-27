import { describe, expect, it } from "vitest";
import { make as App } from "../App.res.mjs";
import { parseGreetForm } from "../Validation.res.mjs";

describe("App", () => {
  it("is a function component", () => {
    expect(typeof App).toBe("function");
  });
});

describe("selected validation library", () => {
  it("trims a valid name", () => {
    expect(parseGreetForm("  Ada  ")).toEqual({ TAG: "Ok", _0: { name: "Ada" } });
  });

  it("rejects whitespace-only input with a useful error", () => {
    expect(parseGreetForm(" \t ").TAG).toBe("Error");
    expect(parseGreetForm(" \t ")._0).toEqual(expect.any(String));
  });
});
