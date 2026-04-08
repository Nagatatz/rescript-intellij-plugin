# タスクリスト: Sphinx ドキュメント改善

## #10 機能前提条件バッジ
- [x] 全8機能ページにバッジを追加（navigation, code-completion, code-editing, code-analysis, advanced, run-build, testing, syntax-highlighting）
- [x] .po ファイル更新・翻訳

## #3 アーキテクチャ図
- [x] `sphinx-docs/_static/img/diagrams/` ディレクトリ作成
- [x] architecture-overview.mmd 作成
- [x] lsp-sequence.mmd 作成
- [x] psi-tree-flow.mmd 作成
- [x] sphinxcontrib-mermaid 拡張を追加（Node.js 不要の代替方式）
- [x] dev/architecture.md に Mermaid ディレクティブを追加
- [x] .po ファイル更新・翻訳

## #8 インタラクティブチュートリアル強化
- [x] quickstart.md をステップバイステップ形式に書き換え
- [x] .po ファイル更新・翻訳

## #7 バージョン別機能マトリクス
- [x] user/version-matrix.md 新規作成
- [x] user/index.md の toctree に追加
- [x] .po ファイル更新・翻訳

## #9 検索メタデータ改善
- [x] user/ 配下の全ページに html_meta frontmatter 追加
- [x] dev/ 配下の全ページに html_meta frontmatter 追加

## #4 チートシート
- [x] user/cheatsheet.md 新規作成
- [x] user/index.md の toctree に追加
- [x] custom.css に @media print ブロック追加
- [x] .po ファイル更新・翻訳

## #5 レシピ集
- [x] user/recipes/index.md 新規作成
- [x] user/recipes/create-react-component.md 新規作成
- [x] user/recipes/find-dead-code.md 新規作成
- [x] user/recipes/setup-monorepo.md 新規作成
- [x] user/recipes/debug-rescript.md 新規作成
- [x] user/recipes/convert-from-typescript.md 新規作成
- [x] user/recipes/optimize-imports.md 新規作成
- [x] user/index.md の toctree に追加
- [x] .po ファイル更新・翻訳

## #6 VSCode 移行ガイド
- [x] user/migration-from-vscode.md 新規作成
- [x] user/index.md の toctree に追加
- [x] .po ファイル更新・翻訳

## 検証
- [x] `make build-all` 成功
- [x] `make linkcheck` 成功

## マージ
- [ ] main にマージ
