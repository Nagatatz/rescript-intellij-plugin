# 設計: Migration Converter の相対パス渡し

## 現状

- `RescriptMigrationConverter.convert` の 34 行目:
  ```kotlin
  val command = buildCommand(settings.rescriptBinaryPath, candidate.file.path)
  ```
  - `candidate.file.path` は `VirtualFile.path`（絶対パス）
  - 一方 `MigrationCandidate.relativePath` は `RescriptMigrationFinder.toCandidates` で計算済み（プロジェクトルートからの相対パス）
- ProcessBuilder の cwd は `project.basePath`、つまり `rescript.json` が置かれるディレクトリ（通常 `manual-test-projects/main` 配下では各 npm パッケージのルート）

## 変更点

### 1. `RescriptMigrationConverter.kt`

- 34 行目を以下に変更:
  ```kotlin
  val command = buildCommand(settings.rescriptBinaryPath, candidate.relativePath)
  ```
- `buildCommand` の KDoc `@param sourcePath` を **「project root からの相対パス。`rescript convert` は cwd 起点で解決するため絶対パスではなく相対パスを渡す」** に書き直す
- 59 行目のログメッセージ `${candidate.file.path}` は診断目的で絶対パスのまま残してよい（ユーザー UI には露出しない）

### 2. `RescriptMigrationConverterTest.kt`

- 4 つの buildCommand テストの sourcePath を絶対 (`/tmp/Main.re`) から相対 (`src/Main.re`) に書き換える
- 「空白入り相対パスを 1 引数として保持」のテストを `src/My Folder/Main.re` に変更
- 期待値も同じく相対パス文字列に揃える

### 3. `RescriptMigrationConverterE2eTest.kt`

- 51 行目はすでに `MigrationCandidate(file = virtualFile, relativePath = "Sample.re")` を組み立てており、修正後の挙動と整合する。変更不要。

## なぜパスは絶対より相対が正しいか

`rescript convert` (= `bsc` の compatibility mode) は `Sys.getcwd () |> Filename.concat path` でファイルを解決する。絶対パスを渡すと `Sys.argv.(n) = "/Users/.../LegacyReason.re"` のまま比較され、bsconfig の `sources` glob と一致しないため `don't know what to do with` で reject される。VSCode 拡張も同様に相対パスで CLI を起動している。

## テスト戦略

- ユニット: `RescriptMigrationConverterTest` の expected argv 4 ケースを相対パス入力に更新
- E2E: 既存の `RescriptMigrationConverterE2eTest` で `relativePath = "Sample.re"` を渡す経路は変わらず通る
- 手動: `manual-test-projects/main` で `LegacyReason.re` の convert が成功することを確認（ステアリング後）

## 後方互換性

- `MigrationCandidate` のシグネチャ・field は変更しない
- public API シグネチャ無変更
