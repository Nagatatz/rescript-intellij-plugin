---
myst:
  html_meta:
    "keywords": "react native template, community cli, bare workflow, rescript, mobile, ios, android, native modules, metro, gentype"
---

# React Native (Community CLI)

{bdg-info}`Mobile` {bdg-warning}`Bare Workflow` {bdg-success}`Validation`

A ReScript-powered React Native app on the **Community CLI bare workflow** — the path you take when you need direct access to the `android/` Gradle project, the `ios/` Xcode workspace, custom native modules in Kotlin/Swift, or any tooling Expo's managed workflow doesn't yet support. The template ships only the JavaScript / TypeScript / ReScript surface; the native projects themselves are produced by running `@react-native-community/cli` after the wizard finishes.

Pick this template if you have native-side requirements (a Kotlin SDK to wrap, a custom CocoaPod, a build flavor that diverges per environment) or if your team already lives in Android Studio / Xcode. If your needs sit comfortably inside Expo's managed pipeline, the {doc}`react-native` template will cost you less day-to-day overhead.

## What You Get

```
my-project/
├── rescript.json                  # JSX + genType enabled, depends on @rescript/react + validation lib
├── package.json                   # react-native + community-cli + babel/metro configs
├── app.json                       # AppRegistry name + displayName
├── tsconfig.json                  # extends @react-native/typescript-config
├── index.js                       # RN entry — AppRegistry.registerComponent(appName, () => App)
├── App.tsx                        # one-liner: `import App from "./src/App.gen"; export default App;`
├── metro.config.js                # extends @react-native/metro-config + adds "mjs" to sourceExts
├── babel.config.js                # @react-native/babel-preset
├── src/
│   ├── App.res                    # @genType — useState + TextInput + Button + FlatList todo list
│   ├── ReactNative.res            # bindings for View / Text / TextInput / Button / FlatList / Style
│   ├── NativeGreeting.res         # example: NativeModules binding (bindings only — Kotlin/Swift impl by you)
│   ├── Validation.res             # zod or sury — parseDraftTodo for the new-todo input
│   └── __tests__/App.test.mjs     # vitest filesystem-only smoke test
├── README.md                      # CLI setup + Android Studio + Native Modules + Troubleshooting + Fallback
├── LICENSE                        # MIT, holder = project name
├── .nvmrc                         # Node 22
├── .gitignore                     # adds android/build/, ios/Pods/, *.hbc, *.keystore, *.apk, *.aab, *.ipa, …
├── .editorconfig                  # 2-space indent, LF line endings
└── .github/
    ├── dependabot.yml             # weekly npm updates
    └── workflows/ci.yml           # install + rescript build + vitest
```

What is **not** in the box: the `android/` and `ios/` directories themselves. They are generated on first run by the Community CLI — see the next section.

## Wizard Options

| Option | Effect |
| --- | --- |
| **Project name** | Becomes the npm `name`, the AppRegistry `name` + `displayName` in `app.json`, the LICENSE holder, and the title rendered above the todo list (`{{projectName}} TODOs`) |
| **Package manager** | npm / pnpm / yarn / bun. Sets `packageManager`, README install/run snippets, and the CI cache key. The README's "Community CLI Setup" section substitutes the chosen install + execute commands (`{{installCmd}}` / `{{execReactNative}}`) |
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
| `zod` *or* `sury` | Validation backend (form input parser) | `TemplateVersions.ZOD` / `SURY` |
| `@react-native-community/cli` *(dev)* | CLI used to scaffold `android/` and `ios/`, run Metro, and trigger native builds | `TemplateVersions.RN_COMMUNITY_CLI` |
| `@react-native/babel-preset` *(dev)* | Babel preset RN bundlers expect | `TemplateVersions.RN_BABEL_PRESET` |
| `@react-native/metro-config` *(dev)* | Default Metro config we extend in `metro.config.js` | `TemplateVersions.RN_METRO_CONFIG` |
| `@react-native/typescript-config` *(dev)* | TS config base for `App.tsx` + the `.gen.tsx` files | `TemplateVersions.RN_TYPESCRIPT_CONFIG` |
| `vitest` *(dev)* | Smoke test runner — pure-Node, never loads RN | `TemplateVersions.VITEST` |
| `@vitest/coverage-v8` *(dev)* | Coverage provider for `test:coverage` | `TemplateVersions.VITEST_COVERAGE_V8` |
| `typescript` *(dev)* | Required for `App.tsx` + the genType-emitted `.gen.tsx` files | `TemplateVersions.TYPESCRIPT` |
| `@types/react` *(dev)* | Type definitions for the React surface genType emits against | `TemplateVersions.REACT_TYPES` |

