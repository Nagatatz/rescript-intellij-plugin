# B 優先度機能 — 設計書

## 全体方針

4 機能すべて既存のアーキテクチャパターンを踏襲し、最小限のコードで実装する。新しいフレームワークやライブラリの導入は不要。

---

## #93 常時型表示パネル (Type Info ToolWindow)

### アーキテクチャ

```
CaretListener → debounce(300ms) → RescriptLspUtils.getHoverType() → UI 更新
```

### ファイル構成

| ファイル | 役割 |
|---------|------|
| `typeinfo/RescriptTypeInfoToolWindowFactory.kt` | ToolWindow 登録（Factory） |
| `typeinfo/RescriptTypeInfoPanel.kt` | パネル UI + CaretListener + debounce ロジック |

### 実装詳細

**パネル UI:**
- `JBLabel` でシンプルにテキスト表示（ReScript 構文ハイライト付きの EditorEx ではなく、軽量実装を選択）
- フォントは IDE のエディタフォントに合わせる（`EditorColorsManager.getInstance().globalScheme.editorFontName`）

**カーソル追従:**
- `EditorFactory.getInstance().eventMulticaster.addCaretListener()` で全エディタのキャレット変更を監視
- `FileEditorManagerListener` でアクティブファイル変更を監視
- debounce: `Alarm(Alarm.ThreadToUse.POOLED_THREAD)` で 300ms の遅延実行
- LSP リクエストはバックグラウンドスレッドで実行し、UI 更新は EDT で行う

**状態管理:**
- ReScript ファイル以外 → `"No ReScript file selected"`
- LSP 未起動 → `"LSP not available"`
- カーソル位置に型情報なし → `"No type information"`
- 型情報あり → 型テキストを表示

**plugin.xml 登録:**
```xml
<toolWindow id="ReScript Type" anchor="bottom" secondary="true"
            factoryClass="com.rescript.plugin.typeinfo.RescriptTypeInfoToolWindowFactory"
            icon="/icons/rescript-file.svg"/>
```

### 参考パターン

`RescriptCompiledJsPreviewToolWindowFactory` / `RescriptCompiledJsPreviewPanel` をベースにするが、EditorEx ではなく JBLabel で軽量化。

---

## #96 レコードスタブ生成 (Record Value Generation)

### アーキテクチャ

```
AnAction → findEnclosingDeclaration(TYPE_DECLARATION)
         → RescriptTypeDeclarationParser.parse() → TypeShape.Record
         → デフォルト値マッピング → テキスト挿入
```

### ファイル構成

| ファイル | 役割 |
|---------|------|
| `generate/RescriptGenerateRecordValueAction.kt` | Generate アクション |

### 実装詳細

**デフォルト値マッピング:**

```kotlin
fun defaultValueForType(typeAnnotation: String): String {
    val trimmed = typeAnnotation.trim()
    return when {
        trimmed == "string" -> "\"\""
        trimmed == "int" -> "0"
        trimmed == "float" -> "0.0"
        trimmed == "bool" -> "false"
        trimmed.startsWith("option") -> "None"
        trimmed.startsWith("array") -> "[]"
        trimmed.startsWith("list") -> "list{}"
        trimmed == "unit" -> "()"
        trimmed.startsWith("(") && trimmed.contains("=>") -> "_ => todo"
        else -> "todo"
    }
}
```

**生成コード例:**

入力:
```rescript
type user = {
  name: string,
  age: int,
  email: option<string>,
}
```

生成結果（カーソル位置に挿入）:
```rescript
{
  name: "",
  age: 0,
  email: None,
}
```

**RescriptGenerateGroup への追加:**
```kotlin
private val actions: Array<AnAction> = arrayOf(
    RescriptGenerateSwitchAction(),
    RescriptGenerateModuleTypeAction(),
    RescriptGenerateMakeAction(),
    RescriptGenerateJsonCodecAction(),
    RescriptGenerateRecordValueAction(), // 追加
)
```

### 参考パターン

`RescriptGenerateMakeAction` と同一構造。`update()` でレコード型の内部判定、`actionPerformed()` で生成・挿入。

---

## #54 IntelliLang 連携強化 (`%re` 正規表現インジェクション)

### アーキテクチャ

既存の `RescriptRawJsInjector` を拡張して `%re` パターンを追加サポートする。

### ファイル構成

| ファイル | 変更内容 |
|---------|---------|
| `injection/RescriptRawJsInjector.kt` | `%re` 判定ロジックを追加（リネームも検討） |

