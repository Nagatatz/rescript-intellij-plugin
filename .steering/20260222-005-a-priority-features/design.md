# 設計書: A 優先度機能実装

## 全体方針

- 既存の Intention/Generate/Navigation パターンに準拠する
- LSP 依存機能は `LspServerManager` 経由でサーバーを取得し、未起動時は graceful に無効化
- テキスト操作は `WriteCommandAction.runWriteCommandAction()` でラップする
- 各機能は独立したクラスとして実装し、`plugin.xml` に登録する

## 機能別設計

### #46 Search Everywhere

**方針:** 既存の `RescriptSymbolContributor`（Go to Symbol）を拡張し、Search Everywhere の Classes / Symbols タブに ReScript シンボルを提供する。

**実装:**
- `RescriptSearchEverywhereContributor` を新規作成
- `SearchEverywhereContributorFactory` を実装
- 既存の `RescriptSymbolContributor` のシンボル収集ロジックを再利用
- ファイル名 + 行番号を表示テキストに含める

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/navigation/RescriptSearchEverywhereContributor.kt`

**登録:**
```xml
<searchEverywhereContributor
    implementation="com.rescript.plugin.navigation.RescriptSearchEverywhereContributor"/>
```

---

### #49 Unresolved Reference Quick Fix

**方針:** LSP 診断メッセージから未解決参照を検出し、`open` 文追加または完全修飾パスへの変換を提案する。`RescriptOpenStatementIndex` を活用してプロジェクト内のモジュールをスキャンする。

**実装:**
- `RescriptUnresolvedReferenceQuickFix` を新規作成（`IntentionAction` を実装）
- LSP 診断の "The value ... is not found" パターンを検出
- `RescriptOpenStatementIndex` でプロジェクト内モジュールの公開シンボルを検索
- 候補として: (1) `open Module` 追加、(2) `Module.symbol` に変換
- `ExternalAnnotator` パターンで LSP 診断にアタッチ

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/quickfix/RescriptUnresolvedReferenceQuickFix.kt`
- `src/main/kotlin/com/rescript/plugin/quickfix/RescriptAddOpenQuickFix.kt`
- `src/main/kotlin/com/rescript/plugin/quickfix/RescriptQualifyReferenceQuickFix.kt`

**登録:** `intentionAction` として登録

---

### #50 Completion Weigher

**方針:** `CompletionWeigher` を実装し、LSP からの補完候補にコンテキストベースの重み付けを行う。

**実装:**
- `RescriptCompletionWeigher` を新規作成（`CompletionWeigher` を拡張）
- 重み付け基準:
  1. `sortText` が提供されている場合はそれを優先（LSP サーバー側の優先順位を尊重）
  2. ローカルスコープの変数（`detail` に "local" を含む候補）
  3. 現在ファイルで open されているモジュールの要素
  4. キーワード補完は最下位

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/completion/RescriptCompletionWeigher.kt`

**登録:**
```xml
<completion.weigher language="ReScript" id="rescriptWeigher"
    implementationClass="com.rescript.plugin.completion.RescriptCompletionWeigher"/>
```

---

### #74 パイプチェーン中間型ヒント

**方針:** `InlayHintsProvider` を実装し、パイプチェーンの各 `->` 後に LSP hover から取得した型情報をインレイヒントとして表示する。

**実装:**
- `RescriptPipeChainTypeHintsProvider` を新規作成
- `InlayHintsProvider` を実装（IntelliJ 2024.2+ API）
- パイプ演算子 `->` のパターンをレクサートークンから検出
- 各パイプ右辺の式の末尾位置で LSP `textDocument/hover` を非同期リクエスト
- 結果の型文字列をインレイヒントとして表示
- Settings UI で有効/無効の切り替えを提供

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/lsp/RescriptPipeChainTypeHintsProvider.kt`

**登録:**
```xml
<codeInsight.inlayHintsProvider language="ReScript"
    implementationClass="com.rescript.plugin.lsp.RescriptPipeChainTypeHintsProvider"/>
```

---

### #75 ラベル付き引数の一括挿入

**方針:** 関数呼び出し位置で LSP hover から関数シグネチャを取得し、ラベル付き引数のプレースホルダーを挿入する Intention。

