# Tasklist: React Native (Community CLI) テンプレート新設

## Phase 0: 準備

- [ ] `EnterWorktree` で作業用 worktree `rn-community-cli-template` に入る

## Phase 1: 実装

### 1.1 バージョン定数の追加

- [ ] `TemplateVersions.kt` に以下の定数を追加:
  - [ ] `RN_COMMUNITY_CLI = "^15.0.0"`
  - [ ] `METRO_CONFIG = "^0.81.0"`
  - [ ] `BABEL_PRESET_RN = "^0.77.0"`
  - [ ] `RN_TYPES = "^0.73.0"`
- [ ] `TemplateVersionsTest.kt` があれば新規定数の検証を追加

### 1.2 ReactNativeCliTemplateFiles.kt の作成

- [ ] `src/main/kotlin/com/rescript/plugin/wizard/templates/ReactNativeCliTemplateFiles.kt` を新規作成
- [ ] KDoc（クラスの責務 1〜3 文、英語）を付与
- [ ] `generate(ctx: TemplateContext): Map<String, String>` を実装
- [ ] `generate(projectName: String): Map<String, String>` のバックコンパット版を実装
- [ ] 以下のファイル生成ロジックを含める:
  - [ ] `rescript.json`（`ProjectFileBuilders.rescriptJson` 使用）
  - [ ] `package.json`（Expo 系依存を除外、Community CLI 依存を追加）
  - [ ] `index.js`（`AppRegistry.registerComponent`）
  - [ ] `App.tsx`（`./src/App.gen` 再エクスポート）
  - [ ] `app.json`（`{ name, displayName }`）
  - [ ] `metro.config.js`（`.res.mjs` を resolver sourceExts に追加）
  - [ ] `babel.config.js`（`module:@react-native/babel-preset`）
  - [ ] `src/App.res`（Todo 画面サンプル）
  - [ ] `src/ReactNative.res`（コアバインディング）
  - [ ] `src/NativeGreeting.res`（ネイティブモジュールバインディング例のみ、Kotlin 実装なし）
  - [ ] `src/__tests__/App.test.mjs`（Vitest スモークテスト）
  - [ ] `README.md`（Prerequisites / Getting Started / Running / Android Studio / Native Modules / Troubleshooting / Fallback）
  - [ ] `.gitignore`（`android/build/`, `android/app/build/`, `android/.gradle/`, `android/local.properties`, `ios/Pods/`, `ios/build/`, `ios/.xcode.env.local`, `*.hbc`, `*.keystore`）
  - [ ] `.editorconfig`
  - [ ] `.github/workflows/ci.yml`

### 1.3 ProjectTemplate.kt への登録

- [ ] `REACT_NATIVE_CLI` enum エントリを追加（displayName: `"React Native (Community CLI)"`, category: `MOBILE`）
- [ ] `import ReactNativeCliTemplateFiles` を追加
- [ ] `generateFiles(ctx)` の `when` 分岐に `REACT_NATIVE_CLI -> ReactNativeCliTemplateFiles.generate(ctx)` を追加

## Phase 2: テスト

### 2.1 ユニットテスト

- [ ] `src/test/kotlin/com/rescript/plugin/wizard/templates/ReactNativeCliTemplateFilesTest.kt` を新規作成
- [ ] 以下のケースを実装:
  - [ ] 必須ファイルがすべて Map のキーに含まれる
  - [ ] `rescript.json` に `@rescript/core`, `@rescript/react` が含まれる
  - [ ] `package.json` に `react-native`, `@react-native-community/cli` が含まれる
  - [ ] `package.json` に `expo` が **含まれない**
  - [ ] `package.json` の `scripts` に `start`, `android`, `ios`, `test`, `res:build`, `res:clean`, `res:dev` が揃う
  - [ ] `.gitignore` に `android/build/` が含まれる
  - [ ] `.gitignore` に `.expo/` が **含まれない**
  - [ ] `App.tsx` が `./src/App.gen` を import する
  - [ ] `metro.config.js` に `'mjs'` が含まれる
  - [ ] `babel.config.js` に `@react-native/babel-preset` が含まれる
  - [ ] `README.md` に "Community CLI" / "Android Studio" / "Native Modules" の見出しが含まれる
  - [ ] `NativeGreeting.res` に `@module("react-native")` と `@scope("NativeModules")` が含まれる
  - [ ] `generate(projectName)` と `generate(ctx)` の戻り値が同一（デフォルト pnpm の場合）

