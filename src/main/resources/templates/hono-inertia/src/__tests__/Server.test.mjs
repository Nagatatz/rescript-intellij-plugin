import { describe, expect, it } from "vitest";
import { app } from "../Server.res.mjs";

describe("Hono + Inertia server", () => {
  it("GET /health returns 200 with a JSON status", async () => {
    const res = await app.request("/health");
    expect(res.status).toBe(200);
    const body = await res.json();
    expect(body).toEqual({ status: "ok" });
  });

  it("GET / responds with the Inertia HTML host page on a non-Inertia visit", async () => {
    const res = await app.request("/");
    expect(res.status).toBe(200);
    const body = await res.text();
    // The host page contains the Inertia mount point.
    expect(body).toMatch(/data-page=/);
  });

  it("GET / with the Inertia header returns a JSON page object", async () => {
    const res = await app.request("/", {
      headers: { "X-Inertia": "true", "X-Inertia-Version": "1" },
    });
    expect(res.status).toBe(200);
    const body = await res.json();
    expect(body.component).toBe("Home");
    expect(body.props.title).toBe("Home");
    expect(body.url).toBe("/");
  });
});
