# 各ウィンドウへの命令文

## 前提

各ウィンドウは `/Users/ngtz/Documents/repos/rescript-intellij-plugin` で Claude Code が起動済み。
命令文の冒頭で worktree ディレクトリへ `cd` する。

| ウィンドウ | worktree パス | ブランチ |
|-----------|-------------|---------|
| Window 1 | `../rescript-wt-reanalyze` | `feature/reanalyze` |
| Window 2 | `../rescript-wt-markdown` | `feature/markdown-highlight` |
| Window 3 | `../rescript-wt-paste-json` | `feature/paste-as-json` |
| Window 4 | `../rescript-wt-region-fold` | `feature/region-folding` |
| Window 5 | `../rescript-wt-incremental-tc` | `feature/incremental-tc` |

**共有インフラ:** なし（全機能が独立したローカル実装）

---

## Window 1: reanalyze 統合

```
cd /Users/ngtz/Documents/repos/rescript-wt-reanalyze

ブランチ `feature/reanalyze` で reanalyze 統合を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-reanalyze/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- reanalyze（デッドコード分析・未処理例外分析）ツールを IDE 内で実行
- `rescript-tools.exe` を `node_modules/rescript/` から自動検出（親ディレクトリ遡り対応）
- ファイル保存時に reanalyze を実行し、結果を警告として表示
- バイナリが見つからない場合はサイレントに無効化

### design.md の要約
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/analysis/RescriptReanalyzeAnnotator.kt`
- 変更ファイル: `plugin.xml`
- `ExternalAnnotator<CollectedInfo, AnnotationResult>` を継承:
  - フェーズ1 `collectInformation()`: ReScript ファイルのパスとプロジェクトベースパスを収集
  - フェーズ2 `doAnnotate()`: `rescript-tools.exe reanalyze -json` を実行、JSON 出力をパース、当該ファイルの diagnostics をフィルタ
  - フェーズ3 `apply()`: diagnostics を `AnnotationHolder` に Warning として登録
- rescript-tools.exe の検出: `node_modules/rescript/rescript-tools.exe` を検索（`RescriptCliDetector` と同様のパターンで親ディレクトリ遡り）
  - macOS/Linux: `node_modules/rescript/rescript-tools.exe`（ReScript 12+ は platform-specific binary）
  - 代替: `node_modules/.bin/rescript-tools`
- JSON パース: `com.google.gson.JsonParser`（IntelliJ バンドル）を使用
- JSON フォーマット: `[{"name": "...", "kind": "warning", "file": "...", "range": [startLine, startChar, endLine, endChar], "message": "..."}]`
- plugin.xml: `<externalAnnotator language="ReScript" implementationClass="...">`
- テスト: ツール検出ロジック + JSON パース + AnnotationResult 生成のテスト

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add reanalyze integration for dead code analysis`

## ステップ 5: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 2: Markdown ReScript ハイライト

```
cd /Users/ngtz/Documents/repos/rescript-wt-markdown

ブランチ `feature/markdown-highlight` で Markdown ReScript ハイライトを実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-markdown-highlight/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- Markdown ファイル内の ` ```rescript ` コードブロックで ReScript ハイライトを有効化
- ` ```res ` / ` ```resi ` もサポート
- Markdown プラグイン未インストール時は機能無効化（optional dependency）

### design.md の要約
- 新規ファイル:
  - `src/main/kotlin/com/rescript/plugin/injection/RescriptMarkdownCodeFenceProvider.kt`
  - `src/main/resources/META-INF/rescript-markdown.xml`
- 変更ファイル:
  - `src/main/resources/META-INF/plugin.xml`（optional dependency 追加）
  - `build.gradle.kts`（`bundledPlugin("org.intellij.plugins.markdown")` 追加）
- `CodeFenceLanguageProvider` を実装:
  - `getLanguageByInfoString()`: "rescript", "res", "resi" → `RescriptLanguage.INSTANCE`
  - `getCompletionVariantsForInfoString()`: "rescript" を補完候補に
- rescript-markdown.xml: `<fenceLanguageProvider>` を `org.intellij.plugins.markdown` namespace で登録
- plugin.xml に `<depends optional="true" config-file="rescript-markdown.xml">org.intellij.plugins.markdown</depends>` を追加
- build.gradle.kts の `intellijPlatform { }` 内の `dependencies` ブロックに `bundledPlugin("org.intellij.plugins.markdown")` を追加
- 既存パターン参考: `rescript-js-injection.xml`（optional dependency の分離 XML パターン）
- テスト: `getLanguageByInfoString()` のマッピングテスト

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add ReScript syntax highlighting in Markdown code fences`

## ステップ 5: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 3: Paste as JSON.t

