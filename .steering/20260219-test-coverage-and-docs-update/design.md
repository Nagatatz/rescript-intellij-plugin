# Design: テストカバレッジ拡充 & 公開ドキュメント更新

## 1. テスト設計

### テストアプローチ

既存テストのパターンに従い、JUnit 4 + スタブベースの軽量テストを作成する。`RescriptImportOptimizerTest` の `stubProxy()` / `stubAstNode()` ヘルパーを共通テストユーティリティとして抽出し、複数テストから再利用する。

### 共通テストユーティリティの抽出

`RescriptImportOptimizerTest` 内の以下のヘルパーを `RescriptTestUtils.kt` に抽出:

- `stubProxy<T>()` — 任意のインターフェースの軽量スタブ生成
- `stubAstNode(type)` — `ASTNode` スタブ生成
- `SimpleStubElement` — `PsiElement` のスタブ実装

**配置:** `src/test/kotlin/com/rescript/plugin/RescriptTestUtils.kt`

### 各テストの設計

#### 1. `RescriptNamesValidatorTest.kt`
- **対象メソッド:** `isIdentifier()`, `isKeyword()`
- **テスト方式:** 直接インスタンス化、`project` 引数は `null` 渡し（未使用のため）
- **テストケース:**
  - lident: `foo`, `_bar`, `camelCase`, `with_prime'`
  - uident: `Foo`, `Belt`, `MyModule`
  - 不正: `""`, `123`, `foo bar`, `-invalid`, `@attr`
  - キーワード: `let`, `type`, `module`, `switch` → true
  - 非キーワード: `foo`, `Belt`, `notAKeyword` → false

#### 2. `RescriptCommandTest.kt`
- **対象メソッド:** `fromId()`, enum プロパティ
- **テスト方式:** 直接呼び出し
- **テストケース:**
  - `fromId("build")` → `BUILD`
  - `fromId("build-watch")` → `BUILD_WATCH`
  - `fromId("clean")` → `CLEAN`
  - `fromId("unknown")` → `BUILD` (デフォルト)
  - 各エントリの `args` プロパティ検証

#### 3. `RescriptPsiUtilsTest.kt`
- **対象メソッド:** `extractName()`, `getIcon()`, `getElementDescription()`
- **テスト方式:** `stubAstNode()` + `SimpleStubElement` でPSIツリーを構築
- **テストケース:**
  - LET_DECLARATION + lident → 名前抽出
  - MODULE_DECLARATION + uident → 名前抽出
  - TYPE_DECLARATION → アイコン = `AllIcons.Nodes.Type`
  - EXTERNAL_DECLARATION → 説明 = "external declaration"
  - 子要素なし → "(anonymous)"
  - `rec` キーワードのスキップ確認

#### 4. `RescriptBreadcrumbsProviderTest.kt`
- **対象メソッド:** `getLanguages()`, `acceptElement()`, `getElementInfo()`
- **テスト方式:** スタブ PsiElement を使用
- **テストケース:**
  - `getLanguages()` が `RescriptLanguage` を含む
  - LET_DECLARATION → `acceptElement()` = true
  - STRING_VALUE トークン → `acceptElement()` = false
  - `getElementInfo()` が `RescriptPsiUtils.extractName()` と一致

#### 5. `RescriptStructureViewElementTest.kt`
- **対象メソッド:** `getAlphaSortKey()`, `getPresentation()`, `getChildren()`
- **テスト方式:** `NavigatablePsiElement` のスタブを構築
- **テストケース:**
  - `getAlphaSortKey()` が要素名を返す
  - `getPresentation()` のテキストとアイコン検証
  - `getChildren()` が NAVIGABLE_TYPES のみ返す

#### 6. `RescriptDuplicateOpenInspectionTest.kt`
- **対象:** 重複 open 検出の内部ロジック
- **テスト方式:** Inspection の `getDisplayName()` / `getGroupDisplayName()` 等のメタ情報、QuickFix の `getFamilyName()` 検証
- **テストケース:**
  - Inspection メタ情報の検証
  - QuickFix family name の検証

#### 7. `RescriptEmptyModuleInspectionTest.kt`
- **対象:** 空モジュール検出
- **テスト方式:** 同上
- **テストケース:**
  - Inspection メタ情報の検証
  - QuickFix family name の検証

