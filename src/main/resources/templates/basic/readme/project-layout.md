| File | Purpose |
| --- | --- |
| `src/App.res` | Entry point: parses arguments, runs the greeting |
| `src/Args.res` | Reads `process.argv` and extracts a named flag |
| `src/Files.res` | Thin bindings over `node:fs/promises` for read/write |
| `src/Validation.res` | Validates `config.json` with {{validationLibrary}} |
| `config.sample.json` | Sample config consumed by `App.res` when `--config` is supplied |