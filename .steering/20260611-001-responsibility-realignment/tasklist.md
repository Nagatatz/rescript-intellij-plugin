# タスクリスト: 責務再配置 (Phase 3)

セクション間依存: なし (1〜3 は独立)。4 (docs) は最後。
各セクション = 1 コミット = 独立にビルド・テスト通過可能な単位。

## セクション 0: セットアップ

- [ ] `git fetch origin` + main の ahead/behind 確認
- [ ] `EnterWorktree` で worktree 作成、`pwd` / `git rev-parse --show-toplevel` で編集パス確認
- [ ] `docs/product-requirements.md` に #129 / #130 を 🚧 付きで追補 (最初のコミットに含める)

## セクション 1: dead code 削除

- [ ] 削除直前に grep で `IntelliJPlatformExtensionWithContentRoot` の参照 0 を再確認
- [ ] `src/test/kotlin/com/rescript/plugin/IntelliJPlatformExtensionWithContentRoot.kt` を削除
- [ ] `docs/repository-structure.md` §2.2 の heavy fixture 言及を削除
- [ ] `./gradlew test` green
- [ ] コミット: `🗑️ Remove unused IntelliJPlatformExtensionWithContentRoot test fixture`

## セクション 2: RescriptLspUtils facade 解体 (#129)

- [ ] `RescriptLspUtilsTest` のパース系 16 ケースをパーサーテスト側と 1 件ずつ突合 (入力文字列 grep)、欠落分を移設
- [ ] intention 5 ファイル + `RescriptMissingArmsBuilderTest` をパーサー直接参照に書き換え
- [ ] `RescriptLspUtils` から委譲メソッド・typealias (L146-193) を削除、KDoc 調整
- [ ] `RescriptLspUtilsTest` からパース系 16 ケースを削除 (URI 変換 4 ケースは残す)
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Dissolve RescriptLspUtils parse facade into direct parser references`

## セクション 3: RescriptTypeDeclarationParser の lang/ 移動 (#130)

- [ ] `git mv` で main + test を `lang/` へ移動、package 行修正
- [ ] 参照 7 main + 2 test ファイルの import 書き換え
- [ ] build.gradle.kts の kover 除外に当該クラスが含まれるか確認し、含まれていれば該当行を削除
- [ ] `./gradlew ktlintCheck test koverVerify` green
- [ ] コミット: `♻️ Move RescriptTypeDeclarationParser from generate to lang`

## セクション 4: ドキュメント同期

- [ ] `docs/repository-structure.md`: lang/ 行に追記、generate/ 行から再パース言及を削除
- [ ] CLAUDE.md: `RescriptTypeDeclarationParser` 言及箇所の変更要否を確認
- [ ] `docs/product-requirements.md`: #129 / #130 を削除
- [ ] sphinx-docs: 更新なしの確認のみ (機能不変)
- [ ] コミット: `📝 Sync docs for Phase 3 responsibility realignment`

## マージ前検証 (DoD Phase 3〜4)

- [ ] `./gradlew ktlintCheck clean buildPlugin test koverVerify verifyPluginStructure` green (minBound 86 維持)
- [ ] `koverHtmlReport` で `lang/RescriptTypeDeclarationParser` のカバレッジ実測確認
- [ ] tasklist 全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認
- [ ] main へマージ → ブランチ削除 → push

## テスト免除の記載

免除対象なし (新規クラスなし。変更は参照書き換え・移動・削除のみで、既存テストが回帰検出器)。
