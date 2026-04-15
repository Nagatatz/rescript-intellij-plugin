# Requirements: React Native (Community CLI) テンプレート新設

## 背景

現行の Project Wizard には React Native テンプレートが 1 つ存在するが、Expo (Managed) ベースのみである。本プラグインの主要ユーザー層である Android Studio ユーザーは、以下の理由から Community CLI (Bare) ワークフローを選ぶことが多い:

- `android/` を Android Studio で直接開いて Gradle を操作したい
- Kotlin/Java のネイティブモジュールを追加したい
- 既存 Android プロジェクトとの統合（Brownfield）を視野に入れている
- Hermes / NDK / Gradle 設定を直接制御したい

Expo 一本では Android Studio ユーザーの期待に応えられない。

## ゴール

既存の `React Native` テンプレート（Expo ベース）に加え、`React Native (Community CLI)` テンプレートを新設する。総テンプレート数を 14 → 15 にする。

### 非ゴール

- 既存 Expo テンプレートの削除・大幅改修（本ステアリングのスコープ外。強化は別ステアリングで扱う）
- ウィザード UI へのラジオボタン追加（案 B は不採用）
- 実 Android SDK を用いた `./gradlew assembleDebug` の CI 検証（nightly では `pnpm install` + `rescript build` のみ）

## ユーザーストーリー

### US-01: Community CLI テンプレート選択

**Android Studio ユーザーとして**、Project Wizard から Community CLI ベースの React Native プロジェクトを生成したい。これにより、生成直後から Android Studio で `android/` を開いて Gradle ビルドを実行できる。

### US-02: ReScript + ネイティブモジュール

**ReScript 開発者として**、Kotlin で書かれたネイティブモジュールを `@module` バインディング経由で呼び出せるサンプルを手に入れたい。ゼロからバインディング手順を調べる手間を削減できる。

### US-03: Metro + ReScript 統合

**ユーザーとして**、`metro.config.js` が `.res.mjs` を resolver 対象として認識するよう最初から構成されていてほしい。追加設定なしで `npm start` → 実機実行ができる。

## 受け入れ条件

### AC-01: テンプレート登録

- [ ] `ProjectTemplate` enum（または同等の仕組み）に `ReactNativeCli` エントリが追加されている
- [ ] ウィザード上の表示名が `React Native (Community CLI)` である
- [ ] 既存 `ReactNative` エントリは表示名を `React Native (Expo)` に変更して併存する

### AC-02: 生成ファイル

以下のファイルが生成される:

- [ ] `rescript.json` — `@rescript/core`, `@rescript/react` 依存、JSX + genType 有効
- [ ] `package.json` — `react`, `react-native`, `@rescript/core`, `@rescript/react` 依存。dev 依存として `vitest`。scripts に `android`/`ios`/`start`/`test`/`res:build`/`res:clean`/`res:dev`
- [ ] `index.js` — React Native entry point (`AppRegistry.registerComponent`)
- [ ] `App.tsx` — ReScript `App.gen` を再エクスポート
- [ ] `src/App.res` — 画面のサンプル（Todo または類似の実例）
- [ ] `src/ReactNative.res` — コアコンポーネントバインディング
- [ ] `src/NativeGreeting.res` — Kotlin ネイティブモジュールへの `@module` バインディング例
- [ ] `src/__tests__/*.test.mjs` — Vitest スモークテスト
- [ ] `metro.config.js` — `.res.mjs` / `.res.js` resolver 設定
- [ ] `babel.config.js` — RN 標準プリセット
- [ ] `android/` と `ios/` は **テンプレート生成では含めない**（`npx react-native-community/cli init-android` 等を README で案内）※ 理由: IntelliJ プロジェクトテンプレートのサイズ肥大化回避
- [ ] `README.md` — Community CLI 前提、Android Studio 手順、ネイティブモジュール追加例
- [ ] `.gitignore` — `android/build/`, `android/app/build/`, `android/.gradle/`, `ios/Pods/`, `ios/build/` を含む
- [ ] `.editorconfig`
- [ ] `.github/workflows/ci.yml` — `pnpm install` + `rescript build` + `vitest run`

