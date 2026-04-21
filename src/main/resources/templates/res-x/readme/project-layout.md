```
src/
├── App.res          Entry: Bun.serve + res-x handler + routing
├── Handler.res      Per-request context + handler bootstrap
├── Layout.res       Shared HTML shell (loads HTMX)
├── Counter.res      Counter component + /counter/* HTMX endpoints
├── TodoForm.res     Todo form component + /todos HTMX endpoint
├── Validation.res   Input validation (selected: {{validationLib}})
└── __tests__/
    └── App.test.mjs Smoke test that App compiles and loads
assets/              Assets processed by Vite (optional, create as needed)
public/              Assets copied as-is to the build (optional)
rescript.json        ReScript config — jsx.module = Hjsx, res-x bsc-flags
vite.config.js       Vite plugin (rescript-x/res-x-vite-plugin.mjs)
package.json
```