```
cd /Users/ngtz/Documents/repos/rescript-wt-paste-json

ブランチ `feature/paste-as-json` で Paste as JSON.t アクションを実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-paste-as-json/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- Edit メニューと右クリックメニューに「Paste as JSON.t」アクションを追加
- クリップボードの JSON を ReScript の `JSON.t` 型表現に変換してペースト
- ReScript ファイルでのみ有効
- 不正な JSON の場合はエラー通知

### design.md の要約
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/paste/RescriptPasteAsJsonAction.kt`
- 変更ファイル: `plugin.xml`
- `AnAction` を継承:
  - `actionPerformed()`:
    1. `CopyPasteManager.getInstance().getContents<String>(DataFlavor.stringFlavor)` でクリップボード取得
    2. `isLikelyJson()` で JSON 判定（`{` or `[` で開始）
    3. `com.google.gson.JsonParser.parseString()` でパース
    4. 再帰的に変換:
       - String → `JSON.String("...")`
       - Number → `JSON.Number(5.)` （float notation: 整数の場合は末尾に `.` を付与）
       - Boolean → `JSON.Boolean(true|false)`
       - Null → `JSON.Null`
       - Array → `JSON.Array([item1, item2])`
       - Object → `JSON.Object(dict{"key": value})`
    5. `WriteCommandAction` でカーソル位置に挿入
  - `update()`: ReScript ファイルでのみ `isEnabledAndVisible = true`
- plugin.xml: `<action>` を `EditorPopupMenu` と `EditMenu` に追加
- テスト: JSON → ReScript 変換の各ケース（string, number, boolean, null, array, object, nested, escape）

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add Paste as JSON.t action`

## ステップ 5: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 4: `//#region` 折りたたみ

```
cd /Users/ngtz/Documents/repos/rescript-wt-region-fold

ブランチ `feature/region-folding` で //#region 折りたたみを実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-region-folding/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- `//#region [名前]` と `//#endregion` コメントマーカーによるカスタム折りたたみ
- `// #region` / `// #endregion`（スペースあり）もサポート
- ネストした region をサポート
- 既存の折りたたみに影響しない

### design.md の要約
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/folding/RescriptCustomFoldingProvider.kt`
- 変更ファイル:
  - `src/main/kotlin/com/rescript/plugin/folding/RescriptFoldingBuilder.kt`（`FoldingBuilderEx` → `CustomFoldingBuilder` へ変更）
  - `src/main/resources/META-INF/plugin.xml`
  - `src/test/kotlin/com/rescript/plugin/folding/RescriptFoldingBuilderTest.kt`
- `CustomFoldingProvider` を継承:
  - `isCustomRegionStart()`: `//#region` or `// #region` を検出
  - `isCustomRegionEnd()`: `//#endregion` or `// #endregion` を検出
  - `getPlaceholderText()`: region 名（なければ "..."）
  - `getDescription()`: "//#region ... //#endregion"
  - `getStartString()` / `getEndString()`: "//#region" / "//#endregion"
- `RescriptFoldingBuilder` を `FoldingBuilderEx` から `CustomFoldingBuilder` に変更:
  - `buildFoldRegions()` → `buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, ...)` に変更（戻り値が Array ではなく MutableList に追加する形式）
  - `getPlaceholderText(node: ASTNode)` → `getLanguagePlaceholderText(node: ASTNode, range: TextRange): String?` に変更
  - `isCollapsedByDefault(node: ASTNode)` → `isRegionCollapsedByDefault(node: ASTNode): Boolean` に変更
  - `isCustomFoldingCandidate(node: ASTNode): Boolean` を追加: `node.elementType == RescriptTokenTypes.SINGLE_COMMENT` の場合に true を返す
- plugin.xml: `<customFoldingProvider implementation="..."/>` を追加
- テスト: 既存テストの API 変更対応 + region 折りたたみテスト追加

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add //#region folding support`

## ステップ 5: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 5: Incremental Type Checking 設定

```
cd /Users/ngtz/Documents/repos/rescript-wt-incremental-tc

ブランチ `feature/incremental-tc` で Incremental Type Checking 設定を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-incremental-tc/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- Settings > Languages & Frameworks > ReScript に「Enable incremental type checking」チェックボックス追加
- デフォルトで有効
- 設定変更が LSP 初期化オプションに反映される
- 設定変更後に LSP サーバーが自動再起動される

### design.md の要約
- 新規ファイル: なし
- 変更ファイル:
  - `src/main/kotlin/com/rescript/plugin/settings/RescriptProjectSettings.kt` — `State` に `incrementalTypecheckingEnabled: Boolean = true` を追加、プロパティ追加
  - `src/main/kotlin/com/rescript/plugin/settings/RescriptConfigurable.kt` — `JCheckBox` を追加し `FormBuilder` に登録、`isModified()`, `apply()`, `reset()`, `disposeUIResources()` を更新、`apply()` で `LspServerManager.getInstance(project).stopAndRestartIfNeeded(RescriptLspServerSupportProvider::class.java)` を呼び出し
  - `src/main/kotlin/com/rescript/plugin/lsp/RescriptLspServerDescriptor.kt` — `createInitializationOptions()` を更新:
    ```kotlin
    override fun createInitializationOptions(): Any {
        val settings = RescriptProjectSettings.getInstance(project)
        return mapOf(
            "extensionConfiguration" to mapOf(
                "codeLens" to true,
                "incrementalTypechecking" to mapOf(
                    "enabled" to settings.incrementalTypecheckingEnabled,
                ),
            ),
        )
    }
    ```
- テスト: 設定値の読み書きテスト + 初期化オプション生成テスト（テスト省略理由: UI コンポーネントと LSP 再起動は単体テスト困難。設定の永続化と初期化オプション生成のみテスト対象）

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add incremental type checking setting`

## ステップ 5: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```
