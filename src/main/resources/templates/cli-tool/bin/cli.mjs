#!/usr/bin/env node
// Tiny wrapper that satisfies npm's `bin` contract: the file must start with a
// shebang so Unix resolves `node` on PATH when the binary is invoked directly.
// The actual implementation lives in `../src/Cli.res.mjs` — extend `Commands.res`
// to add new subcommands. For multi-binary packages, add more entries to the
// `bin` object in package.json and ship a sibling wrapper here (e.g. `{{projectName}}-init.mjs`).
import "../src/Cli.res.mjs";
