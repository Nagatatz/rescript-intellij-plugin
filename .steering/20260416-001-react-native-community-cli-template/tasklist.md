# Tasklist: React Native (Community CLI) テンプレート新設

## Phase 0: 準備

- [x] `EnterWorktree` で作業用 worktree `rn-community-cli-template` に入る

## Phase 1: 実装

### 1.1 バージョン定数の追加

- [x] `TemplateVersions.kt` に以下の定数を追加:
  - [x] `RN_COMMUNITY_CLI = "^15.0.0"`
  - [x] `RN_METRO_CONFIG = "^0.81.0"`
  - [x] `RN_BABEL_PRESET = "^0.81.0"`
  - [x] `RN_TYPES = "^0.73.0"`
- [x] `TemplateVersionsTest.kt` があれば新規定数の検証を追加

### 1.2 ReactNativeCliTemplateFiles.kt の作成

- [x] `src/main/kotlin/com/rescript/plugin/wizard/templates/ReactNativeCliTemplateFiles.kt` を新規作成
- [x] KDoc（クラスの責務 1〜3 文、英語）を付与
- [x] `generate(ctx: TemplateContext): Map<String, String>` を実装
- [x] `generate(projectName: String): Map<String, String>` のバックコンパット版を実装
- [x] 以下のファイル生成ロジックを含める:
  - [x] `rescript.json`（`ProjectFileBuilders.rescriptJson` 使用）
  - [x] `package.json`（Expo 系依存を除外、Community CLI 依存を追加）
  - [x] `index.js`（`AppRegistry.registerComponent`）
  - [x] `App.tsx`（`./src/App.gen` 再エクスポート）
  - [x] `app.json`（`{ name, displayName }`）
  - [x] `metro.config.js`（`.res.mjs` を resolver sourceExts に追加）
  - [x] `babel.config.js`（`module:@react-native/babel-preset`）
  - [x] `src/App.res`（Todo 画面サンプル）
  - [x] `src/ReactNative.res`（コアバインディング）
  - [x] `src/NativeGreeting.res`（ネイティブモジュールバインディング例のみ、Kotlin 実装なし）
  - [x] `src/__tests__/App.test.mjs`（Vitest スモークテスト）
  - [x] `README.md`（Prerequisites / Getting Started / Running / Android Studio / Native Modules / Troubleshooting / Fallback）
  - [x] `.gitignore`（`android/build/`, `android/app/build/`, `android/.gradle/`, `android/local.properties`, `ios/Pods/`, `ios/build/`, `ios/.xcode.env.local`, `*.hbc`, `*.keystore`）
  - [x] `.editorconfig`
  - [x] `.github/workflows/ci.yml`

### 1.3 ProjectTemplate.kt への登録

- [x] `REACT_NATIVE_CLI` enum エントリを追加（displayName: `"React Native (Community CLI)"`, category: `MOBILE`）
- [x] `import ReactNativeCliTemplateFiles` を追加
- [x] `generateFiles(ctx)` の `when` 分岐に `REACT_NATIVE_CLI -> ReactNativeCliTemplateFiles.generate(ctx)` を追加

## Phase 2: テスト

### 2.1 ユニットテスト

- [x] `src/test/kotlin/com/rescript/plugin/wizard/templates/ReactNativeCliTemplateFilesTest.kt` を新規作成
- [x] 以下のケースを実装:
  - [x] 必須ファイルがすべて Map のキーに含まれる
  - [x] `rescript.json` に `@rescript/core`, `@rescript/react` が含まれる
  - [x] `package.json` に `react-native`, `@react-native-community/cli` が含まれる
  - [x] `package.json` に `expo` が **含まれない**
  - [x] `package.json` の `scripts` に `start`, `android`, `ios`, `test`, `res:build`, `res:clean`, `res:dev` が揃う
  - [x] `.gitignore` に `android/build/` が含まれる
  - [x] `.gitignore` に `.expo/` が **含まれない**
  - [x] `App.tsx` が `./src/App.gen` を import する
  - [x] `metro.config.js` に `'mjs'` が含まれる
  - [x] `babel.config.js` に `@react-native/babel-preset` が含まれる
  - [x] `README.md` に "Community CLI" / "Android Studio" / "Native Modules" の見出しが含まれる
  - [x] `NativeGreeting.res` に `@module("react-native")` と `@scope("NativeModules")` が含まれる
  - [x] `generate(projectName)` と `generate(ctx)` の戻り値が同一（デフォルト pnpm の場合）

### 2.2 既存テストへの追記

