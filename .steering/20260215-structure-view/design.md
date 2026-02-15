# Design: Structure View

## 実装アプローチ

IntelliJ Platform の Structure View API（`PsiStructureViewFactory` + `TreeBasedStructureViewBuilder`）を使い、既存パーサーの PSI ツリーからトップレベル宣言をツリー表示する。

module 内のネスト宣言を階層表示するために、パーサーに小規模な拡張を加える。

## コンポーネント設計

### 1. パーサー拡張（RescriptParser.kt の修正）

**現状の問題:**
`parseModuleDeclaration` は `skipToEndOfDeclaration` を呼び、module body 内の全トークンをスキップする。結果として module ノード内に子宣言の PSI ノードが生成されない。

**変更内容:**
`parseModuleDeclaration` を修正し、`{` を検出した場合に body 内で `parseTopLevel` を再帰的に呼び出す。

```kotlin
private fun parseModuleDeclaration(b: PsiBuilder) {
    val m = b.mark()
    b.advanceLexer() // consume 'module'

    if (b.tokenType == RescriptTokenTypes.TYPE || b.tokenType == RescriptTokenTypes.REC) {
        b.advanceLexer()
    }
    if (b.tokenType == RescriptTokenTypes.UIDENT) {
        b.advanceLexer()
    }

    // module body: `= { ... }` の中身を再帰パース
    skipToOpenBrace(b)
    if (b.tokenType == RescriptTokenTypes.LBRACE) {
        b.advanceLexer() // consume '{'
        while (!b.eof() && b.tokenType != RescriptTokenTypes.RBRACE) {
            parseTopLevel(b)
        }
        if (b.tokenType == RescriptTokenTypes.RBRACE) {
            b.advanceLexer() // consume '}'
        }
    } else {
        skipToEndOfDeclaration(b)
    }

    m.done(RescriptElementTypes.MODULE_DECLARATION)
}

/** `{` までのトークンをスキップ（`:`, `=`, 型制約などを飛ばす）*/
private fun skipToOpenBrace(b: PsiBuilder) {
    while (!b.eof()) {
        val t = b.tokenType
        if (t == RescriptTokenTypes.LBRACE) return
        if (isTopLevelStart(t)) return  // brace なしの module（= ModuleName）
        b.advanceLexer()
    }
}
```

**影響範囲:** パーサーのみ。レクサー、トークン定義、他の宣言パースに影響なし。
折りたたみ機能は PSI ツリーを走査するため、互換性に問題なし（module ノード内に子ノードが増えるだけ）。

### 2. RescriptStructureViewFactory（新規作成）

`PsiStructureViewFactory` を実装し、`plugin.xml` に登録するエントリポイント。

```
com.rescript.plugin.structure.RescriptStructureViewFactory
  implements PsiStructureViewFactory
  └── getStructureViewBuilder(psiFile)
        └── TreeBasedStructureViewBuilder を返す
              └── createStructureViewModel(editor)
                    └── RescriptStructureViewModel を返す
```

### 3. RescriptStructureViewModel（新規作成）

`TextEditorBasedStructureViewModel` を継承。ルート要素とソート機能を提供する。

- **ルート要素:** `RescriptStructureViewElement(psiFile)`
- **ソーター:** `Sorter.ALPHA_SORTER`（アルファベット順ソート）
- **表示対象:** `getSuitableClasses()` で `PsiElement::class.java` を返す（全要素をフィルタ候補に）

### 4. RescriptStructureViewElement（新規作成）

`StructureViewTreeElement` と `SortableTreeElement` を実装。各 PSI ノードをツリーアイテムとして表示する。

#### 名前の抽出ロジック

PSI ノードの子トークンから LIDENT / UIDENT を探す:

```kotlin
private fun extractName(element: PsiElement): String {
    val node = element.node
    var child = node.firstChildNode
    var afterKeyword = false
    while (child != null) {
        if (child.elementType in RescriptTokenTypes.TOP_LEVEL_KEYWORDS) {
            afterKeyword = true
        }
        // rec キーワードはスキップ
        if (child.elementType == RescriptTokenTypes.REC) {
            child = child.treeNext
            continue
        }
        if (afterKeyword && child.elementType in listOf(
                RescriptTokenTypes.LIDENT,
                RescriptTokenTypes.UIDENT,
                RescriptTokenTypes.UNDERSCORE
            )) {
            return child.text
        }
        child = child.treeNext
    }
    return "(anonymous)"
}
```

#### アイコンマッピング

| 要素タイプ | アイコン |
|-----------|---------|
| `LET_DECLARATION` | `AllIcons.Nodes.Function` |
| `TYPE_DECLARATION` | `AllIcons.Nodes.Type` |
| `MODULE_DECLARATION` | `AllIcons.Nodes.Module` |
| `EXTERNAL_DECLARATION` | `AllIcons.Nodes.PluginJB` |
| `EXCEPTION_DECLARATION` | `AllIcons.Nodes.ExceptionClass` |

#### 子要素の取得

```kotlin
override fun getChildren(): Array<TreeElement> {
    val psi = element ?: return emptyArray()

    // RescriptFile → トップレベル宣言を返す
    // MODULE_DECLARATION → ネストされた宣言を返す
    return psi.children
        .filter { it.node.elementType in NAVIGABLE_TYPES }
        .map { RescriptStructureViewElement(it) }
        .toTypedArray()
}

companion object {
    val NAVIGABLE_TYPES = setOf(
        RescriptElementTypes.LET_DECLARATION,
        RescriptElementTypes.TYPE_DECLARATION,
        RescriptElementTypes.MODULE_DECLARATION,
        RescriptElementTypes.EXTERNAL_DECLARATION,
        RescriptElementTypes.EXCEPTION_DECLARATION,
    )
}
```

#### ナビゲーション

`NavigatablePsiElement` の `navigate()` に委譲。PSI 要素自体がナビゲーション機能を持つ。

## ファイル構成

```
src/main/kotlin/com/rescript/plugin/
├── lang/
│   └── RescriptParser.kt                    # 修正: module 内再帰パース
└── structure/
    ├── RescriptStructureViewFactory.kt       # 新規
    ├── RescriptStructureViewModel.kt         # 新規
    └── RescriptStructureViewElement.kt       # 新規
```

## plugin.xml への登録

```xml
<!-- Structure view -->
<lang.psiStructureViewFactory language="ReScript"
    implementationClass="com.rescript.plugin.structure.RescriptStructureViewFactory"/>
```

## PSI ツリーの変化（パーサー修正後）

### 修正前（フラット）
```
FILE
├── MODULE_DECLARATION  "module Utils = { let add = ... let sub = ... }"
├── LET_DECLARATION     "let main = ..."
└── TYPE_DECLARATION    "type t = ..."
```

### 修正後（ネスト対応）
```
FILE
├── MODULE_DECLARATION  "module Utils = { ... }"
│   ├── LET_DECLARATION   "let add = ..."
│   └── LET_DECLARATION   "let sub = ..."
├── LET_DECLARATION     "let main = ..."
└── TYPE_DECLARATION    "type t = ..."
```

## テスト方針

- `./gradlew buildPlugin` でビルドが通ることを確認
- `./gradlew runIde` で手動確認:
  - Structure View にトップレベル宣言が表示される
  - module 内の宣言がネストされて表示される
  - クリックでエディタにジャンプする
  - アルファベット順ソートが機能する
