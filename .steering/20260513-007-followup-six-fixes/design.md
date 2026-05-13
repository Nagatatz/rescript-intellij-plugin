# 20260513-007 設計

## 全体方針

requirements.md の 6 案件を **6 つの独立コミット** に分割する。各セクションが green build を保ち、緑のコミットを順に main に積み上げる。順序間に強い依存は無いため、独立に進めて良い。

## 1. Migration Pilot v12 ガード

### 変更ファイル
- `src/main/kotlin/com/rescript/plugin/migration/RescriptMigrationConverter.kt`
- `src/test/kotlin/com/rescript/plugin/migration/RescriptMigrationConverterCliTest.kt` (拡張)

### 設計
新たに `internal fun probeMajorVersion(rescriptBinaryPath: String, workingDir: File): Int?` を追加し、`rescript --version` の stdout 1 行目から正規表現 `^rescript\s+(\d+)\.` または `^(\d+)\.` で major を取り出す。

`convert()` の冒頭で probe → 12 以上なら以下を返す:
```
ConversionResult(candidate, ConversionStatus.FAILED,
  "ReScript ${major} removed the `convert` subcommand. " +
  "Pin `rescript@^11` to enable the Migration Pilot, or convert manually.")
```

タイムアウトは 5 秒に短縮 (version probe のみ)。stderr は無視し、stdout 1 行目だけ読む。

### テスト
- `probeMajorVersion` のパース純粋関数を単体テスト (`"12.2.0\n"` / `"rescript 11.1.4\n"` / `"unexpected\n"` 等)
- 既存 `buildCommand` テストはそのまま

## 2. アイコン赤統一

### 変更ファイル
- 新規 SVG (`src/main/resources/icons/`):
  - `rescript-flow.svg`
  - `rescript-diagram.svg`
  - `rescript-dependencies.svg`
  - `rescript-impact.svg`
  - `rescript-coverage.svg`
  - `rescript-interop.svg`
  - `rescript-migration.svg`
  - `rescript-ppx.svg`
- `src/main/resources/META-INF/plugin.xml` (toolWindow 行 8 箇所)
- `RescriptIcons.kt` (任意: 定数追加して将来コード参照用)

### 設計
既存 `rescript-file.svg` をベースに、共通の **赤角丸 250×250 viewBox 矩形** をフレームとして使い、内側のグリフをツールウィンドウ用途ごとに差し替える。すべて 16×16 出力。

| ファイル | 内側グリフ |
|---|---|
| `rescript-flow.svg` | 縦方向の 3 ノード + 矢印 (decision tree 暗示) |
| `rescript-diagram.svg` | 三角形配置の 3 ノード + 接続線 (module graph) |
| `rescript-dependencies.svg` | 横並びの 2 矩形 (library boxes) |
| `rescript-impact.svg` | 中心 + 放射 3 線 (波及) |
| `rescript-coverage.svg` | 縦棒 3 本 (bar chart) |
| `rescript-interop.svg` | エクスクラメーション (!) (warning) |
| `rescript-migration.svg` | 右向き矢印 (→) |
| `rescript-ppx.svg` | `{ }` 中括弧 (macro 暗示) |

すべて白塗りグリフ (`fill="#fff"`) を赤グラデ背景の上に乗せる。Dark 対応は不要 (赤背景が常に視認できる)。

### plugin.xml 差分 (8 行)
| 旧 | 新 |
|---|---|
| `icon="/icons/rescript-toolwindow.svg"` (Module Diagram) | `icon="/icons/rescript-diagram.svg"` |
| `icon="/icons/rescript-toolwindow.svg"` (Switch Flow) | `icon="/icons/rescript-flow.svg"` |
| `icon="AllIcons.Nodes.PpLib"` (Dependencies) | `icon="/icons/rescript-dependencies.svg"` |
| `icon="AllIcons.Actions.Find"` (Type Impact) | `icon="/icons/rescript-impact.svg"` |
| `icon="AllIcons.General.InspectionsEye"` (Type Coverage) | `icon="/icons/rescript-coverage.svg"` |
| `icon="AllIcons.General.Warning"` (Interop Risk) | `icon="/icons/rescript-interop.svg"` |
| `icon="AllIcons.Actions.Refresh"` (Migration Pilot) | `icon="/icons/rescript-migration.svg"` |
| `icon="AllIcons.Nodes.Plugin"` (PPX) | `icon="/icons/rescript-ppx.svg"` |