**実装:**
- `RescriptInsertLabeledArgsIntention` を新規作成（`PsiElementBaseIntentionAction` を拡張）
- `isAvailable()`: カーソルが関数呼び出しの識別子上にあることを確認
- `invoke()`:
  1. LSP hover でシグネチャを取得
  2. `(~label1: type1, ~label2: type2)` 形式をパース
  3. `(~label1=_, ~label2=_)` を挿入
  4. Template を使って引数間を Tab で移動可能にする

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/intention/RescriptInsertLabeledArgsIntention.kt`

**登録:** `intentionAction`

---

### #77 Make 関数生成

**方針:** 既存の `RescriptGenerateGroup` に新しい Generate アクションを追加。レコード型定義をパースして make 関数を生成する。

**実装:**
- `RescriptGenerateMakeAction` を新規作成（`AnAction` を拡張）
- `RescriptGenerateGroup` のアクション配列に追加
- レコード型の解析は `RescriptTypeDeclarationParser` を拡張して対応
- 生成テンプレート: `let make = (~field1, ~field2=?, ...) => { field1, field2 }`
- `option<_>` 型フィールドには `=?` を付与

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/generate/RescriptGenerateMakeAction.kt`
- `src/main/kotlin/com/rescript/plugin/generate/RescriptTypeDeclarationParser.kt`（拡張）

**登録:** `RescriptGenerateGroup` 内で自動登録

---

### #78 Switch ケース統合

**方針:** switch 式内の同一ボディを持つケースを検出し、`| A | B => body` に統合する Intention。

**実装:**
- `RescriptMergeSwitchCasesIntention` を新規作成
- `isAvailable()`: カーソルが switch 式内のケースパターン上にあることを確認
- switch ブロック内のケースをテキスト解析で抽出（`| pattern => body` のパターン）
- 同一ボディのケースをグルーピングし、パターンを `|` で結合
- ワイルドカード `_` を含むケースは統合対象外

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/intention/RescriptMergeSwitchCasesIntention.kt`

**登録:** `intentionAction`

---

### #89 使用箇所からの関数生成

**方針:** 未定義の関数呼び出しを検出し、引数パターンからスタブ関数を生成する Quick Fix。

**実装:**
- `RescriptGenerateFunctionQuickFix` を新規作成（`IntentionAction` を実装）
- LSP 診断 "The value ... is not found" を検出トリガーとする
- 呼び出し箇所のテキストから引数の数とパターンを推定
- 生成テンプレート: `let funcName = (arg1, arg2) => { todo }`
- カーソル位置の直前のトップレベル宣言の後に挿入

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/quickfix/RescriptGenerateFunctionQuickFix.kt`

**登録:** `intentionAction`

---

### #94 .resi シグネチャ同期

**方針:** `.res` ファイルの宣言変更を検出し、`.resi` との差分をインスペクションで警告。ワンクリックで LSP の `createInterface` を使って再生成する。

**実装:**
- `RescriptSignatureSyncInspection` を新規作成（`LocalInspectionTool` を拡張）
- `.res` ファイルのトップレベル宣言と `.resi` の宣言を比較
- 差分がある場合にインスペクション警告を表示
- Quick Fix として「Sync signature」を提供
- Quick Fix は LSP `textDocument/createInterface` を呼び出し、`.resi` を更新

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/inspection/RescriptSignatureSyncInspection.kt`

**登録:**
```xml
<localInspection language="ReScript" groupName="ReScript"
    implementationClass="com.rescript.plugin.inspection.RescriptSignatureSyncInspection"/>
```

---

### #95 ケースの変数分割（Case Split）

**方針:** switch パターンの変数位置で LSP hover から型を取得し、variant の全コンストラクタに展開する Intention。

**実装:**
- `RescriptCaseSplitIntention` を新規作成
- `isAvailable()`: switch ケースのパターン変数上にカーソルがあることを確認
- `invoke()`:
  1. LSP hover で変数の型を取得
  2. 型が variant の場合、コンストラクタ一覧を解析
  3. 単一パターンを全コンストラクタのケースに展開
  4. ペイロード付きには `_` プレースホルダーを挿入

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/intention/RescriptCaseSplitIntention.kt`

