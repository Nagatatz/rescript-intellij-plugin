| File | Purpose |
| --- | --- |
| `bin/cli.mjs` | `#!/usr/bin/env node` wrapper referenced from `package.json` `bin` |
| `src/Cli.res` | Entry point + subcommand dispatcher |
| `src/Args.res` | Positional / named flag helpers |
| `src/Commands.res` | Subcommands (`Commands.Greet`, `Commands.Init`) |
| `src/Validation.res` | `init` options validation ({{validationLibrary}}) |