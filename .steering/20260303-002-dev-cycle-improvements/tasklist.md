# タスクリスト: 開発サイクル改善

## Phase 1: フック最適化

- [x] `settings.json` から `check-kotlin-build.sh` の PostToolUse エントリを削除
- [x] コミット: `⚡ Remove synchronous build check from PostToolUse hooks`

## Phase 2: CI 改善

- [x] `ci.yml` の `verify` ジョブから `if: github.event_name == 'push'` を削除
- [x] `ci.yml` の test ステップに `koverVerify` を追加
- [x] コミット: `🔧 Run verifyPlugin on PR and add koverVerify to CI`

## Phase 3: カバレッジ閾値

- [x] `build.gradle.kts` に kover verify ルール追加（minBound 50%）
- [x] カバレッジ閾値が現状（51%）を下回らないことを確認
- [x] コミット: `🔧 Add kover minimum coverage threshold`

## Phase 4: Unstable API コメント

- [x] 10箇所の `@Suppress("UnstableApiUsage")` にコメント追加
- [x] ビルド確認
- [x] コミット: `📝 Add stability tracking comments to @Suppress("UnstableApiUsage")`

## Phase 5: ステアリング定義の明確化

- [x] `steering-workflow.md` に軽量変更の定量基準を追加
- [x] コミット: `📝 Clarify lightweight change criteria in steering workflow`

## Phase 6: 完了

- [x] `./gradlew clean buildPlugin` 成功確認
- [ ] tasklist.md 全タスク `[x]` 確認
- [ ] ユーザーにマージ確認
- [ ] main にマージ + ブランチ削除

**テスト省略理由:** 本作業は設定ファイル・CI 設定・コメント・ドキュメントの変更のみであり、新規 Kotlin クラスの追加を含まない。テスト規約の免除対象（ドキュメントのみの変更）に該当する。