**登録:** `intentionAction`

---

### #98 位置引数→ラベル付き引数

**方針:** #75 と類似のアプローチ。既存の引数を LSP シグネチャのラベル名で修飾する Intention。

**実装:**
- `RescriptConvertToLabeledArgsIntention` を新規作成
- `isAvailable()`: 関数呼び出しの引数リスト上にカーソルがあることを確認
- `invoke()`:
  1. LSP hover で関数シグネチャを取得
  2. ラベル名を抽出
  3. 各位置引数の前に `~label=` を挿入
  4. 既にラベル付きの引数はスキップ

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/intention/RescriptConvertToLabeledArgsIntention.kt`

**登録:** `intentionAction`

---

### #100 不要な括弧の削除

**方針:** 不要な括弧を検出して削除する Intention。演算子優先順位と JSX コンテキストを考慮する。

**実装:**
- `RescriptRemoveUnnecessaryParenthesesIntention` を新規作成
- `isAvailable()`: カーソルが括弧の直後/直前にあることを確認
- 削除可能な括弧の判定:
  - 括弧内が単一の識別子・リテラル・関数呼び出し
  - 外側のコンテキストが演算子式でない場合
  - JSX 属性値 `prop={...}` は対象外
- `invoke()`: 括弧のペアを削除

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/intention/RescriptRemoveParenthesesIntention.kt`

**登録:** `intentionAction`

---

### #101 不要な修飾子の削除

**方針:** ファイル内の `open` 文を解析し、冗長なモジュール修飾子を検出・削除する Intention。

**実装:**
- `RescriptRemoveRedundantQualifierIntention` を新規作成
- `isAvailable()`: ドット付き修飾パス（`Module.symbol`）上にカーソルがあることを確認
- ファイル内の `open` 文を収集し、修飾子が冗長かどうかを判定
- 名前衝突の可能性がある場合は削除対象外（同名シンボルが複数モジュールに存在）
- `invoke()`: 修飾子部分（`Module.`）を削除

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/intention/RescriptRemoveQualifierIntention.kt`

**登録:** `intentionAction`

## 共通ユーティリティ

### LSP ヘルパー

多くの機能が LSP hover を使用するため、共通ヘルパーを作成する:

```kotlin
// RescriptLspUtils.kt
object RescriptLspUtils {
    fun getHoverType(project: Project, file: VirtualFile, offset: Int): String?
    fun parseSignatureLabels(signature: String): List<LabeledParam>
    fun parseDiagnosticMessage(message: String): DiagnosticInfo?
}
```

**ファイル:** `src/main/kotlin/com/rescript/plugin/lsp/RescriptLspUtils.kt`

### シグネチャパーサー

#75, #95, #98 で共用する関数シグネチャのパースロジック:

```kotlin
data class LabeledParam(
    val name: String,
    val type: String,
    val isOptional: Boolean
)

fun parseSignature(hoverText: String): List<LabeledParam>
```

## テスト方針

- 各 Intention / Quick Fix のテストは `isAvailable()` の判定と `invoke()` の変換結果を検証
- LSP 依存機能はサーバー未起動時の graceful degradation をテスト
- Generate アクションは生成コードの正確性をテスト
- InlayHints はプロバイダーの登録と基本動作をテスト

## plugin.xml 登録まとめ

| 機能 | Extension Point | 登録先 |
|------|----------------|--------|
| #46 | `searchEverywhereContributor` | plugin.xml |
| #49 | `intentionAction` × 2 | plugin.xml |
| #50 | `completion.weigher` | plugin.xml |
| #74 | `codeInsight.inlayHintsProvider` | plugin.xml |
| #75 | `intentionAction` | plugin.xml |
| #77 | (RescriptGenerateGroup 内) | 追加登録不要 |
| #78 | `intentionAction` | plugin.xml |
| #89 | `intentionAction` | plugin.xml |
| #94 | `localInspection` | plugin.xml |
| #95 | `intentionAction` | plugin.xml |
| #98 | `intentionAction` | plugin.xml |
| #100 | `intentionAction` | plugin.xml |
| #101 | `intentionAction` | plugin.xml |
