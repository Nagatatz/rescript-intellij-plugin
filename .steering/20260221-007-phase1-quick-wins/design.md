# Design: Phase 1 — Quick Wins (8 features)

## 概要

8件の低難易度機能を実装する。すべて既存パターンに従い、パーサー変更なし。

---

## 1. 未使用結果の `->ignore` 追加 (#71)

**ファイル:** `intention/RescriptAddIgnoreIntention.kt`

- `PsiElementBaseIntentionAction` を継承
- `isAvailable`: RescriptFile 内で、カーソルがトップレベル式 or `let _ = ` のない式の上にある場合
  - LET_DECLARATION 内かつ右辺の式にカーソルがある場合のみ (末尾で `->ignore` が有用)
  - 既に `->ignore` がある場合は除外
- `invoke`: カーソル位置の式の末尾に `->ignore` を挿入
- **plugin.xml:** `<intentionAction>` 追加

## 2. 未使用変数の `_` プレフィックス (#91)

**ファイル:** `intention/RescriptAddUnderscorePrefixIntention.kt`

- `PsiElementBaseIntentionAction` を継承
- `isAvailable`: LIDENT トークンにカーソルがあり、親が LET_DECLARATION で、識別子が `_` で始まっていない場合
- `invoke`: 識別子の先頭に `_` を挿入
- **plugin.xml:** `<intentionAction>` 追加

## 3. 冗長ブロック削除 (#72)

**ファイル:** `intention/RescriptRemoveRedundantBracesIntention.kt`

- `PsiElementBaseIntentionAction` を継承
- `isAvailable`: LBRACE or RBRACE トークンにカーソルがあり、ブレースペアの間に単一の式 (非空白要素が1つ) のみ含まれる場合
- `invoke`: ブレースとその内部の空白を削除し、内部式のテキストのみを残す
- **plugin.xml:** `<intentionAction>` 追加

## 4. デコレータ/属性補完 (#90)

**ファイル:** `completion/RescriptDecoratorCompletionContributor.kt`

- `CompletionContributor` を継承
- `extend()` で `CompletionType.BASIC` + `psiElement().afterLeaf("@")` パターンを登録
  - 実際には ARROBASE トークンの直後、または ANNOTATION_NAME トークンを検出
- 補完候補: ReScript の標準デコレータ一覧 (name + description)
  - `@genType` — Generate TypeScript types
  - `@module` — Bind to a JS module
  - `@val` — Bind to a JS value
  - `@scope` — Nested module scope binding
  - `@send` — Bind to a method call
  - `@get` — Bind to a property getter
  - `@set` — Bind to a property setter
  - `@new` — Bind to a constructor
  - `@variadic` — Variadic function binding
  - `@return` — Return type annotation
  - `@string` — String enum encoding
  - `@int` — Int enum encoding
  - `@unwrap` — Unwrap polymorphic variant
  - `@as` — Rename JS output
  - `@inline` — Inline constant
  - `@live` — Mark as used (suppress unused warning)
  - `@dead` — Mark as dead code
  - `@deriving` — Auto-derive functions
  - `@react.component` — React component annotation
  - `@jsx.component` — JSX component annotation
  - `@deprecated` — Mark as deprecated
  - `@unboxed` — Unboxed representation
  - `@tag` — Tag for variant encoding
  - `@obj` — Object creation helper
- **plugin.xml:** `<completion.contributor>` 追加

## 5. 演算子優先順位ホバー表示 (#92)

**ファイル:** `documentation/RescriptDocumentationProvider.kt` (既存ファイルを拡張)

- `generateDoc()` を拡張: 演算子トークンの場合にオペレータ情報を返す
- 演算子と優先順位のマッピングテーブルを companion object に追加
- 表示形式: `<b>operator</b> name<br>Precedence: N<br>Description`

## 6. Long Line Inspection Policy (#80)

**ファイル:** `inspection/RescriptInspectionSuppressor.kt` (既存ファイルを拡張)

