# 設計書: インフラ改善 — Regex キャッシュ・統一・ファイル分割

## 1. #115: Regex インスタンスキャッシュ

### 方針

関数内で毎回インスタンス化されている Regex を、同一クラスの companion object 定数に移動する。

### 対象一覧（42箇所、動的パターン4箇所を除外）

| ファイル | インライン数 | 動的 | 移動対象 |
|---------|------------|------|---------|
| RescriptCommentEvalProvider.kt | 15 | 0 | 15 |
| RescriptPasteAsRescriptProcessor.kt | 6 | 0 | 6 |
| RescriptTypeDeclarationParser.kt | 3 | 0 | 3 |
| RescriptUnwrapDescriptor.kt | 3 | 0 | 3 |
| RescriptExpressionTypeProvider.kt | 2 | 0 | 2 |
| RescriptPasteAsJsxProcessor.kt | 2 | 0 | 2 |
| RescriptSignatureSyncInspection.kt | 2 | 1 | 1 |
| RescriptAddTypeAnnotationIntention.kt | 2 | 1 | 1 |
| RescriptExtractComponentHandler.kt | 1 | 0 | 1 |
| RescriptExtractFunctionHandler.kt | 1 | 0 | 1 |
| RescriptMergeSwitchCasesIntention.kt | 1 | 0 | 1 |
| RescriptTypeSignatureSearchContributor.kt | 1 | 0 | 1 |
| RescriptPpxViewPanel.kt | 1 | 0 | 1 |
| RescriptWorksheetRunner.kt | 1 | 0 | 1 |
| RescriptSearchEverywhereContributor.kt | 1 | 0 | 1 |
| RescriptMutabilityInspection.kt | 1 | 0 | 1 |
| RescriptTypeMismatchParser.kt | 1 | 0 | 1 |
| RescriptInlineHandler.kt | 1 | 1 | 0 |
| RescriptImportUtil.kt | 1 | 1 | 0 |
| **合計** | **46** | **4** | **42** |

### 動的パターン（移動不可）

以下は実行時に変数を使って構築されるため、移動しない：

1. `RescriptInlineHandler.kt:103` — `Regex("""\b${Regex.escape(name)}\b""")`
2. `RescriptImportUtil.kt:100` — `Regex("""(?m)^open\s+$moduleName\s*$""")`
3. `RescriptSignatureSyncInspection.kt:137` — `Regex("""^$keyword\s+(\w+)""")`
4. `RescriptAddTypeAnnotationIntention.kt:105` — `Regex("""let\s+${Regex.escape(name)}\s*=""")`

### 変換パターン

```kotlin
// Before: 関数内でインスタンス化
fun someMethod() {
    val match = Regex("""pattern""").find(text)
}

// After: companion object 定数に移動
companion object {
    private val PATTERN = Regex("""pattern""")
}
fun someMethod() {
    val match = PATTERN.find(text)
}
```

`RescriptCommentEvalProvider.kt` の `listOf(Regex(...), ...)` パターンは、各 Regex を companion object 定数化した上で listOf から参照する：

```kotlin
companion object {
    private val LET_PATTERN = Regex("""^\s*let\s""", RegexOption.MULTILINE)
    private val TYPE_PATTERN = Regex("""^\s*type\s""", RegexOption.MULTILINE)
    // ...
    private val RESCRIPT_PATTERNS = listOf(LET_PATTERN, TYPE_PATTERN, ...)
    private val JS_PATTERNS = listOf(VAR_PATTERN, CONST_PATTERN, ...)
}
```

### 命名規約

- `SCREAMING_SNAKE_CASE` + `_PATTERN` / `_REGEX` サフィックス
- 既存の命名に合わせる（ファイル内で `_PATTERN` なら `_PATTERN` で統一）

---

## 2. #116: 重複 Regex パターン統一

### 方針

`com.rescript.plugin.util.RescriptRegexPatterns` ユーティリティオブジェクトを新設し、複数ファイルで意味的に同一のパターンを集約する。

### 共有パターン一覧

