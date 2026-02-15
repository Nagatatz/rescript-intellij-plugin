# Design: GitHub Actions CI 整備

## 変更対象ファイル

| ファイル | 変更内容 |
|---|---|
| `.github/workflows/ci.yml` | CI ワークフローの改善 |
| `gradle.properties` | `pluginUntilBuild` 削除、`kotlin.stdlib.default.dependency=false` 追加 |
| `build.gradle.kts` | `untilBuild` 参照の削除 |

## 設計

### 1. CI ワークフロー構成 (`ci.yml`)

2 ジョブ構成に変更する:

#### ジョブ 1: `build` (全トリガーで実行)

push(main) と PR(main) の両方で実行。軽量で高速なフィードバックを提供する。

```
steps:
  - Checkout
  - Gradle Wrapper 検証
  - JDK 21 セットアップ
  - Gradle セットアップ (キャッシュ付き)
  - ktlint チェック
  - プラグインビルド
  - テスト実行
  - プラグイン構造検証 (verifyPluginStructure)
  - プロジェクト設定検証 (verifyPluginProjectConfiguration)
  - 成果物アップロード (plugin zip, test results)
```

#### ジョブ 2: `verify` (main push 時のみ実行)

`verifyPlugin` は IDE バイナリのダウンロードを伴い時間がかかるため、main push 時のみ実行する。

```
条件: github.event_name == 'push'
steps:
  - Checkout
  - JDK 21 セットアップ
  - Gradle セットアップ (キャッシュ付き)
  - verifyPlugin (バイナリ互換性検証)
```

### 2. Gradle 設定の警告対応

#### `pluginUntilBuild` の削除

IntelliJ Platform 2024.3+ では `until-build` を設定すると将来の IDE バージョンでプラグインがインストールできなくなる。前方互換性のため削除する。

**`gradle.properties`:**
```diff
- pluginUntilBuild = 254.*
```

**`build.gradle.kts`:**
```diff
  ideaVersion {
      sinceBuild = providers.gradleProperty("pluginSinceBuild")
-     untilBuild = providers.gradleProperty("pluginUntilBuild")
  }
```

#### Kotlin stdlib 依存関係の修正

Gradle Kotlin プラグインが自動追加する stdlib と IntelliJ Platform にバンドルされている stdlib が競合する可能性がある。

**`gradle.properties`:**
```diff
+ kotlin.stdlib.default.dependency=false
```

## 影響範囲

- CI ワークフローの変更: GitHub Actions の動作のみに影響。プラグインのランタイム動作には影響しない
- `pluginUntilBuild` 削除: プラグインが 2025.3 以降の全 IDE バージョンで互換性を持つようになる（`sinceBuild = 253.0` のみで制限）
- Kotlin stdlib 設定: ランタイムでの ClassNotFoundException リスクを軽減
