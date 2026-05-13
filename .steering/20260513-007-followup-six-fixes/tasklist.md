# 20260513-007 タスクリスト

各 Section = 1 commit。順序間の強依存はなく、緑になったセクションから順に main へ積む。

## Section A: README 補強 (Issue #6)

- [x] `manual-test-projects/README.md` の Rename Variant Constructor 行下に VariantUsage.res のクロスファイル fixture 注釈を追加
- [x] `./gradlew ktlintCheck` (README はコード対象外だが念のため)
- [x] コミット: `📝 Clarify VariantUsage.res role in manual-test README`

## Section B: Migration v12 guard (Issue #1)

- [x] `RescriptMigrationConverter` に `probeMajorVersion` (内部) を追加
- [x] `convert()` 冒頭で probe → v12 以上なら早期 actionable エラー
- [x] テスト `RescriptMigrationConverterTest` に parseRescriptMajorVersion の単体テストを追加 ("12.2.0", "rescript 11.1.4", "10.1", 不正出力, 空文字列, build metadata 付き)
- [x] `./gradlew test --tests RescriptMigrationConverterTest` 成功
- [ ] コミット: `🐛 Guard Migration Pilot against ReScript 12 missing convert`

## Section C: Add Missing Arms 2nd-pass (Issue #5)

- [x] `RescriptLspSignatureParser` に `extractBareTypeName` (純関数) を追加
- [x] 新規 `RescriptVariantTypeResolver.resolve(project, typeName)` を `lsp/` に作成 (DumbService ガード + RescriptNameIndex 経由 + RescriptTypeDeclarationParser 流用)
- [x] `RescriptAddMissingArmsDiagnoser` の constructor 抽出経路で 2nd-pass を呼び出す (`resolveByTypeName` lambda 注入)
- [x] テスト追加: `extractBareTypeName` (9 ケース)、`RescriptVariantTypeResolver.matchesTypeHead` (7 ケース)、Diagnoser の bare type-name パス (4 ケース)
- [x] `./gradlew test` で B+C 単体テスト pass
- [ ] コミット: `✨ Resolve variant constructors via stub index when LSP hover returns bare type name`

## Section D: Narrowing binding 横ヒント (Issue #3)

- [x] `RescriptSwitchArmCollector.SwitchArm` に `bindingOffsets: List<Int>` をデフォルト値付きで追加
- [x] collector の pattern スキャンで LIDENT binding 末尾 offset を捕捉 (catch-all bare LIDENT と when guard を除外)
- [x] `RescriptNarrowingHintProvider.buildHints` で binding offset にも個別 hint を出す (binding ごとに hover を再問い合わせ)
- [x] テスト追加: collector で `Some(value)` / `None` / `Pair(left, right)` / wildcard / or-pattern / when guard 各ケース。hint provider で binding hint emit、trivial type filter、multi-arg、bare LIDENT 各ケース
- [ ] sphinx-docs の Narrowing 章を更新 (英語 + `.po` 日本語)
- [x] `./gradlew test` で D 単体テスト pass
- [ ] コミット: `✨ Surface narrowed type next to switch arm bindings`

## Section E: Switch Flow Visual mode (Issue #4)

- [x] 新規 `RescriptVariantFlowGraphView` (JComponent + 純関数 `computeLayout`) を Java2D 描画で実装
- [x] `RescriptVariantFlowPanel` を CardLayout 化、Visual/Source ToggleAction を toolbar に追加 (default Visual)
- [x] 既存 Copy Mermaid/DOT アクションは model から再生成のため両モードで動作 (変更なし)
- [x] empty 状態 hint は textArea/graphView 両方で setDiagram(null) でクリア
- [x] テスト `RescriptVariantFlowGraphViewTest`: 0/1/3/8 arm の computeLayout、wrap 動作、ラベル合成 (8 ケース)
- [ ] sphinx-docs の Switch Flow 章を更新 (英語 + `.po`)
- [x] `./gradlew test` で E 単体テスト pass
- [ ] コミット: `✨ Render Switch Flow Diagram as a visual graph view`

## Section F: ツールウィンドウアイコン赤統一 (Issue #2)

- [x] 新規 SVG 8 種類を `src/main/resources/icons/` に追加 (rescript-flow / -diagram / -dependencies / -impact / -coverage / -interop / -migration / -ppx)
- [x] `plugin.xml` の 8 箇所の `<toolWindow ... icon="...">` を更新 (`AllIcons.*` 参照と `/icons/rescript-toolwindow.svg` 共有を解消)
- [x] CLAUDE.md のレイヤー 3 説明を Narrowing / Flow / Migration / Add Missing Arms について更新
- [ ] スクリーンショット差し替えは不要 (アイコン変更のみ、機能は同じ)
- [ ] `./gradlew ktlintCheck buildPlugin test`
- [ ] コミット: `🎨 Brand tool window icons with ReScript red gradient`

## Section G: マージ準備

- [ ] tasklist 全項目が `[x]` であることを確認
- [ ] DoD Phase 3 セルフチェック (`./gradlew ktlintCheck buildPlugin test` 最終実行)
- [ ] ユーザーに main へのマージ可否を `AskUserQuestion` で確認
- [ ] 承認後、worktree 内で `git checkout main && git merge worktree-20260513-007-followup-six-fixes`
- [ ] セッション終了 (worktree 自動クリーンアップ)
