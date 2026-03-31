# Frequently Asked Questions

## General

Which JetBrains IDEs are supported?
:  The plugin works with any JetBrains IDE version 2025.3 or later that supports LSP, including IntelliJ IDEA (Ultimate and Community), WebStorm, CLion, PyCharm, GoLand, Rider, and others. All features are available regardless of which IDE you use.

Is this plugin free?
:  Yes. The plugin is open-source under the MIT license and free to use. There are no paid tiers or premium features.

Does the plugin work without Node.js?
:  Partially. {term}`Native features <Native Feature>` like syntax highlighting, code folding, brace matching, structure view, and file templates work without Node.js. However, {term}`LSP`-powered features (code completion, diagnostics, go to definition, hover documentation) require Node.js because the {term}`Language Server` runs on it.

What is the difference between native and LSP features?
:  **Native features** are built directly into the plugin using a JFlex lexer and IntelliJ Platform APIs. They are fast, always available, and don't require external tools. **LSP features** are provided by the `@rescript/language-server` running as a background process. They offer deeper semantic understanding (type-aware completion, cross-file navigation, diagnostics) but require the Language Server to be installed and running. See [Feature Overview](features/index.md) for a complete comparison table.

## Language Server

Why did some features stop working?
:  This usually means the {term}`Language Server` has stopped or cannot be found. Check the status bar at the bottom of the IDE for the ReScript build status indicator. Common causes:
   - The `@rescript/language-server` package was removed or updated
   - Node.js is no longer in your PATH
   - The project was moved to a different directory
   
   Try restarting the Language Server via **Tools** → **ReScript** → **Restart LSP Server**. See [Troubleshooting](troubleshooting.md) for more solutions.

How do I update the Language Server?
:  Update the npm package in your project:
   ```bash
   npm update @rescript/language-server
   ```
   Then restart the IDE or use **Tools** → **ReScript** → **Restart LSP Server**. The plugin automatically detects the updated version.

Can I use a globally installed Language Server?
:  Yes. If no project-local installation is found, the plugin falls back to a global installation detected via `which` (or `where` on Windows). However, local installation is recommended to ensure version consistency across team members. See [Installation](installation.md) for the full detection order.

## Workflow

Does the plugin support monorepos?
:  Yes. The plugin searches parent directories for `node_modules/.bin/rescript-language-server`, so a Language Server installed at the monorepo root is automatically detected by projects in subdirectories. Each project should have its own `rescript.json` configuration file.

Why don't I see any diagnostics (errors/warnings)?
:  The {term}`Language Server` requires a successful initial build to generate type information. Run the ReScript compiler at least once:
   ```bash
   npx rescript build
   ```
   After the build completes, diagnostics should appear. You can also start the build watcher from within the IDE — the plugin prompts you on project open. See [Troubleshooting — Build Errors Not Showing](troubleshooting.md#build-errors-not-showing) for more details.

How do I configure the plugin?
:  Go to **Settings** → **Languages & Frameworks** → **ReScript**. You can configure the Language Server path, Node.js interpreter, inlay hints, reanalyze integration, and more. See [Configuration](configuration.md) for all available options.

Can I use the plugin with older ReScript versions?
:  The plugin works with any ReScript version supported by `@rescript/language-server`. Some features (like reanalyze server mode) require ReScript 12.1.0 or later. Check the [Language Server documentation](https://github.com/rescript-lang/rescript-vscode) for version compatibility.

How do I report a bug or request a feature?
:  File an issue on [GitHub](https://github.com/Nagatatz/rescript-intellij-plugin/issues). Include your IDE version, plugin version, ReScript version, and steps to reproduce. The plugin also has a built-in error reporter — if an unhandled exception occurs, it offers to open a pre-filled GitHub issue.
