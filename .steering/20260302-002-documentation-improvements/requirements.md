# Requirements: ドキュメント改善

## 背景

プロジェクト全体のドキュメントカバレッジを調査した結果、以下の改善の余地が特定された。ユーザードキュメント（Sphinx）は当初 35-40 機能が未文書化と推定されていたが、精査の結果ほとんどの機能がすでにカバーされており、真に不足しているのは少数の機能のみ。

## 受け入れ条件

### 1. 開発者ガイド: testing.md にインテグレーションテストセクションを追加

- `BasePlatformTestCase` パターンの解説がある
- 6 つのインテグレーションテストクラスの概要が記載されている
- testData ディレクトリの構成と使い方が記載されている
- ユニットテスト vs インテグレーションテストの使い分けが記載されている

### 2. 開発者ガイド: extending.md に追加パターンを記載

以下のパターンが記載されている:
- Code Vision Provider
- Tool Window
- PSI Stub Index
- LSP カスタムリクエスト
- Paste Processor

### 3. 開発者ガイド: contributing.md にステアリングワークフロー参照を追加

- ステアリングワークフロー（`.steering/` ディレクトリ）への参照がある
- 5 フェーズ DoD への参照がある
- git worktree 運用への参照がある

### 4. ユーザードキュメント: 未文書化機能の追加

以下の機能が `sphinx-docs/user/features/` のいずれかのページに記載されている:
- Type Info ToolWindow
- `%re()` RegExp Injection
- Problem Highlight Filter（node_modules 等のハイライト抑制）
- Word Selection（文字列・括弧・コメントの選択拡大/縮小）
- Project View: コンパイル済み JS のネスト表示・灰色化

### 5. CLAUDE.md: パッケージ網羅性の向上

`プロジェクト構成` セクションで `@docs/repository-structure.md` を参照しているため、実質的に網羅済み。ただし、レイヤー 3 のリストに明示されていないパッケージを追記:
- `commenter/`
- `dependencies/`
- `codevision/`

## スコープ外

- 大規模なユーザードキュメントの再構成
- 新規 Sphinx ページの作成（既存ページへの追記で対応）
- KDoc の追加（95.8% カバレッジで十分）
- README.md の更新（現状正確）
