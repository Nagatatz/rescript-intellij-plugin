import { describe, expect, it } from "vitest";
import { app } from "../Server.res.mjs";

describe("Hono + GraphQL server", () => {
  it("GET /health returns 200 with a JSON status", async () => {
    const res = await app.request("/health");
    expect(res.status).toBe(200);
    const body = await res.json();
    expect(body).toEqual({ status: "ok" });
  });
})
;
