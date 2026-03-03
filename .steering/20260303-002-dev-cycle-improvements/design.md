# 設計: 開発サイクル改善

## 1. Post-Write フック最適化

### 現状
PostToolUse で `format-kotlin.sh` (60s) + `check-kotlin-build.sh` (120s) を同期実行。

### 変更
- `settings.json` から `check-kotlin-build.sh` の PostToolUse フックを削除
- `format-kotlin.sh` のみ残す（フォーマットは高速で即時フィードバックが有用）

## 2. CI verifyPlugin 追加

### 現状
`verify` ジョブが `if: github.event_name == 'push'` で制限されており、PR では実行されない。

### 変更
- `verify` ジョブの `if` 条件を削除し、push/PR 両方で実行
- または `build` ジョブに `verifyPlugin` ステップを追加

### 選択: `verify` ジョブの条件削除
理由: 既存ジョブ構成を活かし、最小限の変更で実現。

## 3. kover カバレッジ閾値

### 変更
`build.gradle.kts` の kover ブロックに `verify` セクションを追加:

```kotlin
kover {
    reports {
        verify {
            onCheck = true
            rule {
                minBound(50, metric = MetricType.LINE)
            }
        }
    }
}
```

現在のカバレッジが閾値を下回る場合は閾値を現状値に合わせて段階的に引き上げ。

## 4. Unstable API コメント

10箇所の `@Suppress("UnstableApiUsage")` に以下の形式でコメント追加:

```kotlin
// UnstableApiUsage: <API名> — tracked since <platformVersion>, check stability on platform upgrade
@Suppress("UnstableApiUsage")
```

## 5. ステアリング軽量変更定義

`steering-workflow.md` の例外セクションに定量的基準を追加:

- 変更対象が **3ファイル以下**
- 合計変更量が **50行以下** (LOC)
- 新規 Extension Point 登録が **不要**
- 新規クラスの作成が **不要**

## 6. Qodana CI 統合

Qodana は既に PR/push で実行されている。GitHub Branch Protection で required check にする提案をドキュメント化（実行は GitHub UI 側の設定のため）。
→ `ci.yml` へのコメント追記で対応。
