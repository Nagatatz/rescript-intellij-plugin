import { describe, expect, it } from "vitest";
import { app } from "../Server.res.mjs";

describe("Full-Stack server", () => {
  it("GET /api/health returns 200 with a JSON status", async () => {
    const res = await app.request("/api/health");
    expect(res.status).toBe(200);
    const body = await res.json();
    expect(body).toEqual({ status: "ok" });
  });
})
;
