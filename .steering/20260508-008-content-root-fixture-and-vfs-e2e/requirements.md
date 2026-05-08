# Phase 3: Content-Root Fixture and VFS End-to-End Tests — Requirements

## 背景

直前のバッチ（20260508-007）で外部 CLI 検証 + CI セットアップは完了したが、content-root を持つ fixture と、それに依存する以下の項目は撤回扱いで残っていた:

1. **populated `FileTypeIndex` / `FilenameIndex` driving fixture テスト**（#7 / #3 / #11）
2. **`RescriptMigrationConverter.convert` の VFS write action 経由 e2e テスト**（#11）

本ステアリングでは heavy IDE fixture（`IdeaTestFixtureFactory.createFixtureBuilder(name, path, false)`）を導入して再挑戦した。**結果:** VFS write action を要する 2 番（e2e）は実装完了。populated index を要する 1 番は heavy fixture でも動作せず（後述）、将来課題として残した。

## 実装中に判明した制約

heavy fixture では `addFileToProject` で追加したファイルが実 file system に書かれ file type も認識されるが、project module に bind されないため `FileTypeIndex.getFiles(...).projectScope` が空のまま（`inProjectScope=false` を実機確認）。これは Java module が提供する source root 自動構成が Kotlin-only sandbox に無いため。populated index の動作は IntelliJ Platform の責務と再分類した。

## ユーザーストーリー

### US-Phase3-01: heavy fixture による populated index 駆動

**実装中の制約により本ステアリングでは撤回。** populated `FileTypeIndex` / `FilenameIndex` の駆動は IntelliJ Platform の Java module 依存を要する。production が `FileTypeIndex.getFiles` を正しく呼び出すこと自体は既存 smoke test でカバー済み。populated 動作は plugin 責務外と整理した。

### US-Phase3-02: VFS write action 経由の Migration Converter e2e

**保守者として**、`RescriptMigrationConverter.convert` が実 `.re` ファイルを `.res` にリネーム + 内容書き換えする動作を検証したい。

**受け入れ条件:**

- [x] `RescriptMigrationConverterE2eTest`: heavy fixture プロジェクトに `.re` を配置 → `convert(project, candidate)` を実行 → 戻り値が `SUCCESS` で対応する `.res` ファイルが内容書き換え済み（CLI 不在時は skip）。CLI 自体が project context（`bsconfig.json`）を要求する環境では `Assumptions.abort` で skip する設計

### US-Phase3-03: heavy fixture の影響評価

**CI 時間に敏感な保守者として**、heavy fixture の起動オーバーヘッドが許容範囲内であることを確認したい。

**受け入れ条件:**

- [x] heavy fixture を使うテスト数を 1 件に絞った（VFS e2e のみ）
- [x] 既存テスト（`IntelliJPlatformExtension` ベース）は変更しない
- [x] heavy fixture テスト 1 件あたりの実行時間 26.4 秒（ローカル skip 含む計測）

## スコープ外

- light fixture 全体の置き換え
- パフォーマンスベンチマークの populated index 化
- LSP 結合テスト
- Java module 依存追加と populated index test の駆動（将来検討、本ステアリングでは撤回）

## 受け入れ確認

- [~] populated integration test 2 件で `Obj.magic` / `external` / `.re` が index から見える — 撤回（Java module 依存制約のため）
- [x] migration converter e2e で heavy fixture が起動し、CLI が ある場合は `.re → .res` のリネーム + 書き換えが実行される
- [x] CLI 不在時は e2e が skip され、test 全体は緑
- [x] 既存テストへのリグレッションなし

## 非機能要件

- heavy fixture テストは該当機能パッケージに配置（`migration/RescriptMigrationConverterE2eTest`）
- test メソッドあたり 30 秒のタイムアウト目安
- 既存 `IntelliJPlatformExtension` は touch しない（並存）
