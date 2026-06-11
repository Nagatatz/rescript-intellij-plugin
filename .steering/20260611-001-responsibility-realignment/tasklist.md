# タスクリスト: 責務再配置 (Phase 3)

セクション間依存: なし (1〜3 は独立)。4 (docs) は最後。
各セクション = 1 コミット = 独立にビルド・テスト通過可能な単位。

## セクション 0: セットアップ

- [x] `git fetch origin` + main の ahead/behind 確認 (0/0)
- [x] `EnterWorktree` で worktree 作成、`pwd` / `git rev-parse --show-toplevel` で編集パス確認
- [x] `docs/product-requirements.md` に #129 / #130 を 🚧 付きで追補 (B 優先度のため #128 (C) より上に配置)

## セクション 1: dead code 削除 → **中止 (実装時判断)**

- [x] 削除直前の grep で**新事実を発見**: コード参照は 0 だが、`docs/good-first-issues.md` Issue #9 (Phase 0 で v1-followups からマージ、2026-06-10) が「`RescriptWorkspaceDiscovery` の heavy-fixture テストをこの fixture で書く」コントリビュータタスクとして本クラスを明示参照している
- [x] **削除を中止**: 文書化された将来の利用予定があり、削除すると準備済みの good-first-issue を壊す。repository-structure.md の heavy fixture 言及も従って維持
- [x] 記録: 一次調査の「参照 0 = dead code」判定はコード走査のみに基づき、good-first-issues.md (調査当時は未マージブランチ上) を見ていなかった。削除前の再 grep を docs/ まで広げたことで検出 — audit-tasks.md の二段検証が機能した事例

## セクション 2: RescriptLspUtils facade 解体 (#129)

- [x] `RescriptLspUtilsTest` のパース系 16 ケースをパーサーテスト側と 1 件ずつ突合 — 全件に等価または上位ケースを確認 (例: `(~name, ~age, unit)` 複合は「multiple labeled」+「skips non-labeled」の 2 ケースで被覆、`Some(int) | None` は `Loading | Success(data) | Error(string)` + `| A | B(int)` で被覆)。**移設不要**
- [x] intention 5 ファイル + `RescriptMissingArmsBuilderTest` をパーサー直接参照に書き換え (KDoc の @see も同時更新)
- [x] `RescriptLspUtils` から委譲メソッド・typealias (旧 L146-193) を削除、KDoc を LSP 通信専任の記述に調整 (~194 → 146 行)
- [x] `RescriptLspUtilsTest` からパース系 16 ケースを削除 (URI 変換 4 ケースは残し、KDoc でパーサーテストへ誘導)
- [x] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Dissolve RescriptLspUtils parse facade into direct parser references`

## セクション 3: RescriptTypeDeclarationParser の lang/ 移動 (#130)

- [x] `git mv` で main + test を `lang/` へ移動、package 行修正
- [x] import 書き換え — 実態は設計時見積りより広く、**generate パッケージ内の同一パッケージ参照に import 追加が必要**: main 7 + test 7 ファイルに `lang.{RescriptTypeDeclarationParser,TypeShape,VariantConstructor,RecordField}` を使用シンボルに応じて追加。`lsp/RescriptVariantTypeResolver` は import 切替 (lsp→generate 依存解消)、`util/RescriptRegexPatterns` は KDoc @see のみ更新 (実依存ではなかった)
- [x] build.gradle.kts の kover 除外確認: `com.rescript.plugin.generate.*` の丸ごと除外であり個別行なし → 変更不要 (lang 移動で自動的にカバレッジ対象化)。PIT は明示クラスリスト (RescriptPaths/RescriptRegexPatterns のみ) のため影響なし
- [x] `./gradlew ktlintCheck` green。`--tests` フィルタ付き実行では koverVerify が「フィルタ分のカバレッジしかない」状態で誤って落ちることを確認 — カバレッジ検証はフルテスト実行とセットでのみ意味を持つ (マージ前フルチェーンで実施)
- [ ] コミット: `♻️ Move RescriptTypeDeclarationParser from generate to lang`

## セクション 4: ドキュメント同期

- [x] `docs/repository-structure.md`: lang/ 行に `RescriptTypeDeclarationParser` 追記、generate/ 行から再パース言及を削除 (代表クラスを実在のものに差し替え)
- [x] CLAUDE.md: Add Missing Switch Arms 段落の `RescriptLspUtils.parseVariantConstructors` を `RescriptLspSignatureParser.parseVariantConstructors` に更新 (facade 解体の追従)。`RescriptTypeDeclarationParser` 言及はパッケージ非依存のため変更不要
- [x] `docs/product-requirements.md`: #129 / #130 を削除 (リファクタリング候補は残り #128 のみ)
- [x] sphinx-docs: 更新なしの確認のみ (機能不変)
- [ ] コミット: `📝 Sync docs for Phase 3 responsibility realignment`

## マージ前検証 (DoD Phase 3〜4)

- [ ] `./gradlew ktlintCheck clean buildPlugin test koverVerify verifyPluginStructure` green (minBound 86 維持)
- [ ] `koverHtmlReport` で `lang/RescriptTypeDeclarationParser` のカバレッジ実測確認
- [ ] tasklist 全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認
- [ ] main へマージ → ブランチ削除 → push

## テスト免除の記載

免除対象なし (新規クラスなし。変更は参照書き換え・移動・削除のみで、既存テストが回帰検出器)。
