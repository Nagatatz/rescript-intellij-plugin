# C 優先度機能バッチ1 — 要求仕様

## 概要

残り 23 件の C 優先度機能のうち、低〜中難易度の 7 件を一括実装する。

## 対象機能

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 53 | Strip Trailing Spaces | 編集 | 低 |
| 55 | Formatting for Injected | インジェクション | 低 |
| 59 | Grazie Text Extractor | その他 | 低 |
| 60 | Element Signature Provider | その他 | 低 |
| 61 | Index Pattern Builder | インデキシング | 低 |
| 68 | File Include Provider | ナビゲーション | 中 |
| 69 | Editor Floating Toolbar | その他 | 中 |

## 受け入れ条件

### 共通

- [ ] 各機能に対応するテストが存在する
- [ ] 各クラスに英語 KDoc が付与されている
- [ ] `plugin.xml`（または `rescript-*.xml`）に EP 登録済み
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] 全テストパス

### #53 Strip Trailing Spaces

- [ ] ReScript ファイルで行末の余分な空白が保存時に除去される
- [ ] 文字列リテラル内の空白は除去しない

### #55 Formatting for Injected

- [ ] `%raw()` 内の JavaScript コードが ReScript ファイルフォーマット時にフォーマットされる
- [ ] 既存の `RescriptFormattingService` との連携

### #59 Grazie Text Extractor

- [ ] Grazie プラグイン有効時、コメントと文字列の自然言語テキストが文法チェック対象になる
- [ ] オプション依存（Grazie 無しでもプラグインは動作する）

### #60 Element Signature Provider

- [ ] ReScript の宣言要素（let, type, module, external, exception）に一意シグネチャを提供する
- [ ] リファクタリングや要素追跡で使用される

### #61 Index Pattern Builder

- [ ] ReScript ファイルのコメント内 TODO/FIXME パターンとワードインデックスが正しく構築される
- [ ] 既存の `RescriptTodoIndexer` と連携

### #68 File Include Provider

- [ ] `open` 文からファイルインクルード関係を認識する
- [ ] File Include Manager 経由のナビゲーションに寄与する

### #69 Editor Floating Toolbar

- [ ] ReScript ファイルでフローティングツールバーが表示される
- [ ] フォーマット、ビルド、コンパイル済み JS 表示のアクションを含む
