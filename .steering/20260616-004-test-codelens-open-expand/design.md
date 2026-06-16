# design — Test Code Lens (#111) + open qualifier 展開 intention (#112)

## 全体方針

- 検出・展開のコアロジックは**純関数として切り出し全網羅テスト**する。
- IntelliJ Platform / Swing への結合（CodeVisionProvider・RunConfigurationProducer・Intention の
  WriteCommandAction）は薄いグルー層に留め、`testing.md` の免除区分（実行構成 UI / editor 結合）に
  該当する部分はテストを免除する（tasklist にて理由明記）。
- 両機能とも LSP 非依存（`RescriptLexer` トークン列 + 軽量 PSI / FilenameIndex のみ）。

## 機能 1: #111 Test Code Lens

### 新規クラス

#### `test/RescriptTestCallDetector.kt`（新規 / 純ロジック / テスト必須）

```kotlin
data class TestCall(
    val functionName: String,   // "describe" | "it" | "test"
    val testName: String,       // 第 1 引数の文字列リテラル内容
    val callOffset: Int,        // 関数名 LIDENT 開始 offset（CodeVision アンカー）
    val nameArgRange: TextRange // 文字列リテラル全体の範囲
)

object RescriptTestCallDetector {
    fun detect(text: CharSequence): List<TestCall>
}
```

- `RescriptLexer` でトークン化し、`LIDENT(describe|it|test)` → `(` → 文字列リテラルの並びを検出。
- 文字列リテラルが補間テンプレート（バッククォート + `${`）の場合は名前抽出をスキップ。
- ネスト（`describe` 内の `it`）も独立した TestCall として個別に拾う。

#### `test/RescriptTestCodeVisionProvider.kt`（新規 / 免除: editor/platform 結合）

- `DaemonBoundCodeVisionProvider` を実装（既存 `RescriptCodeVisionProvider.java` と同 API）。
- id = `rescript.testCodeLens`。
- `RescriptTestSourcesFilter` でテストファイル判定。非テストファイルは空リストを返す。
- `RescriptTestCallDetector.detect` の結果ごとに、`callOffset` 行へ
  `ExecutorAction.getActions(0)`（Run/Debug）を `actionGroups` に持つ CodeVision エントリを生成。

### 変更クラス

#### `test/RescriptTestConfigurationProducer.kt`（変更）

- `setupConfigurationFromContext` で、コンテキスト offset が `RescriptTestCallDetector.detect` の
  いずれかの `nameArgRange` / 行に含まれる場合、その `testName` を `-t <name>` フィルタとして構成に設定。
- 該当しなければ従来のファイル単位構成にフォールバック（既存挙動を維持）。

### plugin.xml

- 既存 codeVision 登録群（~749 付近）に `daemonBoundCodeVisionProvider` を追加。

### リスク

- CodeVision エントリの Run/Debug 配線が `ExecutorAction.getActions(0)` で意図通り実行構成へ橋渡し
  されるか実装時に検証。期待どおり動かない場合は `RunLineMarkerContributor`（ガター実行）への
  フォールバックを検討（既存 `RescriptRunLineMarkerContributor` が前例）。

## 機能 2: #112 open qualifier 展開 intention

### 新規クラス

#### `imports/RescriptModuleMemberExtractor.kt`（新規 / 純ロジック / テスト必須）

```kotlin
object RescriptModuleMemberExtractor {
    fun extractTopLevelNames(text: CharSequence): Set<String>
}
```

- depth-0（波括弧ネスト 0）のトップレベル宣言名を抽出: `let` / `let rec` / `type` / `module` /
  `external` / `exception`。
- `RescriptLexer` トークン列を走査し、ブレース深度を追跡して depth-0 の宣言キーワード直後の
  識別子を集める。

#### `imports/RescriptOpenExpansionPlanner.kt`（新規 / 純ロジック / テスト必須）

```kotlin
data class ExpansionPlan(
    val prefixInsertOffsets: List<Int>, // "M." を前置する各 offset
    val openDeletionRange: TextRange    // 削除する open 文の範囲（行末改行含む）
)

object RescriptOpenExpansionPlanner {
    fun plan(
        text: CharSequence,
        moduleName: String,
        memberNames: Set<String>,
        openStatementRange: TextRange
    ): ExpansionPlan
}
```

- `open` 文より後ろの範囲を走査。
- E（memberNames）に含まれる裸 LIDENT/UIDENT の出現に対し前置 offset を収集。
- 直前トークンが `.`（既に修飾済み）の出現はスキップ（`M.M.x` 二重修飾防止）。
- E に含まれない名前はスキップ。
- 保守的シャドウ除外: 同名の local binding（`let <name>` が open 以降に出現）が見つかる場合は除外。

#### `intention/RescriptExpandOpenQualifierIntention.kt`（新規 / グルー / Intention 本体）

- `RescriptBaseIntention` を継承、`isAvailableInRescript()` でキャレットが `open M` 上かつ
  `FilenameIndex` で `M.res` / `M.resi` がプロジェクト内に存在することを確認。
- 実行時: 対象モジュールファイルを読んで `extractTopLevelNames` → `plan` →
  `Messages` で件数確認 → 単一 `WriteCommandAction` で reverse-offset に `M.` を挿入し open 文を削除。
- ライブラリモジュール（プロジェクト内に `.res` 無し）では `isAvailableInRescript()` が false。

### plugin.xml + リソース

- `<intentionAction>`（language=ReScript / category=ReScript / className=FQDN / skipBeforeAfter=true）を
  既存 intention 群の並びに追加。
- `src/main/resources/intentionDescriptions/RescriptExpandOpenQualifierIntention/description.html` を新設。

### リスク

- 純構文ゆえの精度限界（型に基づくシャドウ解決はできない）。保守的シャドウ除外 + 件数確認ダイアログ +
  1 ステップ Undo で誤書き換えのリスクを緩和する。

## 依存・順序

- #111 と #112 は独立（共有ヘルパなし）。#111 → #112 の順で実装。