| 定数名 | パターン | 現在の重複箇所 |
|--------|---------|--------------|
| `LIDENT` | `^[a-z_][a-zA-Z0-9_']*$` | RescriptNamesValidator, RescriptExtractVariableUtil |
| `UIDENT` | `^[A-Z][a-zA-Z0-9_']*$` | RescriptNamesValidator（単独だが共有価値あり） |
| `WHITESPACE` | `\s+` | RescriptTypeMismatchParser, RescriptExtractVariableUtil, RescriptRunUtils |

### 統一しないパターン

以下は意味的に類似だがキャプチャグループが異なるため、個別ファイルに残す：

- **LABELED_PARAM** — 4ファイルで使用。`~(\w+)` が基本形だが、型キャプチャ `(?:\s*:\s*(.+?))` やデフォルト値キャプチャ `(?:\s*=\s*([^,)]+))` の有無がコンテキストにより異なる。無理に共通化すると利用側の可読性が低下する
- **OPEN_PATTERN** — 4ファイルで使用。multiline フラグの有無、キャプチャグループの範囲が異なる
- **LET_PATTERN** — 5ファイルで使用。型注釈の有無、本体キャプチャの有無がすべて異なる

### 新規ファイル

```kotlin
// src/main/kotlin/com/rescript/plugin/util/RescriptRegexPatterns.kt
package com.rescript.plugin.util

/**
 * Shared regex patterns used across multiple components.
 *
 * Centralizes commonly reused patterns to avoid duplication
 * and unnecessary Regex object instantiation.
 */
object RescriptRegexPatterns {
    /** Matches a lowercase ReScript identifier (lident). */
    val LIDENT = Regex("^[a-z_][a-zA-Z0-9_']*$")

    /** Matches an uppercase ReScript identifier (uident). */
    val UIDENT = Regex("^[A-Z][a-zA-Z0-9_']*$")

    /** Splits text by whitespace. */
    val WHITESPACE = Regex("""\s+""")
}
```

### 書き換え例

```kotlin
// Before (RescriptExtractVariableUtil.kt)
private val LIDENT_REGEX = Regex("""^[a-z_][a-zA-Z0-9_']*$""")

// After
import com.rescript.plugin.util.RescriptRegexPatterns
// ...
val isValid = RescriptRegexPatterns.LIDENT.matches(name)
```

---

## 3. #117: 長大ファイル分割

### 方針

- 登録済み extension point クラスの完全修飾名は変更しない（plugin.xml 更新を回避）
- 内部ロジックを `internal` クラス/オブジェクトに抽出する
- 外部 API の互換性を維持する（facade パターン）

### 分割計画

#### 3.1 RescriptTokenTypes.kt (442行)

**判断: 分割しない**

理由: このファイルは単一責務（トークン型定義）であり、442行の大部分がフィールド宣言の羅列。責務分離の余地がなく、分割すると逆にトークン定義の一覧性が損なわれる。セクションコメントで区分けを改善するのみとする。

#### 3.2 RescriptJsonCodeGenerator.kt (442行) → 3ファイル

| 新ファイル | 内容 | 推定行数 |
|-----------|------|---------|
| `RescriptJsonCodeGenerator.kt` | 公開API（generateBoth/Encoder/Decoder）+ ディスパッチ | ~80 |
| `RescriptJsonEncoderGenerator.kt` | エンコーダ生成ロジック（record/variant） | ~180 |
| `RescriptJsonDecoderGenerator.kt` | デコーダ生成ロジック（record/variant） | ~180 |

```
generate/
├── RescriptJsonCodeGenerator.kt      // facade (public API)
├── RescriptJsonEncoderGenerator.kt   // internal object
└── RescriptJsonDecoderGenerator.kt   // internal object
```

#### 3.3 RescriptParser.kt (425行) → 3ファイル

| 新ファイル | 内容 | 推定行数 |
|-----------|------|---------|
| `RescriptParser.kt` | PsiParser 実装 + トップレベルパース | ~80 |
| `RescriptDeclarationParser.kt` | 宣言パース（let/type/module/external/open/include/exception） | ~180 |
| `RescriptJsxParser.kt` | JSX パース（element/self-closing/fragment/children/attributes） | ~160 |

