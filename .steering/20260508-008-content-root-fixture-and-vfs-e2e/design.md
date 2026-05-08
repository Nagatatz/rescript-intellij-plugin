# Phase 3: Content-Root Fixture and VFS E2E — Design

## 1. アーキテクチャ概要

```
A. Heavy fixture extension
   └─ IntelliJPlatformExtensionWithContentRoot
      ├─ IdeaTestFixtureFactory.createFixtureBuilder("rescript-test", basePath)
      ├─ HeavyIdeaTestFixture (real file system, real project)
      └─ CodeInsightTestFixture wrapper

B. Populated integration tests
   ├─ RescriptInteropScannerPopulatedIntegrationTest
   └─ RescriptMigrationFinderPopulatedIntegrationTest

C. VFS e2e (CLI-gated)
   └─ RescriptMigrationConverterE2eTest  (Assumptions.assumeTrue)
```

## 2. パッケージ構成

```
src/test/kotlin/com/rescript/plugin/
├── IntelliJPlatformExtensionWithContentRoot.kt        (new, heavy fixture)
├── interop/
│   └── RescriptInteropScannerPopulatedIntegrationTest.kt   (new)
├── migration/
│   ├── RescriptMigrationFinderPopulatedIntegrationTest.kt  (new)
│   └── RescriptMigrationConverterE2eTest.kt                (new, CLI-gated)
```

`cli/` パッケージには新規追加せず、CLI-gated e2e も該当機能パッケージ（`migration/`）に配置する（既存 `RescriptMigrationFinderIntegrationTest` の隣に並ぶことで読みやすい）。

## 3. 主要クラス設計

### 3.1 IntelliJPlatformExtensionWithContentRoot (heavy fixture)

```kotlin
class IntelliJPlatformExtensionWithContentRoot :
    BeforeEachCallback, AfterEachCallback, InvocationInterceptor {

    private var fixture: CodeInsightTestFixture? = null
    private var tempDir: Path? = null

    override fun beforeEach(context: ExtensionContext) {
        val testInstance = context.requiredTestInstance
        val name = sanitize(context.displayName)

        val tmp = Files.createTempDirectory("rescript-test-${name}-")
        tempDir = tmp

        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val builder = factory.createFixtureBuilder(name, tmp)
        val codeInsightFixture = factory.createCodeInsightFixture(builder.fixture)
        codeInsightFixture.setUp()

        fixture = codeInsightFixture
        injectField(testInstance, "myFixture", codeInsightFixture)
        injectField(testInstance, "project", codeInsightFixture.project)
    }

    override fun afterEach(context: ExtensionContext) {
        try {
            fixture?.tearDown()
        } finally {
            fixture = null
            tempDir?.toFile()?.deleteRecursively()
            tempDir = null
        }
    }
    // EDT dispatch is identical to IntelliJPlatformExtension
}
```

`createFixtureBuilder(name, basePath)` は heavy fixture を作る IntelliJ Platform 標準 API。実 file system 上に project を構築するため、`addFileToProject` で追加した `.res` / `.re` が `FileTypeIndex` / `FilenameIndex` から確実に見える。

注意点:
- 起動オーバーヘッド: テスト 1 件あたり 3〜10 秒程度（light fixture は 0.5〜2 秒）
- temp directory のクリーンアップを確実に行う（失敗時のごみが残ると後続テストに影響）

### 3.2 RescriptInteropScannerPopulatedIntegrationTest

直前バッチで撤回した内容と同じテストを heavy fixture で再実装。

### 3.3 RescriptMigrationFinderPopulatedIntegrationTest

直前バッチで撤回した内容と同じテストを heavy fixture で再実装。

### 3.4 RescriptMigrationConverterE2eTest (CLI-gated)

```kotlin
@ExtendWith(IntelliJPlatformExtensionWithContentRoot::class)
class RescriptMigrationConverterE2eTest {
    @BeforeEach fun assume() {
        Assumptions.assumeTrue(
            ExternalCliAvailability.isRescriptCliAvailable(),
            "rescript CLI not available; skipping migration converter e2e",
        )
    }

    @Test fun `convert rewrites a re file to res`() {
        val virtualFile = myFixture.addFileToProject("Sample.re", "let x = 42;\n").virtualFile
        val candidate = MigrationCandidate(virtualFile, "Sample.re")
        val result = RescriptMigrationConverter.convert(project, candidate)
        assertEquals(ConversionStatus.SUCCESS, result.status)
        // virtualFile is renamed to Sample.res; verify via VFS lookup
        val renamed = LocalFileSystem.getInstance().findFileByPath(/* path */)
        assertNotNull(renamed)
        assertTrue(renamed.name == "Sample.res")
    }
}
```

注意点:
- `convert` は VFS write action で同期的にリネーム + 書き換え。test method 内で結果を待つだけで OK
- `rescript convert` は project root に `bsconfig.json` を要求する場合があるが、recent versions では plain `.re` 単独でも変換可能。CI で実際に動くかは試行ベースで確認

## 4. テスト戦略

| テスト種別 | 対象 | 手法 |
|-----------|------|------|
| Integration (heavy) | `RescriptInteropScanner.scan` (populated) | heavy fixture + addFileToProject |
| Integration (heavy) | `RescriptMigrationFinder.findCandidates` (populated) | 同上 |
| E2E (heavy + CLI) | `RescriptMigrationConverter.convert` | heavy fixture + `rescript convert`、CLI gated |

## 5. プラグイン互換性

- `IdeaTestFixtureFactory.createFixtureBuilder(name, basePath)` は IntelliJ Platform 2025.3+ で利用可能
- 既存 light fixture との並存（影響範囲はテストファイルのみ）
- Deprecated API なし

## 6. ドキュメント更新

- `.steering/.../requirements.md` の Phase 2/3 リファレンスを完了化
- `docs/repository-structure.md` のテスト構成セクションは既に `cli/` を含むため、heavy fixture の説明を追記
- `tasklist.md` 内で heavy fixture の起動コストと並存方針を記録

## 7. リスク

- heavy fixture が CI で異常に遅くなる可能性 → 4 件以下に制限し、合計 30〜40 秒の増分を見込む
- `rescript convert` CLI が実プロジェクト構成（`bsconfig.json`）を要求する可能性 → e2e test 内で必要なら追加
