# 要求定義: B 優先度機能一括実装

## 概要

feature research (`.steering/20260221-001-feature-research/research.md`) で B 優先度に分類された 21 機能を実装する。

## 対象機能（21 件）

### パーサー変更不要 `★`（14 件）

| # | 機能 | Extension Point | 難易度 |
|---|------|----------------|--------|
| 24 | Backspace Handler（JSX ペア削除） | `backspaceHandlerDelegate` | 低 |
| 27 | Copy/Paste Pre-Processor（文字列エスケープ） | `copyPastePreProcessor` | 中 |
| 28 | Inspection Suppression（コメント抑制） | `lang.inspectionSuppressor` | 低〜中 |
| 29 | Lookup Char Filter（補完中の文字制御） | `lookup.charFilter` | 低 |
| 31 | Project View Node Decorator（ファイル装飾） | `projectViewNodeDecorator` | 低〜中 |
| 32 | File-Based Index（open 文インデックス） | `fileBasedIndex` | 中 |
| 33 | Predefined Code Style（ReScript Standard） | `predefinedCodeStyle` | 低 |
| 34 | Element Description Provider | `elementDescriptionProvider` | 低 |
| 37 | Paste as JSX（HTML→ReScript JSX 変換） | `copyPastePostProcessor` | 中 |
| 38 | Package Dependencies View（rescript.json） | `toolWindow` | 中 |
| 39 | VCS Code Vision（宣言上の VCS 情報） | `vcs.codeVisionLanguageContext` | 低〜中 |
| 40 | Reader Mode（node_modules 読取専用） | `readerModeMatcher` | 低 |
| 41 | Color Preview in Gutter（色リテラル） | `colorProvider` | 低 |
| 42 | Auto Import Options（open 設定 UI） | `autoImportOptionsProvider` | 低〜中 |

### トークンレベル工夫 `▲`（4 件）

| # | 機能 | Extension Point | 難易度 |
|---|------|----------------|--------|
| 22 | Move Element Left/Right（引数並替） | `moveLeftRightHandler` | 中〜高 |
| 23 | Usage Type Provider（用途別グループ化） | `usageTypeProvider` | 中 |
| 25 | Code Block Support Handler（ブロック間移動） | `codeBlockSupportHandler` | 中 |
| 26 | Split/Join List（1行⇔複数行） | `listSplitJoinContext` | 中 |

### LSP/パーサー依存 `●`（3 件）

| # | 機能 | Extension Point | 難易度 |
|---|------|----------------|--------|
| 30 | Quick Documentation Provider | `lang.documentationProvider` | 中 |
| 35 | Safe Delete（使用箇所確認付き削除） | `refactoring.safeDeleteProcessor` | 中〜高 |
| 36 | Name Suggestion Provider（名前候補） | `nameSuggestionProvider` | 中 |

## 受け入れ条件

- 全 21 機能が実装され、`plugin.xml` に登録されている
- 各機能にユニットテストが存在する
- `./gradlew buildPlugin` が成功する
- `./gradlew test` が成功する
- CLAUDE.md, product-requirements.md, functional-design.md, README.md が更新されている

## 制約事項

- 既存のパーサー（軽量パーサー: トップレベル宣言 + JSX のみ）の範囲内で実装
- LSP 依存機能は `@rescript/language-server` の対応状況に依存
- Safe Delete (#35) と Name Suggestion Provider (#36) は LSP の references/hover を使用するため、LSP 未接続時はフォールバック動作を提供