`rescript-toolwindow.svg` は今後参照されなくなるが、互換のため残す。

### テスト
SVG はテスト免除 (リソースファイル)。`plugin.xml` のパースが通ることはビルドで検証される。

## 3. Narrowing binding 横ヒント

### 変更ファイル
- `src/main/kotlin/com/rescript/plugin/narrowing/RescriptSwitchArmCollector.kt`
- `src/main/kotlin/com/rescript/plugin/narrowing/RescriptNarrowingHintProvider.kt`
- `src/test/kotlin/com/rescript/plugin/narrowing/*Test.kt`

### 設計
`SwitchArm` データクラスに `bindingOffsets: List<Int>` (binding identifier の終端 offset) を追加。collector が arm pattern をスキャンして:
- `Constructor(x)` 形式: `(` の後の最初の LIDENT を捕捉
- `Constructor(x, y)` 形式: 最初の LIDENT のみ
- `Constructor` 単独 / リテラル: 空
- or-pattern `| A(x) | B(y)`: 最初の sub-pattern の binding のみ

`RescriptNarrowingHintProvider.buildHints` で:
- 既存の `=> ` 直後ヒント (絞り込み型) はそのまま
- 各 arm の最初の bindingOffset があれば、その位置にも `: <絞り込み型>` を追加

ヒント文字列のフォーマットは既存 `RescriptNarrowingPresenter` を再利用。

### テスト
新規ケース:
- `| Some(x) => ...` で `x` の後に `: int` が出る (option<int> の場合)
- `| None => ...` では何も出ない
- ネスト switch では最内のみ
- or-pattern では最初の binding のみ

## 4. Switch Flow Visual mode

### 変更ファイル
- 新規 `src/main/kotlin/com/rescript/plugin/flow/RescriptVariantFlowGraphView.kt`
- `src/main/kotlin/com/rescript/plugin/flow/RescriptVariantFlowPanel.kt` (CardLayout 化)
- 新規 `src/test/kotlin/com/rescript/plugin/flow/RescriptVariantFlowGraphViewTest.kt`

### 設計
`RescriptVariantFlowGraphView extends JComponent`:
- `setModel(model: RescriptVariantFlowModel?)`
- `paintComponent(g: Graphics)` で Java2D 描画:
  1. scrutinee 矩形 (上段中央): width = `max(scrutineeText, 200)`, height 36, 角丸 8
  2. arm 矩形群 (下段): width 各 max(armText, 120), height 44, 角丸 6、横並び spacing 20
  3. ルートから各 arm への矢印 (orthogonal: 縦線 → 横線 → 縦線、矢印先端は三角形 polygon)
- ノード配置計算は純関数 `computeLayout(model, viewportWidth): Layout`:
  ```kotlin
  data class Layout(
    val rootBox: Rectangle,
    val armBoxes: List<Pair<Rectangle, String>>,  // box + label
    val edges: List<List<Point>>,  // polyline
    val canvasSize: Dimension
  )
  ```
- viewport より広い場合はスクロール (JScrollPane でラップ)

`RescriptVariantFlowPanel`:
- 上部 toolbar に "Visual" / "Source" の `JBRadioButton` を追加
- 中央領域を `JPanel(CardLayout)` にして、`graphView` と `textArea` (既存) を切替
- モデル更新時に両方を更新

### 配色
- ノード塗り: 既存赤グラデの淡色版 `#FFE7E8` (背景) + 枠 `#CB3939`
- テキスト: `JBColor.foreground()` (テーマ追従)
- 矢印: `#CB3939`

### テスト
`computeLayout` の単体テスト:
- 0 arm / 1 arm / 3 arm のレイアウト寸法
- canvasSize がコンテンツ最大幅を反映
- 矢印は root の bottom から arm の top へ向かう

## 5. Add Missing Arms: PSI stub index 2nd-pass

