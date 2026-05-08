# Fixture-Based Integration Tests and Performance Benchmarks — Design

## 1. アーキテクチャ概要

新規プロダクションコードはなし。テストのみを 9 ファイル追加する:

```
src/test/kotlin/com/rescript/plugin/
├── narrowing/
│   └── RescriptNarrowingHintProviderIntegrationTest.kt   (new)
├── impact/
│   └── RescriptTypeTargetResolverIntegrationTest.kt      (new)
├── notebook/
│   └── RescriptNotebookFileEditorIntegrationTest.kt      (new)
├── interop/
│   └── RescriptInteropScannerIntegrationTest.kt          (new)
├── migration/
│   └── RescriptMigrationFinderIntegrationTest.kt         (new)
└── perf/
    ├── RescriptSwitchArmCollectorPerfTest.kt             (new)
    ├── RescriptVariantFlowModelPerfTest.kt               (new)
    ├── RescriptInteropScannerPerfTest.kt                 (new)
    └── RescriptInteropClassifierPerfTest.kt              (new)
```

`perf/` パッケージはプロダクションコードに対応しないテスト専用パッケージで、benchmark を集約する役割を持つ。既存の `RescriptPerformanceBenchmarkTest`（`lang/` パッケージ）と同居しないのは、対象が複数モジュールにまたがるため。

## 2. テスト戦略

### 2.1 Integration tests（fixture-based）

各テストファイルは以下のスケルトンに従う:

```kotlin
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptXxxIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun `<scenario>`() {
        myFixture.configureByText("Sample.res", "...")
        // call the target API
        // assert
    }
}
```

`IntelliJPlatformExtension` は既に存在しており、reflection で `myFixture` / `project` を注入する。

PSI 解析を含むテストは `RescriptParsingTestExtension` を使用するが、本ステアリングでは `myFixture.configureByText` を使う方が `Project` が手に入って便利なため、`IntelliJPlatformExtension` を採用する。

#### A. RescriptTypeTargetResolverIntegrationTest

5 つのテストケース、各々別の型定義シェイプを fixture に置く:

| ケース | サンプル | 期待値 |
|---|---|---|
| `type alias` | `type t = int` | `name=t`, `localName=t` |
| `type record` | `type t = { x: int }` | 同上 |
| `type variant` | `type t = | A | B(int)` | 同上 |
| `type polyvariant` | `type t = [#a \| #b]` | 同上 |
| `type abstract` | `type t` | 同上 |
| `module-qualified` | `module Inner = { type t = int }` 内 | `name=Inner.t` |
| `caret outside type` | `let x = 1` の `let` 上 | `null` |

#### B. RescriptNotebookFileEditorIntegrationTest

`LightVirtualFile` で `.resnb` ファイルを作成 → `RescriptNotebookFileEditor` をインスタンス化 → セル追加して `panel.snapshot()` を取得 → serializer で round-trip。

```kotlin
val file = LightVirtualFile("test.resnb", RescriptNotebookFileType, "")
val editor = RescriptNotebookFileEditor(project, file)
val initial = editor.panel.snapshot()  // 1 empty cell auto-added
val withCell = NotebookDocument(cells = listOf(NotebookCell("let x = 1", "1"), ...))
val json = RescriptNotebookSerializer.toJson(withCell)
val parsed = RescriptNotebookSerializer.fromJson(json)
assertEquals(withCell, parsed)
```

実装上は `panel` フィールドへのアクセス権を internal にする必要がある。最小変更で済むよう、現行の `private` を `internal` に緩める。

#### C. RescriptInteropScannerIntegrationTest

`myFixture.configureByText` で 1〜2 個の `.res` を fixture に置き、`RescriptInteropScanner.scan(project)` の結果を assert。

```kotlin
myFixture.configureByText("A.res", "let x = Obj.magic(payload)")
myFixture.configureByText("B.res", "external alert: string => unit = \"alert\"")
val result = RescriptInteropScanner.scan(project)
val kinds = result.entries.map { it.kind }.toSet()
assertEquals(setOf(InteropKind.OBJ_MAGIC, InteropKind.EXTERNAL), kinds)
```

#### D. RescriptMigrationFinderIntegrationTest

`myFixture.configureByText` で `.re` / `.rei` を作成 → `findCandidates(project)` の結果を assert。

#### E. RescriptNarrowingHintProviderIntegrationTest

`buildHints(text, resolver)` を resolver スタブで呼び出し、project setting OFF / ON の挙動が一致することを確認。`RescriptNarrowingHintProvider.getCollectorFor` 経由で fixture から呼ぶケースは追加しない（既存 InlayHintsCollector の API は IntelliJ Platform 内部で呼ばれることが多く、テストハーネスが脆い）。

代替として、`buildHints` の呼び出し前後で `RescriptProjectSettings.getInstance(project).narrowingHintsEnabled` を切り替えても結果が安定することを軽くテストする。

### 2.2 Performance benchmarks（pure function smoke gate）

各テストファイルは:

```kotlin
class RescriptXxxPerfTest {
    @Test
    fun `<input> finishes within <bound>ms`() {
        val source = generateLargeSample(...)
        val started = System.nanoTime()
        val result = TargetFunction.compute(source)
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        assertTrue(elapsedMs < BOUND_MS, "Took ${elapsedMs}ms (limit ${BOUND_MS}ms)")
        // optional: sanity check on result
        assertEquals(EXPECTED_COUNT, result.size)
    }
}
```

#### A. RescriptSwitchArmCollectorPerfTest

50 個の switch (4 アーム × 50 = 200 アーム) を含む 1000 行サンプルを生成 → `collect()` が 200ms 以内。

#### B. RescriptVariantFlowModelPerfTest

5000 行ネスト switch を含むサンプルを生成 → `buildAtOffset(text, offset)` が 1 秒以内。

#### C. RescriptInteropScannerPerfTest

100KB 相当の合成 `.res` テキスト（1 行 100 文字 × 1000 行 = 100KB）に対し `collectEntriesFromText` が 500ms 以内。

#### D. RescriptInteropClassifierPerfTest

10000 行（中身は実際の `.res` パターンを循環）を 1 行ずつ classifier に通し、合計 500ms 以内。

#### 上限値ポリシー

- ローカル開発機（M1/M3 MacBook、典型実行時間 < 50ms）の **5〜10 倍** を上限とする
- CI マシンの揺らぎを許容する目的で、ぎりぎり値を採用しない
- 上限を緩めるのは恒常的に失敗するときのみ。逆に厳しくするのはリリース判断のタイミングで。

## 3. パッケージ修正の影響範囲

`RescriptNotebookFileEditor.panel` のアクセス修飾子変更:

- 現状: `private val panel: RescriptNotebookPanel`
- 変更後: `internal val panel: RescriptNotebookPanel`

これにより `notebook` パッケージ内のテストから `panel.snapshot()` を呼べるようになる。プロダクション側の他クラスからの参照は無いので影響範囲はテストのみ。

## 4. プラグイン互換性

- 既存の `IntelliJPlatformExtension` / `RescriptParsingTestExtension` パターンを踏襲
- 新規依存追加なし
- Deprecated API なし

## 5. ドキュメント更新

このバッチはテスト追加のみでユーザー向け機能の変化はない。以下のみ更新する:

- 各機能の `requirements.md` の「マージ後にユーザー側で手動検証」とした項目を、`integration test で検証` に置き換える（関連 7 ファイル）
- `docs/repository-structure.md` のテスト構成セクションに `perf/` を追記
- `CLAUDE.md` / `README.md` / `sphinx-docs` には影響なし
