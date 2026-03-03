# 要求: 開発サイクル改善

## 背景

Qodana 対応を契機に開発サイクル全体を見直し、ビルド警告・CI/CD・コード品質・ワークフローの4軸で障害と改善機会を特定した。

## スコープ

以下の6項目を実施する（テスト追加とドキュメント同期チェッカーは別作業として分離）:

1. **Post-Write フック最適化** — `check-kotlin-build.sh` をコミット前チェックに移行し、PostToolUse での同期ビルドチェックを廃止
2. **CI に verifyPlugin 追加** — PR でもバイナリ互換性チェックを実行
3. **kover カバレッジ閾値設定** — 最低カバレッジ閾値を設定
4. **Unstable API コメント追加** — `@Suppress("UnstableApiUsage")` に安定化予定情報のコメント追加
5. **ステアリング軽量変更定義の明確化** — 具体的な閾値を追加
6. **Qodana CI 統合検討** — CI パイプラインとの連携改善

## 受け入れ条件

1. `.kt` ファイル編集時の PostToolUse フック実行が ktlintFormat のみに短縮される
2. CI の `build` ジョブで `verifyPlugin` が PR 時にも実行される
3. kover にカバレッジ閾値が設定されている
4. 全 `@Suppress("UnstableApiUsage")` にコメントが付与されている
5. `steering-workflow.md` に軽量変更の定量的基準が追記されている
6. `./gradlew clean buildPlugin` が成功する
