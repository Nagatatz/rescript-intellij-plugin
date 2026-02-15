# 要求定義: リネームリファクタリング

| 項目 | 内容 |
|---|---|
| 機能名 | リネームリファクタリング |
| 作成日 | 2026-02-16 |
| ステータス | 計画中 |

## 1. 背景と目的

### 背景

ReScript IntelliJ Plugin は LSP 経由で補完・定義ジャンプ・参照検索などの言語機能を提供しているが、リネームリファクタリングは未実装である。IntelliJ Platform 2025.3 の LSP API は `textDocument/rename` をサポートしておらず、カスタム実装が必要。

### 目的

- ユーザーが Shift+F6 (または右クリック → Refactor → Rename) で変数・関数・型・モジュール名をリネームできるようにする
- LSP サーバー (`@rescript/language-server`) の `textDocument/rename` メソッドを活用し、クロスファイルのリネームを実現する

## 2. 変更・追加する機能の説明

LSP サーバーの `textDocument/rename` プロトコルを利用したリネームリファクタリング機能。

- **トリガー**: Shift+F6、右クリックメニュー「Refactor → Rename」
- **対象**: 変数、関数、型、モジュール、レコードフィールド、バリアントなど（LSP サーバーがサポートする範囲）
- **範囲**: 単一ファイル内およびクロスファイル（LSP サーバーの `WorkspaceEdit` レスポンスに基づく）
- **UI**: IntelliJ 標準のリネームダイアログ（新しい名前を入力 → プレビュー → 適用）

## 3. ユーザーストーリー

| # | ユーザー | 操作 | 期待する結果 |
|---|---|---|---|
| 1 | 開発者 | `.res` ファイル内の変数名にカーソルを置き Shift+F6 を押す | リネームダイアログが表示される |
| 2 | 開発者 | 新しい名前を入力して Enter を押す | 変数名とその参照箇所がすべてリネームされる |
| 3 | 開発者 | モジュール名をリネームする | 他のファイルの参照もリネームされる |
| 4 | 開発者 | 無効な識別子名を入力する | エラーメッセージが表示される |
| 5 | 開発者 | LSP サーバーが利用できない状態でリネームを試みる | 適切なエラーメッセージが表示される |

## 4. 受け入れ条件

- [ ] `.res` / `.resi` ファイルで Shift+F6 によりリネームダイアログが表示される
- [ ] LSP サーバーの `textDocument/rename` を呼び出し、返された `WorkspaceEdit` を適用できる
- [ ] 単一ファイル内のリネームが正しく動作する
- [ ] クロスファイルのリネームが正しく動作する（LSP サーバーがサポートする範囲）
- [ ] LSP サーバーが利用不可の場合、適切なエラーメッセージを表示する
- [ ] `./gradlew buildPlugin` がエラーなしで完了する
- [ ] `./gradlew test` が全件パスする

## 5. 制約事項

- IntelliJ Platform 2025.3 の LSP API は `textDocument/rename` を直接サポートしていないため、カスタム `RenameHandler` を実装する必要がある
- リネームの精度は `@rescript/language-server` の実装に依存する
- LSP サーバーが起動していない状態ではリネーム機能は動作しない
- `textDocument/prepareRename` がサポートされている場合はリネーム可能かの事前チェックに利用する

## 6. 関連ドキュメント

- `docs/product-requirements.md` — プロダクト要求定義書
- `docs/functional-design.md` — 機能設計書
- `docs/architecture.md` — 技術仕様書
- [IntelliJ Platform SDK: Rename Refactoring](https://plugins.jetbrains.com/docs/intellij/rename-refactoring.html)
- [LSP Specification: textDocument/rename](https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#textDocument_rename)
