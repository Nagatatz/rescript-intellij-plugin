# Installation

## Requirements

- **JetBrains IDE** — IntelliJ IDEA 2025.3+ (Ultimate or Community), WebStorm, or other JetBrains IDE with LSP support
- **JDK** — 21 or later (bundled with JetBrains IDEs)
- **Node.js** — Installed and available in PATH (required for the Language Server)

## Step 1: Install the Plugin

### From JetBrains Marketplace

1. Open your JetBrains IDE
2. Go to **Settings** → **Plugins** → **Marketplace**
3. Search for **"ReScript"**
4. Click **Install**
5. Restart the IDE when prompted

### From Disk (Manual)

1. Download the latest `.zip` from the [GitHub Releases](https://github.com/Nagatatz/rescript-intellij-plugin/releases) page
2. Go to **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk...**
3. Select the downloaded `.zip` file
4. Restart the IDE when prompted

## Step 2: Install the ReScript Language Server

The plugin uses the [ReScript Language Server](https://github.com/rescript-lang/rescript-vscode) for semantic features like code completion, diagnostics, and navigation. You need to install it in your project:

```bash
# Local installation (recommended)
npm install @rescript/language-server

# Or global installation
npm install -g @rescript/language-server
```

:::{tip}
Local installation (per-project) is recommended. The plugin automatically detects the Language Server in your project's `node_modules/` directory.
:::

### Automatic Installation

If the Language Server is not found when you open a ReScript project, the plugin displays a notification offering to install it automatically:

- **Install with npm/yarn/pnpm** --- Installs `@rescript/language-server` as a dev dependency using the detected package manager. The installation runs in the background, and the Language Server starts automatically when complete.
- **Configure...** --- Opens the ReScript settings page to set a custom Language Server path.
- **Don't show again** --- Dismisses the notification for the current session.

The plugin also shows an editor notification bar at the top of `.res` files when the Language Server is not detected, with a link to install it.

### Detection Order

The plugin searches for the Language Server in this order:

1. `node_modules/.bin/rescript-language-server` (project-local)
2. Parent directories' `node_modules/.bin/` (monorepo support)
3. `node_modules/@rescript/language-server/out/cli.js` (fallback)
4. Global installation (via `which` / `where`)

## Step 3: Verify

1. Open a `.res` file in your project
2. You should see:
   - **Syntax highlighting** — Keywords, strings, comments are colored
   - **No warning banner** — If the Language Server is found, no notification bar appears
   - **Code completion** — Type a few characters and see completion suggestions

:::{note}
If the Language Server is not detected, a notification bar will appear at the top of the editor with instructions to install it. Native features (syntax highlighting, code folding, etc.) will still work without the Language Server.
:::

## Troubleshooting Installation

See the [Troubleshooting](troubleshooting.md) page for common installation issues.
