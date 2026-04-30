# 要件定義: 品質計測・プロセス簡素化・将来リスク管理

## 背景

先のプロジェクト批判的レビュー（`/Users/ngtz/.claude/plans/peppy-pondering-lerdorf.md`）で
抽出された 10 提言のうち、ユーザーが C / D / G / H / I の 5 項目を選定。
本作業はこの 5 項目を実装する。

## ゴール

5 項目すべてを反映した状態で `main` にマージする。各項目は単独でも価値を出し、
相互に補完的に機能する:

| 項目 | ゴール |
|------|--------|
| C | tasklist の必須セクションを簡素化し、「コミット前検証/マージ」記載を DoD 参照に置換 |
| G | CI でカバレッジを強制（既存実装を確認し、release.md ラチェットと整合 |
| H | `plugin-verifier-ignored-problems.txt` の各エントリに `Expires:` を追加、月次 CI で期限切れを警告 |
| D | FUS（Feature Usage Statistics）の最小実装を導入し、wizard / toolwindow / intention の 3 イベントだけ記録 |
| I | PIT (pitest) を `util/` `lang/` に限定して導入。CI では PR 時のみ実行 |

## 受け入れ条件

### C
- [ ] `.claude/rules/steering-workflow.md` の「## tasklist.md の必須セクション」が 2 項目（実装タスク / ドキュメント更新タスク）に縮小されている
- [ ] 既存ステアリングの tasklist.md が壊れない（参照のみで内容は不変）

### G
- [ ] `.github/workflows/ci.yml` の `koverVerify` 実行ステップが残存している
- [ ] `build.gradle.kts` の `kover.minBound` が現状実測値の -3% 以上 -10% 未満に収まっている
- [ ] `release.md` のカバレッジラチェット規約と矛盾しない

### H
- [ ] `plugin-verifier-ignored-problems.txt` の 4 つの `Status: KEEP` エントリすべてに `Expires: YYYY-MM-DD` が追加されている（12 ヶ月後 = 2027-04-29）
- [ ] `.claude/rules/deprecated-api.md` の抑制手順に `Expires:` 必須記載が反映されている
- [ ] `.github/workflows/monthly-verify.yml` で `Expires:` を解析して期限切れエントリを CI Step Summary に警告として表示するステップが追加されている（CI を失敗させない）

### D
- [ ] `src/main/kotlin/com/rescript/plugin/analytics/RescriptFeatureUsageCounter.kt` が存在し、`CounterUsagesCollector` を実装する
- [ ] `wizard.template.selected` / `toolwindow.opened` / `intention.invoked` の 3 イベントが定義されている
- [ ] `plugin.xml` に `<statistics.counterUsagesCollector>` が登録されている
- [ ] 個人情報・ファイルパス・ソース片を一切記録しない（バリデーションロジックがある場合はテストで担保）
- [ ] `RescriptFeatureUsageCounterTest.kt` が存在し、イベント定義の整合性を検証する

### I
- [ ] `build.gradle.kts` に `info.solidsoft.pitest` Gradle plugin が追加されている
- [ ] `pitest` 設定で targetClasses が `com.rescript.plugin.util.*` と `com.rescript.plugin.lang.*` に限定されている
- [ ] `.github/workflows/ci.yml` に PR 時のみ走る `pitest` ジョブが追加されている
- [ ] 初回ベースラインは結果出力のみで失敗させない（threshold 未設定）

## 非機能要件

- **互換性**: 既存ビルド (`./gradlew clean buildPlugin`) が通ること
- **テスト**: 既存テストすべてグリーン
- **ktlint**: 違反ゼロ
- **PluginVerifier**: 新規警告ゼロ
- **セキュリティ (D)**: テレメトリは IntelliJ 標準のオプトインに従う。プラグイン独自に許諾を取得しない。

## 非ゴール

- D は **3 イベント**にのみフォーカス。ウィザード以外の機能（補完、quickfix、テンプレート種別の細分集計など）は今回扱わない
- I の mutation score を CI で強制しない（任意レポートのみ）
- C の他の DoD 簡素化は対象外（DoD の参照リンク維持に留める）

## 参考

- `/Users/ngtz/.claude/plans/peppy-pondering-lerdorf.md` の Phase 1 / Phase 2
- `.claude/rules/release.md` のカバレッジラチェット規約
- `.claude/rules/deprecated-api.md` の抑制手順
