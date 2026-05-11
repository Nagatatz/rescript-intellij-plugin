# Hoogle-style Type Signature Search — Tasklist

各 Section は独立にビルド・テストを通過し、main にマージできる粒度。

## Section A: 型 AST + Parser (PR-1 サイズ)

- [x] T1: `navigation/RescriptTypeAst.kt` — sealed class + sub-types
- [x] T2: `navigation/RescriptTypeParser.kt` — pure object, char-based tokenizer + LL parser
- [x] T3: `navigation/RescriptTypeParserTest.kt` — 50+ ケース (各構文ノード × 失敗 × `=>` 始まり)
- [x] T4: ローカル ktlint / KDoc / test 緑 → コミット

## Section B: Unifier (PR-2 サイズ、A 依存)

- [x] T5: `navigation/RescriptTypeUnifier.kt` — pure object + `MatchScore` enum
- [x] T6: `navigation/RescriptTypeUnifierTest.kt` — 30+ ケース (EXACT / TVAR_MATCH / PARTIAL / MISMATCH の各分岐)
- [x] T7: ローカル ktlint / KDoc / test 緑 → コミット

## Section C: Search Hit + Renderer + Contributor 差し替え (PR-3 サイズ、A+B 依存)

- [x] T8: `navigation/RescriptTypeSignatureSearchHit.kt` — データクラス
- [x] T9: `navigation/RescriptTypeSignatureCellRenderer.kt` — `ColoredListCellRenderer`
- [x] T10: `navigation/RescriptTypeSignatureSearchContributor.kt` — fetchWeightedElements / processSelectedItem / getElementsRenderer を新 AST/Unifier 経由に書き換え
- [x] T11: 既存 contributor 内部の旧 helper (`looksLikeTypeQuery`, `tokenizeSignature`, `matchSignature`, `extractTypeAnnotation`, `rankMatch`) を削除
- [x] T12: 既存 contributor の単体テスト (もしあれば) を削除 or 更新
- [x] T13: ローカル ktlint / KDoc / test / koverVerify / verifyPluginStructure 緑 → コミット

## Section D: ドキュメント (PR-4 サイズ)

- [x] T14: CLAUDE.md レイヤー 3 に Hoogle-style search の段落追加
- [x] T15: docs/repository-structure.md `navigation/` 行に新クラス名を追記
- [x] T16: README.md "Navigation" カテゴリの該当箇所を更新 (構造ベース検索になった旨)
- [x] T17: sphinx-docs/user/features/advanced.md "Type Signature Search" セクションを更新 (Native バッジ + Match Tiers + Limitations) + .po 同期 + `make build-ja` 緑

## マージ

- [x] AskUserQuestion でマージ可否確認
- [x] main へ fast-forward マージ & origin に push
- [x] CI 緑を確認 (CodeQL / Docs / CI all green on edd3028)