### 変更ファイル
- `src/main/kotlin/com/rescript/plugin/lsp/RescriptLspSignatureParser.kt` (拡張)
- `src/main/kotlin/com/rescript/plugin/intention/RescriptAddMissingArmsDiagnoser.kt` (呼び出し追加)
- テスト

### 設計
新規 `internal fun resolveVariantFromTypeName(project: Project, typeName: String): List<VariantInfo>?`:
- `RescriptTypeStubIndex.findTypeDeclarations(project, typeName)` で type 宣言を検索
- 見つかった最初の宣言の元テキストを取得 (`element.text`)
- 既存 `parseVariantConstructors` を再帰呼び出しして constructor を抽出

`Diagnoser` 側:
```kotlin
val constructors = parseVariantConstructors(hoverText)
val resolved = if (constructors.isEmpty()) {
  extractTypeName(hoverText)?.let { resolveVariantFromTypeName(project, it) }
} else constructors
```

`extractTypeName(hoverText)`: hover の最終行から `^([a-z][A-Za-z0-9_]*)\s*$` 相当を抽出。型適用 (`option<int>`) はマッチさせない (既存の hardcoded パスに任せる)。

### Type Stub Index の利用
`indexing/RescriptTypeStubIndex` が既存。`KEY: StubIndexKey<String, PsiElement>` から `StubIndex.getElements(KEY, name, project, scope, PsiElement::class.java)` で取得。要素から `text` プロパティで宣言全体のソースを取得できる。

### テスト
`RescriptLspSignatureParserResolveTest` (heavy fixture):
- 2 ファイル fixture: `Types.res` に `type color = | Red | Blue | Green`、`Main.res` でカーソル位置
- `resolveVariantFromTypeName(project, "color")` が `[Red, Blue, Green]` を返す
- 存在しない型名で `null`
- variant でない型 (`type t = int`) で `null` (空集合)

`extractTypeName` は純関数なので light test:
- `"color"` → `"color"`
- `"option<int>"` → `null`
- `"Module.t"` → `null` (今回スコープ外)

## 6. README 補強

### 変更ファイル
- `manual-test-projects/README.md`

### 設計
既存の Rename Variant Constructor 行のすぐ下に注釈を追加:
```
> Note: `VariantUsage.res` is a *cross-file fixture* for the rename — it
> imports `VariantSamples` and references the same constructors so the
> rename can prove it touches multiple files. Open it on its own to
> test cross-file behavior; do **not** expect a dedicated tool window.
```

英語 (manual-test README は英語ベース)。

## ロールアウト順序

順序間に強依存は無いが、提案する着手順:

1. **#6 README** — 1 行追加、安全な warm-up
2. **#1 Migration v12 guard** — テストありロジックの典型例
3. **#5 Add Missing Arms 2nd-pass** — 既存パーサー拡張のみ
4. **#3 Narrowing binding** — 既存 hint provider の拡張
5. **#4 Switch Flow Visual** — 新規 JComponent でやや大きめ
6. **#2 アイコン** — SVG 8 枚 + plugin.xml、コード変更最少だが面積大

セクションごとに `./gradlew ktlintCheck buildPlugin test` を通してから次へ進む。

## リスク

| リスク | 緩和策 |
|---|---|
| #1 で `rescript --version` 自体が失敗する環境 (binary 未配置) | probe が `null` を返したら従来通り convert を試行し、CLI 側のエラーメッセージで失敗する fallback とする |
| #2 SVG が IntelliJ Light/Dark で見にくい | 赤背景 + 白グリフは両テーマで一定のコントラストを確保できる。`_dark.svg` 不要 |
| #3 SwitchArmCollector の API シグネチャ変更で既存呼び出し元 (flow/ 等) が壊れる | `bindingOffsets` を新フィールドとしてデフォルト空にし、既存呼び出しは無影響 |
| #4 Java2D 描画の cross-platform 違い (フォントメトリクス) | `g.fontMetrics` を使って動的計算、固定 px は使わない |
| #5 Type Stub Index が未ビルドの大規模プロジェクトで重い | `DumbService.isDumb` ガード、indexing 中は 2nd-pass をスキップ |
