# 設計: 品質計測・プロセス簡素化・将来リスク管理

## 全体方針

5 項目を **独立コミット**として段階導入する。各コミットは単独でビルド可能。
順序は破壊リスクの小さい順: C → G → H → D → I。

## 設計詳細

### C: tasklist 必須セクションの簡素化

**変更対象**: `.claude/rules/steering-workflow.md` のみ

**変更前** (4 必須項目):

```
1. 各機能の実装タスク
2. ドキュメント更新タスク
3. コミット前検証タスク
4. マージタスク
```

**変更後** (2 必須項目 + 1 注記):

```
1. 各機能の実装タスク
2. ドキュメント更新タスク

コミット前検証とマージ手順は `.claude/rules/definition-of-done.md` Phase 3〜5 に従う。
tasklist には独自項目があれば追加してよいが、DoD と重複する記載は避けること。
```

**理由**: 以前は tasklist 内に DoD と重複したチェックリストが並び、メンテ時に
両方を同期する必要があった。索引 (DoD) を参照する形に統一する。

### G: CI でのカバレッジ強制

**現状確認**: `.github/workflows/ci.yml` 行 104 にて以下が既に実行されている:

```bash
./gradlew test koverVerify koverXmlReport koverHtmlReport
```

`build.gradle.kts` 行 267 にて `minBound(86)` が設定済み。

**変更内容**:
- 実測値を確認するため `./gradlew test koverHtmlReport` をローカル実行
- `build.gradle.kts` のコメントに「実測値とラチェットの関係」を残す（minBound を実測値 -3% 程度に維持）
- `release.md` の手順と整合していることを再確認するためのコメント追加（コードは触らない）

**変更対象**: `build.gradle.kts`（コメント追記のみ。`minBound` は実測値次第で調整）

### H: ignored-problems 期限管理

**変更対象**: 3 ファイル

#### H-1: `plugin-verifier-ignored-problems.txt`

各 `Status: KEEP` エントリに `Expires: 2027-04-29` を追加（12 ヶ月後）。

```
// Status: KEEP — annotation not yet removed from source.
// Expires: 2027-04-29
```

`Expires:` の意味: この日以降は再確認必須。CI ワーニングが出る。

#### H-2: `.claude/rules/deprecated-api.md`

「抑制手順」セクションの番号 3 に `Expires:` を追加:

```
3. エントリには以下を含める:
   - API 名と使用理由
   - 対象ソースファイル名
   - `Status: KEEP` と `Reviewed: YYYY-MM-DD`
   - `Expires: YYYY-MM-DD`（推奨: Reviewed の 12 ヶ月後）
```

#### H-3: `.github/workflows/monthly-verify.yml`

`verifyPlugin` の前に「期限切れエントリ警告」ステップを追加:

```yaml
- name: Check ignored-problems expiry
  shell: bash
  run: |
    today=$(date -u +%Y-%m-%d)
    expired=()
    while IFS= read -r line; do
      if [[ "$line" =~ Expires:[[:space:]]*([0-9]{4}-[0-9]{2}-[0-9]{2}) ]]; then
        exp="${BASH_REMATCH[1]}"
        if [[ "$exp" < "$today" ]]; then
          expired+=("$line")
        fi
      fi
    done < plugin-verifier-ignored-problems.txt
    if [ ${#expired[@]} -gt 0 ]; then
      {
        echo "## ⚠️ Expired ignored-problems entries"
        echo ""
        echo "Today: $today"
        echo ""
        for e in "${expired[@]}"; do
          echo "- $e"
        done
      } >> "$GITHUB_STEP_SUMMARY"
    fi
    # Always exit 0 (warning only)
```

**設計判断**: 失敗にしない（CI を赤にしない）。月次のリマインダとしてのみ機能させる。
`monthly-verify.yml` は schedule トリガーで月 1 回しか走らないため、ワーニングを
見落とす可能性は低い。

### D: 機能利用統計（FUS）

#### D-1: パッケージ構成

```
src/main/kotlin/com/rescript/plugin/analytics/
├── RescriptFeatureUsageCounter.kt   ← CounterUsagesCollector 実装
└── RescriptFeatureUsageEvents.kt    ← 既存呼び出し点に注入する薄いファサード
```

#### D-2: イベント定義

`RescriptFeatureUsageCounter.kt` で以下 3 イベントを宣言:

```kotlin
class RescriptFeatureUsageCounter : CounterUsagesCollector() {
    override fun getGroup(): EventLogGroup = GROUP

    companion object {
        private val GROUP = EventLogGroup("rescript.features", 1)

        // wizard.template.selected
        private val TEMPLATE_KIND = EventFields.String(
            "template_kind",
            allowedValues = listOf(
                "basic", "vite-react", "nextjs", "hono", "hono-graphql",
                "hono-inertia", "cloudflare-workers", "aws-lambda",
                "google-cloud-run", "electron", "react-native-expo",
                "react-native-cli", "npm-library", "cli-tool", "monorepo",
                "full-stack", "res-x",
                "tanstack-start", "remix-rr-v7", "astro", "waku",
            ),
        )
        val WIZARD_TEMPLATE_SELECTED = GROUP.registerEvent(
            "wizard.template.selected",
            TEMPLATE_KIND,
        )

        // toolwindow.opened
        private val TOOLWINDOW_ID = EventFields.String(
            "toolwindow_id",
            allowedValues = listOf(
                "ReScript Type", "ReScript PPX", "ReScript REPL",
                "ReScript Compiled JS", "ReScript Dependencies",
                "ReScript Dependency Diagram",
            ),
        )
        val TOOLWINDOW_OPENED = GROUP.registerEvent(
            "toolwindow.opened",
            TOOLWINDOW_ID,
        )

        // intention.invoked
        private val INTENTION_ID = EventFields.StringValidatedByCustomRule(
            "intention_id",
            ClassNameRuleValidator::class.java,
        )
        val INTENTION_INVOKED = GROUP.registerEvent(
            "intention.invoked",
            INTENTION_ID,
        )
    }
}
```

