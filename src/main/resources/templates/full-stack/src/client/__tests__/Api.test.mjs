import { describe, expect, it } from "vitest";

describe("client ApiClient module", () => {
  it("loads without throwing", async () => {
    await expect(import("../ApiClient.res.mjs")).resolves.toBeDefined();
  });
});
