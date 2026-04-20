import { afterEach, describe, expect, it, vi } from "vitest";
import { fetchWithTimeout } from "../Fetcher.res.mjs";

afterEach(() => vi.restoreAllMocks());

describe("fetchWithTimeout", () => {
  it("returns the response when fetch resolves in time", async () => {
    const fake = { ok: true };
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(fake));
    await expect(fetchWithTimeout("https://example.test", 100)).resolves.toBe(fake);
  });
});
