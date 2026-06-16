# design — JSX 改良プログラム (C→E→B→A→D)

## 全体方針

すべて純構文・LSP 非依存。既存 PSI（`JSX_ELEMENT` / `JSX_SELF_CLOSING_ELEMENT` / `JSX_FRAGMENT`、`RescriptPsi.kt:47-51`）を土台にする。

`JSX_ELEMENT` ノードの子トークン構造（`RescriptJsxParser.parseJsxTagOrSelfClosing`）:

```
JSX_ELEMENT
├─ TAG_LT '<'
├─ JSX_TAG_NAME | JSX_COMPONENT_NAME      ← 開きタグ名（dotted path は DOT 区切りで複数トークン）
├─ (attributes…)
├─ TAG_GT | GT '>'
├─ (children: nested JSX / {expr} / text)
├─ TAG_LT_SLASH '</'
├─ JSX_TAG_NAME | JSX_COMPONENT_NAME      ← 閉じタグ名
└─ TAG_GT '>'
```

`JSX_FRAGMENT` はタグ名トークンを持たない。`JSX_SELF_CLOSING_ELEMENT` は `TAG_AUTO_CLOSE '/>'` で終わり閉じタグ名を持たない。

## 共有ヘルパ（E で新設、B/A が再利用）

### `lang/psi/RescriptJsxTagPairUtil`（新規 object）

PSI 範囲操作のみを行う純関数群。すべて unit test 必須。

```kotlin
/** 開きタグ名・閉じタグ名のトークン範囲とテキストを保持する。 */
data class JsxTagNames(
    val openNameRange: TextRange?,   // 開きタグ名（dotted path 全体）
    val openName: String?,
    val closeNameRange: TextRange?,  // 閉じタグ名（null = 閉じタグ未記述）
    val closeName: String?,
)

object RescriptJsxTagPairUtil {
    /** caret 位置の要素から最も近い JSX_ELEMENT を返す（FRAGMENT/SELF_CLOSING は対象外）。 */
    fun findEnclosingJsxElement(element: PsiElement): PsiElement?

    /** JSX_ELEMENT ノードから開き/閉じタグ名の範囲とテキストを抽出する。 */
    fun extractTagNames(jsxElement: PsiElement): JsxTagNames

    /** 与えられた offset が開きタグ名・閉じタグ名のどちら（または範囲外）かを返す。 */
    fun sideAt(jsxElement: PsiElement, offset: Int): TagSide  // OPEN / CLOSE / NONE
}

enum class TagSide { OPEN, CLOSE, NONE }
```

抽出アルゴリズム: `JSX_ELEMENT.node` の子を線形走査。
- `TAG_LT` の直後から、連続する `JSX_TAG_NAME` / `JSX_COMPONENT_NAME` / `DOT` を結合して開きタグ名範囲とする（最初の name トークン開始〜最後の name/DOT トークン終了）。
- `TAG_LT_SLASH` の直後から同様に閉じタグ名範囲を抽出。`TAG_LT_SLASH` 自体が無ければ `closeName* = null`。

`dotted path`（`<Foo.Bar>`）は範囲全体を 1 つの名前として扱う（B のハイライト・A のミラーは全体を対象にする）。

## C: Surround with JSX

### EP / 配置
`lang.surroundDescriptor` は登録済み（`RescriptSurroundDescriptor`）。`surround/RescriptSurroundDescriptor.kt` の `surrounders` 配列に 2 つ追加する。

### 実装
既存 `RescriptBaseSurrounder` を継承（template 文字列 + caret 範囲方式が既に確立済み）。

- `RescriptJsxElementSurrounder("Surround with JSX element <tag>")`
  - `generateTemplate(sel) = "<div>\n  $sel\n</div>"`
  - `getCursorRange` = 開きタグの `div` を選択範囲として返す（タグ名を即リネーム可能に）。
  - 注: A 実装後は、この `div` 編集が閉じタグに同期するため UX が連続する。
- `RescriptJsxFragmentSurrounder("Surround with Fragment <>...</>")`
  - `generateTemplate(sel) = "<>\n  $sel\n</>"`
  - `getCursorRange` = フラグメント末尾（編集対象なし）。

