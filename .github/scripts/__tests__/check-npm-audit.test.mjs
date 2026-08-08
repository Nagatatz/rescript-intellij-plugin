// Tests for the audit gate's decision logic.
//
// This script decides whether a vulnerability blocks a release, so a bug that
// makes it silently pass is worse than no gate at all — an implementation that
// always returns an empty `blocking` list would look green forever. These cases
// pin down both directions: what must block, and what must not.

import { strict as assert } from "node:assert";
import test from "node:test";

import { advisoryId, evaluate } from "../check-npm-audit.mjs";

/** Builds a minimal `npm audit --json` payload from advisory descriptors. */
function auditJson(advisories) {
  const vulnerabilities = {};
  for (const a of advisories) {
    vulnerabilities[a.name] = {
      name: a.name,
      severity: a.severity,
      via: [
        {
          source: a.source ?? 1,
          name: a.name,
          severity: a.severity,
          title: a.title ?? `${a.name} issue`,
          url: a.url,
        },
      ],
    };
  }
  return { vulnerabilities };
}

const allow = (id, extra = {}) => ({
  allow: [
    {
      id,
      package: "pkg",
      reason: "documented",
      url: `https://github.com/advisories/${id}`,
      reviewed: "2026-01-01",
      expires: "2027-01-01",
      ...extra,
    },
  ],
});

const TODAY = "2026-08-09";

test("a high advisory with an empty allowlist blocks", () => {
  const result = evaluate(
    auditJson([{ name: "sharp", severity: "high", url: "https://github.com/advisories/GHSA-aaaa-bbbb-cccc" }]),
    { allow: [] },
    { today: TODAY },
  );
  assert.equal(result.blocking.length, 1);
  assert.equal(result.blocking[0].id, "GHSA-aaaa-bbbb-cccc");
});

test("a high advisory listed in the allowlist does not block", () => {
  const result = evaluate(
    auditJson([{ name: "image-size", severity: "high", url: "https://github.com/advisories/GHSA-aaaa-bbbb-cccc" }]),
    allow("GHSA-aaaa-bbbb-cccc"),
    { today: TODAY },
  );
  assert.equal(result.blocking.length, 0);
  assert.equal(result.allowed.length, 1);
});

test("advisories below the severity threshold do not block", () => {
  const result = evaluate(
    auditJson([{ name: "uuid", severity: "moderate", url: "https://github.com/advisories/GHSA-dddd-eeee-ffff" }]),
    { allow: [] },
    { today: TODAY },
  );
  assert.equal(result.blocking.length, 0);
});

test("lowering the threshold makes a moderate advisory block", () => {
  const result = evaluate(
    auditJson([{ name: "uuid", severity: "moderate", url: "https://github.com/advisories/GHSA-dddd-eeee-ffff" }]),
    { allow: [] },
    { severityThreshold: "moderate", today: TODAY },
  );
  assert.equal(result.blocking.length, 1);
});

test("an expired allowlist entry is reported but still suppresses the failure", () => {
  const result = evaluate(
    auditJson([{ name: "image-size", severity: "high", url: "https://github.com/advisories/GHSA-aaaa-bbbb-cccc" }]),
    allow("GHSA-aaaa-bbbb-cccc", { expires: "2026-01-02" }),
    { today: TODAY },
  );
  assert.equal(result.expired.length, 1);
  assert.equal(result.blocking.length, 0, "expiry is a prompt to re-check, not a new failure");
});

test("an allowlist entry matching no advisory is reported as stale", () => {
  const result = evaluate(auditJson([]), allow("GHSA-aaaa-bbbb-cccc"), { today: TODAY });
  assert.equal(result.stale.length, 1);
  assert.equal(result.blocking.length, 0);
});

test("the same advisory reached through several packages is counted once", () => {
  const json = auditJson([
    { name: "metro", severity: "high", url: "https://github.com/advisories/GHSA-aaaa-bbbb-cccc" },
    { name: "react-native", severity: "high", url: "https://github.com/advisories/GHSA-aaaa-bbbb-cccc" },
  ]);
  const result = evaluate(json, { allow: [] }, { today: TODAY });
  assert.equal(result.blocking.length, 1);
});

test("string entries in `via` name a parent package and are ignored", () => {
  const json = { vulnerabilities: { metro: { name: "metro", severity: "high", via: ["image-size"] } } };
  const result = evaluate(json, { allow: [] }, { today: TODAY });
  assert.equal(result.blocking.length, 0);
});

test("advisoryId falls back to the numeric source when the url has no GHSA id", () => {
  assert.equal(advisoryId({ source: 1234, name: "pkg", url: "https://example.test/x" }), "npm-1234");
});

test("an unknown severity threshold is rejected rather than silently passing", () => {
  assert.throws(() => evaluate(auditJson([]), { allow: [] }, { severityThreshold: "urgent" }), /unknown severity/);
});
