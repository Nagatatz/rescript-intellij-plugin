# Phase 2: External Tools and Content-Root Fixtures — Requirements

## 背景

直前のバッチ（20260508-006）で integration test と smoke benchmark を追加したが、3 つのカテゴリは Phase 2 に持ち越した:

1. **Mermaid Live / graphviz `dot` でのレンダリング検証**（#1 Variant Flow Diagram）
2. **populated FileTypeIndex / FilenameIndex を要する fixture テスト**（#7 / #3 / #11）
3. **`rescript convert` CLI を実行する end-to-end テスト**（#11 Reason Migration Pilot）

これらは CI インフラ変更（追加バイナリのインストール）や、既存の light project descriptor では駆動できない API を要するため、独立したバッチとして対応する。

## ユーザーストーリー

### US-Phase2-01: 外部 CLI による出力検証

**プラグイン保守者として**、Mermaid / DOT / `rescript convert` の出力が外部 CLI で受理可能であることを CI で自動検証したい。

**受け入れ条件:**

- [ ] `RescriptVariantFlowMermaidExporterCliTest`: 生成 Mermaid 文字列を `mmdc` で SVG レンダリングできる（CLI 不在時は skip）
- [ ] `RescriptVariantFlowDotExporterCliTest`: 生成 DOT 文字列を `dot -Tsvg` でレンダリングできる（CLI 不在時は skip）
- [ ] `RescriptMigrationConverterCliTest`: 実 `.re` テキストに対して `RescriptMigrationConverter.convert` が成功し、生成 `.res` の内容が ReScript として valid（CLI 不在時は skip）
- [ ] CI ワークフロー（`ci.yml`）に `mmdc` (`@mermaid-js/mermaid-cli`)、`graphviz`（apt）、`rescript`（npm）のインストールステップを追加
- [ ] ローカル開発機で 3 つのうち 1 つも入っていない場合でも `./gradlew test` 全体は成功する（`Assumptions.assumeTrue` で skip）

### US-Phase2-02: Content-root 付き fixture による populated index 駆動

**保守者として**、`FileTypeIndex.getFiles` / `FilenameIndex.getAllFilesByExt` を駆動する fixture テストで populated ケースを assert したい。

**受け入れ条件:**

- [ ] `RescriptContentRootProjectDescriptor`（または同等）を導入し、`addFileToProject` で追加した `.res` / `.re` が index に乗ることを保証する
- [ ] `IntelliJPlatformExtensionWithContentRoot` を導入し、既存 `IntelliJPlatformExtension` と並存させる（既存テストには影響を与えない）
- [ ] `RescriptInteropScannerIntegrationTest` を populated ケースに拡張（`Obj.magic`、`external` を含む `.res` を 2 件配置 → 期待する InteropKind が出る）
- [ ] `RescriptMigrationFinderIntegrationTest` を populated ケースに拡張（`.re` / `.rei` / `.res` を混在配置 → `.re` / `.rei` のみが候補）

## スコープ外

- パフォーマンス測定（1000 ファイルプロジェクトなど）— smoke benchmark は既に追加済み、より厳密な perf gate は別途
- LSP 結合テスト
- カバレッジラチェット引き上げ
- mutation testing の対象拡大

## 受け入れ確認

- [ ] CI ワークフローで 3 つの CLI が利用可能になり、対応するテストが実行される
- [ ] ローカル CLI 不在時に CI 用テストがすべて skip される
- [ ] populated fixture テストで InteropScanner と MigrationFinder の戻り値が期待通り
- [ ] 既存テストへのリグレッションなし

## 非機能要件

- 外部 CLI 呼び出しは 30 秒タイムアウト（既存 `RescriptReplExecutor` と同じ運用）
- CI ジョブの実行時間増分は < 2 分（CLI のインストールが主因）
- ローカルでの skip 判定は `Assumptions.assumeTrue` で行い、ログに skip 理由を残す
