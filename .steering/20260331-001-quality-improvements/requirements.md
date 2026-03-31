# 要求: コード品質改善

## 目的

ブランチカバレッジの向上（79% → 85%+）とエラーハンドリングの改善により、プラグインの堅牢性を高める。

## 対象

### 1. ブランチカバレッジ改善

未カバーのエラーパス・例外パス・null チェック分岐にテストを追加する。

| クラス | 現在のブランチカバレッジ | 未カバー内容 |
|--------|----------------------|-------------|
| `RescriptProcessUtils` | 62.5% (5/8) | タイムアウト・InterruptedException パス |
| `RescriptSecurityUtils` | 80% (8/10) | `isWithinProject()` の null ケース |
| `RescriptFileUtil` | 80% (8/10) | 未特定の2分岐 |

### 2. エラーハンドリング改善

| 箇所 | 問題 | 修正内容 |
|------|------|---------|
| `RescriptCodeVisionProvider.java:108` | `catch (Exception)` でログなし | ログ追加 |
| `DtsParserProcess.kt` | `extractScript()` に競合状態 | `synchronized` 追加 |
| `RescriptDependenciesPanel.kt` | 汎用 `Exception` キャッチ | 具体的な例外型に分割 |
| `RescriptLspUtils.kt` | URI パース失敗時にログなし | トレースログ追加 |

## 受け入れ条件

- [ ] ブランチカバレッジが 85% 以上
- [ ] 特定された4箇所のエラーハンドリングが改善されている
- [ ] 全テストがパスする
- [ ] `./gradlew ktlintCheck buildPlugin test` が成功する