**セキュリティ**:
- `template_kind`, `toolwindow_id` は **closed enum**（allowedValues）。任意文字列を受け付けない。
- `intention_id` は IntelliJ 提供の `ClassNameRuleValidator` で検証。FQCN 以外を弾く。
- ファイルパス・ソース片は記録しない。

#### D-3: 呼び出し点

最小スコープ: 今回は **イベント定義と plugin.xml 登録のみ**。実際の `log()` 呼び出しは
後続作業（または将来の hookup PR）に分離する。これによりレビューの単位を明確に保つ。

理由:
- 各呼び出し点（wizard / toolwindow / intention）はそれぞれ独立した PR にしやすい
- 計測基盤の正しさと、利用箇所の正しさは別レイヤーの問題
- 即効性のある「FUS が登録されている状態」を先に達成しておけば、後続 PR で安全に hookup できる

ただし、最低限の動作確認用にウィザードの 1 箇所（`RescriptModuleBuilder` の `commit`）には
hookup する。これにより `Help → Diagnostic Tools → Internal Statistics` で
計測可能性を即座に確認できる。

#### D-4: plugin.xml 登録

`<extensions>` セクションに以下を追加:

```xml
<statistics.counterUsagesCollector
    implementationClass="com.rescript.plugin.analytics.RescriptFeatureUsageCounter"/>
```

#### D-5: テスト

`RescriptFeatureUsageCounterTest.kt` で以下を検証:
- `getGroup()` の戻り値が固定値であること
- イベント ID が想定通り（`wizard.template.selected` 等）
- 各イベントフィールドが定義されていること（リフレクションでなく `registerEvent` の戻り値で検証）

UI 結合は不要。`CounterUsagesCollector` は POJO 的なクラスなので IDE 起動なしでテスト可能。

### I: mutation testing 導入

#### I-1: build.gradle.kts への plugin 追加

```kotlin
plugins {
    // ... 既存
    id("info.solidsoft.pitest") version "1.15.0"
}
```

`libs.versions.toml` を使う既存スタイルに合わせるなら同所に登録するが、初回導入は
直接バージョン指定で十分（後で `libs` に移行可能）。

#### I-2: pitest 設定

```kotlin
pitest {
    pitestVersion.set("1.17.0")
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(
        listOf(
            "com.rescript.plugin.util.*",
            "com.rescript.plugin.lang.*",
        ),
    )
    excludedClasses.set(
        listOf(
            "com.rescript.plugin.lang.RescriptFlexLexer*",
            "com.rescript.plugin.lang.RescriptDeclarationParser*",
            "com.rescript.plugin.lang.RescriptJsxParser*",
            "com.rescript.plugin.lang.RescriptParserDefinition*",
            "com.rescript.plugin.lang.RescriptFindUsagesProvider*",
            "com.rescript.plugin.lang.RescriptUsageTypeProvider*",
            "com.rescript.plugin.lang.psi.*",
        ),
    )
    threads.set(2)
    outputFormats.set(listOf("HTML", "XML"))
    timestampedReports.set(false)
    // 初回ベースライン: ラチェットなし。レポートのみ。
    // failWhenNoMutations.set(false)
}
```

`util/`, `lang/` 配下でも IDE 結合な PSI クラスや自動生成クラスは除外。

#### I-3: CI 統合

`.github/workflows/ci.yml` に新規ジョブを追加:

```yaml
mutation-test:
  if: github.event_name == 'pull_request'
  runs-on: ubuntu-latest
  timeout-minutes: 30
  steps:
    - uses: actions/checkout@v6
    - uses: actions/setup-java@v5
      with: { distribution: temurin, java-version: 21 }
    - uses: gradle/actions/setup-gradle@v6
    - name: Run pitest
      run: ./gradlew pitest
    - name: Upload pitest report
      if: always()
      uses: actions/upload-artifact@v7
      with:
        name: pitest-report
        path: build/reports/pitest/
        if-no-files-found: ignore
```

**設計判断**: PR 時のみ実行。main では走らせない（時間コスト）。

## コミット粒度

5 つの独立コミット（順序通り）:

1. ♻️ Simplify required tasklist sections in steering workflow rule
2. 🔧 Document coverage ratchet alignment in build.gradle.kts (G; minBound 調整があれば内包)
3. 🔧 Add Expires field to plugin-verifier-ignored-problems and monthly verify warning
4. ✨ Add Feature Usage Statistics counter for ReScript plugin
5. ✨ Add pitest mutation testing for util and lang packages

最後にステアリングの tasklist を完了状態にしてコミット (📝)。

## リスク

- **D** の FUS は IntelliJ プラットフォーム API なので IDE 起動なしのテストが難しい場合がある
  → mock を使うか、既存テストパターンを参考にする
- **I** の pitest は configuration cache と競合する可能性
  → ローカルで `./gradlew pitest --no-configuration-cache` を試行し、必要なら除外設定を入れる
- **H** のシェルスクリプトは date 比較を文字列で行う（ISO 8601 形式の lexicographical 順を利用）
  → タイムゾーン UTC 固定で問題ないことを確認

## 参考

- IntelliJ Platform FUS docs: https://plugins.jetbrains.com/docs/intellij/feature-usage-statistics.html
- pitest-gradle: https://github.com/szpak/gradle-pitest-plugin
