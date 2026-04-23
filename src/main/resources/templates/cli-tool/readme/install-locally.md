After `{{cmdBuild}}`, link globally:

```bash
npm link
{{projectName}} greet Alice
```

To expose additional executables, add entries to the `bin` object in
`package.json` and ship a sibling wrapper under `bin/`:

```jsonc
// package.json
"bin": {
  "{{projectName}}": "./bin/cli.mjs",
  "{{projectName}}-init": "./bin/init.mjs"
}
```

Each wrapper needs the `#!/usr/bin/env node` shebang so Unix picks up
the right interpreter when npm installs the binary.