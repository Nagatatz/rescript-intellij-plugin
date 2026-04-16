---
myst:
  html_meta:
    "keywords": "settings generator, configuration, json, plugin settings, interactive"
---

# Settings Generator

Build a custom plugin configuration by toggling the options below. The JSON output updates live — copy it to your clipboard or download as a file, then save it next to your project's `rescript.json` or apply via **Settings > Languages & Frameworks > ReScript**.

:::{note}
Not every setting maps to a `rescript.json` field directly. Some values (such as `codeLens`) are sent to the language server through LSP initialization options; others (such as `errorLens`) are stored as IDE-local plugin settings. The JSON output uses a unified shape that mirrors the plugin's internal `RescriptProjectSettings` structure.
:::

```{raw} html
<div class="settings-container" id="settings-generator">
  <div class="settings-form-container">
    <!-- Form is populated by JavaScript -->
  </div>
  <div class="settings-output-panel">
    <textarea class="settings-output" readonly aria-label="Generated JSON settings"></textarea>
    <div class="settings-actions">
      <button type="button" class="settings-copy">Copy to clipboard</button>
      <button type="button" class="settings-download">Download JSON</button>
      <button type="button" class="settings-reset secondary">Reset to defaults</button>
      <span class="settings-status" role="status" aria-live="polite"></span>
    </div>
  </div>
</div>
```

## How to use

1. **Toggle settings** in the left panel to enable or disable each feature.
2. **Edit text fields** for custom paths (e.g., LSP binary location).
3. **Copy** the generated JSON or **Download** it as `rescript-plugin-settings.json`.
4. Apply the settings either by:
   - Importing via **File > Settings > Languages & Frameworks > ReScript** (select "Import from JSON" — planned)
   - Or manually setting each value in the settings UI using the JSON as a reference

## See also

- [Configuration](configuration.md) — Full settings reference
- [Keyboard Shortcuts](keyboard-shortcuts.md) — Shortcut reference
