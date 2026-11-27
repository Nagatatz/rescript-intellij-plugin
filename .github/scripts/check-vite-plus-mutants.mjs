import assert from "node:assert/strict";
import { kotlinSource, requireUpstreamAgreement, upstreamFixture, versionContract } from "./vite-plus-contract.mjs";

const source = kotlinSource();
const upstream = upstreamFixture();
const check = (text) => requireUpstreamAgreement(versionContract(text), upstream);
check(source);

const mutants = [
  ["legacy CLI range", /(val VITE_PLUS = )"[^"]+"/, '$1"^1.1.0"'],
  ["legacy core range", /(val VITE_PLUS_CORE = )"[^"]+"/, '$1"^1.1.0"'],
  ["unrelated Vite package", /(val VITE_PLUS_VITE_ALIAS = ).+/, "$1VITE"],
  ["different exact core", /(val VITE_PLUS_CORE = )"[^"]+"/, '$1"1.6.0"'],
  ["different coverage provider", /(val VITE_PLUS_VITEST_COVERAGE_V8 = ).+/, '$1"5.0.4"'],
];

let killed = 0;
for (const [name, pattern, replacement] of mutants) {
  const changed = source.replace(pattern, replacement);
  assert.notEqual(changed, source, `Mutation did not apply: ${name}`);
  try {
    check(changed);
    console.error(`SURVIVED: ${name}`);
  } catch (error) {
    console.log(`KILLED: ${name} (${error.message})`);
    killed += 1;
  }
}
console.log(`${killed}/${mutants.length} targeted source mutants killed`);
if (killed !== mutants.length) process.exitCode = 1;