- `isSuppressedFor` に `"LongLine"` toolId の特別処理を追加
- 要素が以下のコンテキスト内にある場合に抑制:
  - `@module(...)`, `@val(...)` 等のアノテーション内 (ANNOTATION 要素)
  - `%raw(...)`, `%ffi(...)` 内
  - STRING_VALUE トークン内
- 既存のロジックは変更しない (追加ロジックのみ)

## 7. 識別子ケース修正 (#73)

**ファイル:** `intention/RescriptFixIdentifierCaseIntention.kt`

- `PsiElementBaseIntentionAction` を継承
- `isAvailable`:
  - UIDENT にカーソルがあり、親が LET_DECLARATION → 小文字始まりに変換可能
  - LIDENT にカーソルがあり、親が MODULE_DECLARATION → 大文字始まりに変換可能
  - 既に正しいケースの場合は表示しない
- `invoke`:
  - Module 名: `firstChar.uppercase() + rest` (PascalCase)
  - 変数名: `firstChar.lowercase() + rest` (camelCase)
- `getText()`: 動的に `"Convert to PascalCase"` or `"Convert to camelCase"` を表示
- **plugin.xml:** `<intentionAction>` 追加

## 8. MultiLang Commenter (#79)

**ファイル:** `commenter/RescriptMultiLangCommenterProvider.kt`

- `CommenterDataHolder`, `SelfManagingCommenter<CommenterDataHolder>` を実装する方式は複雑すぎるため、より軽量なアプローチを採用:
- `CustomBackedCommenter` を実装し、`%raw()` / `%ffi()` ブロック内の場合は JavaScript のコメント構文を返す
- ただし、ReScript と JavaScript はどちらも `//` と `/* */` を使用するため、**実質的にコメント構文は同じ**
- 代わりに `CommenterWithLineSuffix` は不要。ReScript のコメント構文は JS と同一なので、実際には現行 Commenter で対応済み
- **結論:** この機能は ReScript と JavaScript のコメント構文が同一のため、**実装不要** (現行の `RescriptCommenter` で既に正しく動作)

→ **#79 は実装不要。代わりの機能として省略し、7件の実装とする。**

---

## plugin.xml 変更

```xml
<!-- Intention: Add ->ignore -->
<intentionAction>
    <language>ReScript</language>
    <category>ReScript</category>
    <className>com.rescript.plugin.intention.RescriptAddIgnoreIntention</className>
    <skipBeforeAfter>true</skipBeforeAfter>
</intentionAction>

<!-- Intention: Add _ prefix -->
<intentionAction>
    <language>ReScript</language>
    <category>ReScript</category>
    <className>com.rescript.plugin.intention.RescriptAddUnderscorePrefixIntention</className>
    <skipBeforeAfter>true</skipBeforeAfter>
</intentionAction>

<!-- Intention: Remove redundant braces -->
<intentionAction>
    <language>ReScript</language>
    <category>ReScript</category>
    <className>com.rescript.plugin.intention.RescriptRemoveRedundantBracesIntention</className>
    <skipBeforeAfter>true</skipBeforeAfter>
</intentionAction>

<!-- Intention: Fix identifier case -->
<intentionAction>
    <language>ReScript</language>
    <category>ReScript</category>
    <className>com.rescript.plugin.intention.RescriptFixIdentifierCaseIntention</className>
    <skipBeforeAfter>true</skipBeforeAfter>
</intentionAction>

<!-- Decorator/Attribute Completion -->
<completion.contributor language="ReScript"
    implementationClass="com.rescript.plugin.completion.RescriptDecoratorCompletionContributor"/>
```

## テスト

各機能に対応するユニットテストを作成:
- `intention/RescriptAddIgnoreIntentionTest.kt`
- `intention/RescriptAddUnderscorePrefixIntentionTest.kt`
- `intention/RescriptRemoveRedundantBracesIntentionTest.kt`
- `intention/RescriptFixIdentifierCaseIntentionTest.kt`
- `completion/RescriptDecoratorCompletionContributorTest.kt`
- `documentation/RescriptDocumentationProviderTest.kt` (既存テストに追加)
- `inspection/RescriptInspectionSuppressorTest.kt` (既存テストに追加)