### AC-03: README 必須セクション

- [ ] **Prerequisites**: JDK 21、Android Studio、Android SDK、NDK、watchman（macOS/Linux）
- [ ] **Native project init**: `npx @react-native-community/cli init-android` 相当の手順（または `npx react-native upgrade`）
- [ ] **Running on Android**: `pnpm start` → 別ターミナルで `pnpm android` の2ターミナル運用
- [ ] **Opening in Android Studio**: `android/` ディレクトリを Android Studio で開く手順
- [ ] **Adding native modules**: `NativeGreeting.res` バインディングの使用例のみ記載（Kotlin 実装側の雛形は含めず、公式 RN ドキュメントへのリンクで済ませる）
- [ ] **Troubleshooting**: `adb reverse tcp:8081 tcp:8081`、Metro キャッシュクリア、Gradle clean
- [ ] **Fallback 注記**: `@react-native-community/cli` のバージョン変動による手順差異

### AC-04: 依存バージョンの集約

- [ ] `TemplateVersions.kt` に `REACT_NATIVE_CLI` 等の新規定数を追加（既存の `REACT_NATIVE` を流用可の場合は流用）
- [ ] 直接バージョン文字列をテンプレート内にハードコードしない

### AC-05: テスト

- [ ] `ReactNativeCliTemplateFilesTest`（新規）が `generate(projectName)` / `generate(ctx)` の戻り値について、必須ファイルの存在、`rescript.json` 依存関係、`package.json` scripts を検証
- [ ] `RescriptModuleBuilder` 相当のウィザードテストに Community CLI テンプレートの分岐テストを追加
- [ ] 既存 `ReactNativeTemplateFilesTest` は表示名変更以外に回帰が出ていない

### AC-06: 統合テスト

- [ ] `TemplateIntegrationTest` に Community CLI テンプレート対象のケースを追加
- [ ] nightly ワークフローで `pnpm install` + `pnpm rescript build` が成功する
- [ ] Android ネイティブビルド（`./gradlew assembleDebug`）は CI 対象外

### AC-07: ドキュメント同期

- [ ] `CLAUDE.md` レイヤー 3 の「プロジェクトウィザード」記述を「14 テンプレート」→「15 テンプレート」に更新し、Community CLI を追記
- [ ] `README.md` Features セクションを更新
- [ ] `sphinx-docs/user/features/advanced.md`（または該当ページ）に Community CLI テンプレートを追記
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` を同一コミットで更新し `make build-ja` が成功する
- [ ] `docs/product-requirements.md` の「Project Wizard」エントリを更新

## 技術制約

- IntelliJ Platform 2025.3+、JDK 21+
- テンプレート生成はファイルシステム書き込みのみ。外部コマンドは呼び出さない（`npx` 実行はユーザー側）
- `android/` ディレクトリは生成しない（`README` で `npx` 経由の初期化を案内）
- 生成ファイルの文字コードは UTF-8、改行は LF

## リスクと緩和策

| リスク | 緩和策 |
|---|---|
| Community CLI のバージョン変動 | README に fallback 注記、`TemplateVersions.kt` で集約管理 |
| ネイティブモジュールバインディング例の誤り | 統合テストで最低限 `rescript build` の通過を確認。README の手順は Kotlin コード例のみに限定し、動作保証は明記しない |
| テンプレート数増加による保守負荷 | 既存 `ReactNativeTemplateFiles.kt` とロジック共通部は `CommonFiles.kt` / `ProjectFileBuilders` に寄せる |
| 表示名変更による既存ユーザー影響 | 内部 enum 名は維持し、表示名のみ変更。過去プロジェクトには影響なし |
