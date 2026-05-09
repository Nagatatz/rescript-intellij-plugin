# Hoogle-style Type Signature Search — Tasklist

各 Section は独立にビルド・テストを通過し、main にマージできる粒度。

## Section A: 型 AST + Parser (PR-1 サイズ)

- [ ] T1: `navigation/RescriptTypeAst.kt` — sealed class + sub-types
- [ ] T2: `navigation/RescriptTypeParser.kt` — pure object, char-based tokenizer + LL parser
- [ ] T3: `navigation/RescriptTypeParserTest.kt` — 50+ ケース (各構文ノード × 失敗 × `=>` 始まり)
- [ ] T4: ローカル ktlint / KDoc / test 緑 → コミット

## Section B: Unifier (PR-2 サイズ、A 依存)

- [ ] T5: `navigation/RescriptTypeUnifier.kt` — pure object + `MatchScore` enum
- [ ] T6: `navigation/RescriptTypeUnifierTest.kt` — 30+ ケース (EXACT / TVAR_MATCH / PARTIAL / MISMATCH の各分岐)
- [ ] T7: ローカル ktlint / KDoc / test 緑 → コミット

## Section C: Search Hit + Renderer + Contributor 差し替え (PR-3 サイズ、A+B 依存)

- [ ] T8: `navigation/RescriptTypeSignatureSearchHit.kt` — データクラス
- [ ] T9: `navigation/RescriptTypeSignatureCellRenderer.kt` — `ColoredListCellRenderer`
- [ ] T10: `navigation/RescriptTypeSignatureSearchContributor.kt` — fetchWeightedElements / processSelectedItem / getElementsRenderer を新 AST/Unifier 経由に書き換え
- [ ] T11: 既存 contributor 内部の旧 helper (`looksLikeTypeQuery`, `tokenizeSignature`, `matchSignature`, `extractTypeAnnotation`, `rankMatch`) を削除
- [ ] T12: 既存 contributor の単体テスト (もしあれば) を削除 or 更新
- [ ] T13: ローカル ktlint / KDoc / test / koverVerify / verifyPluginStructure 緑 → コミット

## Section D: ドキュメント (PR-4 サイズ)

- [ ] T14: CLAUDE.md レイヤー 3 に Hoogle-style search の段落追加
- [ ] T15: docs/repository-structure.md `navigation/` 行に新クラス名を追記
- [ ] T16: README.md "Navigation" カテゴリの該当箇所を更新 (構造ベース検索になった旨)
- [ ] T17: sphinx-docs/user/features/navigation.md (もしくは同等) に "Type Signature Search" セクションを追加 + .po 同期 + `make build-ja` 緑

## マージ

- [ ] AskUserQuestion でマージ可否確認
- [ ] main へ fast-forward マージ & origin に push
- [ ] CI 緑を確認