### 2.2 既存テストへの追記

- [ ] `ProjectTemplateTest.kt` に以下を追加:
  - [ ] `REACT_NATIVE_CLI.generateFiles("myproj")` が空でない Map を返す
  - [ ] `REACT_NATIVE_CLI.category == TemplateCategory.MOBILE`
  - [ ] `REACT_NATIVE_CLI.displayName == "React Native (Community CLI)"`
  - [ ] Expo 版と Community CLI 版で `package.json` 内容が異なる（`expo` キーの有無）

### 2.3 統合テスト

- [ ] `TemplateIntegrationTest.kt` に `REACT_NATIVE_CLI` ケースを追加（nightly 限定）
- [ ] 検証内容: 生成 → `pnpm install` → `pnpm rescript build` が成功する
- [ ] 検証対象外: `react-native run-android`, Metro 起動, Gradle ビルド

## Phase 3: ドキュメント

### 3.1 CLAUDE.md 更新

- [ ] レイヤー 3「プロジェクトウィザード」記述の「14 テンプレート」を「15 テンプレート」に変更
- [ ] Community CLI を列挙に追加（bare workflow + Android Studio 統合向け）

### 3.2 README.md 更新

- [ ] Features セクションの Project Wizard に Community CLI を追記

### 3.3 sphinx-docs 更新

- [ ] `sphinx-docs/user/features/advanced.md` の Project Wizard 章に Community CLI セクションを追加
- [ ] `cd sphinx-docs && make gettext && make update-po`
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po` の新規 `msgstr` を日本語で埋める
- [ ] `make build-ja` が成功することを確認

### 3.4 product-requirements.md 更新

- [ ] 実装済み機能テーブルの Project Wizard エントリを 14 → 15 に更新
- [ ] Community CLI の説明を追記

## Phase 4: コミット前検証

- [ ] `./gradlew ktlintCheck` が成功する
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] `./gradlew test` が成功する
- [ ] ビルド警告が新たに増加していない
- [ ] 新規 `.kt` ファイルすべてに KDoc が付与されている
- [ ] 新規 `.kt` ファイルすべてに対応する `*Test.kt` が存在する（`ReactNativeCliTemplateFiles` → `ReactNativeCliTemplateFilesTest`）
- [ ] Extension Point を実装するクラスなし → `plugin.xml` 更新不要
- [ ] セキュリティ: 外部入力バリデーション不要（テンプレートは静的文字列生成のみ）、`ProcessBuilder` 使用なし、絶対パス露出なし

## Phase 5: コミット

機能単位でコミットを分割する。各コミット前に `tasklist.md` の該当タスクを `[x]` に更新し、コミットに同梱する。

- [ ] コミット 1: `✨ Add React Native (Community CLI) template`
  - 対象: `TemplateVersions.kt`, `ReactNativeCliTemplateFiles.kt`, `ReactNativeCliTemplateFilesTest.kt`, `ProjectTemplate.kt`, `ProjectTemplateTest.kt`, `TemplateIntegrationTest.kt`
- [ ] コミット 2: `📝 Document React Native (Community CLI) template`
  - 対象: `CLAUDE.md`, `README.md`, `sphinx-docs/user/features/advanced.md`, `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po`, `docs/product-requirements.md`
- [ ] コミット 3: `📝 Mark tasklist complete for RN Community CLI template`
  - 対象: `.steering/20260416-001-react-native-community-cli-template/tasklist.md`

## Phase 6: マージ

- [ ] requirements.md の全受け入れ条件を満たしていることを確認
- [ ] tasklist.md のすべてのタスクが `[x]` になっている
- [ ] `AskUserQuestion` でユーザーにマージ可否を確認
- [ ] 承認後、worktree 内で `git checkout main && git merge <作業ブランチ>` を実行
- [ ] 作業ブランチを `git branch -d` で削除
- [ ] セッションを終了（worktree の自動クリーンアップを発動）

## 免除対象の明記

- なし（新規クラス `ReactNativeCliTemplateFiles` はテンプレートデータ生成ロジックのためテスト必須、免除対象外）

## メモ

- `NativeGreeting.res` は **バインディングのみ** とし、Kotlin 実装例は README にも含めない（ユーザー要望 #3）
- `android/`, `ios/` ディレクトリは生成対象外（ユーザー要望 #1）
- 既存 Expo 版 `REACT_NATIVE` の displayName は既に `"React Native (Expo)"` になっているため改名コミット不要
