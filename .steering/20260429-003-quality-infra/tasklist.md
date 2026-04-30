# タスクリスト: 品質計測・プロセス簡素化・将来リスク管理

## 各機能の実装タスク

### C: tasklist 必須セクションの簡素化
- [x] `.claude/rules/steering-workflow.md` の「## tasklist.md の必須セクション」を 2 項目+注記に書き換え
- [x] `git commit -m "♻️ Simplify required tasklist sections in steering workflow rule"`

### G: CI でのカバレッジ強制（実体は既に存在 — 整合性確認）
- [x] `./gradlew test koverHtmlReport` で実測カバレッジを確認
- [x] 必要に応じて `build.gradle.kts` の `minBound(86)` を実測 -3% に調整
- [x] `build.gradle.kts` の kover ブロックに「ラチェット規約は release.md 準拠」のコメントを追記
- [x] `git commit -m "🔧 Document coverage ratchet alignment in build.gradle.kts"`

### H: ignored-problems 期限管理
- [x] `plugin-verifier-ignored-problems.txt` の 4 KEEP エントリに `Expires: 2027-04-29` を追加
- [x] `.claude/rules/deprecated-api.md` の抑制手順に `Expires:` 必須記載を追記
- [x] `.github/workflows/monthly-verify.yml` に期限切れチェックステップを追加（CI を失敗させない）
- [x] ローカルでシェル式の文字列日付比較が動作することを sanity check
- [x] `git commit -m "🔧 Add Expires field to plugin-verifier-ignored-problems and monthly verify warning"`

### D: 機能利用統計（FUS）
- [x] `src/main/kotlin/com/rescript/plugin/analytics/RescriptFeatureUsageCounter.kt` を作成
- [x] 3 イベント (`wizard.template.selected`, `toolwindow.opened`, `intention.invoked`) を `EventLogGroup("rescript.features", 1)` に登録
- [x] `template_kind`, `toolwindow_id` を closed enum バリデーションで定義（allowedValues 使用）
- [x] `intention_id` を `ClassNameRuleValidator` で検証
- [x] `plugin.xml` に `<statistics.counterUsagesCollector>` を登録
- [x] `RescriptModuleBuilder` の `commit` 時にウィザードの 1 イベントを発火（最低限の hookup）
- [x] `src/test/kotlin/com/rescript/plugin/analytics/RescriptFeatureUsageCounterTest.kt` を作成
- [x] テストでイベント定義の整合性を検証
- [x] `./gradlew ktlintCheck buildPlugin test` 通過
- [x] `git commit -m "✨ Add Feature Usage Statistics counter for ReScript plugin"`

### I: mutation testing (PIT) 導入
- [x] `build.gradle.kts` に `info.solidsoft.pitest` plugin を追加
- [x] `pitest` ブロックで targetClasses を `util.*` `lang.*` に限定、IDE 結合クラスを除外
- [x] `.github/workflows/ci.yml` に PR 時のみ実行する `mutation-test` ジョブを追加
- [x] ローカルで `./gradlew pitest` が成功することを確認
- [x] `git commit -m "✨ Add pitest mutation testing for util and lang packages"`

## ドキュメント更新タスク

- [x] CLAUDE.md の「CI/CD」表に Mutation Testing が CI で動く旨を追記
- [x] `docs/repository-structure.md` に `analytics/` パッケージ行を追加
- [x] sphinx-docs/dev/ に「Coverage と Mutation Testing」のセクション or ページを追加（既存 testing.md があればそこに追記）
- [x] sphinx-docs/locale/ja の対応 .po を `make update-po` で更新し未翻訳分を埋める
- [x] `make build-ja` 通過確認
- [x] tasklist の更新と doc 更新を 1 つのコミットに含める: `📝 Update docs for FUS and pitest`

注: コミット前検証とマージ手順は `.claude/rules/definition-of-done.md` Phase 3〜5 に従う。