#### 8. `RescriptPostfixTemplateProviderTest.kt`
- **対象メソッド:** `getTemplates()`, `isTerminalSymbol()`
- **テスト方式:** 直接インスタンス化
- **テストケース:**
  - `getTemplates()` が 7 テンプレートを返す
  - 各テンプレートの `key` 検証 (`switch`, `pipe`, `log`, `some`, `ok`, `error`, `ignore`)
  - `isTerminalSymbol('.')` → true
  - `isTerminalSymbol(',')` → false

## 2. ドキュメント更新設計

### README.md

Features セクションをカテゴリ別に整理:

```
## Features

### Core Language Support
- Syntax highlighting (JFlex lexer)
- Semantic highlighting (LSP semantic tokens)
- Code folding (blocks, comments, JSX, custom regions)
- Brace matching
- Line and block comments
- Structure view
- Breadcrumb navigation
- Spellchecking

### LSP Features
- Code completion
- Go to definition
- Hover documentation
- Find references
- Diagnostics
- Inlay hints
- Signature help
- Code Lens (type annotations)
- Rename refactoring

### Navigation
- Go to Symbol (Cmd+Option+O)
- Switch .res/.resi (Alt+O)
- Go to Related (.res/.resi/.js)
- Open Compiled JavaScript (Alt+Shift+J)
- Copy Qualified Name (Cmd+Shift+Alt+C)

### Code Editing
- Code formatting (rescript format CLI)
- Smart Enter (bracket/switch/pipe completion)
- Statement Up/Down Mover (Alt+Shift+Up/Down)
- Postfix Completion (.switch, .pipe, .log, .some, .ok, .error, .ignore)
- Live Templates (15 snippets)
- Surround With (if/switch/try/block)
- Smart quote completion

### Code Generation & Refactoring
- Create Interface File (.resi generation)
- Intention Actions (Wrap with Some/Ok/Error, Add @genType)
- Import Optimizer (remove duplicate open)
- Paste as JSON.t

### Inspections
- Duplicate open statement detection
- Empty module declaration detection
- Missing rescript.json warning

### Project Integration
- Run configuration (Build / Build Watch / Clean)
- Gutter run icons
- Compiler status widget
- Console output file path links
- File templates (Module, Interface, Component)
- New > ReScript File action
- rescript.json icon & JSON Schema
- TODO indexing
- Editor notification (LSP not found)

### Analysis
- reanalyze integration (dead code / exception analysis)

### Language Injection
- JavaScript highlighting in %raw() blocks
- Markdown code fence highlighting
```

### plugin.xml `<description>`

HTML 形式で主要機能カテゴリを簡潔にリスト化。README ほど詳細にせず、Marketplace での閲覧に適した粒度にする。

## 3. 影響範囲

### 新規ファイル
- `src/test/kotlin/com/rescript/plugin/RescriptTestUtils.kt`
- `src/test/kotlin/com/rescript/plugin/refactor/RescriptNamesValidatorTest.kt`
- `src/test/kotlin/com/rescript/plugin/run/RescriptCommandTest.kt`
- `src/test/kotlin/com/rescript/plugin/lang/psi/RescriptPsiUtilsTest.kt`
- `src/test/kotlin/com/rescript/plugin/breadcrumb/RescriptBreadcrumbsProviderTest.kt`
- `src/test/kotlin/com/rescript/plugin/structure/RescriptStructureViewElementTest.kt`
- `src/test/kotlin/com/rescript/plugin/inspection/RescriptDuplicateOpenInspectionTest.kt`
- `src/test/kotlin/com/rescript/plugin/inspection/RescriptEmptyModuleInspectionTest.kt`
- `src/test/kotlin/com/rescript/plugin/completion/RescriptPostfixTemplateProviderTest.kt`

### 変更ファイル
- `src/test/kotlin/com/rescript/plugin/imports/RescriptImportOptimizerTest.kt` — ヘルパーを `RescriptTestUtils` へ移動し import に変更
- `README.md` — Features セクション全面更新
- `src/main/resources/META-INF/plugin.xml` — `<description>` 更新

### プロダクションコードへの変更
なし
