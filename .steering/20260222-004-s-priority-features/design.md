# 設計: S 優先度機能実装

## #110 GitHub エラーレポート

### アーキテクチャ
- `ErrorReportSubmitter` を継承した `RescriptErrorReporter` を作成
- ブラウザベース方式: GitHub Issues の URL を生成しブラウザで開く

### クラス設計
- `com.rescript.plugin.RescriptErrorReporter`
  - `getReportActionText()`: "Report to ReScript Plugin GitHub"
  - `submit()`: スタックトレース・環境情報を含む GitHub Issue URL を生成、ブラウザで開く

### 登録
- `plugin.xml`: `<errorHandler implementation="com.rescript.plugin.RescriptErrorReporter"/>`

---

## #84 Parameter Info Handler

### 調査結果
- IntelliJ 2025.3+ の LSP API は `textDocument/signatureHelp` を自動サポート
- rescript-language-server は signatureHelp に対応済み
- **追加実装不要の可能性が高い** → 動作確認し、不足があればカスタムハンドラを実装

### フォールバック設計（必要な場合のみ）
- `com.rescript.plugin.completion.RescriptParameterInfoHandler`
  - `ParameterInfoHandler<PsiElement, Any>` を実装
  - LSP の signatureHelp レスポンスをネイティブ UI に変換

---

## #45 Go to Implementation

### アーキテクチャ
- 既存の `RescriptGotoSuperHandler` を双方向化
- `.res` → `.resi`（既存の Super 方向）と `.resi` → `.res`（新規 Implementation 方向）の両方を1つのハンドラで処理

### クラス設計
- `RescriptGotoSuperHandler` を拡張: `.resi` ファイルでの呼び出し時に `.res` の対応宣言にジャンプ
- `com.rescript.plugin.navigation.RescriptGotoImplementationAction` を新規作成（Navigate メニュー登録用）

### 登録
- `plugin.xml`: `<action>` で Navigate メニューに登録

---

## #70 Pipe ⇔ 関数呼び出し変換

### アーキテクチャ
- 2つの `PsiElementBaseIntentionAction` を作成
- テキストベースのパターンマッチングと変換

### クラス設計
- `com.rescript.plugin.intention.RescriptConvertPipeToFunctionCallIntention`
  - `isAvailable()`: カーソル位置に `->` パイプ演算子があるか検出
  - `invoke()`: `expr->Module.func(args)` → `Module.func(expr, args)` に変換
- `com.rescript.plugin.intention.RescriptConvertFunctionCallToPipeIntention`
  - `isAvailable()`: カーソル位置が `Module.func(expr, ...)` パターンか検出
  - `invoke()`: `Module.func(expr, args)` → `expr->Module.func(args)` に変換

### 登録
- `plugin.xml`: `<intentionAction>` で2つ登録

---

## #76 インターフェース公開/非公開

### アーキテクチャ
- 2つの `PsiElementBaseIntentionAction` を作成
- PSI ツリーから宣言を抽出し、対応ファイルに追加/削除

### クラス設計
- `com.rescript.plugin.intention.RescriptAddToInterfaceIntention`
  - `isAvailable()`: `.res` ファイル内の宣言で、対応 `.resi` が存在し、未公開の宣言
  - `invoke()`: 宣言のシグネチャを `.resi` に追加
- `com.rescript.plugin.intention.RescriptRemoveFromInterfaceIntention`
  - `isAvailable()`: `.resi` ファイル内の宣言
  - `invoke()`: `.resi` から宣言を削除

### 登録
- `plugin.xml`: `<intentionAction>` で2つ登録

---

## #83 型ミスマッチインラインヒント

### アーキテクチャ
- 既存の `RescriptErrorLensManager` を拡張
- エラーメッセージから型ミスマッチ情報をパースし、構造化して表示

### クラス設計
- `com.rescript.plugin.errorlens.RescriptTypeMismatchParser`
  - ReScript コンパイラの型エラーメッセージを解析
  - `expected: X` と `actual: Y` を抽出
- `RescriptErrorLensRenderer` を拡張
  - 型ミスマッチの場合、expected/actual を色分けして表示

### 登録
- 既存の Error Lens インフラ内で処理（新規 Extension Point 不要）
