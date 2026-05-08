---
myst:
  html_meta:
    "keywords": "troubleshooting, errors, problems, fixes, language server"
---

# Troubleshooting

Common issues and their solutions.

## Language Server Not Detected

**Symptom:** A notification bar appears at the top of the editor saying the Language Server was not found.

**Solutions:**

1. Install the Language Server in your project:
   ```bash
   npm install @rescript/language-server
   ```

2. Make sure Node.js is in your PATH:
   ```bash
   node --version  # Should output a version number
   ```

3. Restart the IDE after installing the Language Server

4. Check that `rescript.json` exists in your project — either at the root or in a workspace sub-package such as `packages/core/`. The plugin auto-detects ReScript packages declared by `pnpm-workspace.yaml` or `package.json#workspaces` and falls back to a depth-limited scan; see [Monorepo Support](features/advanced.md#monorepo-support) for details.

## "rescript.json not found in this project" Warning

**Symptom:** The inspection reports that `rescript.json` is missing even though it exists somewhere inside the project.

**Solutions:**

1. Confirm the configuration file is named exactly `rescript.json` or `bsconfig.json` and is a regular file (not a directory).
2. If it lives more than four directories deep from the project base, declare its parent in **Settings** → **Languages & Frameworks** → **ReScript** → **Project package roots** (one relative path per line, e.g. `packages/core`).
3. For pnpm/npm/yarn workspaces, ensure the package directory is matched by `pnpm-workspace.yaml` or the `workspaces` field in `package.json`.

This warning was previously raised whenever `rescript.json` was absent from the project root. Recent versions only raise it when no ReScript package root is detected anywhere in the workspace.

## No Syntax Highlighting

**Symptom:** `.res` files appear as plain text without colors.

**Solutions:**

1. Check that the file extension is `.res` or `.resi`
2. Verify the plugin is installed: **Settings** → **Plugins** → search for "ReScript"
3. Try restarting the IDE

## Code Completion Not Working

**Symptom:** No completion suggestions appear when typing.

**Solutions:**

1. Ensure the Language Server is installed and detected (no notification bar)
2. Check that the ReScript project has been built at least once:
   ```bash
   npx rescript build
   ```
3. Wait a few seconds after opening a file — the Language Server needs time to initialize
4. Try pressing `Cmd+Space` manually to trigger completion

## Formatting Not Working

**Symptom:** `Cmd+Option+L` does nothing or shows an error.

**Solutions:**

1. Ensure `rescript` is installed in your project:
   ```bash
   npm install rescript
   ```
2. Check that `rescript.json` exists in the project root
3. Verify the file has no syntax errors (formatting may fail on invalid code)

## Build Errors Not Showing

**Symptom:** No inline error annotations despite compilation errors.

**Solutions:**

1. Ensure the Language Server is running (check status bar)
2. Save the file — diagnostics update on save
3. Check the **Problems** panel (`Alt+6`) for any listed issues
4. Try restarting the Language Server: close and reopen the project

## reanalyze Not Working

**Symptom:** No dead code warnings despite unused code.

**Solutions:**

1. Install reanalyze:
   ```bash
   npm install reanalyze
   ```
2. Build the project first:
   ```bash
   npx rescript build
   ```
3. Check the reanalyze path in **Settings** → **Languages & Frameworks** → **ReScript**

## High CPU or Memory Usage

**Symptom:** IDE becomes slow when working with ReScript files.

**Solutions:**

1. Close unused projects
2. Increase IDE memory: **Help** → **Change Memory Settings**
3. Disable unused inspections: **Settings** → **Editor** → **Inspections** → uncheck ReScript inspections you don't need
4. Check if the Language Server process is consuming excessive resources

## Plugin Conflicts

**Symptom:** Unexpected behavior when other language plugins are installed.

**Solutions:**

1. Check if the old `reasonml-idea-plugin` is installed and disable it
2. Ensure there are no conflicting file type associations for `.res` files

## Diagnostic Toolkit

When quick fixes above don't resolve the issue, the following steps help you gather evidence and reset state before filing a bug.

### Verify Node.js and the Language Server

```bash
# Confirm Node.js is reachable from the same PATH the IDE uses
node --version
which node

# Confirm @rescript/language-server is installed locally or globally
ls node_modules/@rescript/language-server/out/server.js 2>/dev/null \
  || npm ls -g @rescript/language-server
```

If Node.js works in your shell but the IDE reports it missing, the IDE may be launched without your shell's PATH. On macOS, launching the IDE from Terminal (`idea .` or `webstorm .`) propagates your shell environment; the Dock launcher may not.

### Restart the Language Server

1. **Tools → Restart ReScript LSP** — fully restarts the server without reopening the project. Use this after installing dependencies, switching branches, or when completion/diagnostics go silent.
2. **Tools → Dump ReScript LSP State** — prints the server's internal request/response queues and initialization options to the Event Log. Attach the output when reporting bugs related to LSP behavior.

### Clear Caches

Stale indexes and caches are a frequent cause of wrong completions, missing references, or outdated diagnostics.

1. **File → Invalidate Caches** → check *Clear file system cache and Local History* → click *Invalidate and Restart*.
2. Delete the ReScript build output when `rescript build` reports phantom errors:
   ```bash
   rm -rf lib/bs lib/es6 lib/ocaml
   npx rescript clean
   npx rescript build
   ```

### Inspect Logs

1. **Help → Show Log in Finder** (macOS) / **Show Log in Explorer** (Windows/Linux).
2. Open `idea.log` and filter for `ReScript`, `rescript-language-server`, or `com.rescript.plugin` to isolate plugin output.
3. For reproducible issues, enable verbose logging: **Help → Diagnostic Tools → Debug Log Settings…** → add `#com.rescript.plugin:trace`.

### Reset Plugin Configuration

If settings appear corrupted, remove the plugin's persisted state:

```bash
# macOS
rm ~/Library/Application\ Support/JetBrains/<IDE>/options/RescriptProjectSettings.xml

# Linux
rm ~/.config/JetBrains/<IDE>/options/RescriptProjectSettings.xml

# Windows (PowerShell)
Remove-Item "$env:APPDATA\JetBrains\<IDE>\options\RescriptProjectSettings.xml"
```

Restart the IDE afterwards; a fresh default file is generated.

## Reporting Issues

If you can't resolve the issue:

1. Check [existing issues](https://github.com/Nagatatz/rescript-intellij-plugin/issues) on GitHub
2. File a new issue with:
   - IDE version and OS
   - Plugin version
   - ReScript and Language Server versions
   - Steps to reproduce
   - Error logs (from **Help** → **Show Log in Finder/Explorer**)
   - Output of **Tools → Dump ReScript LSP State** if the issue involves LSP features
