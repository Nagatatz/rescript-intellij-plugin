# 設計: S 優先度機能一括実装

## 全体方針

- パーサー（`RescriptParser.kt`, `Rescript.flex`）の変更は行わない
- 既存の PSI ノード（`RescriptElementTypes`）、トークン（`RescriptTokenTypes`）、ユーティリティ（`RescriptPsiUtils`）を活用
- 各機能は独立したパッケージ/ファイルに配置し、`plugin.xml` に Extension Point として登録
- 既存コードパターン（`RescriptSurroundDescriptor`, `RescriptBreadcrumbsProvider`, `RescriptSpellcheckingStrategy` 等）に倣う

---

## 1. Unwrap/Remove

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptUnwrapDescriptor.kt`

### 設計

```
RescriptUnwrapDescriptor : UnwrapDescriptor
  ├── getUnwrappers() → List<RescriptBaseUnwrapper>
  │
  ├── RescriptSomeUnwrapper     — Some(expr) → expr
  ├── RescriptOkUnwrapper       — Ok(expr) → expr
  ├── RescriptErrorUnwrapper    — Error(expr) → expr
  ├── RescriptIfUnwrapper       — if (...) { body } → body
  ├── RescriptSwitchUnwrapper   — switch ... { | _ => body } → body
  ├── RescriptTryUnwrapper      — try { body } catch { ... } → body
  └── RescriptBlockUnwrapper    — { body } → body
