# Requirements: Qodana レポート指摘事項の修正

## 概要

GitHub Actions の Qodana スキャン（run #22093016636）で検出された 17 件の問題を修正する。

- Critical: 3件
- High: 10件
- Moderate: 4件

## 問題一覧

| # | 重要度 | 種別 | ファイル | 内容 |
|---|--------|------|----------|------|
| 1-3 | Critical | deprecated API | `RescriptConfigurable.kt` L26,L37 / `RescriptSettingsEditor.kt` L35 | `addBrowseFolderListener` 4引数版が deprecated for removal |
| 4-8 | High | unused symbol | `RescriptTokenTypes.kt` L302,267,275,189,241 | `BACKSLASH`, `SHARP`, `AMPERSAND`, `SHARPSHARP`, `SINGLE_QUOTE` が未使用 |
| 9-11 | High | string capitalization | `RescriptFormattingService.kt` L34 / `RescriptConfigurable.kt` L38 / `RescriptMissingConfigInspection.kt` L29 | 文字列先頭の大文字小文字 |
| 12 | High | redundant nullable | `RescriptLineIndentProvider.kt` L23 | 戻り値型 `String?` だが null を返さない |
| 13 | High | readResolve missing | `RescriptLanguage.kt` L5 | Serializable object に readResolve がない |
| 14-16 | Moderate | multi-dollar interpolation | `RescriptCodeStyleSettingsProvider.kt` L42,57 | `${'$'}` を multi-dollar interpolation で簡略化可能 |
| 17 | Moderate | if-null foldable | `RescriptRunConfiguration.kt` L53 | if-null パターンを Elvis 演算子に変換可能 |

## 受け入れ条件

- 全 17 件の指摘を修正する
- `./gradlew clean buildPlugin` が成功する
- 機能的な挙動に変更がないこと