### 実装詳細

**レクサートークン列:**

`%re("/pattern/flags")` は以下のトークン列になる:
```
PERCENT → LIDENT("re") → LPAREN → STRING("/pattern/flags") → RPAREN
```

注意: `raw` / `ffi` は専用トークン（`RescriptTokenTypes.RAW` / `FFI`）だが、`re` は `LIDENT` として字句解析される。

**判定ロジックの変更:**

`isInsideRawBlock()` を `isInsideInjectionBlock()` にリネームし、戻り値を `Language?`（注入言語）にする:

```kotlin
private fun detectInjectionLanguage(element: PsiElement): Language? {
    // 既存の RAW/FFI チェック → JavaScript
    // 新規: LIDENT("re") チェック → RegExp
}
```

`RegExp` 言語は `Language.findLanguageByID("RegExp")` で取得可能（IntelliJ に標準搭載）。

**`%re` の文字列形式:**

ReScript の `%re` は `%re("/pattern/flags")` の形式。注入時には先頭・末尾の `/` とフラグ部分を除外してパターン本体のみを注入する:
- 入力: `"/abc+def/gi"` → 注入範囲: `abc+def`（`/` の内側のみ）

### 参考パターン

`RescriptRawJsInjector.isInsideRawBlock()` の既存ロジックを拡張。

---

## #82 分割代入の解除 (Expand Destructuring)

### アーキテクチャ

```
PsiElementBaseIntentionAction → テキストベースパターン検出
  → `let {field1, field2} = expr` を検出
  → 個別の let バインディングに展開
```

### ファイル構成

| ファイル | 役割 |
|---------|------|
| `intention/RescriptExpandDestructuringIntention.kt` | Intention アクション |

### 実装詳細

**検出パターン:**

```kotlin
// let { field1, field2, ... } = expr のパターンを正規表現で検出
val DESTRUCTURING_PATTERN = Regex(
    """let\s+\{([^}]+)\}\s*=\s*(.+)"""
)
```

**可用性判定 (`isAvailable`):**

1. ReScript ファイル内であること
2. カーソルが `let { ... } = ...` の `{` 〜 `}` の範囲内にあること
3. 正規表現でパターンがマッチすること

**展開ロジック (`invoke`):**

入力:
```rescript
let {name, age, email} = user
```

出力:
```rescript
let name = user.name
let age = user.age
let email = user.email
```

**エッジケース:**
- `let {name: n, age: a} = user` — エイリアスパターン → `let n = user.name`
- `let {name, ...rest} = user` — スプレッド → 展開不可（`isAvailable` で false）
- ネストした分割代入 `let {a: {b}} = x` → 展開不可（`isAvailable` で false）

**検出の制限:**
- 軽量パーサーは式レベルの解析を行わないため、テキストベースの正規表現マッチングを使用
- 単一行の `let { ... } = ...` パターンのみ対象（複数行の分割代入は対象外）

### 参考パターン

`RescriptCaseSplitIntention` — テキストベースのパターンマッチ + `WriteCommandAction` による文書操作。

---

## テスト方針

各機能に対応するテストクラスを作成:

| テストクラス | テスト対象 |
|-------------|----------|
| `RescriptTypeInfoPanelTest.kt` | 免除（Swing UI + LSP 結合） |
| `RescriptGenerateRecordValueActionTest.kt` | デフォルト値マッピングロジック |
| `RescriptRawJsInjectorTest.kt` | `%re` パターン検出 + 注入範囲計算 |
| `RescriptExpandDestructuringIntentionTest.kt` | パターンマッチング + 展開ロジック |

---

## plugin.xml 変更

```xml
<!-- #93: Type Info ToolWindow -->
<toolWindow id="ReScript Type" anchor="bottom" secondary="true"
            factoryClass="com.rescript.plugin.typeinfo.RescriptTypeInfoToolWindowFactory"
            icon="/icons/rescript-file.svg"/>

<!-- #96: Record Value は既存の RescriptGenerateGroup に追加するのみ（plugin.xml 変更不要） -->

<!-- #54: 既存の RescriptRawJsInjector を拡張（plugin.xml 変更不要） -->

<!-- #82: Expand Destructuring Intention -->
<intentionAction>
  <language>ReScript</language>
  <className>com.rescript.plugin.intention.RescriptExpandDestructuringIntention</className>
  <categoryKey>rescript.intention.category</categoryKey>
</intentionAction>
```