```

### 実装方針

- `UnwrapDescriptor` を実装し、各 unwrapper を `Pair<PsiElement, Unwrapper>` として返す
- カーソル位置から外向きに探索し、該当パターンを見つけたら unwrap 候補に追加
- `Some(`, `Ok(`, `Error(` はテキストパターンマッチ（カーソル位置からトークンを前方スキャンして `UIDENT + LPAREN` を検出し、対応する `RPAREN` を特定）
- `if`, `switch`, `try` はキーワードトークン + ブレースバランスで body を特定
- `{ }` はカーソル位置の最寄り `LBRACE`/`RBRACE` ペアを検出

### plugin.xml 登録

```xml
<lang.unwrapDescriptor language="ReScript"
    implementationClass="com.rescript.plugin.editor.RescriptUnwrapDescriptor"/>
```

---

## 2. Go to Test / Create Test

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/navigation/RescriptTestCreator.kt`

### 設計

```
RescriptTestCreator : TestCreator
  ├── isAvailable() — .res/.resi ファイルかチェック
  └── createTest() — テストファイルの探索 or 生成
```

### テストファイル探索ロジック

1. 現在のファイル名が `Foo.res` の場合、以下の順に探索:
   - 同ディレクトリ: `Foo_test.res`, `Foo.test.res`
   - `__tests__/` サブディレクトリ: `Foo_test.res`, `Foo.test.res`
   - 親ディレクトリの `__tests__/`: `Foo_test.res`, `Foo.test.res`
2. テストファイルの場合（`_test.res` / `.test.res`）、逆方向に実装ファイルを探索

### テスト生成ロジック

- `RescriptTestFrameworkDetector` でフレームワーク（jest/vitest）を検出
- ボイラープレート挿入:

```rescript
// Jest
open Jest
describe("Foo", () => {
  test("should work", () => {
    expect(true)->toBe(true)
  })
})

// Vitest
@@warning("-33") // suppress unused open
open Vitest
describe("Foo", () => {
  test("should work", () => {
    expect(true)->toBe(true)
  })
})
```

### plugin.xml 登録

```xml
<testCreator language="ReScript"
    implementationClass="com.rescript.plugin.navigation.RescriptTestCreator"/>
```

---

## 3. Tree Structure Provider (.resi nesting)

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/projectview/RescriptTreeStructureProvider.kt`

### 設計

```
RescriptTreeStructureProvider : TreeStructureProvider
  └── modify() — .resi ノードを .res の子としてネスト
```

### 実装方針

- `TreeStructureProvider.modify()` で子ノード一覧を受け取る
- `.res` ファイルと `.resi` ファイルをペアリング（同名チェック）
- ペアが見つかった `.resi` を元のリストから除去し、`.res` ノードの子として `NestingTreeNode` を返す
- 設定 `RescriptProjectSettings` に `nestResiFiles: Boolean = true` を追加し、無効時は素通し

### plugin.xml 登録

```xml
<treeStructureProvider
    implementation="com.rescript.plugin.projectview.RescriptTreeStructureProvider"/>
```

---

## 4. Typed Handler — JSX 閉じタグ自動挿入

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptTypedHandler.kt`

### 設計

```
RescriptTypedHandler : TypedHandlerDelegate
  └── charTyped('>')
      ├── JSX タグ名を後方スキャンで取得
      ├── 自己閉じ（前が '/'）なら何もしない
      ├── コメント/文字列内なら何もしない
      └── `</tagName>` を挿入してカーソルをタグ間に配置
```

### JSX タグ名の取得ロジック

1. `>` が入力された位置の直前のテキストを後方スキャン
2. `<tagName attr1 attr2>` のパターンから `tagName` を抽出
3. `<` を見つけるまで戻り、`<` の直後の identifier を取得
4. `Module.Component` のようなドット付きパスも対応

### コンテキスト判定

- `PsiFile` が `RescriptFile` でなければスキップ
- カーソル位置のトークンが `SINGLE_COMMENT`, `MULTI_COMMENT`, `STRING_VALUE` ならスキップ
- 直前のトークンが `TAG_AUTO_CLOSE` (`/>`) ならスキップ

### plugin.xml 登録

```xml
<typedHandler
    implementation="com.rescript.plugin.editor.RescriptTypedHandler"/>
```

---

## 5. Bundled Dictionary

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/spellcheck/RescriptBundledDictionaryProvider.kt`
- `src/main/resources/dictionaries/rescript.dic`

### 設計

```
RescriptBundledDictionaryProvider : BundledDictionaryProvider
  └── getBundledDictionaries() → ["/dictionaries/rescript.dic"]
```

### 辞書内容

ReScript 固有の用語を1行1単語で記載:

- 言語キーワード/構文: `rescript`, `gentype`, `genType`, `uncurried`, `polyvariant`, `functor`
- 標準モジュール: `Belt`, `Js`, `Dom`, `Pervasives`, `Obj`, `Nullable`
- アノテーション: `genType`, `scope`, `val`, `module`, `send`, `get`, `set`, `new`, `variadic`, `inline`, `unboxed`, `deriving`
- ツール/エコシステム: `rescript`, `rescriptjson`, `bsconfig`, `reanalyze`, `vitest`
- 一般的な型名/関数: `unshift`, `foreach`, `indexOf`, `parseInt`, `stringify`

### plugin.xml 登録

```xml
<spellchecker.bundledDictionaryProvider
    implementation="com.rescript.plugin.spellcheck.RescriptBundledDictionaryProvider"/>
```

---

## 6. Context Info — Declaration Range Handler

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptDeclarationRangeHandler.kt`

### 設計

```
RescriptDeclarationRangeHandler : DeclarationRangeHandler<PsiElement>
  └── getDeclarationRange()
      ├── 宣言の先頭（キーワード）から名前の末尾までの TextRange を返す
      └── MODULE_DECLARATION: "module Foo" / LET_DECLARATION: "let foo" / TYPE_DECLARATION: "type t"
```

### 実装方針

- `getDeclarationRange()` は、宣言ノードの開始位置からキーワード + 名前の範囲を返す
- 既存の `RescriptPsiUtils.extractName()` と同様のロジックで名前位置を特定
- `NAVIGABLE_TYPES` に含まれる要素タイプのみ対応

### plugin.xml 登録

```xml
<declarationRangeHandler key="com.rescript.plugin.lang.psi.RescriptElementType"
    implementationClass="com.rescript.plugin.editor.RescriptDeclarationRangeHandler"/>
```

---

## 7. Test Source Filter

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/test/RescriptTestSourcesFilter.kt`

### 設計

```
RescriptTestSourcesFilter : TestSourcesFilter
  └── isTestSource()
      ├── ファイル名パターン: *_test.res, *.test.res, *_test.resi, *.test.resi
      └── ディレクトリパターン: __tests__/ 配下
```

### 実装方針

- `TestSourcesFilter.isTestSource()` でファイルのパスとファイル名をチェック
- パス内に `__tests__` セグメントがあるか、ファイル名が `_test.res` / `.test.res` で終わるかを判定

### plugin.xml 登録

```xml
<testSourcesFilter
    implementation="com.rescript.plugin.test.RescriptTestSourcesFilter"/>
```

---

## 8. FindUsagesProvider + WordsScanner

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/lang/RescriptFindUsagesProvider.kt`

### 設計

```
RescriptFindUsagesProvider : FindUsagesProvider
  ├── getWordsScanner() → RescriptWordScanner (DefaultWordsScanner ベース)
  ├── canFindUsagesFor() → NAVIGABLE_TYPES の PSI 要素
  ├── getType() → "function" / "module" / "type" / "external" / "exception"
  ├── getDescriptiveName() → RescriptPsiUtils.extractName()
  └── getNodeText() → 要素テキストの1行目
```

### RescriptWordScanner

- `DefaultWordsScanner` を使用
- レクサー: `RescriptLexer` (既存の FlexAdapter)
- 識別子トークン: `LIDENT`, `UIDENT`
- コメントトークン: `SINGLE_COMMENT`, `MULTI_COMMENT`
- 文字列トークン: `STRING_VALUE`

### plugin.xml 登録

```xml
<lang.findUsagesProvider language="ReScript"
    implementationClass="com.rescript.plugin.lang.RescriptFindUsagesProvider"/>
```

---

## テスト方針

各機能に対して `src/test/kotlin/com/rescript/plugin/` 配下にテストクラスを作成:

| 機能 | テストクラス | テスト内容 |
|------|-------------|-----------|
| Unwrap/Remove | `editor/RescriptUnwrapDescriptorTest.kt` | 各 unwrapper のテキスト変換結果を検証 |
| Go to Test | `navigation/RescriptTestCreatorTest.kt` | ファイル名パターンマッチのロジックテスト |
| Tree Structure | `projectview/RescriptTreeStructureProviderTest.kt` | .resi ネストロジックのユニットテスト |
| Typed Handler | `editor/RescriptTypedHandlerTest.kt` | JSX 閉じタグ挿入のテキスト変換テスト |
| Bundled Dictionary | `spellcheck/RescriptBundledDictionaryProviderTest.kt` | 辞書ファイルの存在・内容チェック |
| Context Info | `editor/RescriptDeclarationRangeHandlerTest.kt` | 宣言範囲の TextRange 計算テスト |
| Test Source Filter | `test/RescriptTestSourcesFilterTest.kt` | ファイルパスパターンの判定テスト |
| Find Usages | `lang/RescriptFindUsagesProviderTest.kt` | シンボル種類表示、WordsScanner のテスト |
