# 設計: B 優先度機能一括実装 (21 件)

## 全体方針

- パーサー (`RescriptParser.kt`, `Rescript.flex`) の変更は行わない
- 既存の PSI ノード (`RescriptElementTypes`)、トークン (`RescriptTokenTypes`)、ユーティリティ (`RescriptPsiUtils`) を活用
- 各機能は独立したパッケージ/ファイルに配置し、`plugin.xml` に Extension Point として登録
- 既存コードパターン (S 優先度実装) に倣う
- LSP 依存機能 (#30, #35, #36) は LSP 未接続時のフォールバック動作を提供

---

## パーサー変更不要 `★` (14 件)

### #24 Backspace Handler (JSX ペア削除)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptBackspaceHandler.kt`

#### 設計

```
RescriptBackspaceHandler : BackspaceHandlerDelegate
  └── beforeCharDeleted(c, file, editor)
      ├── JSX 閉じタグ `</tag>` のペア削除
      │   カーソルが `<div>|</div>` の位置で `>` を削除 → `</div>` も削除
      └── テンプレートリテラル `${` 削除時に `}` も削除
```

#### 実装方針

- `BackspaceHandlerDelegate.beforeCharDeleted()` をオーバーライド
- 削除対象の文字が `>` の場合: 直前の文字列が `<tagName>` パターンか確認し、直後に `</tagName>` が続くなら閉じタグを自動削除
- 既存の `RescriptTypedHandler.extractJsxTagName()` のロジックを再利用可能

#### plugin.xml 登録

```xml
<backspaceHandlerDelegate
    implementation="com.rescript.plugin.editor.RescriptBackspaceHandler"/>
```

---

### #27 Copy/Paste Pre-Processor (文字列エスケープ)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptCopyPastePreProcessor.kt`

#### 設計

```
RescriptCopyPastePreProcessor : CopyPastePreProcessor
  ├── preprocessOnCopy() → null (変更なし)
  └── preprocessOnPaste(project, file, editor, text, rawText)
      ├── カーソルが STRING_VALUE 内 → 特殊文字をエスケープ
      │   " → \"、\ → \\、改行 → \n、タブ → \t
      └── それ以外 → text をそのまま返す
```

#### 実装方針

- `CopyPastePreProcessor` を実装
- `preprocessOnPaste()` でカーソル位置のトークンタイプを確認
- `STRING_VALUE` トークン内の場合のみエスケープ処理を適用
- テンプレートリテラル (`JS_STRING_OPEN`〜`JS_STRING_CLOSE` 間) 内では `$` と `` ` `` もエスケープ

#### plugin.xml 登録

```xml
<copyPastePreProcessor
    implementation="com.rescript.plugin.editor.RescriptCopyPastePreProcessor"/>
```

---

### #28 Inspection Suppression (コメント抑制)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/inspection/RescriptInspectionSuppressor.kt`

#### 設計

```
RescriptInspectionSuppressor : InspectionSuppressor
  ├── isSuppressedFor(element, toolId) → Boolean
  │   要素の直前の行コメントに "// noinspection <toolId>" があれば true
  └── getSuppressActions(element, toolId) → Array<SuppressQuickFix>
      └── SuppressForElementFix: 要素の前に "// noinspection <toolId>" コメントを挿入
```

#### 実装方針

- `InspectionSuppressor.isSuppressedFor()` で要素の直前のコメントをスキャン
- `// noinspection RescriptDuplicateOpen` のようなパターンを検出
- `// noinspection ALL` で全インスペクションを抑制
- `getSuppressActions()` で Quick Fix を提供し、コメント挿入を自動化

#### plugin.xml 登録

```xml
<lang.inspectionSuppressor language="ReScript"
    implementationClass="com.rescript.plugin.inspection.RescriptInspectionSuppressor"/>
```

---

### #29 Lookup Char Filter (補完中の文字制御)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/completion/RescriptLookupCharFilter.kt`

#### 設計

```
RescriptLookupCharFilter : CharFilter
  └── acceptChar(c, prefixLength, lookup) → Result?
      ├── '.' → ADD_TO_PREFIX (パイプチェーン継続)
      ├── '~' → ADD_TO_PREFIX (ラベル付き引数)
      ├── '(' → SELECT_ITEM_AND_FINISH_LOOKUP (確定+括弧)
      ├── ' ' → SELECT_ITEM_AND_FINISH_LOOKUP (確定+スペース)
      └── その他 → null (デフォルト動作)
```

#### 実装方針

- `CharFilter.acceptChar()` で文字ごとの動作を返す
- ReScript ファイルの場合のみ適用 (`lookup.psiFile is RescriptFile`)
- `.` 入力でパイプチェーンの補完を継続、`~` でラベル付き引数の補完を継続

#### plugin.xml 登録

```xml
<lookup.charFilter
    implementation="com.rescript.plugin.completion.RescriptLookupCharFilter"/>
```

---

### #31 Project View Node Decorator (ファイル装飾)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/projectview/RescriptProjectViewNodeDecorator.kt`

#### 設計

```
RescriptProjectViewNodeDecorator : ProjectViewNodeDecorator
  └── decorate(node, data)
      ├── .res ファイル + 対応する .resi あり → " (has interface)" サフィックス表示
      ├── @genType アノテーション含む → "@genType" バッジ表示
      └── rescript.json → バージョン番号をサフィックス表示
```

#### 実装方針

- `ProjectViewNodeDecorator.decorate()` で `PresentationData` を修正
- `.resi` の存在チェックは `VirtualFile.parent.findChild()` で軽量に実行
- `@genType` チェックはファイル先頭のテキストスキャン（File-Based Index #32 と連携可能）
- `rescript.json` のバージョンは JSON パースで取得

#### plugin.xml 登録

```xml
<projectViewNodeDecorator
    implementation="com.rescript.plugin.projectview.RescriptProjectViewNodeDecorator"/>
```

---

### #32 File-Based Index (open 文インデックス)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/indexing/RescriptOpenStatementIndex.kt`

#### 設計

```
RescriptOpenStatementIndex : ScalarIndexExtension<String>
  ├── getName() → ID("rescript.open.statements")
  ├── getIndexer() → DataIndexer
  │   └── map(inputData) → レクサーで OPEN + UIDENT パターンをスキャン
  │       結果: { "Belt.Array" → void, "Js.Promise" → void }
  ├── getKeyDescriptor() → EnumeratorStringDescriptor
  ├── getVersion() → 1
  └── getInputFilter() → .res / .resi ファイルのみ
```

#### 実装方針

- `ScalarIndexExtension<String>` を使用（open 文のモジュール名をキーとするインデックス）
- `DataIndexer.map()` でファイル内容をレクサーでスキャンし、`OPEN` キーワード後の `UIDENT` (+ `.UIDENT` チェーン) を収集
- 用途: 「このモジュールを open しているファイル一覧」の高速検索
- `FileBasedIndex.getInstance().getContainingFiles(ID, moduleName, scope)` で利用

#### plugin.xml 登録

```xml
<fileBasedIndex
    implementation="com.rescript.plugin.indexing.RescriptOpenStatementIndex"/>
```

---

### #33 Predefined Code Style (ReScript Standard)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/codestyle/RescriptPredefinedCodeStyle.kt`

#### 設計

```
RescriptPredefinedCodeStyle : PredefinedCodeStyle("ReScript Standard", RescriptLanguage)
  └── apply(settings, language)
      ├── indentOptions.INDENT_SIZE = 2
      ├── indentOptions.CONTINUATION_INDENT_SIZE = 2
      ├── indentOptions.TAB_SIZE = 2
      └── indentOptions.USE_TAB_CHARACTER = false
```

#### 実装方針

- `PredefinedCodeStyle` を継承し、`rescript format` と一致するインデント設定をプリセットとして提供
- Settings > Code Style > ReScript の「Set from Predefined Style」から選択可能

#### plugin.xml 登録

```xml
<predefinedCodeStyle
    implementation="com.rescript.plugin.codestyle.RescriptPredefinedCodeStyle"/>
```

---

### #34 Element Description Provider

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/lang/RescriptElementDescriptionProvider.kt`

#### 設計

```
RescriptElementDescriptionProvider : ElementDescriptionProvider
  └── getElementDescription(element, location) → String?
      ├── LET_DECLARATION → "function 'name'" / "value 'name'"
      ├── TYPE_DECLARATION → "type 'name'"
      ├── MODULE_DECLARATION → "module 'Name'"
      ├── EXTERNAL_DECLARATION → "external 'name'"
      ├── EXCEPTION_DECLARATION → "exception 'Name'"
      ├── OPEN_STATEMENT → "open 'ModulePath'"
      └── RescriptFile → "file 'FileName.res'"
```

#### 実装方針

- `ElementDescriptionProvider` を実装し、`ElementDescriptionLocation` に応じた説明テキストを返す
- リファクタリングダイアログ、Find Usages ヘッダー等で使用される
- 既存の `RescriptPsiUtils.extractName()` と `getElementDescription()` を活用

#### plugin.xml 登録

```xml
<elementDescriptionProvider
    implementation="com.rescript.plugin.lang.RescriptElementDescriptionProvider"/>
```

---

### #37 Paste as JSX (HTML → ReScript JSX 変換)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/paste/RescriptPasteAsJsxProcessor.kt`

#### 設計

```
RescriptPasteAsJsxProcessor : CopyPastePostProcessor<TextBlockTransferableData>
  ├── collectTransferableData() → 空 (コピー時は何もしない)
  ├── extractTransferableData() → クリップボードデータ抽出
  └── processTransferableData()
      ├── HTML かどうかを判定 (< タグ検出)
      ├── 属性名変換: class→className, onclick→onClick, for→htmlFor, etc.
      ├── style 属性変換: style="color: red" → style={ReactDOM.Style.make(~color="red", ())}
      ├── 自己閉じタグ: <br> → <br />, <img ...> → <img ... />
      └── 変換結果の通知バルーン表示 (Undo 可能)
```

#### 実装方針

- `CopyPastePostProcessor` を実装
- HTML 属性名の変換マップ: `class→className`, `for→htmlFor`, `onclick→onClick`, `onchange→onChange` 等
- 正規表現でタグと属性をパース (完全な HTML パーサーは不要、実用的な範囲で)
- void 要素 (`br`, `hr`, `img`, `input`, `meta`, `link`) の自己閉じ化
- 変換適用後に Notification で通知 (「HTML was converted to ReScript JSX」)

#### plugin.xml 登録

```xml
<copyPastePostProcessor
    implementation="com.rescript.plugin.paste.RescriptPasteAsJsxProcessor"/>
```

---

### #38 Package Dependencies View (rescript.json)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/dependencies/RescriptDependenciesToolWindowFactory.kt`
- `src/main/kotlin/com/rescript/plugin/dependencies/RescriptDependenciesPanel.kt`

#### 設計

```
RescriptDependenciesToolWindowFactory : ToolWindowFactory, DumbAware
  └── createToolWindowContent(project, toolWindow)
      └── RescriptDependenciesPanel(project)
          ├── ツリー構造:
          │   ├── bs-dependencies
          │   │   ├── @rescript/react (1.0.0) [node_modules から取得]
          │   │   └── @rescript/core (1.0.0)
          │   ├── bs-dev-dependencies
          │   │   └── @rescript/tools (0.6.0)
          │   └── pinned-dependencies
          │       └── ...
          ├── ダブルクリック → node_modules 内のパッケージフォルダを開く
          └── リフレッシュボタン → rescript.json 再読み込み
```

#### 実装方針

- `ToolWindowFactory` + `JBTreeTable` / `Tree` でツリー表示
- `rescript.json` を Gson でパースし、`bs-dependencies`, `bs-dev-dependencies`, `pinned-dependencies` を取得
- 各依存パッケージのバージョンは `node_modules/<pkg>/package.json` の `version` フィールドから取得
- `VirtualFileListener` で `rescript.json` 変更を監視し、自動リフレッシュ

#### plugin.xml 登録

```xml
<toolWindow id="ReScript Dependencies" anchor="right"
    icon="AllIcons.Nodes.PpLib"
    factoryClass="com.rescript.plugin.dependencies.RescriptDependenciesToolWindowFactory"/>
```

---

### #39 VCS Code Vision (宣言上の VCS 情報)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/codevision/RescriptVcsCodeVisionContext.kt`

#### 設計

```
RescriptVcsCodeVisionContext : VcsCodeVisionLanguageContext
  ├── isAccepted(element) → NAVIGABLE_TYPES に含まれる要素のみ true
  └── handleClick(mouseEvent, editor, element) → デフォルト動作
```

#### 実装方針

- `VcsCodeVisionLanguageContext` を実装し、宣言要素 (`LET_DECLARATION`, `TYPE_DECLARATION`, `MODULE_DECLARATION`, `EXTERNAL_DECLARATION`, `EXCEPTION_DECLARATION`) を VCS Code Vision の対象として登録
- IntelliJ Platform が自動的に「Last changed by X, N days ago」情報を表示
- 既存の `RescriptPsiUtils.NAVIGABLE_TYPES` を活用

#### plugin.xml 登録

```xml
<vcs.codeVisionLanguageContext language="ReScript"
    implementationClass="com.rescript.plugin.codevision.RescriptVcsCodeVisionContext"/>
```

---

### #40 Reader Mode (node_modules 読取専用)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptReaderModeMatcher.kt`

#### 設計

```
RescriptReaderModeMatcher : ReaderModeMatcher
  └── matches(project, file, editor, mode) → Boolean
      └── file.path に "node_modules" が含まれ、かつ .res/.resi ファイル → true
```

#### 実装方針

- `ReaderModeMatcher.matches()` でファイルパスをチェック
- `node_modules/` 配下の `.res` / `.resi` ファイルを Reader Mode 対象に
- Reader Mode ではフォント拡大、行間調整等の読みやすい表示が適用される

#### plugin.xml 登録

```xml
<readerModeMatcher
    implementation="com.rescript.plugin.editor.RescriptReaderModeMatcher"/>
```

---

### #41 Color Preview in Gutter (色リテラル)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptColorProvider.kt`

#### 設計

```
RescriptColorProvider : ElementColorProvider
  ├── getColorFrom(element) → Color?
  │   STRING_VALUE トークンの内容が色コードの場合に Color を返す
  │   ├── "#RRGGBB" / "#RRGGBBAA" / "#RGB" → hex パース
  │   ├── "rgb(R, G, B)" / "rgba(R, G, B, A)" → 数値パース
  │   └── "hsl(H, S%, L%)" → HSL→RGB 変換
  └── setColorTo(element, color) → PsiElement
      └── 色リテラル文字列を新しい色の hex に置換
```

#### 実装方針

- `ElementColorProvider.getColorFrom()` で `STRING_VALUE` トークンの内容を正規表現でスキャン
- hex パターン: `#([0-9a-fA-F]{3,8})`
- rgb パターン: `rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)(?:\s*,\s*([\d.]+))?\s*\)`
- hsl パターン: `hsla?\(\s*(\d+)\s*,\s*(\d+)%\s*,\s*(\d+)%(?:\s*,\s*([\d.]+))?\s*\)`
- ガターアイコンとしてカラースウォッチを表示、クリックで ColorChooser ダイアログ

#### plugin.xml 登録

```xml
<colorProvider
    implementation="com.rescript.plugin.editor.RescriptColorProvider"/>
```

---

### #42 Auto Import Options (open 設定 UI)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/imports/RescriptAutoImportOptionsProvider.kt`

#### 設計

```
RescriptAutoImportOptionsProvider : AutoImportOptionsProvider
  └── createComponent() → JPanel
      ├── チェックボックス: "Optimize imports on the fly" (自動 import 最適化)
      ├── チェックボックス: "Add open statements automatically" (自動 open 追加)
      └── テキストフィールド: "Exclude modules" (除外モジュール、カンマ区切り)
```

#### 実装方針

- `AutoImportOptionsProvider` を実装し、Settings > Editor > General > Auto Import に ReScript セクションを追加
- 設定値は `RescriptProjectSettings` に追加:
  - `autoOptimizeImports: Boolean = false`
  - `autoAddOpenStatements: Boolean = true`
  - `excludedModules: String = ""`
- 設定値は既存の `RescriptImportOptimizer` で参照

#### plugin.xml 登録

```xml
<autoImportOptionsProvider
    instance="com.rescript.plugin.imports.RescriptAutoImportOptionsProvider"/>
```

---

## トークンレベル工夫 `▲` (4 件)

### #22 Move Element Left/Right (引数並替)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptMoveElementHandler.kt`

#### 設計

```
RescriptMoveElementHandler : MoveElementLeftRightHandler
  └── getMovableSubElements(element) → Array<PsiElement>
      カンマ区切りの子要素を返す（関数引数、配列要素、レコードフィールド、タプル要素）
```

#### 実装方針

- カーソル位置から最寄りの括弧ペア (`()`, `[]`, `{}`) を特定
- 括弧内のカンマ (`COMMA`) 区切りの要素を `PsiElement` の配列として返す
- ネストした括弧を考慮してバランス解析を行う
- 要素の範囲は前のカンマ/開き括弧から次のカンマ/閉じ括弧まで
- トークンスキャンで実装 (PSI 宣言ノードではなく、テキスト範囲ベース)

#### plugin.xml 登録

```xml
<moveLeftRightHandler language="ReScript"
    implementationClass="com.rescript.plugin.editor.RescriptMoveElementHandler"/>
```

---

### #23 Usage Type Provider (用途別グループ化)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/lang/RescriptUsageTypeProvider.kt`

#### 設計

```
RescriptUsageTypeProvider : UsageTypeProvider
  └── getUsageType(element) → UsageType?
      ├── 直前トークンが OPEN → "Open statement"
      ├── 直前トークンが TYPE → "Type reference"
      ├── 直前トークンが COLON → "Type annotation"
      ├── 親が JSX 要素内 → "JSX attribute"
      ├── 直前トークンが PIPE → "Pattern match"
      ├── 直前トークンが EQ (let の右辺) → "Value reference"
      └── デフォルト → null (汎用グループ)
```

#### 実装方針

- `UsageTypeProvider.getUsageType()` で要素の周辺トークンを分析
- 前後のトークン/キーワードから使用コンテキストを推測
- `UsageType` のカスタムインスタンスを返す

#### plugin.xml 登録

```xml
<usageTypeProvider
    implementation="com.rescript.plugin.lang.RescriptUsageTypeProvider"/>
```

---

### #25 Code Block Support Handler (ブロック間移動)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptCodeBlockHandler.kt`

#### 設計

```
RescriptCodeBlockHandler : CodeBlockSupportHandler
  ├── getTopLevelElement(element) → PsiElement?
  │   最寄りの宣言ノード (NAVIGABLE_TYPES) を返す
  └── getCodeBlockStart(element, offset) → TextRange?
      ├── switch の `|` アーム間ナビゲーション
      ├── if/else if/else ブロック間移動
      └── try/catch 間移動
```

#### 実装方針

- `CodeBlockSupportHandler` を実装
- キーワードトークン (`SWITCH`, `IF`, `ELSE`, `TRY`, `CATCH`) の位置をトークンスキャンで特定
- `|` (PIPE) トークンの位置から switch アームの先頭を特定
- ブレース (`{`, `}`) バランスを考慮してブロック境界を判定

#### plugin.xml 登録

```xml
<codeBlockSupportHandler language="ReScript"
    implementationClass="com.rescript.plugin.editor.RescriptCodeBlockHandler"/>
```

---

### #26 Split/Join List (1行 ⇔ 複数行)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptListSplitJoinContext.kt`

#### 設計

```
RescriptListSplitJoinContext : ListSplitJoinContext
  └── extractElements(element)
      カンマ区切りの要素リストを返す:
      ├── 関数引数: (a, b, c)
      ├── 配列リテラル: [1, 2, 3]
      ├── レコード: {name: "foo", age: 25}
      └── タプル: (1, "a", true)

Split: (a, b, c) →
  (
    a,
    b,
    c,
  )

Join: 逆変換
```

#### 実装方針

- `ListSplitJoinContext` を実装
- カーソル位置から最寄りの括弧ペアを特定 (トークンスキャン)
- カンマ区切り要素をリスト化し、Split/Join のテキスト変換を実行
- Split 時はインデントを追加し、末尾カンマを付与 (ReScript スタイル)
- Join 時はインデントと末尾カンマを除去

#### plugin.xml 登録

```xml
<listSplitJoinContext language="ReScript"
    implementationClass="com.rescript.plugin.editor.RescriptListSplitJoinContext"/>
```

---

## LSP/パーサー依存 `●` (3 件)

### #30 Quick Documentation Provider

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/documentation/RescriptDocumentationProvider.kt`

#### 設計

```
RescriptDocumentationProvider : AbstractDocumentationProvider
  ├── generateDoc(element, originalElement) → String?
  │   ├── LSP 接続時: textDocument/hover で型情報+ドキュメントを取得
  │   │   → Markdown を HTML に変換してレンダリング
  │   └── LSP 未接続時: PSI ベースのフォールバック
  │       → 宣言タイプ + 名前 + ファイルパスを表示
  ├── getQuickNavigateInfo(element, originalElement) → String?
  │   → 宣言の1行要約 ("let foo: int → string")
  └── generateHoverDoc(element, originalElement) → String?
      → generateDoc と同じ (Ctrl+Q / ホバー両対応)
```

#### 実装方針

- `AbstractDocumentationProvider` を継承
- LSP 接続時: `LspServerManager` から running サーバーを取得し、`textDocument/hover` リクエストを送信
- LSP のホバーレスポンス (`MarkupContent`) の Markdown を `HtmlChunk` に変換
- コードブロック部分に ReScript シンタックスハイライトを適用
- LSP 未接続時のフォールバック: `RescriptPsiUtils.extractName()` + `getElementDescription()` で基本情報を表示

#### plugin.xml 登録

```xml
<lang.documentationProvider language="ReScript"
    implementationClass="com.rescript.plugin.documentation.RescriptDocumentationProvider"/>
```

---

### #35 Safe Delete (使用箇所確認付き削除)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/refactor/RescriptSafeDeleteProcessor.kt`

#### 設計

```
RescriptSafeDeleteProcessor : SafeDeleteProcessorDelegate
  ├── handlesElement(element) → NAVIGABLE_TYPES の要素のみ true
  ├── findUsages(element, allElementsDelete, usages) → NonCodeUsageSearchInfo?
  │   ├── LSP 接続時: textDocument/references で使用箇所を検索
  │   └── LSP 未接続時: WordsScanner ベースの検索にフォールバック
  ├── getElementsToSearch(element, module, allElementsToDelete) → Collection<PsiElement>?
  ├── getAdditionalElementsToDelete(element, allElementsToDelete, askUser) → Collection<PsiElement>?
  └── prepareForRefactoring() → UsageInfo[] のフィルタリング
```

#### 実装方針

- `SafeDeleteProcessorDelegate` を実装
- `findUsages()` で LSP の `textDocument/references` を使用して使用箇所を検索
- 使用箇所が 0 件なら安全に削除、存在すれば確認ダイアログを表示
- LSP 未接続時は `FindUsagesProvider` の `WordsScanner` を使ったテキスト検索にフォールバック
- 削除対象は宣言全体 (PSI ノード) + 改行

#### plugin.xml 登録

```xml
<refactoring.safeDeleteProcessor
    implementation="com.rescript.plugin.refactor.RescriptSafeDeleteProcessor"/>
```

---

### #36 Name Suggestion Provider (名前候補)

#### ファイル構成

- `src/main/kotlin/com/rescript/plugin/refactor/RescriptNameSuggestionProvider.kt`

#### 設計

```
RescriptNameSuggestionProvider : NameSuggestionProvider
  └── getSuggestedNames(element, nameSuggestionContext, result) → SuggestedNameInfo?
      ├── LSP 接続時: textDocument/hover から型情報を取得
      │   型名ベースの候補: User.t → "user", array<Item.t> → "items"
      ├── PSI ベースの候補:
      │   宣言タイプ: MODULE_DECLARATION → "myModule", TYPE_DECLARATION → "myType"
      └── コンテキストベース:
          ファイル名から: "UserProfile.res" → "userProfile"
```

#### 実装方針

- `NameSuggestionProvider` を実装
- LSP 接続時: `textDocument/hover` の型情報から名前候補を生成
  - `User.t` → `user`, `array<string>` → `strings`, `option<int>` → `maybeInt`
- PSI ベース: 要素のタイプと周辺コンテキストから候補を生成
- camelCase / snake_case の変換ヘルパーを含む

#### plugin.xml 登録

```xml
<nameSuggestionProvider
    implementation="com.rescript.plugin.refactor.RescriptNameSuggestionProvider"/>
```

---

## テスト方針

各機能に対して `src/test/kotlin/com/rescript/plugin/` 配下にテストクラスを作成。

| # | 機能 | テストクラス | テスト内容 |
|---|------|-------------|-----------|
| 22 | Move Element Left/Right | `editor/RescriptMoveElementHandlerTest.kt` | カンマ区切り要素の検出・移動ロジック |
| 23 | Usage Type Provider | `lang/RescriptUsageTypeProviderTest.kt` | 周辺トークンから使用タイプ判定 |
| 24 | Backspace Handler | `editor/RescriptBackspaceHandlerTest.kt` | JSX ペア削除のテキスト変換 |
| 25 | Code Block Support | `editor/RescriptCodeBlockHandlerTest.kt` | ブロック境界の特定 |
| 26 | Split/Join List | `editor/RescriptListSplitJoinContextTest.kt` | 1行⇔複数行変換 |
| 27 | Copy/Paste Pre-Processor | `editor/RescriptCopyPastePreProcessorTest.kt` | 文字列エスケープ処理 |
| 28 | Inspection Suppression | `inspection/RescriptInspectionSuppressorTest.kt` | noinspection コメント検出 |
| 29 | Lookup Char Filter | `completion/RescriptLookupCharFilterTest.kt` | 文字ごとの Result 判定 |
| 30 | Quick Documentation | `documentation/RescriptDocumentationProviderTest.kt` | テスト省略理由: LSP 結合必須 |
| 31 | Node Decorator | `projectview/RescriptProjectViewNodeDecoratorTest.kt` | 装飾ロジック |
| 32 | File-Based Index | `indexing/RescriptOpenStatementIndexTest.kt` | open 文パターンの抽出 |
| 33 | Predefined Code Style | `codestyle/RescriptPredefinedCodeStyleTest.kt` | インデント設定値の検証 |
| 34 | Element Description | `lang/RescriptElementDescriptionProviderTest.kt` | 説明テキスト生成 |
| 35 | Safe Delete | `refactor/RescriptSafeDeleteProcessorTest.kt` | テスト省略理由: LSP 結合必須 |
| 36 | Name Suggestion | `refactor/RescriptNameSuggestionProviderTest.kt` | 型名→変数名候補の変換 |
| 37 | Paste as JSX | `paste/RescriptPasteAsJsxProcessorTest.kt` | HTML→JSX 変換ロジック |
| 38 | Dependencies View | `dependencies/RescriptDependenciesPanelTest.kt` | テスト省略理由: UI コンポーネント |
| 39 | VCS Code Vision | `codevision/RescriptVcsCodeVisionContextTest.kt` | 要素受理判定 |
| 40 | Reader Mode | `editor/RescriptReaderModeMatcherTest.kt` | パスパターン判定 |
| 41 | Color Preview | `editor/RescriptColorProviderTest.kt` | 色パース (#hex, rgb, hsl) |
| 42 | Auto Import Options | `imports/RescriptAutoImportOptionsProviderTest.kt` | テスト省略理由: UI コンポーネント |

**テスト省略理由:**
- #30, #35: LSP サーバーとの結合が必須。フォールバック部分のユニットテストは作成する
- #38, #42: Swing UI コンポーネント中心のため単体テスト困難

---

## plugin.xml 登録一覧

21 件の Extension Point 登録を `plugin.xml` の `<extensions>` セクションに追加:

```xml
<!-- B-Priority: Editing -->
<backspaceHandlerDelegate implementation="com.rescript.plugin.editor.RescriptBackspaceHandler"/>
<copyPastePreProcessor implementation="com.rescript.plugin.editor.RescriptCopyPastePreProcessor"/>
<moveLeftRightHandler language="ReScript" implementationClass="com.rescript.plugin.editor.RescriptMoveElementHandler"/>
<codeBlockSupportHandler language="ReScript" implementationClass="com.rescript.plugin.editor.RescriptCodeBlockHandler"/>
<listSplitJoinContext language="ReScript" implementationClass="com.rescript.plugin.editor.RescriptListSplitJoinContext"/>

<!-- B-Priority: Completion -->
<lookup.charFilter implementation="com.rescript.plugin.completion.RescriptLookupCharFilter"/>

<!-- B-Priority: Code Analysis -->
<lang.inspectionSuppressor language="ReScript" implementationClass="com.rescript.plugin.inspection.RescriptInspectionSuppressor"/>

<!-- B-Priority: Documentation -->
<lang.documentationProvider language="ReScript" implementationClass="com.rescript.plugin.documentation.RescriptDocumentationProvider"/>
<predefinedCodeStyle implementation="com.rescript.plugin.codestyle.RescriptPredefinedCodeStyle"/>

<!-- B-Priority: Find Usages -->
<elementDescriptionProvider implementation="com.rescript.plugin.lang.RescriptElementDescriptionProvider"/>
<usageTypeProvider implementation="com.rescript.plugin.lang.RescriptUsageTypeProvider"/>

<!-- B-Priority: Project View -->
<projectViewNodeDecorator implementation="com.rescript.plugin.projectview.RescriptProjectViewNodeDecorator"/>

<!-- B-Priority: Indexing -->
<fileBasedIndex implementation="com.rescript.plugin.indexing.RescriptOpenStatementIndex"/>

<!-- B-Priority: Refactoring -->
<refactoring.safeDeleteProcessor implementation="com.rescript.plugin.refactor.RescriptSafeDeleteProcessor"/>
<nameSuggestionProvider implementation="com.rescript.plugin.refactor.RescriptNameSuggestionProvider"/>

<!-- B-Priority: Paste -->
<copyPastePostProcessor implementation="com.rescript.plugin.paste.RescriptPasteAsJsxProcessor"/>

<!-- B-Priority: Tool Window -->
<toolWindow id="ReScript Dependencies" anchor="right" icon="AllIcons.Nodes.PpLib"
    factoryClass="com.rescript.plugin.dependencies.RescriptDependenciesToolWindowFactory"/>

<!-- B-Priority: IDE Integration -->
<vcs.codeVisionLanguageContext language="ReScript" implementationClass="com.rescript.plugin.codevision.RescriptVcsCodeVisionContext"/>
<readerModeMatcher implementation="com.rescript.plugin.editor.RescriptReaderModeMatcher"/>
<colorProvider implementation="com.rescript.plugin.editor.RescriptColorProvider"/>
<autoImportOptionsProvider instance="com.rescript.plugin.imports.RescriptAutoImportOptionsProvider"/>
```

---

## 新規ファイル一覧 (24 ファイル)

### ソース (23 ファイル)

| パッケージ | ファイル名 |
|-----------|-----------|
| `editor/` | `RescriptBackspaceHandler.kt` |
| `editor/` | `RescriptCopyPastePreProcessor.kt` |
| `editor/` | `RescriptMoveElementHandler.kt` |
| `editor/` | `RescriptCodeBlockHandler.kt` |
| `editor/` | `RescriptListSplitJoinContext.kt` |
| `editor/` | `RescriptReaderModeMatcher.kt` |
| `editor/` | `RescriptColorProvider.kt` |
| `completion/` | `RescriptLookupCharFilter.kt` |
| `inspection/` | `RescriptInspectionSuppressor.kt` |
| `codestyle/` | `RescriptPredefinedCodeStyle.kt` |
| `lang/` | `RescriptElementDescriptionProvider.kt` |
| `lang/` | `RescriptUsageTypeProvider.kt` |
| `indexing/` | `RescriptOpenStatementIndex.kt` |
| `documentation/` | `RescriptDocumentationProvider.kt` |
| `refactor/` | `RescriptSafeDeleteProcessor.kt` |
| `refactor/` | `RescriptNameSuggestionProvider.kt` |
| `paste/` | `RescriptPasteAsJsxProcessor.kt` |
| `dependencies/` | `RescriptDependenciesToolWindowFactory.kt` |
| `dependencies/` | `RescriptDependenciesPanel.kt` |
| `codevision/` | `RescriptVcsCodeVisionContext.kt` |
| `imports/` | `RescriptAutoImportOptionsProvider.kt` |
| `projectview/` | `RescriptProjectViewNodeDecorator.kt` |

### 変更ファイル

| ファイル | 変更内容 |
|---------|---------|
| `plugin.xml` | 21 Extension Point 登録追加 |
| `RescriptProjectSettings.kt` | Auto Import オプション用設定フィールド追加 |

### テスト (18 ファイル)

上記テスト方針の表に記載の 18 テストクラス (省略 3 件を除く)。
