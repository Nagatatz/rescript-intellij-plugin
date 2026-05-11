# Cross-file Variant Constructor Rename — Tasklist

各セクションは独立にビルド・テストを通過し `main` にマージできる粒度。順序は推奨だが、Section A だけマージしても緑のはずなので段階的に push 可。

## Section A: Classifier + Occurrence (PR-1 サイズ)

- [x] T1: `intention/RescriptConstructorOccurrence.kt` — データクラス + `Kind` enum
- [x] T2: `intention/RescriptConstructorOccurrenceClassifier.kt` — pure object
- [x] T3: `intention/RescriptConstructorOccurrenceClassifierTest.kt` — 30+ ケース
- [x] T4: ローカル ktlint / KDoc / test 緑、コミット

## Section B: Finder (PR-2 サイズ、A 依存)

- [x] T5: `intention/RescriptConstructorOccurrenceFinder.kt` — `PsiSearchHelper` ベース、internal helper を pure に切り出してテスト可能にする
- [x] T6: `intention/RescriptConstructorOccurrenceFinderTest.kt` — 純粋ヘルパで複数ファイル相当のテキストを検証
- [x] T7: ローカル ktlint / KDoc / test 緑、コミット

## Section C: Intention + 登録 (PR-3 サイズ、A+B 依存)

- [x] T8: `intention/RescriptRenameVariantConstructorIntention.kt` — `PsiElementBaseIntentionAction` 実装
- [x] T9: `plugin.xml` に `<intentionAction>` 登録
- [x] T10: `build.gradle.kts` kover excludes に Intention クラスを追加
- [x] T11: ローカル ktlint / KDoc / test / koverVerify 緑、コミット

## Section D: ドキュメント (PR-4 サイズ)

- [x] T12: CLAUDE.md レイヤー 3 に Rename Variant Constructor Intention の段落追加
- [x] T13: docs/repository-structure.md `intention/` 行に新クラス名を追記
- [x] T14: README.md "Code Editing" / "Refactoring" カテゴリに項目追加
- [x] T15: sphinx-docs/user/features/code-editing.md に解説追加 + .po 同期 + `make build-ja` 緑

## マージ

- [x] AskUserQuestion でマージ可否確認
- [x] main へ fast-forward マージ & origin に push
- [x] CI 緑を確認
