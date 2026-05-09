# Type Coverage Heat Map — Design

## アーキテクチャ概要

新規パッケージ `coverage/` を追加する。LSP に依存しない純粋構文ベースの分析。`impact/` パッケージのデザイン (Scanner + Model + Panel + ToolWindowFactory + Action) を踏襲する。

## クラス構成

| クラス | 責務 |
|--------|------|
| `RescriptTypeCoverageClassifier` | トップレベル `let` 宣言ソース → `LetCoverage` (ANNOTATED / INFERRED) 判定 (pure object) |
| `RescriptTypeCoverageScanner` | `FileTypeIndex` で `.res` ファイルを列挙し、各ファイルから let 宣言を抽出して classifier に通す |
| `RescriptTypeCoverageModel` | データクラス: `FileCoverage`, `ProjectCoverage`, `LetCoverage` enum |
| `RescriptTypeCoveragePanel` | Swing UI: `JBTable` + ツールバー + Refresh アクション |
| `RescriptTypeCoverageToolWindowFactory` | ToolWindow Extension Point の実装 |
| `RescriptTypeCoverageRefreshAction` | ツールバー再走査アクション |

## Classifier ヒューリスティック

入力: トップレベル `let` 宣言の **ソース文字列** (例: `let total: int = arr->Array.reduce(0, (+))`)

判定ロジック:

1. 文字列を `RescriptLexer` でトークン化
2. `let` キーワードの次の IDENT トークンの直後を探索開始位置とする
3. 次に現れる **depth-0 の** トークンを順に見ていき、`=` までに `:` が現れたら ANNOTATED
4. パラメータリストの `(...)` や record literal の `{...}` 内部はスキップ (depth > 0)

判定例:

| ソース | 判定 |
|--------|------|
| `let x: int = 5` | ANNOTATED |
| `let f: (int) => string = ...` | ANNOTATED |
| `let x = 5` | INFERRED |
| `let f = (x: int) => x + 1` | INFERRED (paren 内のみ) |
| `let f = (x: int): int => x + 1` | INFERRED (`:int` は `=` の後) |
| `let xs = [1, 2, 3]` | INFERRED |
| `let user: {name: string} = {name: "x"}` | ANNOTATED |

## Scanner

```kotlin
fun scan(project: Project): ProjectCoverage {
    val files = FileTypeIndex.getFiles(
        RescriptFileType.INSTANCE,
        GlobalSearchScope.projectScope(project),
    )
    val perFile = files.map { vf ->
        val source = String(vf.contentsToByteArray(), Charsets.UTF_8)
        val lets = RescriptParser.collectLetDeclarations(source) // 既存 API を再利用 (なければ新設)
        val classifications = lets.map { letDecl ->
            RescriptTypeCoverageClassifier.classifyLet(letDecl.source)
        }
        FileCoverage(
            file = vf,
            totalLets = lets.size,
            annotatedLets = classifications.count { it == LetCoverage.ANNOTATED },
        )
    }
    return ProjectCoverage(perFile)
}
```

`RescriptParser.collectLetDeclarations` が無ければ、scanner 内で軽量にトップレベル `let` 宣言の range を列挙する (波括弧バランス + `let` キーワード起点) — 既存 `RescriptDeclarationParser` の発想を流用。

## ToolWindow Panel UI

- 中央: `JBTable` (TableModel + 各カラムの custom renderer)
- 上部: `ActionToolbar` with `RescriptTypeCoverageRefreshAction`
- 下部: ステータスラベル (`X files scanned, Y bindings, Z% project coverage`)
- ダブルクリックで `OpenFileDescriptor` 経由ジャンプ

カラム:

| # | 名前 | 型 |
|---|------|------|
| 1 | File | String (relative path) |
| 2 | Total | Int |
| 3 | Annotated | Int |
| 4 | Inferred | Int |
| 5 | Coverage % | Double (0.0〜100.0) — 色付きセル |

初期ソート: Coverage% 昇順。カラムヘッダクリックでソート切替。

## カラーリング

```kotlin
fun coverageColor(percent: Double): JBColor = when {
    percent < 30.0 -> JBColor(0xC75450, 0xE74C3C) // 赤
    percent < 70.0 -> JBColor(0xE08600, 0xF0AD4E) // 黄
    else           -> JBColor(0x59A869, 0x2ECC71) // 緑
}
```

セル背景に薄い色付け、テキストはそのまま。

## ファイル列挙の上限

- ハードキャップ: 2,000 ファイル (それ以上は走査停止し、ステータスバーに警告)
- ソフトキャップ: 1,500 ファイル超でステータスバーに進捗表示
- 大規模プロジェクト想定: 500 ファイル規模で初回 < 2s、refresh < 500ms

## Extension Point 登録

`plugin.xml` に追加:

```xml
<toolWindow id="ReScript Type Coverage"
            anchor="bottom"
            secondary="true"
            factoryClass="com.rescript.plugin.coverage.RescriptTypeCoverageToolWindowFactory"
            icon="com.rescript.plugin.RescriptIcons.FILE"/>
```

## テスト方針

- `RescriptTypeCoverageClassifierTest`: 30+ ケース (annotated / inferred / 関数型 / record / nested paren / 型変数)
- `RescriptTypeCoverageScannerTest`: light fixture (PsiFileFactory.createFileFromText) で 5 ファイル分の `.res` を読ませ、ProjectCoverage の中身を確認
- `RescriptTypeCoverageModelTest`: `FileCoverage.coveragePercent`, `ProjectCoverage.aggregate` 計算ロジック
- Panel / Action / ToolWindowFactory: testing.md 免除対象 (Swing UI / IDE lifecycle)

## kover excludes

新規 Panel / Action / ToolWindowFactory は build.gradle.kts の kover excludes に追加し、最小 86% を維持する:

```kotlin
"com.rescript.plugin.coverage.RescriptTypeCoveragePanel*",
"com.rescript.plugin.coverage.RescriptTypeCoverageRefreshAction*",
"com.rescript.plugin.coverage.RescriptTypeCoverageToolWindowFactory*",
```