```
lang/
├── RescriptParser.kt              // PsiParser (plugin.xml 登録済み)
├── RescriptDeclarationParser.kt   // internal class
└── RescriptJsxParser.kt           // internal class
```

パーサーヘルパー（skipBalanced 等）は最も利用頻度の高いファイルに配置するか、利用箇所が複数にまたがる場合は `RescriptParser.kt` に残す。

#### 3.4 RescriptDocumentationProvider.kt (403行) → 3ファイル

| 新ファイル | 内容 | 推定行数 |
|-----------|------|---------|
| `RescriptDocumentationProvider.kt` | AbstractDocumentationProvider 実装 | ~120 |
| `RescriptOperatorDocumentation.kt` | 演算子情報マップ + ドキュメント生成 | ~150 |
| `RescriptExternalDocUrls.kt` | Belt/Js モジュールの外部 URL マッピング | ~130 |

```
documentation/
├── RescriptDocumentationProvider.kt   // plugin.xml 登録済み
├── RescriptOperatorDocumentation.kt   // internal object
└── RescriptExternalDocUrls.kt         // internal object
```

#### 3.5 RescriptUnwrapDescriptor.kt (370行) → 3ファイル

| 新ファイル | 内容 | 推定行数 |
|-----------|------|---------|
| `RescriptUnwrapDescriptor.kt` | UnwrapDescriptor 実装 + レンジ検出 | ~150 |
| `RescriptUnwrappers.kt` | BaseUnwrapper + 3つの具象 Unwrapper クラス | ~120 |
| `RescriptUnwrapUtils.kt` | ブラケットマッチング、空白処理ヘルパー | ~100 |

```
editor/
├── RescriptUnwrapDescriptor.kt   // plugin.xml 登録済み
├── RescriptUnwrappers.kt         // internal classes
└── RescriptUnwrapUtils.kt        // internal object
```

#### 3.6 RescriptLspUtils.kt (317行) → 3ファイル

| 新ファイル | 内容 | 推定行数 |
|-----------|------|---------|
| `RescriptLspUtils.kt` | サーバー取得、URI 変換、hover 型取得 | ~100 |
| `RescriptLspSignatureParser.kt` | シグネチャパース（ラベル付き引数、variant） | ~120 |
| `RescriptLspDiagnosticParser.kt` | 診断メッセージパース + データクラス | ~100 |

```
lsp/
├── RescriptLspUtils.kt              // public object (facade)
├── RescriptLspSignatureParser.kt    // internal object
└── RescriptLspDiagnosticParser.kt   // internal object
```

外部から `RescriptLspUtils.parseSignatureLabels()` で呼ばれている箇所は、facade パターンで互換性を維持：

```kotlin
object RescriptLspUtils {
    // 既存の公開メソッドはそのまま維持
    fun parseSignatureLabels(sig: String) = RescriptLspSignatureParser.parseLabels(sig)
    fun parseVariantConstructors(type: String) = RescriptLspSignatureParser.parseVariants(type)
    fun parseDiagnosticMessage(msg: String) = RescriptLspDiagnosticParser.parse(msg)
}
```

---

## 4. 実装順序

1. **#116** — `RescriptRegexPatterns` を新設（他の作業の前提）
2. **#115** — 各ファイルのインライン Regex を companion object 定数に移動（#116 の共有パターンも利用）
3. **#117** — ファイル分割（#115 完了後の方がスムーズ）

---

## 5. テスト方針

- リファクタリングのため新規テストは原則不要
- 既存テストの全パスで機能的な等価性を保証
- `RescriptRegexPatterns` のみ新規テスト作成（パターンの正確性を検証）
- ビルド成功 (`./gradlew clean buildPlugin`) を各コミット後に確認

---

## 6. 変更ファイル数の見積もり

| カテゴリ | 新規 | 変更 |
|---------|------|------|
| #115 Regex キャッシュ | 0 | 19 |
| #116 Regex 統一 | 1 (+テスト1) | 4 |
| #117 ファイル分割 | 10 | 5 |
| **合計** | **12** | **28** |
