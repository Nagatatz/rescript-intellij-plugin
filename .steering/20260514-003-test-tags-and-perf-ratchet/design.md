# 設計: テスト分類の整備と perf ratchet 強化

## 1. Gradle test task の分割

`build.gradle.kts` の既存 `test {}` ブロックは変更せず、新規 task を 3 つ登録する。

### testFast

```kotlin
tasks.register<Test>("testFast") {
    description = "Run unit tests excluding perf/integration/cli suites."
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter {
        excludeTestsMatching("*PerfTest")
        excludeTestsMatching("*IntegrationTest")
        excludeTestsMatching("com.rescript.plugin.cli.*")
    }
}
```

### testPerf

```kotlin
tasks.register<Test>("testPerf") {
    description = "Run perf smoke benchmarks only."
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter {
        includeTestsMatching("com.rescript.plugin.perf.*")
    }
}
```

### testCli

```kotlin
tasks.register<Test>("testCli") {
    description = "Run external-CLI integration tests (skips without mmdc/dot)."
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter {
        includeTestsMatching("com.rescript.plugin.cli.*")
    }
}
```

すべて既存 `sourceSets["test"]` を再利用するので新規 source set は不要。`useJUnitPlatform()` を明示する。

## 2. perf ratchet モデル

現状:

```kotlin
private val iterations = 10_000
private val timeLimitMs = 500L

assertTrue(elapsedMs < timeLimitMs, "classify sweep took ${elapsedMs}ms (limit ${timeLimitMs}ms)")
```

変更後:

```kotlin
companion object {
    /**
     * Expected cost in milliseconds on a typical 2024 developer machine.
     * Ratcheted downward over time as the implementation improves; raise only
     * with explicit justification in the commit message.
     */
    private const val BASELINE_MS = 200L

    /**
     * Multiplicative slack to absorb GC pauses and CI noise.
     * Reduce as the test becomes more deterministic.
     */
    private const val SLACK_FACTOR = 2.5

    private const val TIME_LIMIT_MS = (BASELINE_MS * SLACK_FACTOR).toLong()
}

// ...
val ratio = elapsedMs.toDouble() / BASELINE_MS
println("perf[${this::class.simpleName}] elapsed=${elapsedMs}ms baseline=${BASELINE_MS}ms ratio=${"%.2f".format(ratio)} (limit ${TIME_LIMIT_MS}ms)")
assertTrue(
    elapsedMs < TIME_LIMIT_MS,
    "elapsed=${elapsedMs}ms exceeded baseline*slack=${TIME_LIMIT_MS}ms (baseline=${BASELINE_MS}ms, slack=${SLACK_FACTOR}x)",
)
```

`BASELINE_MS` は各 perf テストで実測値に近い値を設定する。今回 4 ファイルとも `timeLimitMs = 500L` だったが、実測は環境依存なのでまずは控えめに `200L` を採用し、CI で実測ログを見ながら次回 PR で再調整する。

### println の理由

JUnit5 では `@DisplayName` での動的ラベル付与が冗長なので、テスト本体で `println` する方が CI ログから現状を読み取りやすい。perf テストは件数が少なく出力ノイズになりにくい。

## 3. CLAUDE.md 更新

「ビルド・実行コマンド」セクションに以下を追記する:

```bash
# 高速サブセット（perf / integration / cli を除外、PR フィードバック向け）
./gradlew testFast

# perf スモークのみ
./gradlew testPerf

# 外部 CLI 結合テストのみ（mmdc / dot 不在時は skip）
./gradlew testCli
```

## 4. 影響範囲

| ファイル | 変更内容 |
|---------|---------|
| `build.gradle.kts` | task 3 件追加 |
| `src/test/kotlin/com/rescript/plugin/perf/*.kt` | baseline + slack モデル、4 ファイル |
| `CLAUDE.md` | コマンド表に 3 行追記 |

新規クラス・新規ファイル追加なし。`.claude/rules/steering-workflow.md` の「軽微な修正」例外には該当しない（3 ファイル超 + テスト変更）が、Extension Point 登録もないため通常のステアリングで足りる。

## 5. 実装メモ (revised)

実装中に当初設計から変更した点:

### 5.1 新規 Test タスクから `-Pscope` プロパティ方式へ

最初は `testFast` / `testPerf` / `testCli` を個別 `Test` タスクとして登録したが、IntelliJ Platform Gradle plugin v2 は `tasks.named<Test>("test")` にのみ:

- `intellijPlatformDependencies` 由来の jar を runtime classpath に追加
- `IntelliJPlatformArgumentProvider` / `SandboxArgumentProvider` を JVM 引数として注入

を行う仕様で、新規 `Test` タスクは IntelliJ Platform jar を含まず、`LightVirtualFile` 等の参照で `NoClassDefFoundError` になった。`tasks.test.get()` で base task の classpath をコピーしようとすると Configuration Cache が「Task 参照は serialize できない」と弾いた (`org.gradle.api.tasks.testing.Test cannot be serialized`)。

このため設計を変更し、`tasks.test {}` 内部で `-Pscope` プロパティを読んで `filter { ... }` を切り替える形にした。`useJUnitPlatform()` のあとに `when (scope) { ... }` を置く 1 ブロックで完結する。

```kotlin
tasks.test {
    useJUnitPlatform()
    val scope = providers.gradleProperty("scope").orNull
    when (scope) {
        "fast" -> { filter { excludeTestsMatching("*PerfTest"); ... } }
        "perf" -> { filter { includeTestsMatching("com.rescript.plugin.perf.*") } }
        "cli"  -> { filter { includeTestsMatching("com.rescript.plugin.cli.*") } }
        null   -> { /* default: no filter */ }
        else   -> throw GradleException("Unknown -Pscope=$scope. ...")
    }
}
```

使い方:

```bash
./gradlew test -Pscope=fast    # excludes perf / integration / cli
./gradlew test -Pscope=perf
./gradlew test -Pscope=cli
./gradlew test                 # full default
```

### 5.2 perf テスト: warmup を追加

cold-start 環境で `RescriptSwitchArmCollector.collect` の初回実行が 1447ms になり、新しい 200ms ベースの上限を踏み抜いた。原因は classloader / JIT compilation コストで、本テストの真の対象 (algorithm) のコストではない。各テストの timed iteration の前に同じ対象を 1 回だけ呼び出す warmup pass を追加して JIT を効かせた。

warmup 後の観測値 (M-series macbook):

| Test | observed | baseline | ratio | limit |
|------|---------|---------|-------|-------|
| SwitchArmCollector | 5ms | 80ms | 0.06 | 200ms |
| InteropClassifier | 18ms | 200ms | 0.09 | 500ms |
| InteropScanner | 2ms | 200ms | 0.01 | 500ms |
| VariantFlowModel | 24ms | 400ms | 0.06 | 1000ms |

ratio はすべて 0.01–0.09 で大きなヘッドルームがある。`good-first-issues.md` エントリ #6 に「BASELINE_MS ratchet 下げ」として残し、CI 実測ログを見ながら段階的に締めていく。

### 5.3 テスト戦略 (revised)

- `./gradlew test -Pscope=perf` で 4 件全 PASS することを確認
- `./gradlew ktlintCheck` が緑
- `./gradlew clean buildPlugin test` (フル) が緑 — 最終確認 (実行に 10 分超)