## Generating the Native Projects

The wizard does not run `@react-native-community/cli` for you. After the project is generated and dependencies are installed, run:

```bash
<your install command>
npx @react-native-community/cli init-android
npx @react-native-community/cli init-ios        # macOS only
```

If `init-android` / `init-ios` are unavailable in your CLI revision, use the upstream template as a fallback:

```bash
npx @react-native-community/cli@latest init tmp
# then copy tmp/android (and tmp/ios) into this project
```

Prerequisites:

| Target | Toolchain |
| --- | --- |
| Android | JDK 21, Android Studio, Android SDK + NDK, watchman (macOS/Linux) |
| iOS | macOS, Xcode 16+, CocoaPods |

The README ships expanded versions of these instructions and substitutes your chosen package manager into the command lines.

## Key Files

### `App.tsx`

Intentionally trivial — an inline string in the generator, not loaded from a resource file:

```tsx
import App from "./src/App.gen";

export default App;
```

`AppRegistry` (in `index.js`) calls `() => App` and does not invoke any factory. genType emits the matching `App.gen.tsx` next to `src/App.res` during compilation.

### `index.js`

The native-side entry point. Standard React Native bootstrap:

```js
import { AppRegistry } from "react-native";
import App from "./App";
import { name as appName } from "./app.json";

AppRegistry.registerComponent(appName, () => App);
```

### `metro.config.js`

The single non-default piece of bundler configuration: appending `"mjs"` to Metro's `sourceExts` so it picks up ReScript-compiled output.

```js
const { getDefaultConfig, mergeConfig } = require("@react-native/metro-config");

const defaultConfig = getDefaultConfig(__dirname);

const config = {
  resolver: {
    sourceExts: [...defaultConfig.resolver.sourceExts, "mjs"],
  },
};

module.exports = mergeConfig(defaultConfig, config);
```

If you ever see a `Module not found` error pointing at a `.res.mjs` file, this resolver entry is the first thing to verify.

### `babel.config.js`

Single line of config:

```js
module.exports = { presets: ["module:@react-native/babel-preset"] };
```

ReScript compiles `.res` to standards-compliant ES modules — no Babel transform of ReScript output is needed; this preset only services the JS / TS layer.

### `src/App.res`

The interactive todo list. Identical to the Expo template's demo — `useState` slices for `todos`, `draft`, `error`; validation gate before append; `FlatList` for render — and uses the same `@genType @react.component` annotations so genType emits `App.gen.tsx`.

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

### `src/ReactNative.res`

Same minimal binding set as the Expo template: `View`, `Text`, `TextInput`, `Button`, `FlatList`, plus a `Style.make` helper. Each module follows the `@module("react-native") @react.component external make: (...) => React.element = "..."` pattern; copy it to bind more components, or use a third-party package name in place of `"react-native"` for community modules.

### `src/NativeGreeting.res`

The reason most teams pick the bare workflow: a worked example of binding a custom Kotlin/Swift `NativeModule`.

```rescript
module NativeGreeting = {
  @module("react-native") @scope("NativeModules")
  external module_: {"greet": string => promise<string>} = "NativeGreeting"

  let greet = (name: string): promise<string> => module_["greet"](name)
}
```

This file contains **bindings only**. The Kotlin (Android) or Swift (iOS) implementation must be added separately under `android/app/src/main/java/...` or `ios/`. The template intentionally does **not** ship the native side — it varies per platform and per minimum-OS target, and bundling a stale example would be more harmful than helpful. Follow the official RN guides:

- Android: <https://reactnative.dev/docs/legacy/native-modules-android>
- iOS: <https://reactnative.dev/docs/legacy/native-modules-ios>

### `src/Validation.res`

`parseDraftTodo: string => result<draftTodo, string>`. Same shape and behavior as the Expo template — trims input, rejects blanks, rejects entries longer than 120 characters, otherwise hands back a `draftTodo` record.

### `src/__tests__/App.test.mjs`

Filesystem-only smoke test — checks that the compiled artifacts exist via `existsSync`. Importing `App.res.mjs` from plain Node would pull in `react-native`, which Vitest can't run. Treat this as a build canary; use Jest + `jest-react-native` for behavior tests.

