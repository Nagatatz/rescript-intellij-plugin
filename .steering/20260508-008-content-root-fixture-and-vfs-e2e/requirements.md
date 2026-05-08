# Phase 3: Content-Root Fixture and VFS End-to-End Tests — Requirements

## 背景

直前のバッチ（20260508-007）で外部 CLI 検証 + CI セットアップは完了したが、content-root を持つ fixture と、それに依存する以下の項目は撤回扱いで残っている:

1. **populated `FileTypeIndex` / `FilenameIndex` driving fixture テスト**（#7 / #3 / #11）
2. **`RescriptMigrationConverter.convert` の VFS write action 経由 e2e テスト**（#11）
3. **populated index に対する性能ベンチマーク**（#3 / #7、後続オプション）

直前の試行で判明した制約:
- `DefaultLightProjectDescriptor` は `IdeaTestUtil.getMockJdk17` を経由し、`LanguageLevelModuleExtension`（Java module 限定）を要求するため `NoClassDefFoundError`
- `LightProjectDescriptor` 直接継承では `addFileToProject` がしても index に乗らなかった

本ステアリングでは、**heavy IDE fixture**（`IdeaTestFixtureFactory.createFixtureBuilder(name, baseDir)`）を使って実 file system のテストプロジェクトを構築するアプローチに切り替える。light fixture より起動コストは高い（テスト 1 件あたり ~5 秒程度の見込み）が、Java module 依存なしで content root + populated index が確実に動く。

## ユーザーストーリー

### US-Phase3-01: heavy fixture による populated index 駆動

**プラグイン保守者として**、`addFileToProject` で配置した `.res` / `.re` ファイルが `FileTypeIndex.getFiles` / `FilenameIndex.getAllFilesByExt` で見えることを CI で検証したい。

**受け入れ条件:**

- [ ] `IntelliJPlatformExtensionWithContentRoot` を heavy fixture ベースで実装し、`myFixture.addFileToProject` で追加したファイルが `FileTypeIndex` から見えるようにする
- [ ] `RescriptInteropScannerPopulatedIntegrationTest`: `Obj.magic` / `external` / `@bs.send external` を含む `.res` を 2 件配置 → 期待する `InteropKind` セットが返る + `MEDIUM` risk が判定される
- [ ] `RescriptMigrationFinderPopulatedIntegrationTest`: `.re` / `.rei` / `.res` を混在配置 → `.re` / `.rei` のみが候補 + 相対パスが project base 起点

### US-Phase3-02: VFS write action 経由の Migration Converter e2e

**保守者として**、`RescriptMigrationConverter.convert` が実 `.re` ファイルを `.res` にリネーム + 内容書き換えする動作を、`rescript` CLI が CI に存在する条件下で検証したい。

**受け入れ条件:**

- [ ] `RescriptMigrationConverterE2eTest`: heavy fixture プロジェクトに `.re` を配置 → `convert(project, candidate)` を実行 → 戻り値が `SUCCESS` で、対応する `.res` ファイルが内容書き換え済みで存在する（CLI 不在時は skip）
- [ ] 不正な `.re` ファイル（構文エラー含む）を配置した場合に `convert` が `FAILED` を返し、元 `.re` は変更されない

### US-Phase3-03: heavy fixture の影響評価

**CI 時間に敏感な保守者として**、heavy fixture 由来の起動オーバーヘッドが 1 件あたり 10 秒未満に収まり、テスト全体時間が 30 秒以上は増えないことを確認したい。

**受け入れ条件:**

- [ ] heavy fixture を使うテスト数を 4 件以下に制限する（populated 2 件 + e2e 2 件まで）
- [ ] 既存テスト（`IntelliJPlatformExtension` ベース）は変更しない（影響範囲をテストファイルと新規 extension のみに留める）

## スコープ外

- light fixture 全体の置き換え
- パフォーマンスベンチマークの populated index 化（smoke benchmark は別途）
- LSP 結合テスト
- mutation testing 範囲拡大

## 受け入れ確認

- [ ] populated integration test 2 件で `Obj.magic` / `external` / `.re` が index から見える
- [ ] migration converter e2e で `.re → .res` のリネーム + 書き換えが完了する（CLI ありの場合）
- [ ] CLI 不在時は e2e がすべて skip され、test 全体は緑
- [ ] 既存テストへのリグレッションなし

## 非機能要件

- heavy fixture テストは `cli/` 直下ではなく該当機能パッケージに配置（`interop/` / `migration/`）
- test メソッドあたり 30 秒のタイムアウト
- 既存 `IntelliJPlatformExtension` は touch しない（並存）
