# 要件定義書: インフラ改善 — Regex キャッシュ・統一・ファイル分割

## 概要

プロダクト要求定義書 #115, #116, #117 のインフラリファクタリングを実施する。

## 背景

コードベースの成長に伴い以下の技術的負債が蓄積している：

1. **#115 Regex インスタンスキャッシュ** — 関数内で `Regex(...)` を毎回インスタンス化している箇所が約48箇所あり、不要なオブジェクト生成が繰り返されている
2. **#116 重複 Regex パターン統一** — LIDENT, LABELED_PARAM, OPEN_PATTERN 等の意味的に同一の正規表現パターンが複数ファイルに重複定義されている
3. **#117 長大ファイル分割** — 7ファイルが300行超であり、単一ファイルの責務が過大になっている

## 対象範囲

### #115: Regex インスタンスキャッシュ（優先度 A）

**現状:** 全103箇所の Regex 使用のうち約48箇所がインライン（関数呼び出し毎に再生成）

**要件:**
- 関数内でインスタンス化されている Regex を companion object 定数に移動する
- ただし、動的パターン（`Regex.escape(name)` を含む等）は除外する
- 既にcompanion object 定数になっている約55箇所は変更不要

**主要対象ファイル（インライン Regex が多い順）:**
| ファイル | インライン数 |
|---------|------------|
| RescriptCommentEvalProvider.kt | 18 |
| RescriptPasteAsRescriptProcessor.kt | 6 |
| RescriptTypeDeclarationParser.kt | 3 |
| RescriptUnwrapDescriptor.kt | 3 |
| RescriptExpressionTypeProvider.kt | 2 |
| RescriptPasteAsJsxProcessor.kt | 2 |
| RescriptSignatureSyncInspection.kt | 2 |
| RescriptAddTypeAnnotationIntention.kt | 2 |
| その他（各1箇所） | ~10 |

### #116: 重複 Regex パターン統一（優先度 B）

**現状:** 以下のパターンが複数ファイルで重複定義されている

| パターン | 重複箇所 |
|---------|---------|
| LIDENT（小文字識別子） `^[a-z_][a-zA-Z0-9_']*$` | RescriptNamesValidator, RescriptExtractVariableUtil |
| UIDENT（大文字識別子） `^[A-Z][a-zA-Z0-9_']*$` | RescriptNamesValidator (のみだが共有価値あり) |
| LABELED_PARAM（ラベル付き引数） `~(\w+)...` | RescriptChangeSignatureHandler, RescriptLspUtils, RescriptGenerateDocCommentIntention, RescriptGenerateModuleImplAction |
| OPEN_PATTERN（open文） `^open\s+...` | RescriptDependencyDiagramProvider, RescriptFileIncludeProvider, RescriptImportUtil, RescriptWorksheetRunner |
| WHITESPACE `\s+` | RescriptTypeMismatchParser, RescriptExtractVariableUtil, RescriptRunUtils |

**要件:**
- `com.rescript.plugin.util.RescriptRegexPatterns` ユーティリティオブジェクトを新設する
- 意味的に共通のパターンを集約する
- 各ファイルからの参照を共通パターンへ書き換える
- 注意: LABELED_PARAM は使用コンテキストによりキャプチャグループが異なるため、完全統一が難しい場合は基本パターンのみ共有する

### #117: 長大ファイル分割（優先度 B）

**現状:** 300行超のファイル7件

| ファイル | 行数 | 分割推奨度 |
|---------|------|----------|
| RescriptTokenTypes.kt | 442 | 高 |
| RescriptJsonCodeGenerator.kt | 442 | 中 |
| RescriptParser.kt | 425 | 中 |
| RescriptDocumentationProvider.kt | 403 | 中 |
| RescriptUnwrapDescriptor.kt | 370 | 高 |
| RescriptConfigurable.kt | 343 | 低（UI — スキップ） |
| RescriptLspUtils.kt | 317 | 中 |

**要件:**
- 分割推奨度「高」「中」の6ファイルを対象に分割を行う
- RescriptConfigurable.kt（Swing UI）は分割対象外とする
- 分割後も外部API（public メソッド/プロパティ）の互換性を維持する
- plugin.xml の登録変更が必要な場合は同時に更新する

## 受け入れ条件

1. インライン Regex が companion object 定数に移動されている（動的パターンを除く）
2. 重複パターンが `RescriptRegexPatterns` に集約されている
3. 300行超のファイル（UI除く）が適切に分割されている
4. `./gradlew clean buildPlugin` が成功する
5. 既存テストがすべてパスする
6. 機能的な振る舞いに変更がない（純粋なリファクタリング）
