import { readFileSync } from "node:fs";

// Resolves the literal and constant-reference forms used by TemplateVersions.kt.
// Never treat an unknown expression as valid; this check is deliberately not a Kotlin parser.
export function versionContract(source) {
  const declarations = new Map();
  for (const [, name, expression] of source.matchAll(/^\s*val ([A-Z][A-Z0-9_]*)\s*=\s*("[^"\n]*"|[A-Z][A-Z0-9_]*)\s*$/gm)) {
    declarations.set(name, expression);
  }
  function lookup(name, seen = new Set()) {
    if (seen.has(name) || !declarations.has(name)) throw new Error(`Cannot resolve ${name}`);
    const next = new Set(seen).add(name);
    const expr = declarations.get(name);
    return expr.startsWith('"')
      ? expr.slice(1, -1).replace(/\$([A-Z][A-Z0-9_]*)/g, (_, ref) => lookup(ref, next))
      : lookup(expr, next);
  }
  return {
    cli: lookup("VITE_PLUS"),
    core: lookup("VITE_PLUS_CORE"),
    alias: lookup("VITE_PLUS_VITE_ALIAS"),
    vitest: lookup("VITE_PLUS_VITEST"),
    coverage: lookup("VITE_PLUS_VITEST_COVERAGE_V8"),
  };
}

export function requireUpstreamAgreement(tuple, upstream) {
  const exact = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$/;
  const prefix = "npm:@voidzero-dev/vite-plus-core@";
  if (![tuple.cli, tuple.core, tuple.vitest, tuple.coverage].every((v) => exact.test(v))) {
    throw new Error("The Vite+ and Vitest tuple must use exact versions");
  }
  if (tuple.cli !== upstream.version || !upstream.dependencies.vite.startsWith(prefix)) {
    throw new Error("The Vite+ release or its published Vite package is not the recorded upstream");
  }
  if (tuple.alias !== prefix + tuple.core ||
      tuple.alias !== upstream.dependencies.vite ||
      tuple.vitest !== upstream.dependencies.vitest ||
      tuple.coverage !== tuple.vitest) {
    throw new Error("Vite+ core alias, direct dependency, or Vitest pins disagree with upstream");
  }
}

export const sourceUrl = new URL(
  "../../src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateVersions.kt",
  import.meta.url,
);

export const kotlinSource = () => readFileSync(sourceUrl, "utf8");
export const upstreamFixture = () =>
  JSON.parse(readFileSync(new URL("fixtures/vite-plus-upstream.json", import.meta.url), "utf8"));
