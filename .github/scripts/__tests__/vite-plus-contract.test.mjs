import assert from "node:assert/strict";
import { test } from "node:test";
import { kotlinSource, requireUpstreamAgreement, upstreamFixture, versionContract } from "../vite-plus-contract.mjs";

test("real wizard pins match the independently captured npm metadata", () => {
  requireUpstreamAgreement(versionContract(kotlinSource()), upstreamFixture());
});

test("seeded resolver property accepts the matching tuple and rejects any one-field drift", () => {
  const seed = 0x5eedc0de;
  let state = seed;
  const next = () => ((state = (Math.imul(1664525, state) + 1013904223) >>> 0) >>> 9) % 10000;
  const version = () => `${next()}.${next()}.${next()}`;
  const different = (v) => v.replace(/\d+$/, (last) => String(Number(last) + 1));
  const prefix = "npm:@voidzero-dev/vite-plus-core@";

  for (let i = 0; i < 512; i++) {
    const [cli, core, vitest] = [version(), version(), version()];
    const tuple = { cli, core, alias: prefix + core, vitest, coverage: vitest };
    const upstream = { version: cli, dependencies: { vite: tuple.alias, vitest } };
    const label = `seed=0x${seed.toString(16)} iteration=${i} tuple=${JSON.stringify(tuple)}`;
    assert.doesNotThrow(() => requireUpstreamAgreement(tuple, upstream), label);
    for (const [key, value] of Object.entries(tuple)) {
      assert.throws(() => requireUpstreamAgreement({ ...tuple, [key]: different(value) }, upstream), `${label} field=${key}`);
    }
  }
});
