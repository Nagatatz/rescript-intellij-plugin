# 設計: Dependency Diagram の Extension Point 登録

## アプローチ選択

### 選択肢 A: IntelliJ UML プラグイン (`com.intellij.diagram`) ベース

`com.intellij.diagram.DiagramProvider` 系インターフェース（`DiagramProvider`, `DiagramVfsResolver`, `DiagramElementManager`, `DiagramNodeContentManager`, `DiagramRelationshipManager`, `DiagramExtras`）を実装。

- 利点: ネイティブの UML ビューア、ズーム/パン、レイアウト自動化が無料で得られる
- 欠点: **`com.intellij.diagram` は IntelliJ Ultimate 専用プラグイン**。Community / WebStorm 単体では動作しない。CLAUDE.md「全 JetBrains IDE 対応」方針に反する

### 選択肢 B: 自前 ToolWindow + Mermaid ベース可視化（採用）

ToolWindow に依存関係を可視化するパネルを実装。**Mermaid 記法のテキスト表示** と **DOT エクスポート** をサポート。

- 利点: Community/Ultimate 両対応。実装シンプル。`docs/diagrams/*.mmd` と同じツールチェーン
- 欠点: ネイティブ UML のような対話操作はできない（テキスト + 静的画像）

→ **選択肢 B を採用**

## アーキテクチャ

```
diagram/
├── RescriptDependencyDiagramProvider.kt    # 既存: 依存関係抽出（変更なし）
├── RescriptDependencyDiagramModel.kt        # 既存: モデル + DOT エクスポート（変更なし）
├── RescriptDependencyDiagramPanel.kt        # 新規: ToolWindow 内表示パネル（Swing JComponent）
├── RescriptDependencyDiagramToolWindowFactory.kt  # 新規: ToolWindow 登録 Factory
├── RescriptDependencyDiagramAction.kt       # 新規: Analyze メニュー Action（ToolWindow を開く）
├── RescriptDependencyDiagramExportAction.kt # 新規: DOT エクスポート Action
└── RescriptMermaidExporter.kt               # 新規: Mermaid 記法生成（ユーティリティ）
```

## コンポーネント詳細

### 1. `RescriptMermaidExporter` (新規ユーティリティ)

`RescriptDependencyDiagramModel` から **Mermaid `graph TD` 記法** の文字列を生成。Mermaid 記法は HTML プレビュー化が容易（既存 `docs/diagrams/` と同じ）。

```kotlin
object RescriptMermaidExporter {
    fun toMermaid(model: RescriptDependencyDiagramModel): String
}
```

エスケープ: モジュール名はノード ID 安全文字（英数字 + `_`）に正規化。元の名前は `["..."]` ラベルで表示。

### 2. `RescriptDependencyDiagramPanel` (新規 Swing パネル)

`SimpleToolWindowPanel` ベース。3 つの領域:

- 上部: ツールバー（Refresh / Export DOT / Export Mermaid / Copy to Clipboard）
- 中央: テキストエリア（Mermaid 記法 + ノード/エッジサマリ）
- 下部: ステータスバー（モジュール数 / エッジ数）

「テスト免除カテゴリ: Swing UI コンポーネント」に該当（`.claude/rules/testing.md`）。

### 3. `RescriptDependencyDiagramToolWindowFactory` (新規 Factory)

`com.intellij.openapi.wm.ToolWindowFactory` を実装。

- ToolWindow ID: `"ReScript Module Diagram"`（既存 `ReScript Dependencies` と区別）
- アンカー: `right`
- アイコン: `AllIcons.FileTypes.Diagram` または専用 SVG
- 名称が変わるため `sphinx-docs/user/features/advanced.md` のドキュメントを「View > Tool Windows > ReScript Module Diagram」に修正

「テスト免除カテゴリ: Swing UI コンポーネント」に該当。

### 4. `RescriptDependencyDiagramAction` (新規 Action)

`AnAction` を実装。`Analyze` メニューと右クリックコンテキストメニューに登録（`<add-to-group group-id="AnalyzeMenu"/>`）。

