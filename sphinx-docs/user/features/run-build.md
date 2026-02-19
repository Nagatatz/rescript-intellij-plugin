# Run & Build

The plugin integrates with the ReScript compiler to provide build functionality directly within the IDE.

## Run Configurations

Create and manage ReScript build configurations from **Run** → **Edit Configurations** → **+** → **ReScript**.

### Available Commands

| Command | Description |
|---------|-------------|
| **Build** | Compile the project once |
| **Build (Watch)** | Compile and watch for file changes |
| **Clean** | Remove compiled artifacts |

### Configuration Options

- **Working directory** — The project root (where `rescript.json` is located)
- **Command** — Build, Build Watch, or Clean

## Gutter Run Icons

A green ▶ run icon appears in the gutter of `.res` files. Click it to quickly start a build.

## Build Status

The status bar at the bottom of the IDE shows the current compilation status:

- **Success** — Build completed without errors
- **Error** — Build failed with errors (click to see details)
- **Warning** — Build completed with warnings

The status is updated in real-time via LSP notifications from the Language Server.

## Console Output

Build output appears in the Run panel. File paths in error messages are automatically converted to clickable links — click them to jump directly to the error location in your source code.
