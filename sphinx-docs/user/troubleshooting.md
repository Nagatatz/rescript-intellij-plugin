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

4. Check that `rescript.json` exists in your project root

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

## Reporting Issues

If you can't resolve the issue:

1. Check [existing issues](https://github.com/Nagatatz/rescript-intellij-plugin/issues) on GitHub
2. File a new issue with:
   - IDE version and OS
   - Plugin version
   - ReScript and Language Server versions
   - Steps to reproduce
   - Error logs (from **Help** → **Show Log in Finder/Explorer**)
