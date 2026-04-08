---
myst:
  html_meta:
    "keywords": "monorepo, workspace, multi-package, language server"
---

# Set Up a Monorepo

{bdg-primary}`LSP Required`

## Goal

Configure the ReScript IntelliJ Plugin to work correctly with a monorepo that contains multiple ReScript packages, each with its own `rescript.json`.

## Steps

### 1. Open the workspace root

Open the monorepo root directory in your JetBrains IDE using **File > Open**. A typical monorepo structure looks like:

```
my-monorepo/
  packages/
    app/
      rescript.json
      src/
        App.res
    shared/
      rescript.json
      src/
        Utils.res
  package.json
```

### 2. Verify Language Server detection

The plugin searches for `@rescript/language-server` starting from the project root's `node_modules/`. In most monorepo setups, the language server is hoisted to the root:

```
my-monorepo/
  node_modules/
    @rescript/
      language-server/    <-- detected automatically
  packages/
    ...
```

If the language server is installed, you should see the LSP status in the bottom status bar.

### 3. Configure custom path (if needed)

If the language server is not auto-detected (e.g., it is installed only in a sub-package), configure the path manually:

1. Open **Settings > Languages & Frameworks > ReScript**
2. Set the **Language Server path** to the correct location
3. Click **Apply**

### 4. Verify rescript.json recognition

Each package's `rescript.json` is recognized automatically by the plugin:

- The file gets the ReScript icon in the **Project** panel
- JSON Schema validation and completion work inside `rescript.json`
- The **Framework Detector** may prompt you to configure the ReScript framework

### 5. Run builds per package

Each package needs its own build process. Use **Run Configurations** to set up separate build tasks:

1. **Run > Edit Configurations > + > ReScript Build**
2. Set the working directory to the specific package (e.g., `packages/app/`)
3. Repeat for each package that needs an independent build

Alternatively, use the **Run Anything** dialog ({kbd}`Ctrl+Ctrl`) and type `rescript build` from the desired package directory.

## Expected Result

- Language Server features (completion, diagnostics, go-to-definition) work across all packages
- Each package's `rescript.json` is recognized with proper schema validation
- Build configurations are set up for each package independently

## Tips

- If diagnostics appear stale after switching between packages, use **Tools > ReScript > Restart LSP Server** to refresh
- The **Package Dependencies** tool window shows dependencies for the currently focused `rescript.json`

## Related Features

- [Installation](../installation.md) --- Language Server setup and auto-install
- [Configuration](../configuration.md) --- Plugin settings reference
- [Run & Build](../features/run-build.md) --- Run configurations for ReScript builds
