# 要求定義: S 優先度機能実装

## 概要

ロードマップの S 優先度6件を一括実装する。

## 対象機能

| # | 機能 | 難易度 |
|---|------|--------|
| 110 | GitHub エラーレポート | 低 |
| 84 | Parameter Info Handler | 中 |
| 45 | Go to Implementation | 中 |
| 70 | Pipe ⇔ 関数呼び出し変換 | 中 |
| 76 | インターフェース公開/非公開 | 中 |
| 83 | 型ミスマッチインラインヒント | 中 |

## 機能要件

### #110 GitHub エラーレポート
- 未処理例外発生時にユーザーが GitHub Issues に報告できる
- ブラウザベース方式: スタックトレース・環境情報を含む Issue URL を生成し、ユーザーのブラウザで開く
- `com.intellij.errorHandler` Extension Point を使用

### #84 Parameter Info Handler
- 関数呼び出し時に Ctrl+P でラベル付き引数のパラメータ情報を表示
- LSP の Signature Help を活用（IntelliJ 2025.3+ LSP API が自動サポートする可能性あり）
- 自動サポートされない場合のみカスタムハンドラを実装

### #45 Go to Implementation
- `.resi` インターフェースファイルから対応する `.res` 実装ファイルの宣言にジャンプ
- 既存の `RescriptGotoSuperHandler`（.res → .resi）の逆方向
- Ctrl+Alt+B または Navigate > Go to Implementation

### #70 Pipe ⇔ 関数呼び出し変換
- `arr->Array.map(f)` → `Array.map(arr, f)` への変換（Intention Action）
- `Array.map(arr, f)` → `arr->Array.map(f)` への逆変換（Intention Action）
- Alt+Enter から呼び出し可能

### #76 インターフェース公開/非公開
- `.res` ファイルの宣言を `.resi` に追加して公開する Intention
- `.resi` ファイルの宣言を削除して非公開にする Intention
- `.resi` が存在しない場合は利用不可

### #83 型ミスマッチインラインヒント
- 型エラー箇所に expected/actual の型情報をインライン表示
- 既存の Error Lens インフラを拡張
- ReScript コンパイラのエラーメッセージから型情報をパース
