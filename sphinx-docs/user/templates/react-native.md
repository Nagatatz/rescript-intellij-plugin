---
myst:
  html_meta:
    "keywords": "react native template, expo, rescript, mobile, ios, android, gentype, validation, zod, sury"
---

# React Native (Expo)

{bdg-info}`Mobile` {bdg-primary}`Expo` {bdg-success}`Validation`

A ReScript-powered React Native app on the **Expo-managed workflow** — no `android/` or `ios/` directories to maintain, no Xcode/Android Studio prerequisites for day-to-day work, and a single `expo start` command to drive iOS / Android / web from one terminal. ReScript components are exposed to the JS entry point via genType, so the world-facing `App.tsx` stays a one-line re-export.

Pick this template when your team wants to ship a mobile app without owning the native build pipeline. If you already need direct access to `android/app/build.gradle` or custom CocoaPods, jump to the {doc}`react-native-cli` template instead — it covers the bare workflow.

## What You Get

```
my-project/
├── rescript.json                  # JSX + genType enabled, depends on @rescript/react + validation lib
├── package.json                   # type: module-less (RN bundler eats CJS), expo + react-native deps
├── app.json                       # Expo manifest — name + slug substitute the project name
├── tsconfig.json                  # Extends expo/tsconfig.base, strict + noImplicitAny
├── App.tsx                        # one-liner: `import App from "./src/App.gen"; export default App;`
├── src/
│   ├── App.res                    # @genType — useState + TextInput + Button + FlatList todo list
│   ├── ReactNative.res            # bindings for View / Text / TextInput / Button / FlatList / Style
│   ├── Validation.res             # zod or sury — parseDraftTodo for the new-todo input
│   └── __tests__/App.test.mjs     # vitest filesystem-only smoke test (does not import RN)
├── README.md                      # Bindings + Adding Screens + Project Layout sections
├── LICENSE                        # MIT, holder = project name
├── .nvmrc                         # Node 22
├── .gitignore                     # node_modules + ReScript output + .expo/, android/, ios/, *.tsbuildinfo
├── .editorconfig                  # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml             # weekly npm updates
    └── workflows/ci.yml           # install + rescript build + vitest
```

Note that `android/` and `ios/` are listed in `.gitignore` — Expo manages them under the hood; you only ever drop into them via `expo prebuild` or `expo run:*`, and the generated trees should not be checked in.

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, the Expo `name` + `slug` in `app.json`, the LICENSE holder, and the title rendered above the todo list (`{{projectName}} TODOs`) |
| **Package manager** | npm / pnpm / yarn / bun. Sets `packageManager`, README install/run snippets, and the CI cache key. Note: Expo CLI works with all four, but EAS Build historically preferred npm/yarn — check current Expo docs if you go to production |
| **Validation library** | `zod` ↔ `sury`. Picks which `src/Validation.res` variant ships and which dep lands in `dependencies` |

## Key Dependencies

| Package | Purpose | Version |
| --- | --- | --- |
| `rescript` | ReScript compiler | `TemplateVersions.RESCRIPT` |
| `@rescript/core` | Standard library | `TemplateVersions.RESCRIPT_CORE` |
| `@rescript/runtime` | Runtime stubs imported by compiled `.res.mjs` | `TemplateVersions.RESCRIPT_RUNTIME` |
| `@rescript/react` | React bindings (JSX-enabled `rescript.json`) | `TemplateVersions.RESCRIPT_REACT` |
| `react` | UI library shared with React Native | `TemplateVersions.REACT` |
| `react-native` | Native UI primitives | `TemplateVersions.REACT_NATIVE` |
| `expo` | Managed workflow CLI + native runtime | `TemplateVersions.EXPO` |
| `zod` *or* `sury` | Validation backend (form input parser) | `TemplateVersions.ZOD` / `SURY` |
| `vitest` *(dev)* | Smoke test runner — pure-Node, never loads RN | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |
| `typescript` *(dev)* | Required for `App.tsx` + the genType-emitted `.gen.tsx` files | `TemplateVersions.TYPESCRIPT` |
| `@types/react` *(dev)* | Type definitions for the React surface genType emits against | `TemplateVersions.REACT_TYPES` |