### テスト
`surround/RescriptSurroundDescriptorTest.kt`（既存があれば追記）で `generateTemplate` / `getCursorRange` の純ロジックを検証。`generateTemplate` は純関数なのでテスト可能。`getElementsToSurround` は既存実装を流用。

## E: 閉じタグ不一致 Inspection

### EP / 配置
`localInspection` を `plugin.xml` に登録（既存 inspection 群に倣う）。`inspection/RescriptMismatchedJsxTagInspection.kt`（新規 `LocalInspectionTool`）。

### 実装
`buildVisitor` で file を走査し、`JSX_ELEMENT` ノードを再帰収集（`PsiTreeUtil.collectElementsOfType` ではなく既存の children 走査スタイルに合わせる）。各要素について:

```
val names = RescriptJsxTagPairUtil.extractTagNames(jsxElement)
if (names.openName != null && names.closeName != null && names.openName != names.closeName) {
    holder.registerProblem(closeNameRange, "Closing tag </${closeName}> does not match opening tag <${openName}>")
}
```

- 閉じタグ未記述（`closeName == null`）は警告しない（編集途中のため）。
- `JSX_FRAGMENT` / `JSX_SELF_CLOSING_ELEMENT` は名前比較対象外。
- Quick Fix（閉じタグを開きタグに合わせる）は **任意**。本ステアリングでは付けず、E の受け入れ条件は検出のみ。将来拡張余地として design に記録。

### テスト
共有ヘルパ `RescriptJsxTagPairUtilTest.kt`（名前抽出・dotted path・fragment・self-closing・閉じ欠落）と、`RescriptMismatchedJsxTagInspectionTest.kt`（`LightPlatformCodeInsightFixture` ベースで `<div></span>` 検出 / `<div></div>` 非検出 / ネスト同名 / fragment 非検出）。

## B: タグペアハイライト

### EP / 配置
`highlightUsagesHandlerFactory` は登録済み（`RescriptHighlightUsagesHandlerFactory`）。同 factory の `createHighlightUsagesHandler` に分岐を追加するか、専用 factory を新設する。

**判断**: 既存 factory は keyword ハイライト専用で `KEYWORD_MAPPING` 駆動。JSX タグ名（`JSX_TAG_NAME` / `JSX_COMPONENT_NAME`）は別系統なので、既存 factory の冒頭に「caret が JSX タグ名なら JSX ハンドラを返す」分岐を足す（factory は 1 言語 1 個が自然なため新設せず分岐追加）。ハンドラ本体は新クラス `RescriptJsxTagHighlightHandler`（`highlight/` 配下）に分離。

### 実装
```
caret token が JSX_TAG_NAME / JSX_COMPONENT_NAME
 → findEnclosingJsxElement → extractTagNames
 → openNameRange と closeNameRange の両方を highlight（caret 側含め両端を強調）
```
`closeName == null` の場合は開きタグのみ。reuse: `RescriptJsxTagPairUtil`。

### テスト
ヘルパ（B 専用ロジックは薄いので）と、`RescriptJsxTagHighlightHandlerTest.kt`（fixture で caret を開きタグ名に置き、`computeUsages` が 2 範囲を返すこと）。

## A: ペアタグ同期リネーム

### EP / 配置
`typedHandler`（`RescriptTypedHandler` が登録済み）と `backspaceHandlerDelegate`（`RescriptBackspaceHandler` が登録済み）に JSX タグ名同期を追加する。ミラー計算ロジックは共有ヘルパに純関数として追加し、ハンドラは薄い wiring に留める。

### ミラー計算（純関数・テスト対象）
`RescriptJsxTagPairUtil` に追加:

```kotlin
/**
 * 片側のタグ名編集を他方へミラーする置換を計算する。
 * 編集前に開き名 == 閉じ名（同期中）だった場合のみミラーする。
 */
fun computeSyncEdit(
    names: JsxTagNames,
    editedSide: TagSide,
    preEditEditedName: String,  // 当該キーストローク適用前の編集側の名前
): SyncEdit?   // (mirrorRange, newText) or null
```

