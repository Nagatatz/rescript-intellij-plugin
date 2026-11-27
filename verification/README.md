# Vite+ wizard compatibility baseline

This baseline covers the dependency tuple the ReScript wizard writes. It is not
a proof of npm, Gradle, the IDE runtime, or any generated application.

- Property test: `node --test .github/scripts/__tests__/vite-plus-contract.test.mjs`
  exercises exact pins against an upstream metadata snapshot and 512 seeded
  tuples, with one-field drift for each tuple. Seed and failing input are logged.
- Targeted mutation check: `node .github/scripts/check-vite-plus-mutants.mjs`
  mutates the actual Kotlin version source in memory; the unmodified source must
  pass, and all five documented changes must fail. This is not a repo-wide mutation score.
- Formal check: `z3 -smt2 verification/vite-plus-resolver.smt2` must print
  `unsat` then `sat`. Under faithful resolution of exact pins and a stable
  registry mapping, the exact tuple cannot disagree with the CLI's expected
  core package, core version or Vitest. The independent range example has a
  model that disagrees. The Node checks link this policy to the Kotlin constants.
- Wizard integration: `./gradlew integrationTest --info` on JDK 25 checks
  the actual scaffold. The Vite+ template unit case also checks generated
  manifests for zod and sury, across package manager choices.

Update the CLI and core tuple together. Confirm the exact published values
using `npm view vite-plus@<new-pin> version dependencies --json`, and verify the
matching coverage provider, then update the source, fixture and Vitest pins.
Compare the upstream alias, not just the package-version major. Run the exact
end-to-end wizard integration before treating new upstream compatibility as established.
