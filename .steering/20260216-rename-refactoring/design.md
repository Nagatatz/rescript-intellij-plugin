# 設計: リネームリファクタリング

| 項目 | 内容 |
|---|---|
| 機能名 | リネームリファクタリング |
| 作成日 | 2026-02-16 |

## 1. 実装アプローチ

IntelliJ Platform 2025.3 の LSP API は `textDocument/rename` を組み込みサポートしていないため、以下のハイブリッドアプローチで実装する:

1. **`RenameHandler`** をカスタム実装し、Shift+F6 のトリガーをインターセプト
2. **`lsp4jServerClass`** をオーバーライドし、LSP4J の `LanguageServer` インタフェース（`textDocument/rename` 含む）をサーバープロキシに公開
3. **`LspServerManager`** 経由で実行中の LSP サーバーを取得し、`textDocument/rename` リクエストを送信
4. 返却された **`WorkspaceEdit`** を IntelliJ の `Document` API で適用

### リネームフロー

```
ユーザー Shift+F6
    ↓
RescriptRenameHandler.isAvailableOnDataContext()
    ├─ ReScript ファイルか？
    ├─ LSP サーバーが起動しているか？
    └─ カーソル位置が識別子上か？
    ↓
RescriptRenameHandler.invoke()
    ↓
textDocument/prepareRename (オプション)
    ├─ リネーム可能 → 現在の名前・範囲を取得
    └─ リネーム不可 → エラーメッセージ表示して終了
    ↓
IntelliJ リネームダイアログ表示 (Messages.showInputDialog)
    ↓
textDocument/rename (新しい名前を送信)
    ↓
WorkspaceEdit レスポンス受信
    ↓
WorkspaceEditApplier.apply()
    ├─ 各ファイルの TextEdit を Document に適用
    └─ WriteCommandAction 内で実行 (Undo 対応)
```

## 2. 変更するコンポーネント

| ファイル | 変更内容 | 変更種別 |
|---|---|---|
| `refactor/RescriptRenameHandler.kt` | Shift+F6 インターセプト、LSP rename リクエスト送信、WorkspaceEdit 適用 | 新規 |
| `refactor/RescriptNamesValidator.kt` | ReScript 識別子バリデーション | 新規 |
| `lsp/RescriptLspServerDescriptor.kt` | `lsp4jServerClass` オーバーライドを追加 | 修正 |
| `resources/META-INF/plugin.xml` | `renameHandler`, `namesValidator` 登録 | 修正 |

## 3. データ構造の変更

なし。既存の PSI 構造やトークン型への変更は不要。LSP プロトコルの標準型（`RenameParams`, `WorkspaceEdit`, `TextEdit`）のみ使用する。

## 4. 影響範囲の分析

### 直接的な影響

- `RescriptLspServerDescriptor` — `lsp4jServerClass` プロパティの追加。既存の `lspCustomization` には影響しない
- `plugin.xml` — 新しい extension point の追加。既存登録との競合なし

### 間接的な影響

- LSP サーバー起動フロー — `lsp4jServerClass` のオーバーライドにより LSP4J プロキシの生成方法が変わる可能性。既存の semantic tokens、completion 等の機能に影響がないことを確認する必要あり
- Undo/Redo — `WriteCommandAction` で適用するため、IntelliJ 標準の Undo が使えるが、クロスファイル編集の Undo 動作は検証が必要

## 5. 技術的な判断

| 判断項目 | 選択肢 | 採用 | 理由 |
|---|---|---|---|
| リネーム方式 | A: LSP `textDocument/rename` / B: PSI ベースの自前実装 | A | LSP サーバーがクロスファイルのリネームをサポートしており、精度が高い。PSI ベースは軽量パーサーのため式レベルの参照を追跡できない |
| LSP サーバーアクセス | A: `lsp4jServerClass` オーバーライド / B: `LspRequest` カスタムリクエスト / C: 独自 LSP クライアント | A | IntelliJ 公式ドキュメントが推奨する方法。`textDocument/rename` は LSP4J の標準インタフェースに含まれるため、`LanguageServer` をプロキシクラスに指定するだけで利用可能 |
| リネームダイアログ | A: IntelliJ 標準の `Messages.showInputDialog` / B: カスタムダイアログ | A | 標準 UI でユーザーが慣れている。将来的にプレビュー機能が必要になればカスタムに移行可能 |
| prepareRename | A: 使用する / B: 使用しない | A | リネーム不可能な位置でのエラーハンドリングが改善される。サーバーが未対応の場合は graceful にフォールバック |
| 識別子バリデーション | A: `NamesValidator` 実装 / B: バリデーションなし | A | 空文字やキーワードの入力を防止。ReScript の識別子規則に従ったバリデーションを提供 |

### コンポーネント詳細設計

#### RescriptRenameHandler

```kotlin
class RescriptRenameHandler : RenameHandler {
    // isAvailableOnDataContext: ReScriptファイル + LSPサーバー起動中 + 識別子上
    // invoke: prepareRename → ダイアログ → rename → WorkspaceEdit適用
}
```

- `isAvailableOnDataContext()`: `DataContext` からファイルとカーソル位置を取得し、ReScript ファイルかつ LSP サーバーが起動中であることを確認
- `invoke()`: 非同期で LSP リクエストを送信し、結果を `WriteCommandAction` で適用

#### RescriptNamesValidator

```kotlin
class RescriptNamesValidator : NamesValidator {
    // isIdentifier: ReScript の識別子規則 (lident: [a-z_][a-zA-Z0-9_']*, uident: [A-Z][a-zA-Z0-9_']*)
    // isKeyword: ReScript の予約語チェック
}
```

#### lsp4jServerClass のオーバーライド

```kotlin
// RescriptLspServerDescriptor 内
override val lsp4jServerClass: Class<*> = LanguageServer::class.java
```

これにより LSP4J が `LanguageServer` インタフェース全体（`textDocument/rename` 含む）をプロキシに公開する。

#### WorkspaceEdit の適用

`WorkspaceEdit.changes` (Map<URI, List<TextEdit>>) を処理:
1. URI → VirtualFile → Document に変換
2. TextEdit を逆順（後ろから）で適用（オフセットのずれを防止）
3. `WriteCommandAction.runWriteCommandAction` 内で実行（Undo 対応）
4. `FileDocumentManager.saveAllDocuments()` で永続化