- [x] `ProjectTemplateTest.kt` に以下を追加:
  - [x] `REACT_NATIVE_CLI.generateFiles("myproj")` が空でない Map を返す
  - [x] `REACT_NATIVE_CLI.category == TemplateCategory.MOBILE`
  - [x] `REACT_NATIVE_CLI.displayName == "React Native (Community CLI)"`
  - [x] Expo 版と Community CLI 版で `package.json` 内容が異なる（`expo` キーの有無）
  - [x] `enum has 15 entries` に更新
  - [x] React 系テンプレートリストに `REACT_NATIVE_CLI` を追加

### 2.3 統合テスト

- [x] `TemplateIntegrationTest.kt` は `@EnumSource(ProjectTemplate::class)` で全エントリを自動検証するため、新規 enum エントリ追加により自動的に対象化される（コード変更不要）
- [x] 検証内容: 生成 → `pnpm install` → `pnpm rescript build` が成功する（既存ロジック）
- [x] 検証対象外: `react-native run-android`, Metro 起動, Gradle ビルド

## Phase 3: ドキュメント

### 3.1 CLAUDE.md 更新

- [x] レイヤー 3「プロジェクトウィザード」記述の「14 テンプレート」を「15 テンプレート」に変更
- [x] Community CLI を列挙に追加（bare workflow + Android Studio 統合向け）

### 3.2 README.md 更新

- [x] Features セクションの Project Wizard に Community CLI を追記

### 3.3 sphinx-docs 更新

- [x] `sphinx-docs/user/features/advanced.md` の Project Wizard 章に Community CLI セクションを追加
- [x] `cd sphinx-docs && make gettext && make update-po`
- [x] `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po` の新規 `msgstr` を日本語で埋める
- [x] `make build-ja` が成功することを確認

### 3.4 product-requirements.md 更新

- [x] 実装済み機能テーブルの Project Wizard エントリを 14 → 15 に更新
- [x] Community CLI の説明を追記

## Phase 4: コミット前検証

- [x] `./gradlew ktlintCheck` が成功する
- [x] `./gradlew clean buildPlugin` が成功する
- [x] `./gradlew test` が成功する
- [x] ビルド警告が新たに増加していない
- [x] 新規 `.kt` ファイルすべてに KDoc が付与されている
- [x] 新規 `.kt` ファイルすべてに対応する `*Test.kt` が存在する（`ReactNativeCliTemplateFiles` → `ReactNativeCliTemplateFilesTest`）
- [x] Extension Point を実装するクラスなし → `plugin.xml` 更新不要
- [x] セキュリティ: 外部入力バリデーション不要（テンプレートは静的文字列生成のみ）、`ProcessBuilder` 使用なし、絶対パス露出なし

## Phase 5: コミット

機能単位でコミットを分割する。各コミット前に `tasklist.md` の該当タスクを `[x]` に更新し、コミットに同梱する。

- [x] コミット 1: `✨ Add React Native (Community CLI) template`
  - 対象: `TemplateVersions.kt`, `ReactNativeCliTemplateFiles.kt`, `ReactNativeCliTemplateFilesTest.kt`, `ProjectTemplate.kt`, `ProjectTemplateTest.kt`, `TemplateVersionsTest.kt`, `.steering/` 新規
- [x] コミット 2: `📝 Document React Native (Community CLI) template`
  - 対象: `CLAUDE.md`, `README.md`, `sphinx-docs/user/features/advanced.md`, `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po`, `docs/product-requirements.md`
- [x] コミット 3: `📝 Mark tasklist complete for RN Community CLI template`
  - 対象: `.steering/20260416-001-react-native-community-cli-template/tasklist.md`

## Phase 6: マージ

- [x] requirements.md の全受け入れ条件を満たしていることを確認
- [x] tasklist.md のすべてのタスクが `[x]` になっている
- [x] `AskUserQuestion` でユーザーにマージ可否を確認
- [x] 承認後、worktree 内で `git checkout main && git merge <作業ブランチ>` を実行
- [x] 作業ブランチを `git branch -d` で削除
- [x] セッションを終了（worktree の自動クリーンアップを発動）

## 免除対象の明記

- なし（新規クラス `ReactNativeCliTemplateFiles` はテンプレートデータ生成ロジックのためテスト必須、免除対象外）

## メモ

- `NativeGreeting.res` は **バインディングのみ** とし、Kotlin 実装例は README にも含めない（ユーザー要望 #3）
- `android/`, `ios/` ディレクトリは生成対象外（ユーザー要望 #1）
- 既存 Expo 版 `REACT_NATIVE` の displayName は既に `"React Native (Expo)"` になっているため改名コミット不要
