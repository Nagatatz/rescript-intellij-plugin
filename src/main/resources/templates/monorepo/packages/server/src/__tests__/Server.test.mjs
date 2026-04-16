import { describe, expect, it } from "vitest";
import { app } from "../Server.res.mjs";

describe("Monorepo server", () => {
  it("GET /api/hello returns 200 with a JSON greeting", async () => {
    const res = await app.request("/api/hello");
    expect(res.status).toBe(200);
    const body = await res.json();
    expect(body.message).toMatch(/Hello/);
  });
})
;
