# Phase 2: External Tools and Content-Root Fixtures — Design

## 1. アーキテクチャ概要

3 系統の追加を 1 PR で実施する:

```
A. CI infra (.github/workflows/ci.yml)
   └─ mmdc / graphviz / rescript の `Setup` ステップを build ジョブに追加

B. Content-root fixture (src/test/kotlin/com/rescript/plugin/)
   └─ RescriptContentRootProjectDescriptor (LightProjectDescriptor)
   └─ IntelliJPlatformExtensionWithContentRoot (existing extension のバリアント)

C. External CLI tests (src/test/kotlin/com/rescript/plugin/cli/)
   ├─ RescriptVariantFlowMermaidExporterCliTest      (mmdc)
   ├─ RescriptVariantFlowDotExporterCliTest          (graphviz dot)
   └─ RescriptMigrationConverterCliTest              (rescript convert)
```

`cli/` パッケージは既存 `perf/` 同様、複数モジュールにまたがる外部依存テストを集約する役割。

## 2. パッケージ構成

```
src/test/kotlin/com/rescript/plugin/
├── IntelliJPlatformExtensionWithContentRoot.kt       (new)
├── RescriptContentRootProjectDescriptor.kt           (new)
├── interop/RescriptInteropScannerIntegrationTest.kt  (extend)
├── migration/RescriptMigrationFinderIntegrationTest.kt (extend)
└── cli/
    ├── ExternalCliAvailability.kt                    (new, shared helper)
    ├── RescriptVariantFlowMermaidExporterCliTest.kt  (new)
    ├── RescriptVariantFlowDotExporterCliTest.kt      (new)
    └── RescriptMigrationConverterCliTest.kt          (new)
```

`.github/workflows/ci.yml` も併せて編集。

## 3. 主要クラス設計

### 3.1 RescriptContentRootProjectDescriptor

```kotlin
object RescriptContentRootProjectDescriptor : DefaultLightProjectDescriptor() {
    // DefaultLightProjectDescriptor は content root を 1 個自動で追加するため、
    // EMPTY_PROJECT_DESCRIPTOR と違って addFileToProject されたファイルが
    // 検索 index に乗る。Kotlin 言語サポートなどは初期化しないので軽量。
}
```

`DefaultLightProjectDescriptor` は IntelliJ Platform 標準で、light test で content root 付きの project を作るためのデフォルト descriptor。`EMPTY_PROJECT_DESCRIPTOR` との差は content root の有無のみ。

### 3.2 IntelliJPlatformExtensionWithContentRoot

既存 `IntelliJPlatformExtension` のコピーで、`createLightFixtureBuilder` に渡す descriptor だけを `RescriptContentRootProjectDescriptor` に差し替える。重複を避けるため、共通部分をヘルパー関数に抽出する案もあるが、まずはコピーで動作確認 → リファクタは余裕があれば後回し。

新規テストは `@ExtendWith(IntelliJPlatformExtensionWithContentRoot::class)` を使う。既存テストは無変更。

### 3.3 ExternalCliAvailability (shared helper)

```kotlin
object ExternalCliAvailability {
    fun isMermaidCliAvailable(): Boolean = canRun(listOf("mmdc", "--version"))
    fun isDotAvailable(): Boolean = canRun(listOf("dot", "-V"))
    fun isRescriptCliAvailable(): Boolean = canRun(listOf("npx", "rescript", "-h"))
    
    private fun canRun(argv: List<String>): Boolean = try {
        ProcessBuilder(argv)
            .redirectErrorStream(true)
            .start()
            .also { it.waitFor(5, TimeUnit.SECONDS) }
            .exitValue() == 0
    } catch (_: Exception) { false }
}
```

各 CLI test は `@BeforeEach` で `Assumptions.assumeTrue(...)` を呼んで CLI 不在時に skip。

### 3.4 RescriptVariantFlowMermaidExporterCliTest

```kotlin
class RescriptVariantFlowMermaidExporterCliTest {
    @BeforeEach fun assume() { Assumptions.assumeTrue(ExternalCliAvailability.isMermaidCliAvailable()) }
    
    @Test fun `option pattern produces parseable Mermaid`() {
        val diagram = sampleOptionDiagram()
        val mermaid = RescriptVariantFlowMermaidExporter.toMermaid(diagram)
        val tmp = createTempFile(".mmd"); tmp.writeText(mermaid)
        val output = createTempFile(".svg")
        val exit = ProcessBuilder("mmdc", "-i", tmp.toString(), "-o", output.toString())
            .redirectErrorStream(true).start().also { it.waitFor(30, TimeUnit.SECONDS) }.exitValue()
        assertEquals(0, exit)
        assertTrue(output.length() > 0)
    }
}
```

DOT 版も同様。`dot -Tsvg -o output.svg input.dot` で SVG を生成し、exit code と出力サイズを assert。

### 3.5 RescriptMigrationConverterCliTest

実 `.re` テキストを temp ファイルに書き、`MigrationCandidate(virtualFile, relPath)` を構築 → `RescriptMigrationConverter.convert(project, candidate)` を呼び、`ConversionStatus.SUCCESS` であることと `.res` ファイルが期待内容になっていることを確認。

ただし `convert` は VFS write action を使うため、`@ExtendWith(IntelliJPlatformExtensionWithContentRoot::class)` で project 環境を立ち上げる必要がある。

### 3.6 populated integration test

`RescriptInteropScannerIntegrationTest` と `RescriptMigrationFinderIntegrationTest` のベース extension を `IntelliJPlatformExtensionWithContentRoot` に差し替えた populated ケースを追加（既存テストは残す）。

## 4. CI ワークフロー変更

`.github/workflows/ci.yml` の `build` ジョブに setup ステップを追加:

```yaml
- name: Install Mermaid CLI
  run: npm install -g @mermaid-js/mermaid-cli

- name: Install graphviz
  run: sudo apt-get update && sudo apt-get install -y graphviz

- name: Install ReScript CLI
  run: npm install -g rescript
```

これらは `./gradlew test` の前に実行する。CLI 不在時の skip 機構があるためローカル開発機への影響はなし。

## 5. テスト戦略

| テスト種別 | 対象 | 手法 |
|-----------|------|------|
| Integration | `RescriptInteropScanner.scan` (populated) | content-root fixture + assertEquals |
| Integration | `RescriptMigrationFinder.findCandidates` (populated) | 同上 |
| External | Mermaid Exporter | `mmdc` 実行、exit 0 + 非空 SVG |
| External | DOT Exporter | `dot` 実行、exit 0 + 非空 SVG |
| External | Migration Converter | `rescript convert` 実行、`.re → .res` 変換結果検証 |

## 6. プラグイン互換性

- `DefaultLightProjectDescriptor` は IntelliJ Platform 2025.3+ で利用可能（既存）
- 外部 CLI は CI 環境にインストールされるが、ランタイムには影響なし
- Deprecated API なし

## 7. ドキュメント更新

- `docs/repository-structure.md` のテスト構成セクションに `cli/` を追記
- `.claude/rules/testing.md` の「免除対象」に「外部 CLI 結合テスト」を追記しない方針（`Assumptions.assumeTrue` で skip するためテスト免除ではなく optional 動作）
- 6 機能の `.steering/.../requirements.md` で「Phase 2 で対応」とした項目に対応する CI/CLI テスト名を追記
