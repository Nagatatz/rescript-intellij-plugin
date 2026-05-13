# 要求: Migration Converter の絶対パス問題を修正

## 背景

サンドボックス IDE で `Reason → ReScript Migration Pilot` ツールウィンドウから `manual-test-projects/main/src/LegacyReason.re` を選択して `Convert` を押すと、`rescript convert` CLI が以下のエラーを返して失敗する:

```
[fail] src/LegacyReason.re  don't know what do with /Users/ngtz/Documents/repos/rescript-intellij-plugin/manual-test-projects/main/src/LegacyReason.re
```

`rescript convert` (実体は `bsc -bs-re-out`) は **rescript.json のあるディレクトリからの相対パス** を引数に要求する。プラグイン側は `candidate.file.path`（絶対パス）を渡しているため拒否される。

## ユーザーストーリー

**ReScript プロジェクトに残った Reason ファイルを抱える開発者として**、Migration Pilot から選択した `.re` / `.rei` ファイルを実際に `.res` / `.resi` へ変換できることで、移行作業を進めたい。

## 受け入れ条件

- [ ] `manual-test-projects/main/src/LegacyReason.re` を Migration Pilot から選択して Convert すると、`.res` ファイルへの変換が成功する
- [ ] `RescriptMigrationConverter.convert` は `MigrationCandidate.relativePath` を引数として CLI に渡す
- [ ] 既存の `RescriptMigrationConverterTest` の `buildCommand` テストが相対パス入力で通る
- [ ] `RescriptMigrationConverterE2eTest` (CLI 利用可能時のみ実行) が引き続きグリーン

## 制約

- 既存の `buildCommand` 引数名 `sourcePath` は維持する。意味は「rescript.json からの相対パス」に変わる旨を KDoc で明記する
- ProcessBuilder の cwd は引き続き `project.basePath` を使用（変更しない）