- アクション名: "Show ReScript Module Diagram"
- 実行時: ToolWindow を `activate()`

「テスト免除カテゴリ: IDE ライフサイクル依存」に近い。最小限の単体テストとして "Action 登録時にクラッシュしない" は確認する。

### 5. `RescriptDependencyDiagramExportAction` (新規 Action)

DOT 形式 / Mermaid 形式を選択して **クリップボードへコピー** または **ファイルに保存**。

「テスト免除カテゴリ: なし」 → ロジック部分（DOT/Mermaid 生成 + 戻り値判定）はユニットテスト必須。

## plugin.xml 登録

`<extensions>` に以下を追加:

```xml
<toolWindow id="ReScript Module Diagram" anchor="right"
            icon="AllIcons.FileTypes.Diagram"
            factoryClass="com.rescript.plugin.diagram.RescriptDependencyDiagramToolWindowFactory"/>
```

`<actions>` に以下を追加:

```xml
<action id="ReScript.ShowModuleDiagram"
        class="com.rescript.plugin.diagram.RescriptDependencyDiagramAction"
        text="Show ReScript Module Diagram"
        description="Open the ReScript module dependency diagram tool window">
    <add-to-group group-id="AnalyzeMenu" anchor="last"/>
</action>
```

DOT/Mermaid エクスポート Action は ToolWindow ツールバー内のみ（メニュー登録不要）。

## ドキュメント整合性

実装完了に伴い以下を更新:

| ファイル | 変更 |
|---------|------|
| `sphinx-docs/user/features/advanced.md:933` | "View > Tool Windows > Dependency Diagram" → "View > Tool Windows > ReScript Module Diagram"。"Analyze > Module Dependency Diagram" → "Analyze > Show ReScript Module Diagram" |
| `sphinx-docs/user/features/advanced.md:935-952` | "How It Works"・"Features" を実装に合わせて修正（インタラクティブレイアウト → Mermaid テキスト表示等） |
| `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po` | 上記 .md 変更に対応する `.po` 更新 |
| `docs/functional-design.md:397` Extension Point マップ | ToolWindow / Action を追記 |
| `docs/repository-structure.md:78` | `diagram/` の代表クラス列を新規 4 クラスに更新 |
| `docs/product-requirements.md:280-287` | US-15 の受け入れ条件は実装で実証可能になるためそのまま |
| `README.md:74` | "Dependency diagram" の説明を「Mermaid + DOT 出力」に微修正 |
| `CLAUDE.md` レイヤー 3 | `diagram/` セクションがあれば更新（なければ追記不要） |

## セキュリティ

- DOT エクスポートのモジュール名エスケープは既存 `escapeDot()` を継続使用
- Mermaid エクスポートのノード ID は英数字 + `_` のみに正規化（特殊文字を取り除く）
- ファイル保存時のパスは `FileChooser` 経由（ユーザーが選択）。コード側で絶対パス組み立て不要

## テスト戦略

| 対象 | テスト | 免除可否 |
|------|--------|---------|
| `RescriptMermaidExporter` | 単体: 各種モデル → Mermaid 出力。エスケープ検証 | 必須 |
| `RescriptDependencyDiagramExportAction` | 単体: DOT 生成 / Mermaid 生成の戻り値検証 | 必須 |
| `RescriptDependencyDiagramPanel` | UI Swing | 免除（理由: Swing UI コンポーネント） |
| `RescriptDependencyDiagramToolWindowFactory` | UI Swing | 免除（理由: Swing UI / IDE ライフサイクル） |
| `RescriptDependencyDiagramAction` | 軽量: 名前・ID チェック | 免除可（理由: IDE ライフサイクル）。tasklist に明記 |
| 既存 `RescriptDependencyDiagramProviderTest` | 既存テストを維持・回帰検証 | — |
| 既存 `RescriptDependencyDiagramModelTest` | 既存テストを維持・回帰検証 | — |
