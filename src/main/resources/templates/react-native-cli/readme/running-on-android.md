Two terminals are required: one for Metro, one for the native build.

```bash
# Terminal 1 — ReScript watcher
{{cmdResDev}}

# Terminal 2 — Metro bundler
{{cmdStart}}

# Terminal 3 — Native build + install on emulator/device
{{cmdAndroid}}
```

The `metro.config.js` shipped with this template adds `mjs` to Metro's resolver so
that `.res.mjs` artifacts from ReScript are picked up automatically.
