# パフォーマンス検証手順

本ドキュメントは `docs/product-requirements.md` の **NFR-01: パフォーマンス** に定義された目標値を、いつ・どのように計測し、結果を記録するかを定義する。Kover カバレッジラチェット（`build.gradle.kts` の `koverVerify`）と同様、リリースのたびに値が悪化しないかを確認するためのプロセスである。

## 1. 目標値（NFR-01 再掲）

| 項目 | 目標値 | 根拠 |
|---|---|---|
| シンタックスハイライト更新 | < 16ms（60fps 相当） | エディタ入力時の体感即座 |
| IDE 起動への影響 | 無視できるレベル | 遅延ロード（`postStartupActivity`） |
| メモリ使用量 | プラグイン単体 50MB 以下 | 他プラグインとの共存 |
| LSP 起動時間 | Node.js プロセス起動に依存 | プラグイン側のオーバーヘッド最小 |

## 2. 計測手段

### 2.1 シンタックスハイライト更新時間

**ツール**: IntelliJ Platform Profiler（IDE 同梱）

**手順**:

1. `./gradlew runIde` で開発用 IDE を起動
2. Sample プロジェクト（`src/uiTest/testData/sample-project/`）を開く
3. **Help** → **Diagnostic Tools** → **Start Profiling** を選択
4. 大きめの `.res` ファイル（500 行以上）を開いて編集を行う
5. プロファイル結果から `RescriptLexer.advance()` および `RescriptSyntaxHighlighter.getTokenHighlights()` の累積時間を取得
6. 1 文字編集あたりの平均時間を算出

**合格基準**: 1 編集あたり 16ms 未満

### 2.2 IDE 起動への影響

**ツール**: IntelliJ Platform Startup Performance Tool

**手順**:

1. `./gradlew runIde -PstartupReport=true` （カスタムタスク要）
2. IDE 起動時に `~/.IntelliJIdea<version>/log/idea.log` にスタートアップ時間が記録される
3. プラグイン無効化時との差分を取る

**合格基準**: プラグインによる起動遅延 200ms 以下

### 2.3 メモリ使用量

**ツール**: JVM Heap Dump（**Help** → **Diagnostic Tools** → **Capture Memory Snapshot**）

**手順**:

1. 開発用 IDE を起動し、サンプルプロジェクトを開いて 5 分間操作
2. メモリスナップショットを取得
3. **Memory Analyzer**（または IntelliJ 同梱）で `com.rescript.plugin.*` のクラス階層とインスタンスの保持メモリを集計

**合格基準**: プラグイン関連オブジェクトの合計 50MB 以下

### 2.4 LSP 起動時間

**ツール**: `idea.log` のタイムスタンプ + 自前ログ（`RescriptLspServerDescriptor.startServerProcess` 周辺）

**合格基準**: ユーザー操作（プロジェクトを開く）から LSP `initialize` 完了まで 3 秒以下（Node.js v18 / 標準的なマシンで）

## 3. 計測タイミング

| タイミング | 担当 | 対象指標 | 結果の扱い |
|---|---|---|---|
| **メジャーリリース前**（手動） | リリース担当 | 全 4 指標 | リリースノートまたは `docs/performance-results.md` に記録 |
| **マイナーリリース前**（手動） | リリース担当 | 起動時間・ハイライト更新の差分のみ | 異常時のみ記録 |
| **CI 月次**（推奨・未実装） | GitHub Actions（cron） | 起動時間・メモリ使用量 | アーティファクトとして保存、退化時は GitHub Issue を自動起票 |
| **大規模リファクタ後** | 実装者 | 影響を受けた指標 | PR 本文に記載 |

## 4. 結果の記録

### 4.1 個別リリース時

リリースノート（`plugin.xml` の `<change-notes>` または GitHub Release）に必要に応じて以下を記載:

```text
Performance:
- Highlight update: 12.4ms (target: < 16ms) ✓
- Plugin memory: 38MB (target: < 50MB) ✓
- LSP startup: 1.2s (target: < 3s) ✓
```

### 4.2 計測履歴の長期記録

将来的に `docs/performance-results.md` を追加し、バージョンごとの計測値を表形式で蓄積することを推奨する。フォーマット例:

```markdown
| バージョン | 計測日 | ハイライト更新 | プラグインメモリ | LSP 起動時間 | 計測環境 |
|---|---|---|---|---|---|
| 0.1.12 | 2026-04-26 | 12.4ms | 38MB | 1.2s | macOS 14.5 / M1 / IDEA 2025.3.2 |
```

## 5. パフォーマンスラチェット（提案）

カバレッジラチェット（`koverVerify` の `minBound`）と類似の方針で、計測値の退化をブロックする CI ジョブを追加できる:

```kotlin
// build.gradle.kts への追加例
tasks.register("performanceRatchet") {
    doLast {
        val results = readPerformanceResults()  // CI で取得した実測値
        val baseline = readPreviousBaseline()   // 前回計測値
        if (results.highlightMs > baseline.highlightMs * 1.10) {
            throw GradleException("Highlight update regressed by >10%")
        }
        // ... 他の指標も同様
    }
}
```

ラチェットルール:

| 指標 | 退化閾値 |
|---|---|
| ハイライト更新 | 前回比 +10% で fail |
| 起動時間影響 | 前回比 +20% で fail |
| プラグインメモリ | 前回比 +15% で fail |
| LSP 起動時間 | 環境依存のため CI ラチェットは行わない |

ただし、ハードウェアや IDE バージョンの違いで揺らぎが大きいため、CI ジョブは **退化警告のみ** に留め、リリースブロックには使わない設計を推奨する。

## 6. 既知の制約

- **ハードウェア依存**: 計測値は CPU・メモリ・ストレージで大きく変動する。比較は同一環境で行うこと
- **JIT ウォームアップ**: IDE 起動直後の計測は JIT 最適化が効いていないため、5 分のウォームアップ後に再計測する
- **LSP の影響分離**: `@rescript/language-server` のバージョン差は LSP 起動時間に直接影響する。プラグイン側のメトリクスは LSP 接続後の安定状態で取得する

## 7. 参照

- IntelliJ Platform Performance Profiling: <https://plugins.jetbrains.com/docs/intellij/performance.html>
- Kover カバレッジラチェット: `build.gradle.kts` の `koverVerify` ブロック
- リリース手順: `.claude/rules/release.md`
