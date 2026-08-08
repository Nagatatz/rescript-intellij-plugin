#!/usr/bin/env node
// Runs `npm audit --json` and fails only on advisories that are NOT documented
// in an allowlist. npm audit has no exclusion mechanism of its own, and some
// advisories cannot be resolved by editing TemplateVersions.kt at all: when the
// only published version of a transitive package is itself inside the vulnerable
// range, there is nothing to bump to. Without a way to record that, the gate has
// to be either permanently red or lowered to `critical`, and both lose the
// signal the gate exists for.
//
// Exclusions are keyed by GHSA advisory id, never by package name, so a future
// unrelated vulnerability in the same package still fails the build.
//
// Usage: node check-npm-audit.mjs [--allowlist <path>] [--severity <level>]
//   Run from the directory holding the package.json / package-lock.json to audit.
//   Exits 1 if any advisory at or above <level> is not allowlisted.

import { execFileSync } from "node:child_process";
import { appendFileSync, readFileSync } from "node:fs";
import { pathToFileURL } from "node:url";

const SEVERITY_ORDER = ["info", "low", "moderate", "high", "critical"];

/**
 * Extracts the GHSA id from an advisory url.
 * npm's audit payload carries the id only inside the url for some registries,
 * so the url is the one field that is reliably present.
 */
export function advisoryId(via) {
  if (via.url) {
    const m = via.url.match(/GHSA-[0-9a-z-]+/i);
    if (m) return m[0];
  }
  // Fall back to the numeric source id so an entry is still addressable.
  return via.source !== undefined ? `npm-${via.source}` : `unknown-${via.name}`;
}

/**
 * Pure decision function — no I/O, so the fixtures in __tests__ can drive it.
 *
 * @param auditJson parsed output of `npm audit --json`
 * @param allowlist parsed allowlist file ({ allow: [...] })
 * @param options.severityThreshold lowest severity that blocks
 * @param options.today ISO date used to evaluate `expires`
 * @returns blocking / allowed / expired / stale advisory lists
 */
export function evaluate(auditJson, allowlist, { severityThreshold = "high", today } = {}) {
  const minRank = SEVERITY_ORDER.indexOf(severityThreshold);
  if (minRank < 0) throw new Error(`unknown severity: ${severityThreshold}`);

  const allowById = new Map();
  for (const entry of allowlist?.allow ?? []) allowById.set(entry.id, entry);

  // One advisory can surface under many packages; collapse to a unique set so
  // the report counts problems, not dependency paths.
  const found = new Map();
  for (const vuln of Object.values(auditJson?.vulnerabilities ?? {})) {
    for (const via of vuln.via ?? []) {
      if (typeof via !== "object") continue; // string entries just name a parent package
      if (SEVERITY_ORDER.indexOf(via.severity) < minRank) continue;
      const id = advisoryId(via);
      if (!found.has(id)) {
        found.set(id, { id, name: via.name, severity: via.severity, title: via.title, url: via.url });
      }
    }
  }

  const blocking = [];
  const allowed = [];
  for (const advisory of found.values()) {
    const entry = allowById.get(advisory.id);
    if (entry) allowed.push({ ...advisory, entry });
    else blocking.push(advisory);
  }

  // Expired entries are reported but do not fail: the expiry is a prompt to
  // re-check upstream, not evidence that something new broke. Failing here
  // would block unrelated releases on a calendar date.
  const expired = [];
  const stale = [];
  for (const entry of allowById.values()) {
    if (entry.expires && today && entry.expires < today) expired.push(entry);
    if (!found.has(entry.id)) stale.push(entry);
  }

  return { blocking, allowed, expired, stale };
}

function parseArgs(argv) {
  const args = { allowlist: null, severity: "high" };
  for (let i = 0; i < argv.length; i += 1) {
    if (argv[i] === "--allowlist") args.allowlist = argv[++i];
    else if (argv[i] === "--severity") args.severity = argv[++i];
  }
  return args;
}

function report(result, severity) {
  const lines = [];
  const push = (s) => {
    lines.push(s);
    console.log(s);
  };

  if (result.allowed.length > 0) {
    push(`### Allowed advisories (${result.allowed.length})`);
    for (const a of result.allowed) {
      push(`- \`${a.id}\` **${a.name}** (${a.severity}) — ${a.entry.reason}`);
      push(`  - reviewed ${a.entry.reviewed}, expires ${a.entry.expires}`);
    }
  }
  for (const e of result.expired) {
    push(`> [!WARNING] Allowlist entry \`${e.id}\` (${e.package}) expired on ${e.expires} — re-check upstream.`);
  }
  for (const s of result.stale) {
    push(`> [!NOTE] Allowlist entry \`${s.id}\` (${s.package}) matched no advisory — it can probably be removed.`);
  }
  if (result.blocking.length > 0) {
    push(`### Blocking advisories (${result.blocking.length})`);
    for (const b of result.blocking) {
      push(`- \`${b.id}\` **${b.name}** (${b.severity}) — ${b.title}`);
      push(`  - ${b.url}`);
    }
    push("");
    push(
      `Fix by raising the pin in \`TemplateVersions.kt\`. Only add an allowlist entry when no ` +
        `published version resolves the advisory, and record why.`,
    );
  } else {
    push(`No un-allowlisted advisories at or above \`${severity}\`.`);
  }

  if (process.env.GITHUB_STEP_SUMMARY) {
    appendFileSync(process.env.GITHUB_STEP_SUMMARY, `${lines.join("\n")}\n`);
  }
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const allowlist = args.allowlist ? JSON.parse(readFileSync(args.allowlist, "utf8")) : { allow: [] };

  // npm audit exits non-zero whenever it finds anything, so the status is not
  // usable as a signal here — only the payload is.
  // On Windows npm is npm.cmd, and since the CVE-2024-27980 mitigation Node
  // refuses to spawn .cmd/.bat without a shell. The argument list here is fixed
  // — no interpolated input — so enabling the shell on Windows adds no injection
  // surface. CI runs on Linux and takes the plain execFile path.
  const isWindows = process.platform === "win32";
  let raw;
  try {
    raw = execFileSync(isWindows ? "npm.cmd" : "npm", ["audit", "--json"], {
      encoding: "utf8",
      maxBuffer: 64 * 1024 * 1024,
      shell: isWindows,
    });
  } catch (e) {
    raw = e.stdout;
    if (!raw) {
      console.error(`npm audit produced no output: ${e.message}`);
      process.exit(2);
    }
  }

  const result = evaluate(JSON.parse(raw), allowlist, {
    severityThreshold: args.severity,
    today: new Date().toISOString().slice(0, 10),
  });

  report(result, args.severity);
  process.exit(result.blocking.length > 0 ? 1 : 0);
}

// Only run when invoked directly, so the tests can import evaluate() without
// shelling out to npm. Compare resolved file URLs — matching on basename alone
// would also fire when some other script of the same name is the entry point.
if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  main();
}
