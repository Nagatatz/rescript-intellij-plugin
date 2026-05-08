# Fixture-Based Integration Tests and Performance Benchmarks — Requirements

## 背景

直前のバッチ（#2 / #1 / #7 / #4 / #3 / #11）で追加した 6 機能は、各 requirements.md に「マージ後にユーザー側で手動検証」とした項目をいくつか残している:

- LSP 未起動時の挙動（#2）
- カーソル下の型解決と参照ジャンプ（#7）
- Notebook ラウンドトリップ（#4）
- 大規模ファイル / プロジェクトでのスキャン応答性（#1 / #3 / #2）
- `.re` 列挙の整合性（#11）

これらの多くは IntelliJ Platform fixture (`myFixture` / `IntelliJPlatformExtension`) を使ってインプロセスで自動化できる。手動 QA に頼り続けるとリグレッション検出が遅れ、機能横断の関係（例: collector の修正が下流の flow / impact / interop に波及する）を見逃しやすい。

本ステアリングでは、推奨アプローチ「1 + 2」（fixture-based integration test + 純粋関数の smoke benchmark）を 1 つの PR として追加する。外部ツール統合（Mermaid CLI / graphviz / `rescript convert`）は CI インフラ変更を伴うため、本ステアリングのスコープ外とする（別 PR で議論）。

## ユーザーストーリー

### US-Tests-01: 6 機能横断の fixture テスト

**プラグイン保守者として**、6 機能の核となる挙動を IntelliJ Platform fixture でテストし、リファクタ時のリグレッションを CI で検出したい。

**受け入れ条件:**

- [ ] `RescriptTypeTargetResolverIntegrationTest`: 5 種類の型定義（alias / record / variant / polymorphic variant / abstract）でカーソル位置の型を解決できる
- [ ] `RescriptNotebookFileEditorIntegrationTest`: 空 Notebook を開き、セル追加 → save → 再 read で内容が round-trip する
- [ ] `RescriptInteropScannerIntegrationTest`: fixture プロジェクトに `.res` ファイルを 1 つ置き、`scan(project)` が期待エントリを返す
- [ ] `RescriptMigrationFinderIntegrationTest`: fixture プロジェクトに `.re` / `.rei` ファイルを置き、`findCandidates(project)` がそれらを返す
- [ ] `RescriptNarrowingHintProviderIntegrationTest`: 設定 OFF 時に `buildHints` 経路ではヒントが 0 件（既存ユニットテストで担保済み）に加え、resolver 注入で 0 件返却が integration 経路でも一貫することを確認

各テストは `IntelliJPlatformExtension` または `RescriptParsingTestExtension` をベースに、`myFixture.configureByText` でファイルを配置する。

### US-Tests-02: 純粋関数の smoke benchmark

**性能リグレッションを早期に検出したい保守者として**、collector / classifier / scanner の主要 pure function に上限緩めの実行時間 assert を入れたい。

**受け入れ条件:**

- [ ] `RescriptSwitchArmCollectorPerfTest`: 50 個の switch を含む 1000 行サンプルを 200ms 以内で処理する
- [ ] `RescriptVariantFlowModelPerfTest`: 5000 行サンプルの buildAtOffset が 1 秒以内に完了する
- [ ] `RescriptInteropScannerPerfTest`: 100KB の合成 `.res` テキストに対する `collectEntriesFromText` が 500ms 以内に完了する
- [ ] `RescriptInteropClassifierPerfTest`: 10000 行スイープが 500ms 以内に完了する

上限値は CI マシンのばらつきを考慮して **緩め**（典型値の 5〜10 倍）に設定し、フレーキー化を避ける。極端な悪化のみ拾う「smoke gate」として位置付ける。

## スコープ外

- 外部ツール統合（Mermaid CLI / graphviz / `rescript convert`）— CI インフラ変更を伴うため別 PR で議論
- LSP モックを介したエンドツーエンドテスト（既存の hover stub で担保済み）
- 並列実行ベンチマーク（パフォーマンス改善側の作業として別途）
- カバレッジラチェット引き上げ（テスト追加に伴う koverVerify の minBound 更新は本ステアリングで行うが、目標値の見直しはリリース時に）

## 受け入れ確認

- [ ] 5 種類の fixture-based integration test が新規追加され、ローカルで `./gradlew test` がグリーン
- [ ] 4 件の smoke benchmark が追加され、ローカルで実行時間を満たす
- [ ] CI で実行されることを確認（既存 `./gradlew test` の対象に含まれる）
- [ ] フレーキーな失敗が発生しない（10 回連続実行で全件成功）— マージ後に手動検証

## 非機能要件

- ベンチマークの上限値は環境依存を考慮して `assert(elapsed < threshold)` 形式とし、`Thread.sleep` のような external timing には依存しない
- fixture テストは既存の `IntelliJPlatformExtension` パターンを踏襲する
- ベンチマーク用のテストデータ生成はテストファイル内で完結させる（リソースファイルの新規追加は最小化）