## Key Files

### `App.tsx`

Intentionally trivial:

```tsx
import App from "./src/App.gen";

export default App;
```

This is the file Expo's runtime boots into. The real implementation is in `src/App.res`; genType emits `src/App.gen.tsx` next to it during compilation, and the JS layer just re-exports the typed component. Do not write logic here — it would be erased the next time you regenerate.

### `src/App.res`

The interactive todo list. Demonstrates the patterns you actually need on day one:

- `React.useState` for `todos`, `draft`, and `error` slices
- `ReactNative.TextInput` for the new-todo entry, `ReactNative.Button` for submit
- Validation gate: `Validation.parseDraftTodo(draft)` decides whether the entry joins `todos` or surfaces as a banner
- `ReactNative.FlatList` to render the todo list with a stable `keyExtractor`

```rescript
let addTodo = () => {
  switch Validation.parseDraftTodo(draft) {
  | Error(message) => setError(_ => Some(message))
  | Ok({text}) =>
    let nextId = todos->Array.reduce(0, (max, t) => t.id > max ? t.id : max) + 1
    setTodos(prev => prev->Array.concat([{id: nextId, text}]))
    setDraft(_ => "")
    setError(_ => None)
  }
}
```

The `@genType @react.component` annotations on `make` are what produce the `App.gen.tsx` that `App.tsx` imports.

### `src/ReactNative.res`

Minimal bindings over the React Native core component set used by `App.res`:

| Module | Maps to | Notes |
| --- | --- | --- |
| `View` | `react-native` `View` | Layout container; takes `style` + `children` |
| `Text` | `react-native` `Text` | Text leaves (RN refuses to render bare strings) |
| `TextInput` | `react-native` `TextInput` | Controlled input (`value` + `onChangeText`) |
| `Button` | `react-native` `Button` | Tap target with `title` + `onPress` |
| `FlatList` | `react-native` `FlatList` | Virtualized list with `data` / `keyExtractor` / `renderItem` |
| `Style.make` | inline-style record builder | `~flex`, `~padding`, `~margin`, `~backgroundColor` |

Each module follows the same `@module("react-native") @react.component external make: (...) => React.element = "..."` shape. Add more (e.g. `ScrollView`, `Image`, `Pressable`) by copying the pattern. For third-party native modules, bind against the package name instead of `"react-native"`.

### `src/Validation.res`

`parseDraftTodo: string => result<draftTodo, string>`. The shape is the same regardless of zod vs sury, so the call site never branches. Trims input, rejects blank entries, rejects entries longer than 120 characters, and otherwise hands the trimmed string back wrapped in a `draftTodo` record.

### `src/__tests__/App.test.mjs`

The smoke test deliberately **does not import** `App.res.mjs`. Loading the compiled output would drag the React Native bundler in, which Vitest can't run under plain Node. Instead, the test only checks that the compiler emitted the expected file (`existsSync(...)`). Treat it as a "did the build complete" canary, not a behavioral test.

For component logic, write a separate test under your own framework — RN component tests typically run under Jest with the `jest-expo` preset.

### `tsconfig.json`

Extends `expo/tsconfig.base`, adds `"strict": true` and `"noImplicitAny": true`. The strict pair is required to keep genType-emitted code honest — the generated `.gen.tsx` files do not include defensive `any` casts and rely on every binding having a precise type.

### `app.json`

Expo's manifest, scoped to the bare minimum:

```json
{
  "expo": {
    "name": "{{projectName}}",
    "slug": "{{projectName}}",
    "version": "1.0.0"
  }
}
```

Add `icon`, `splash`, `ios.bundleIdentifier`, `android.package`, etc. when you start preparing for store submission — see the Expo docs for the current set.