### `tsconfig.json`

Extends `@react-native/typescript-config` with `"strict": true` and `"noImplicitAny": true`. The strict pair keeps the genType-emitted `.gen.tsx` files honest.

### `.gitignore`

The bare workflow needs more aggressive ignores than the Expo flavor: build outputs (`android/build/`, `android/app/build/`, `ios/build/`, `ios/Pods/`), gradle caches (`android/.gradle/`), local Android config (`android/local.properties`), Hermes bytecode (`*.hbc`), signing material (`*.keystore`, `ios/.xcode.env.local`), build artifacts (`*.apk`, `*.aab`, `*.ipa`), and TypeScript incremental info (`*.tsbuildinfo`).

Note that `android/` and `ios/` themselves are *not* ignored — once you generate them you commit the configs (`build.gradle`, `Podfile`, `Info.plist`, etc.) so the team builds reproducibly.

## npm Scripts

| Script | Description |
| --- | --- |
| `start` | `react-native start` — launch Metro |
| `android` | `react-native run-android` — Gradle build + install on a connected emulator/device |
| `ios` | `react-native run-ios` — Xcode build + install on a simulator/device (macOS only) |
| `test` | `vitest run` — run the source smoke test |
| `test:coverage` | `vitest run --coverage` — same, with v8 coverage |
| `res:build` | `rescript` — one-shot ReScript compile |
| `res:dev` | `rescript -w` — recompile on save |
| `res:clean` | `rescript clean` — remove generated `.res.mjs` and `.gen.tsx` |

A typical session uses three terminals: `npm run res:dev` (ReScript watcher), `npm start` (Metro), and `npm run android` / `npm run ios` (or Android Studio / Xcode for the native build).

## Day-Two Recipes

- {doc}`../recipes/create-react-component` — same JSX patterns apply (substitute `View` for `<div>`)
- {doc}`../recipes/optimize-imports` — keep `src/ReactNative.res` and `src/NativeGreeting.res` lean as bindings grow
- {doc}`../recipes/find-dead-code` — useful as the screen + native-module surface grows

For ReScript-side editor workflows once the project is open, see {doc}`../features/index`.

## Notes

- **Native projects are generated separately.** The wizard ships only the JS/TS + ReScript surface. Run `npx @react-native-community/cli init-android` / `init-ios` after the wizard, or use the upstream template fallback — see "Generating the Native Projects" above.
- **Open `android/`, not the project root, in Android Studio.** Gradle expects to be the IDE root. Set Gradle JDK to 21 under `File > Settings > Build, Execution, Deployment > Build Tools > Gradle`. Metro must already be running before you hit `Run > Run 'app'`.
- **`metro.config.js` is the single non-default file.** The `mjs` sourceExt is what makes ReScript output resolvable. If you `mergeConfig` further customizations, preserve that resolver entry or `.res.mjs` imports will start failing with "Module not found".
- **Native modules are bindings only.** `NativeGreeting.res` shows the ReScript half; you write the Kotlin / Swift half. The template stays neutral on which RN native-modules architecture you target (legacy bridge vs. TurboModules / Fabric) — the link in the file points at the legacy docs for clarity.
- **`@react-native-community/cli` moves quickly.** Command names and flags drift between minor releases. If the README's snippets diverge from the current CLI, fall back to the official docs at <https://reactnative.dev>; the ReScript half of the template (`rescript.json`, `src/*.res`, `metro.config.js`) is unaffected.
- **Common stumbling blocks** (also documented in the README's Troubleshooting section):
  - *Metro cannot reach the device:* `adb reverse tcp:8081 tcp:8081`
  - *Stale Metro cache:* restart Metro with `--reset-cache`
  - *Gradle errors after upgrading deps:* `cd android && ./gradlew clean`, then rebuild
  - *Module not found for `.res.mjs`:* confirm `metro.config.js` still appends `mjs` to `resolver.sourceExts`
- **The smoke test is intentionally toothless.** Same constraint as the Expo flavor — `App.res.mjs` cannot be imported from plain Node. Use Jest with the appropriate RN preset for component-level tests.
- **genType is mandatory.** The boot path `index.js → App.tsx → src/App.gen` only works because `rescript.json` has genType enabled. Disabling it breaks `App.tsx` immediately.
