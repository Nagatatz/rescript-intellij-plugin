# 要求: Switch Flow ToolWindow の空状態に使い方ヒントを表示する

## 背景

サンドボックステストで「Variant Usage の使い方がわかりません」と報告があった。リポジトリには `Variant Usage` という名前の機能はなく、ユーザーが見ていたのは下記いずれかと推定される:

1. `manual-test-projects/main/src/VariantUsage.res` というフィクスチャファイル名（Rename Variant Constructor intention のクロスファイルテスト用）
2. **Variant Flow Diagram (Switch Flow)** ツールウィンドウ — 開いても「Open a ReScript file to see its switch flow.」「No switch under caret.」のような短いメッセージしか出ず、何をすればよいかわからない

特に (2) は機能を発見した直後のユーザーがほぼ確実に詰まる箇所。空状態のテキストが「使い方」を語っていない。

## ユーザーストーリー

**プラグインに付属する Variant 系機能を初めて触る開発者として**、Switch Flow ツールウィンドウを開いた瞬間に「何をすれば diagram が出るか」が分かることで、ドキュメントを参照せずに最初の体験ができたい。同様に、`VariantUsage.res` のような fixture を見ても、それがプラグイン機能ではなく単なるテスト用 fixture であると見分けたい。

## 受け入れ条件

- [ ] Switch Flow ツールウィンドウを開いて ReScript ファイルがない / `switch` 式がない場合、テキストエリアに **3〜5 行の使い方ヒント** が表示される（短い 1 行の status だけでなく）
- [ ] ヒントは「.res ファイルを開く → `switch` 式にキャレットを置く → 自動的に diagram が出る」を具体的に伝える
- [ ] `manual-test-projects/main/src/VariantUsage.res` の先頭コメントに、これがフィクスチャである旨を明示
- [ ] 既存の動作（diagram があるときの描画）は変更しない
- [ ] 純粋ヘルパー (`emptyStateMessage(reason)`) を切り出し、メッセージ文字列の回帰をテスト

## 制約

- Panel 本体は Swing UI 免除。ヒント文字列の決定ロジックは pure helper に切り出してテスト
- 既存テスト無変更
- sphinx-docs `advanced.md` の Variant Flow セクションは内容変更不要（既に詳しい）
