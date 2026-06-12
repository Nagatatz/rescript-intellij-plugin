#!/usr/bin/env node
// Generates a synthetic package.json from the npm version constants that
// the wizard templates write into user projects, so `npm audit` can cover
// dependencies Dependabot cannot see (they live in Kotlin source, not in a
// manifest).
//
// The package-name -> constant mapping is NOT hand-maintained: it is
// extracted from the template sources themselves, by scanning for the two
// patterns every template uses to reference TemplateVersions:
//
//     "pkg-name" to TemplateVersions.CONSTANT
//     deps["pkg-name"] = TemplateVersions.CONSTANT
//
// Usage: node .github/scripts/audit-template-versions.mjs [outDir]
//   Writes <outDir>/package.json (default: .github/scripts/template-audit).
//   The caller then runs `npm install --package-lock-only` and `npm audit`.

import { readFileSync, readdirSync, mkdirSync, writeFileSync } from "node:fs";
import { join, dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = resolve(dirname(fileURLToPath(import.meta.url)), "../..");
const templatesDir = join(
  repoRoot,
  "src/main/kotlin/com/rescript/plugin/wizard",
);
const versionsFile = join(templatesDir, "templates/TemplateVersions.kt");
const outDir = resolve(process.argv[2] ?? join(repoRoot, ".github/scripts/template-audit"));

// 1. Constant name -> version string, from TemplateVersions.kt.
const versions = {};
for (const m of readFileSync(versionsFile, "utf8").matchAll(
  /(?:const )?val ([A-Z0-9_]+) = "([^"]+)"/g,
)) {
  versions[m[1]] = m[2];
}

// 2. npm package name -> constant name, from every template source file.
const ktFiles = [];
for (const dir of [templatesDir, join(templatesDir, "templates")]) {
  for (const f of readdirSync(dir)) {
    if (f.endsWith(".kt")) ktFiles.push(join(dir, f));
  }
}
// npm package names are lowercase-only; the char class excludes camelCase
// template-variable keys (e.g. "htmxVersion") that share the same Kotlin shape.
const depPattern =
  /"([@a-z0-9][@a-z0-9./_-]*)"(?:\s+to\s+|\]\s*=\s*)TemplateVersions\.([A-Z0-9_]+)/g;
const dependencies = {};
const conflicts = [];
for (const file of ktFiles) {
  for (const m of readFileSync(file, "utf8").matchAll(depPattern)) {
    const [, pkg, constant] = m;
    const version = versions[constant];
    if (version === undefined) {
      console.error(`ERROR: ${file} references unknown constant ${constant}`);
      process.exit(1);
    }
    // Only semver-range values belong in package.json (skips engine pins
    // or docker tags if a future constant ever flows through this pattern).
    if (!/^[\^~]?\d/.test(version)) continue;
    if (dependencies[pkg] !== undefined && dependencies[pkg] !== version) {
      conflicts.push(`${pkg}: ${dependencies[pkg]} vs ${version}`);
      continue; // keep the first occurrence; conflicts are reported below
    }
    dependencies[pkg] = version;
  }
}

if (conflicts.length > 0) {
  console.error(`WARNING: conflicting versions for the same package:\n  ${conflicts.join("\n  ")}`);
}
const count = Object.keys(dependencies).length;
if (count < 30) {
  // The templates reference far more than 30 packages today; a sudden drop
  // means the extraction pattern broke and the audit would silently shrink.
  console.error(`ERROR: only ${count} packages extracted — extraction pattern likely broken`);
  process.exit(1);
}

mkdirSync(outDir, { recursive: true });
writeFileSync(
  join(outDir, "package.json"),
  `${JSON.stringify(
    {
      name: "template-versions-audit",
      private: true,
      description:
        "Synthetic manifest generated from TemplateVersions.kt for npm audit; never installed or published.",
      dependencies: Object.fromEntries(Object.entries(dependencies).sort()),
    },
    null,
    2,
  )}\n`,
);
console.log(`Wrote ${join(outDir, "package.json")} with ${count} dependencies.`);
