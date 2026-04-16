# Design: React Native (Community CLI) テンプレート

## 設計方針

- **既存パターンの踏襲**: `ReactNativeTemplateFiles.kt`（Expo 版）と同じ構造で `ReactNativeCliTemplateFiles.kt` を新規作成する。共通処理は `CommonFiles` / `ProjectFileBuilders` に委譲する
- **display name 変更は不要**: `REACT_NATIVE` エントリは既に `"React Native (Expo)"` になっているため改名コミットは不要
- **依存バージョン集約**: `TemplateVersions.kt` に `RN_COMMUNITY_CLI` 等を追記
- **android/ios は生成しない**: README で `npx react-native init` の手順を案内。テンプレートは JS/TS + ReScript 部分のみ

## ファイル追加・変更

### 新規ファイル

| パス | 説明 |
|------|------|
| `src/main/kotlin/com/rescript/plugin/wizard/templates/ReactNativeCliTemplateFiles.kt` | Community CLI テンプレート生成ロジック |
| `src/test/kotlin/com/rescript/plugin/wizard/templates/ReactNativeCliTemplateFilesTest.kt` | テンプレート生成の単体テスト |

### 変更ファイル

| パス | 変更内容 |
|------|---------|
| `src/main/kotlin/com/rescript/plugin/wizard/ProjectTemplate.kt` | `REACT_NATIVE_CLI` enum エントリ追加、import 追加、when 分岐追加 |
| `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateVersions.kt` | `RN_COMMUNITY_CLI` + 関連 devDeps バージョン定数追加 |
| `src/test/kotlin/com/rescript/plugin/wizard/ProjectTemplateTest.kt` | 新規 enum エントリに対する generateFiles 検証 |
| `src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateIntegrationTest.kt` | Community CLI テンプレート検証ケース追加 |
| `CLAUDE.md` | プロジェクトウィザード記述を 14 → 15 テンプレートに更新 |
| `README.md` | Features セクション更新 |
| `sphinx-docs/user/features/advanced.md` | Community CLI テンプレートを追記 |
| `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po` | 日本語訳更新 |
| `docs/product-requirements.md` | Project Wizard エントリ更新（14 → 15） |

## ProjectTemplate.kt への追加

```kotlin
REACT_NATIVE_CLI(
    "React Native (Community CLI)",
    "Mobile app with React Native Community CLI (bare workflow) for native Android/iOS integration",
    TemplateCategory.MOBILE,
),
```

`when` 分岐にも追加:

```kotlin
REACT_NATIVE_CLI -> ReactNativeCliTemplateFiles.generate(ctx)
```

## TemplateVersions.kt への追加

```kotlin
// React Native Community CLI (bare workflow)
const val RN_COMMUNITY_CLI = "^15.0.0"
const val METRO_CONFIG = "^0.81.0"
const val BABEL_PRESET_RN = "^0.77.0"
const val RN_TYPES = "^0.73.0"
```

バージョンは 2026-04 時点の安定版を基準に設定。nightly 統合テストで実際にインストール可能か検証する。

## ReactNativeCliTemplateFiles.kt の構造

Expo 版 (`ReactNativeTemplateFiles.kt`) を雛形とし、以下の差分を持つ:

### 生成ファイル一覧

| パス | 内容 |
|------|------|
| `rescript.json` | Expo 版と同一（`@rescript/core`, `@rescript/react`, JSX + genType） |
| `package.json` | Expo 系依存を削除し、RN Community CLI 依存を追加。scripts に `start`/`android`/`ios`/`lint`/`test`/`res:build`/`res:clean`/`res:dev` |
| `index.js` | `AppRegistry.registerComponent(appName, () => App)` |
| `App.tsx` | ReScript `App.gen` の再エクスポート |
| `app.json` | `{ "name": projectName, "displayName": projectName }` |
| `metro.config.js` | `.res.mjs` / `.res.js` を resolver 対象に含める設定 |
| `babel.config.js` | `module:@react-native/babel-preset` プリセット |
| `src/App.res` | Todo 画面サンプル（Expo 版とほぼ同一、ただしステータスバー表示を追加して Bare workflow らしさを出す） |
| `src/ReactNative.res` | コアコンポーネントバインディング（Expo 版と共通） |
| `src/NativeGreeting.res` | ネイティブモジュールバインディング例（詳細は後述） |
| `src/__tests__/App.test.mjs` | Vitest スモークテスト（`.res.mjs` 存在確認） |
| `README.md` | Community CLI 専用セクション（Prerequisites / Native init / Running / Android Studio / Adding native modules / Troubleshooting / Fallback） |
| `.gitignore` | Expo 版の `.expo/` を削除、代わりに `android/build/`, `android/app/build/`, `android/.gradle/`, `ios/Pods/`, `ios/build/`, `*.hbc`, `*.keystore` を追加 |
| `.editorconfig` | `CommonFiles.editorconfig()` |
| `.github/workflows/ci.yml` | `CommonFiles.ciWorkflow(ctx, hasTest = true)` |

### package.json の dependencies / devDependencies

**dependencies:**
- `rescript` → `TemplateVersions.RESCRIPT`
- `@rescript/core` → `TemplateVersions.RESCRIPT_CORE`
- `@rescript/react` → `TemplateVersions.RESCRIPT_REACT`
- `react` → `TemplateVersions.REACT`
- `react-native` → `TemplateVersions.REACT_NATIVE`

**devDependencies:**
- `@react-native-community/cli` → `TemplateVersions.RN_COMMUNITY_CLI`
- `@react-native/babel-preset` → `TemplateVersions.BABEL_PRESET_RN`
- `@react-native/metro-config` → `TemplateVersions.METRO_CONFIG`
- `@types/react-native` → `TemplateVersions.RN_TYPES`
- `vitest` → `TemplateVersions.VITEST`

