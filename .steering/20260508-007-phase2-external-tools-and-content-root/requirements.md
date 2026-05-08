# Phase 2: External Tools and Content-Root Fixtures — Requirements

## 背景

直前のバッチ（20260508-006）で integration test と smoke benchmark を追加したが、3 つのカテゴリは Phase 2 に持ち越した:

1. **Mermaid Live / graphviz `dot` でのレンダリング検証**（#1 Variant Flow Diagram）
2. **populated FileTypeIndex / FilenameIndex を要する fixture テスト**（#7 / #3 / #11）
3. **`rescript convert` CLI を実行する end-to-end テスト**（#11 Reason Migration Pilot）

これらは CI インフラ変更（追加バイナリのインストール）や、既存の light project descriptor では駆動できない API を要するため、独立したバッチとして対応する。

実装中に判明した制約により、本ステアリングは外部 CLI 検証 + CI 設定追加のみを対象とし、content-root fixture は別ステアリングへ再分類した（詳細は tasklist.md 末尾）。

## ユーザーストーリー

### US-Phase2-01: 外部 CLI による出力検証

**プラグイン保守者として**、Mermaid / DOT / `rescript convert` の出力が外部 CLI で受理可能であることを CI で自動検証したい。

**受け入れ条件:**

- [x] `RescriptVariantFlowMermaidExporterCliTest`: 生成 Mermaid 文字列を `mmdc` で SVG レンダリングできる（CLI 不在時は skip）
- [x] `RescriptVariantFlowDotExporterCliTest`: 生成 DOT 文字列を `dot -Tsvg` でレンダリングできる（CLI 不在時は skip）
- [x] `RescriptMigrationConverterCliTest`: `RescriptMigrationConverter.buildCommand` が組み立てる argv を `npx rescript convert` で実行し、CLI に受理されることを検証（CLI 不在時は skip）。実 `.re → .res` の VFS write action 経由 e2e は content-root fixture 導入後の課題
- [x] CI ワークフロー（`ci.yml`）に `mmdc` (`@mermaid-js/mermaid-cli`)、`graphviz`（apt）、`rescript`（npm）のインストールステップを追加
- [x] ローカル開発機で 3 つのうち 1 つも入っていない場合でも `./gradlew test` 全体は成功する（`Assumptions.assumeTrue` で skip）

### US-Phase2-02: Content-root 付き fixture による populated index 駆動

**保守者として**、`FileTypeIndex.getFiles` / `FilenameIndex.getAllFilesByExt` を駆動する fixture テストで populated ケースを assert したい。

**実装中に判明した制約により本ステアリングでは撤回。** `DefaultLightProjectDescriptor` が Java module の `LanguageLevelModuleExtension` を要求し、Kotlin-only sandbox では `NoClassDefFoundError` が発生する。`LightProjectDescriptor` 直接継承では index に乗らない（実機検証済み）。Java module サポートを sandbox に加えるか別アプローチを採るかは別ステアリングで扱う。

**受け入れ条件:** 全項目を将来課題に再分類。

## スコープ外

- パフォーマンス測定（1000 ファイルプロジェクトなど）— smoke benchmark は既に追加済み
- LSP 結合テスト
- カバレッジラチェット引き上げ
- mutation testing の対象拡大
- content-root 付き fixture の導入（上記理由により別ステアリングへ）

## 受け入れ確認

- [x] CI ワークフローで 3 つの CLI が利用可能になり、対応するテストが実行される
- [x] ローカル CLI 不在時に CI 用テストがすべて skip される（実機確認済み: dot / mmdc / rescript いずれも未インストール環境で 3 件 skip）
- [~] populated fixture テストで InteropScanner と MigrationFinder の戻り値が期待通り — 撤回（content-root fixture の制約）
- [x] 既存テストへのリグレッションなし（`./gradlew test` 全件緑）

## 非機能要件

- 外部 CLI 呼び出しは 30〜60 秒タイムアウト（`mmdc` のみ初回のフォントロードで時間がかかるため 60 秒に延長）
- CI ジョブの実行時間増分は < 2 分（CLI のインストールが主因）
- ローカルでの skip 判定は `Assumptions.assumeTrue` で行い、ログに skip 理由を残す
