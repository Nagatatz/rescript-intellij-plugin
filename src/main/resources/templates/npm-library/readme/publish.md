1. Bump the version in `package.json`.
2. Run `{{cmdBuild}}` to regenerate TypeScript bindings.
3. Run `{{cmdTest}}` to confirm Vitest passes.
4. Run `npm publish --access public`.