**scripts:**
- `start` → `react-native start`
- `android` → `react-native run-android`
- `ios` → `react-native run-ios`
- `test` → `vitest run`
- `res:build` → `rescript`
- `res:clean` → `rescript clean`
- `res:dev` → `rescript -w`

### NativeGreeting.res の内容（バインディングのみ）

Kotlin の実装例は含めず、「ユーザーが書いた Kotlin Native Module を ReScript から呼ぶときのバインディング形」だけを提示する:

```rescript
// Bindings for a custom native module exposed from Android/iOS.
// Implement the matching Kotlin/Swift module following the React Native
// docs, then import it through these bindings.
module NativeGreeting = {
  @module("react-native") @scope("NativeModules")
  external module_: {"greet": string => promise<string>} = "NativeGreeting"

  let greet = (name: string): promise<string> => module_["greet"](name)
}
```

README では上記の使い方のみ記載し、Kotlin 側の `ReactContextBaseJavaModule` 実装例は **含めない**（要件 #3）。代わりに公式 RN ドキュメントへのリンクを貼る。

### metro.config.js の内容

```js
const { getDefaultConfig, mergeConfig } = require('@react-native/metro-config');

const defaultConfig = getDefaultConfig(__dirname);

const config = {
  resolver: {
    sourceExts: [...defaultConfig.resolver.sourceExts, 'mjs'],
  },
};

module.exports = mergeConfig(defaultConfig, config);
```

### babel.config.js の内容

```js
module.exports = {
  presets: ['module:@react-native/babel-preset'],
};
```

### README の構成（見出しレベル 2）

1. `Prerequisites` — JDK 21、Android Studio、Android SDK、NDK、watchman（macOS/Linux）、Xcode（macOS）
2. `Getting Started` — `pnpm install` → `npx react-native init-android` 相当の初期化ガイド（Community CLI のバージョンにより手順が変動する旨を注記）
3. `Running on Android` — Metro (`pnpm start`) + `pnpm android` の 2 ターミナル運用
4. `Opening in Android Studio` — `android/` を開く手順、Gradle 同期、実機/エミュレータでの実行
5. `Adding Native Modules` — `NativeGreeting.res` バインディングの使い方のみ。Kotlin 実装は [React Native 公式 docs](https://reactnative.dev/docs/legacy/native-modules-android) へ誘導
6. `Troubleshooting` — `adb reverse tcp:8081 tcp:8081`、`./gradlew clean`、Metro cache clear (`pnpm start --reset-cache`)
7. `Fallback` — Community CLI のバージョン変動に伴う手順差異があれば公式ドキュメントを優先する旨

`CommonFiles.readme` の `extraSections` パラメータを使って各セクションを渡す。

### .gitignore の内容

`CommonFiles.gitignore` を呼び出し、`extra` に以下を渡す:

```
android/build/
android/app/build/
android/.gradle/
android/local.properties
ios/Pods/
ios/build/
ios/.xcode.env.local
*.hbc
*.keystore
```

## テスト戦略

### ReactNativeCliTemplateFilesTest

- `generate(projectName)` / `generate(ctx)` が必須キーをすべて含む Map を返す
- `rescript.json` に `@rescript/core`, `@rescript/react` が含まれる
- `package.json` に `react-native`, `@react-native-community/cli` が含まれ、`expo` は **含まれない**
- `scripts` に `start`, `android`, `ios`, `test`, `res:build`, `res:clean`, `res:dev` がある
- `.gitignore` に `android/build/` が含まれる
- `App.tsx` が `./src/App.gen` を import
- `metro.config.js` に `'mjs'` が含まれる
- `README.md` に "Community CLI" / "Android Studio" / "Native Modules" セクションが含まれる
- `NativeGreeting.res` に `@module("react-native")` と `@scope("NativeModules")` が含まれる

### ProjectTemplateTest への追記

- `REACT_NATIVE_CLI.generateFiles("myproj")` が空でない Map を返す
- `REACT_NATIVE_CLI.category == TemplateCategory.MOBILE`
- `REACT_NATIVE_CLI.displayName == "React Native (Community CLI)"`
- Expo 版と Community CLI 版が **異なる** Map を返す（`expo` キーの有無で判別）

### TemplateIntegrationTest

既存の nightly パターンに従い、生成 → `pnpm install` → `pnpm rescript build` を実行する。`react-native run-android` は Android SDK 依存のため **対象外**。Metro 起動も不要。

## ドキュメント同期

### CLAUDE.md

レイヤー 3「プロジェクトウィザード」の記述:

- 「14 テンプレート (...)」 → 「15 テンプレート (...)」
- React Native Community CLI を列挙に追加

### README.md

Features セクションの Project Wizard 箇所に Community CLI の一行を追加。

### sphinx-docs/user/features/advanced.md

Project Wizard の章に Community CLI セクションを新設。対応する `.po` を `make gettext && make update-po` で生成し、日本語 `msgstr` を埋める。

### docs/product-requirements.md

実装済み機能テーブルの Project Wizard エントリを 14 → 15 に更新し、Community CLI を追記。

## リスクと緩和

| リスク | 緩和 |
|---|---|
| `@react-native-community/cli` のバージョン変動 | `TemplateVersions.kt` で集約。README に fallback 注記 |
| Metro resolver 設定ミスで `.res.mjs` が読めない | 統合テストは `rescript build` までしか行わないため Metro 自体の検証は手動。README にトラブルシュートを記載 |
| 統合テストの pnpm install 時間増加 | nightly のみ実行のため許容。既存 14 テンプレートに 1 つ足すだけなので影響は軽微 |
| Android SDK 非依存で検証しきれない | 受け入れ条件で明示的にスコープ外とする |