## npm Scripts

| Script | Description |
| --- | --- |
| `start` | `expo start` — launch Metro + the Expo Dev Tools |
| `android` | `expo start --android` — build and launch on a connected Android emulator/device |
| `ios` | `expo start --ios` — build and launch on an iOS simulator/device (macOS only) |
| `test` | `vitest run` — run the source smoke test |
| `test:coverage` | `vitest run --coverage` — same, with v8 coverage |
| `res:build` | `rescript` — one-shot ReScript compile |
| `res:dev` | `rescript -w` — recompile on save |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` and `.gen.tsx` |

In a normal session you'll keep two terminals open: `npm run res:dev` for the ReScript watcher and `npm start` for Metro.

## Adding a Screen

There is no router on day one — `App.res` is the single mounted component. To grow into a multi-screen app:

1. **Pick a router.** `react-navigation` is the de-facto standard for both managed and bare workflows; the Expo docs cover the install (`expo install @react-navigation/native @react-navigation/native-stack` plus the platform peers).
2. **Bind it in ReScript.** Either drop a thin `Navigation.res` next to `ReactNative.res` (same `@module(...) @react.component external make` pattern) or pull in a community binding package.
3. **Move the todo screen.** Lift `App.res`'s body into `src/Screens/Todos.res` and turn `App.res` into the navigator container that mounts it as the initial route.
4. **Add new screens as siblings of `Todos.res`.** Keep one screen per file so genType emits one `.gen.tsx` per route — the JS layer can import them without touching the navigator.

The README's "Adding Screens" section walks through the same flow.

## Adding a Component Binding

`src/ReactNative.res` only ships the components `App.res` actually uses. To bind another (e.g. `ScrollView`):

```rescript
module ScrollView = {
  @module("react-native") @react.component
  external make: (
    ~horizontal: bool=?,
    ~contentContainerStyle: Style.t=?,
    ~children: React.element=?,
  ) => React.element = "ScrollView"
}
```

Same pattern for community modules — replace `"react-native"` with the package name (`"react-native-reanimated"`, `"@shopify/flash-list"`, …). Wrap the result in a module so the `make` constructor is namespaced.

## Day-Two Recipes

- {doc}`../recipes/create-react-component` — same JSX patterns apply (substitute `View` for `<div>`)
- {doc}`../recipes/optimize-imports` — keep `src/ReactNative.res` lean as you bind more components
- {doc}`../recipes/find-dead-code` — useful as the screen count grows

For ReScript-side editor workflows once the project is open, see {doc}`../features/index`.

## Notes

- **Managed workflow only.** Expo Go and `expo run:*` cover most projects, but anything that requires a custom native module not yet on the Expo SDK ecosystem will need either a config plugin or graduation to the bare workflow ({doc}`react-native-cli`).
- **`android/` and `ios/` are gitignored on purpose.** Expo regenerates them from `app.json` + `expo.config.js`. Committing them locks you out of `expo prebuild` upgrades.
- **The smoke test is intentionally toothless.** It only checks the compiler emitted the file, because importing `App.res.mjs` from plain Node would pull in `react-native`. Add component tests under Jest + `jest-expo` if you need behavior coverage.
- **genType is mandatory here, not optional.** `App.tsx` imports `./src/App.gen`; the `.gen.tsx` file is emitted only because `rescript.json` has `"genTypeConfig"` enabled. Disabling it will break the boot path.
- **Vitest is for the source canary, not for components.** The Vitest you see in `devDependencies` exists to run the smoke test (and any pure-data ReScript tests you add under `src/`). It is not configured to render RN components.
- **Validation guards the form, not the network.** The shipped `parseDraftTodo` only protects the local state. If you wire this app to a backend, validate the server response in a separate `Validation.parse*` function before destructuring.
- **Expo SDK / React Native pin must move together.** Bumping `expo` typically requires the matching `react-native` minor — do them in one commit and re-run the `npm install` so peer-dep warnings don't drift.
