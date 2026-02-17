# Design: テストカバレッジの導入と GitHub Actions 表示

## 実装アプローチ

### 1. Kover Gradle プラグインの導入

`build.gradle.kts` に Kover プラグインを追加し、レポート設定を行う。

**変更ファイル:** `build.gradle.kts`

```kotlin
plugins {
    // ... 既存のプラグイン
    id("org.jetbrains.kotlinx.kover") version "0.9.4"
}

kover {
    reports {
        total {
            xml {
                onCheck = false
                xmlFile = file("build/reports/kover/report.xml")
            }
            html {
                onCheck = false
                htmlDir = file("build/reports/kover/html")
            }
        }
        filters {
            excludes {
                // JFlex 自動生成コードを除外
                classes("com.rescript.plugin.lang.RescriptFlexLexer")
            }
        }
    }
}
```

### 2. GitHub Actions CI ワークフローの更新

**変更ファイル:** `.github/workflows/ci.yml`

#### 2a. テスト実行後に Kover レポート生成

`./gradlew test` を `./gradlew test koverXmlReport koverHtmlReport` に変更。

#### 2b. PR コメント投稿

[`mi-kas/kover-report@v1`](https://github.com/mi-kas/kover-report) を使用して PR にカバレッジコメントを投稿。

```yaml
- name: Add coverage report to PR
  if: github.event_name == 'pull_request'
  uses: mi-kas/kover-report@v1
  with:
    path: ${{ github.workspace }}/build/reports/kover/report.xml
    title: Code Coverage
    update-comment: true
    coverage-counter-type: LINE
```

#### 2c. Job Summary にカバレッジ表示

Kover の XML レポートを解析し、Job Summary に出力するステップを追加。

```yaml
- name: Add coverage to Job Summary
  if: always()
  run: |
    # XML からカバレッジ率を抽出して GITHUB_STEP_SUMMARY に書き込み
```

#### 2d. HTML レポートをアーティファクトとしてアップロード

```yaml
- name: Upload coverage report
  if: always()
  uses: actions/upload-artifact@v6
  with:
    name: coverage-report
    path: build/reports/kover/html/
```

### 3. .gitignore の確認

Kover の生成レポート (`build/` 配下) は既に `.gitignore` でカバーされているため、追加変更は不要。

## 影響範囲

| ファイル | 変更内容 |
|---------|---------|
| `build.gradle.kts` | Kover プラグイン追加、レポート設定 |
| `.github/workflows/ci.yml` | カバレッジ生成・表示ステップ追加 |

## リスク

- **ビルド時間の増加**: Kover のインストルメンテーションにより、テスト実行時間がわずかに増加する可能性がある（通常 5-10% 程度）
- **PR コメント権限**: フォークからの PR ではコメント投稿が制限される（GitHub の仕様）
