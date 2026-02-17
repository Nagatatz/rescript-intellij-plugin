# Design: Postfix Completion

## 実装アプローチ

IntelliJ Platform の `PostfixTemplateProvider` extension point を使用して、ReScript 固有の Postfix Completion テンプレートを提供する。

## 新規ファイル

### `src/main/kotlin/com/rescript/plugin/completion/RescriptPostfixTemplateProvider.kt`

`PostfixTemplateProvider` を実装し、7つのテンプレートをインナークラスとして定義する。

**クラス構成:**

```
RescriptPostfixTemplateProvider : PostfixTemplateProvider
├── SwitchPostfixTemplate : PostfixTemplate     (.switch)
├── PipePostfixTemplate : PostfixTemplate       (.pipe)
├── LogPostfixTemplate : PostfixTemplate        (.log)
├── SomePostfixTemplate : PostfixTemplate       (.some)
├── OkPostfixTemplate : PostfixTemplate         (.ok)
├── ErrorPostfixTemplate : PostfixTemplate      (.error)
└── IgnorePostfixTemplate : PostfixTemplate     (.ignore)
```

**テンプレート共通ロジック:**

- `isApplicable`: PsiElement のファイルが ReScript であること、コメント・文字列内でないことを確認
- `expand`: `Editor` と `Document` を使用してテキスト置換を実行

**各テンプレートの expand ロジック:**

| テンプレート | 処理 |
|-------------|------|
| `.switch` | `expr` を `switch expr {\| _ => }` に置換し、カーソルを `=>` の後に移動 |
| `.pipe` | `.pipe` を `->` に置換 |
| `.log` | `expr` を `Console.log(expr)` に置換 |
| `.some` | `expr` を `Some(expr)` に置換 |
| `.ok` | `expr` を `Ok(expr)` に置換 |
| `.error` | `expr` を `Error(expr)` に置換 |
| `.ignore` | `.ignore` を `->ignore` に置換 |

## 変更ファイル

### `src/main/resources/META-INF/plugin.xml`

```xml
<!-- Postfix Completion -->
<codeInsight.template.postfixTemplateProvider language="ReScript"
    implementationClass="com.rescript.plugin.completion.RescriptPostfixTemplateProvider"/>
```

## 適用判定の詳細

`isApplicable` メソッドでは:
1. `context` の `containingFile` が ReScript ファイルであることを確認
2. `context` のトークンタイプが `SINGLE_COMMENT`, `MULTI_COMMENT`, `STRING_VALUE`, `JS_STRING_OPEN`, `JS_STRING_CLOSE` でないことを確認

## 展開処理の詳細

`expand` メソッドでは:
1. `context` の PsiElement からドットの前の式テキストを取得
2. `editor.document` を使用して文字列置換を実行
3. `WriteCommandAction` 内で実行（`expand` は write action 内で呼ばれる）
4. 必要に応じてカーソル位置を調整
