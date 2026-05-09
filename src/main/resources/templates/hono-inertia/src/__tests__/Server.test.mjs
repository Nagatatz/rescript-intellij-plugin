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

  it("GET / serves SSR-rendered HTML inside the Inertia mount point", async () => {
    const res = await app.request("/");
    expect(res.status).toBe(200);
    const body = await res.text();
    // `Ssr.renderInertia` runs server-side and writes the rendered Home page
    // into `<div id="app" data-page='…'>…</div>`, so the response must contain
    // both the mount point and a non-empty body inside it. We assert the page
    // headline rather than any specific HTML scaffold so adjustments to
    // MainLayout don't break the smoke.
    expect(body).toMatch(/<div id="app" data-page='[^']+'>[\s\S]*<\/div>/);
    expect(body).toContain("<h1>Home</h1>");
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
