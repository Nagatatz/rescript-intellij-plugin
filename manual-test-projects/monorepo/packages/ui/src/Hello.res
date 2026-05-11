// Tiny module so the workspace package compiles. The plugin's
// RescriptWorkspaceDiscovery should auto-detect this packages/ui
// directory from pnpm-workspace.yaml (or the root package.json
// "workspaces" field) and add it to packageRoots.

let greet = (name: string): string => "Hello from ui, " ++ name
