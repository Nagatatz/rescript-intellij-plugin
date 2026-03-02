# Requirements: C 優先度機能 残り 16 件

## 概要

`docs/product-requirements.md` に残る C 優先度機能 16 件をすべて実装する。前回の C 優先度バッチ 1（#53, #55, #59, #60, #61, #68, #69）と同じステアリングワークフロー・コミット粒度で進める。

## 対象機能

### Batch 2: 中〜高難易度（4 件）

| # | 機能 | カテゴリ | 説明 |
|---|------|---------|------|
| 57 | Scratch File | 実行 | ReScript スクラッチファイルの実行 |
| 58 | REPL | 実行 | インタラクティブ実行環境 |
| 66 | Suggested Refactoring | リファクタリング | コード品質改善のリファクタリングを提案 |
| 104 | JS→ReScript 変換 | Paste | JavaScript コードを ReScript に変換してペースト |

### Batch 3: 高難易度（9 件）

| # | 機能 | カテゴリ | 説明 |
|---|------|---------|------|
| 63 | Inline Variable/Function | リファクタリング | 変数・関数をインライン展開 |
| 65 | Introduce Constant | リファクタリング | リテラル値を定数に抽出 |
| 67 | Dependency Diagram | その他 | モジュール依存関係のダイアグラム生成 |
| 86 | React コンポーネント抽出 | リファクタリング | JSX を新しい React コンポーネントに抽出 |
| 87 | PPX 展開ビュー | ToolWindow | PPX マクロの展開結果をツールウィンドウに表示 |
| 88 | モジュールタイプ実装生成 | Generate | モジュールタイプのスケルトン実装を生成 |
| 105 | 型ホール支援 | Quick Fix | `_` 型ホールに対する候補型の提案 |
| 106 | コメント内コード評価 | Editor | ドキュメントコメント内のコード例を評価・検証 |
| 107 | Worksheet モード | Editor | ファイル全体をインタラクティブに評価するモード |

### Batch 4: 非常に高難易度（3 件）

| # | 機能 | カテゴリ | 説明 |
|---|------|---------|------|
| 62 | Extract Function | リファクタリング | 選択コードを新しい関数に抽出 |
| 64 | Change Signature | リファクタリング | 関数シグネチャの変更と呼び出し側の自動修正 |
| 108 | 型シグネチャ検索 | ナビゲーション | 型シグネチャから関数を逆引き検索 |

## 受け入れ条件

1. 全 16 機能が `plugin.xml`（または該当の `META-INF/rescript-*.xml`）に登録されている
2. 各機能に対応するテストファイルが存在する（UI/LSP 依存のもの以外）
3. `./gradlew clean buildPlugin` が成功する
4. 全テストがパスする
5. KDoc が全クラスに付与されている
6. CLAUDE.md, README.md, sphinx-docs, product-requirements.md が更新されている
7. 各機能が個別コミットされている
8. ロードマップの残り機能数が 0 件に更新されている

## 実装方針

- 全バッチを 1 ブランチ (`feature/c-priority-features-remaining`) で実装する
- 16 機能 × 個別コミット + 1 ドキュメント更新コミット = 17 コミット
- 既存の実装パターン（inspection, intention, tool window 等）に従う
- PSI ベースのテキスト解析を中心にし、LSP 依存は最小限にする
