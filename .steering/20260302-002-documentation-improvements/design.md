# Design: ドキュメント改善

## 1. testing.md 更新

`sphinx-docs/dev/testing.md` の「Writing Tests」セクションの後に「Integration Tests」セクションを追加する。

**追加内容:**
- `BasePlatformTestCase` パターンの概要（IDE プラットフォーム上でのテスト）
- ユニットテスト vs インテグレーションテストの使い分けガイド
- 6 つのインテグレーションテストクラスの一覧（Highlighting, Folding, Structure, Indent, Parser, Lexer）
- testData ディレクトリ構成の説明
- コード例（`myFixture.configureByText()` → assertion パターン）

## 2. extending.md 更新

`sphinx-docs/dev/extending.md` の「Common Extension Point Patterns」セクションに以下のパターンを追加する。各パターンは既存の Inspection/Intention/Action パターンと同様の構成（コード例 + plugin.xml 登録）。

### 追加パターン:
1. **Code Vision Provider** — `CodeVisionProvider` インターフェース、`TextCodeVisionEntry` の使い方、plugin.xml 登録
2. **Tool Window** — `ToolWindowFactory` インターフェース、content パネルの構築、plugin.xml 登録
3. **PSI Stub Index** — `StubElementTypeHolder` + `StubIndex` パターン、`StubIndexKey` の定義、`plugin.xml` のスタブ要素型とインデックス登録
4. **LSP カスタムリクエスト** — `LspServerDescriptor` 拡張、カスタムコマンドの送信パターン
5. **Paste Processor** — `CopyPastePreProcessor` / `CopyPastePostProcessor` インターフェース

## 3. contributing.md 更新

「Development Workflow」セクションに「AI-Assisted Development」サブセクションを追加し、以下の参照を含める:

- `.claude/rules/steering-workflow.md` — ステアリングワークフロー
- `.claude/rules/definition-of-done.md` — 5 フェーズ DoD
- `.claude/rules/git-conventions.md` — worktree 運用（git worktree セクション）

## 4. ユーザードキュメント追記

既存のページに追記する形で対応。新規ページは作成しない。

| 機能 | 追記先ページ |
|------|-------------|
| Type Info ToolWindow | `features/advanced.md` |
| `%re()` RegExp Injection | `features/advanced.md`（既存の `%raw()` JavaScript Injection セクションの直後） |
| Problem Highlight Filter | `features/code-analysis.md` |
| Word Selection | `features/code-editing.md` |
| Project View: JS ネスト表示 | `features/advanced.md`（既存の Project View Enhancements セクション） |

各機能の記載は既存の文書スタイル（H2/H3、コード例、設定パス）に従う。

## 5. CLAUDE.md 更新

レイヤー 3 のリストに以下を追記:
- **コメンター** (`commenter/`) — 行/ブロックコメントの Commenter 実装
- **パッケージ依存関係** (`dependencies/`) — npm 依存関係ツリー
- **Code Vision** (`codevision/`) — CodeVision API による関数の型注釈表示

※ `paste/`, `settings/`, `template/`, `util/` は他の機能の説明に含まれているため追記不要。

## 6. 技術的な留意点

- Sphinx ビルド: `cd sphinx-docs && uv sync && make build-all` で検証
- toctree への追加は不要（既存ページへの追記のみ）
- コード変更なし（ドキュメントのみの変更）
