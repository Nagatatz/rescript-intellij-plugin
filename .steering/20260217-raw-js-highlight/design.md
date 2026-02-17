# Design: %raw() JavaScript ハイライト

## 実装方針

IntelliJ Platform の `MultiHostInjector` を使用して、`%raw()` / `%%raw()` 内の文字列リテラルに JavaScript 言語を注入する。

## 新規ファイル

### `src/main/kotlin/com/rescript/plugin/injection/RescriptRawJsInjector.kt`

- `MultiHostInjector` を実装
- `elementsToInjectIn()`: `PsiElement` (ASTWrapperPsiElement) を返す
- `getLanguagesToInject()`:
  1. 対象要素が `STRING_VALUE` トークンであることを確認
  2. 前のトークン列が `PERCENT` → `RAW` → `LPAREN` パターンに一致するかチェック
  3. 一致する場合、JavaScript 言語を注入
  4. テンプレートリテラル (`%%raw(`...`)`) の場合は `PERCENT` → `PERCENT` → `RAW` → `LPAREN` パターン

### `src/main/resources/META-INF/rescript-js-injection.xml`

- JavaScript optional dependency の設定ファイル
- `multiHostInjector` extension point に `RescriptRawJsInjector` を登録

## 変更ファイル

### `src/main/resources/META-INF/plugin.xml`

- `<depends optional="true" config-file="rescript-js-injection.xml">JavaScript</depends>` を追加

## トークン列の分析

ReScript の `%raw("js code")` は以下のトークン列になる:
- `PERCENT` → `RAW` → `LPAREN` → `STRING_VALUE` → `RPAREN`

`%%raw(`js code`)` は:
- `PERCENT` → `PERCENT` → `RAW` → `LPAREN` の後にテンプレートリテラル

## JavaScript 言語の取得

```kotlin
Language.findLanguageByID("JavaScript")
    ?: Language.findLanguageByID("ECMAScript 6")
```

null の場合は注入しない（JavaScript プラグインが未インストール）。

## 文字列内容の注入範囲

`STRING_VALUE` トークンにはクォート文字が含まれるため、注入範囲はクォートを除いた内側部分とする:
- `"js code"` → offset 1 から length-2 の範囲
