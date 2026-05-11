Tauri ships a Rust `cargo` build alongside the Vite+ renderer, so the
production loop is a two-step combine. The renderer bundles to `dist/`
via Vite+; Tauri picks that up via `build.frontendDist` and embeds it
into the platform binary it produces.

### Prerequisites for `tauri build`

- A Rust toolchain (`rustup default stable`)
- Platform-specific build deps — see the
  [Tauri 2 prerequisites guide](https://v2.tauri.app/start/prerequisites/)
  (Linux needs `webkit2gtk` + `libsoup`; Windows needs the MSVC C++ build
  tools; macOS needs Xcode CLI tools)

### Generating app icons

`bundle.icon` is intentionally omitted from `tauri.conf.json` so
`tauri dev` works on a fresh checkout without an icon source. Before
running `tauri build` you need to provide one:

```bash
# Use any 1024×1024 PNG as the source. Tauri produces every required
# .png/.ico/.icns size and writes them to src-tauri/icons/.
tauri icon path/to/source.png
```

Then re-enable the icon list inside `bundle`:

```jsonc
"bundle": {
  "active": true,
  "targets": "all",
  "icon": [
    "icons/32x32.png",
    "icons/128x128.png",
    "icons/icon.icns",
    "icons/icon.ico"
  ]
}
```

### Building a release artifact

```bash
tauri build
# Output lands in src-tauri/target/release/bundle/<platform>/
```

Per-platform `targets` knobs (`"deb"`, `"appimage"`, `"msi"`, `"dmg"`,
`"app"`) let you trim the bundle list — see
[Bundle configuration](https://v2.tauri.app/distribute/) for the full
matrix.

### Signing & auto-updater

Out of scope for the starter template. When you decide on a distribution
channel:

- **macOS notarisation**: hold a Developer ID Application certificate +
  set `bundle.macOS.signingIdentity` + run `xcrun notarytool submit`
- **Windows code signing**: hold a code-signing certificate + set
  `bundle.windows.certificateThumbprint`
- **Auto-updates**: enable Tauri's `updater` plugin and host signed
  manifests; the
  [updater guide](https://v2.tauri.app/plugin/updater/) walks through
  key generation and config

CI doesn't run `tauri build` in this template — the matrix would need
per-OS runners plus signing identities, which varies wildly per project.
Add the workflow yourself once your distribution strategy is settled.
