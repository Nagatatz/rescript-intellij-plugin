# Third-Party Notices

This file lists third-party software, resources, and tools used by the
ReScript IntelliJ Plugin, along with their respective licenses.

---

## Bundled Resources

### rescript.schema.json

- **Description:** JSON Schema for `rescript.json` configuration files,
  derived from the ReScript project documentation.
- **Source:** [ReScript](https://rescript-lang.org/)
- **License:** LGPL-3.0 (ReScript compiler)
- **Note:** This schema is included to provide editor validation and
  autocompletion for `rescript.json` files.

---

## IntelliJ Platform APIs (Provided at Runtime)

The plugin uses APIs and libraries provided by the IntelliJ Platform.
These are **not bundled** in the plugin distribution; they are supplied by the
host IDE at runtime.

| Library | License | Usage |
|---------|---------|-------|
| IntelliJ Platform SDK | Apache 2.0 | IDE integration APIs |
| Gson (`com.google.gson`) | Apache 2.0 | JSON parsing (bundled with IntelliJ Platform) |
| Kotlin Standard Library | Apache 2.0 | Kotlin runtime (provided by IDE) |

---

## External Tools (Invoked as Separate Processes)

The plugin invokes the following tools as **external processes** via CLI or
stdio. They are **not bundled** in the plugin distribution and must be
installed separately by the user.

| Tool | License | Invocation |
|------|---------|------------|
| [@rescript/language-server](https://github.com/rescript-lang/rescript-vscode) | MIT | LSP server via stdio |
| [ReScript Compiler CLI](https://github.com/rescript-lang/rescript) | LGPL-3.0 (with linking exception) | `rescript build` / `rescript format` via CLI |
| [TypeScript](https://github.com/microsoft/TypeScript) | Apache 2.0 | TypeScript Compiler API loaded by `dts-to-json.js` |
| [Node.js](https://github.com/nodejs/node) | MIT | Runtime for LSP server and `dts-to-json.js` |

Since these tools are invoked as separate processes (not linked or bundled),
their licenses do not impose requirements on the plugin's license.

---

## Build-Time Only Dependencies (Not Distributed)

The following dependencies are used during development and build only.
They are **not included** in the distributed plugin.

| Library | License | Usage |
|---------|---------|-------|
| [JUnit 4](https://github.com/junit-team/junit4) | EPL 1.0 | Unit testing |
| [GrammarKit](https://github.com/JetBrains/Grammar-Kit) | Apache 2.0 | JFlex lexer generation |
| [ktlint](https://github.com/pinterest/ktlint) | MIT | Kotlin linter |
| [Kover](https://github.com/Kotlin/kotlinx-kover) | Apache 2.0 | Code coverage |

---

## License Compatibility Summary

- **Plugin license:** MIT
- **Bundled third-party code:** None (all dependencies are provided by IntelliJ
  Platform or installed separately by the user)
- **LGPL tools:** Invoked as external processes only; no LGPL code is linked
  into or distributed with the plugin
- **Conclusion:** No license contamination. The plugin can be distributed under
  the MIT license without restriction.