ルール（IntelliJ HTML の `XmlTagNameSynchronizer` に倣う）:
- `editedSide == OPEN` かつ `preEditEditedName == closeName`（編集前は同期）なら、`closeNameRange` を現在の `openName` に置換。
- `editedSide == CLOSE` も対称。
- self-closing / fragment / 閉じ欠落 / 編集前から不一致 → `null`（ミラーしない。不一致は E が警告する役割）。

### ハンドラ wiring（editor 結合のためテスト免除、理由を tasklist に明記）
- `RescriptTypedHandler.beforeCharTyped` で「caret が JSX タグ名内か」「編集前に開き名==閉じ名か」を記録。
- `charTyped`（および backspace 後）で再パースし、`computeSyncEdit` の結果をミラー側へ適用。
- 文字入力コマンドと同一 `WriteCommandAction` 内で document を変更することで Undo を 1 ステップに保つ（既存 JSX 自動クローズと同方式）。

### テスト
`RescriptJsxTagPairUtilTest.kt` に `computeSyncEdit` の網羅テスト（open→close / close→open / 編集前不一致は null / self-closing は null / fragment は null）。**加えてハンドラ統合は heavy fixture でタイピング同期スモークを 1 本** 用意する（`<div>` の `div` を 1 文字編集 → `</div>` 追従 / Undo 1 ステップ）。

## D: 構造ビュー JSX ノード

### 判断
`NAVIGABLE_TYPES`（`RescriptPsiUtils.kt:19`）は構造ビュー・breadcrumb・navbar が共有する。ここに `JSX_ELEMENT` / `JSX_FRAGMENT` を加えると 3 機能すべてに JSX が出る。

- **構造ビュー**: コンポーネント本体の JSX ツリー（`App > div > ul > li`）を俯瞰でき価値が高い。
- **breadcrumb**: caret 位置の JSX 階層パンくずは有用。
- **懸念**: 巨大な JSX で構造ビューがノイズ過多になる可能性。

**結論**: `JSX_ELEMENT` と `JSX_FRAGMENT` を `NAVIGABLE_TYPES` に追加して実装する（self-closing は leaf だが子を持たないため構造ビュー上は任意 — 含めると葉ノードが増えるので **含めない**）。表示テキストは開きタグ名（fragment は `<>`）。`extractName` / `getIcon` / `getElementDescription` に JSX 分岐を追加し、タグ名抽出は `RescriptJsxTagPairUtil.extractTagNames` を再利用。アイコンは `AllIcons.Nodes.Tag`。

ノイズが想定以上なら self-closing 除外に加えフラグメント除外も検討するが、まず element + fragment で出して評価する。

### テスト
`RescriptPsiUtilsTest.kt`（既存に追記）で JSX ノードの `extractName`（開きタグ名 / fragment は `<>`）、`getElementDescription`、`NAVIGABLE_TYPES` 包含を検証。構造ビュー描画自体は既存 `RescriptStructureViewElement` の汎用ロジックに乗るため UI テスト不要。

## ドキュメント同期（各機能のコミットに同梱）

| 機能 | CLAUDE.md | README | sphinx (EN + JA .po) |
|------|-----------|--------|----------------------|
| C | layer3 編集系 | Code Editing | code-editing.md |
| E | layer3 分析系 | Code Analysis | code-analysis.md |
| B | layer3 編集系 | Code Editing | code-editing.md |
| A | layer3 編集系 | Code Editing | code-editing.md |
| D | layer3 ナビ系 | Navigation | navigation.md / advanced.md |

`repository-structure.md` のパッケージ表に新規クラス（`RescriptJsxTagPairUtil`, `RescriptMismatchedJsxTagInspection`, `RescriptJsxTagHighlightHandler`, JSX surrounder 群）を追記。

## 実装順と依存

1. **E**（共有ヘルパ `RescriptJsxTagPairUtil` を含むため最優先で土台を確立）→ ただしユーザー承認順は C 先行。
   - 調整: **C を先に**（独立・ヘルパ不要）、次に **E**（ヘルパ新設）、以降 B→A がヘルパ依存、最後に D。
   - したがって実装順は **C → E → B → A → D** を維持。C はヘルパに依存しないので順序問題なし。
