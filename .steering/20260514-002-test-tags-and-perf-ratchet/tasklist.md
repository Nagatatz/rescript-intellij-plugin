# タスクリスト: テスト分類の整備と perf ratchet 強化

> **実装中の方針変更**: 当初は `testFast` / `testPerf` / `testCli` の新規 Test タスクを登録する計画だったが、IntelliJ Platform Gradle plugin v2 が `tasks.test` にのみ JVM 引数プロバイダ・runtime classpath を付加する設計のため、新規 `Test` タスクでは `NoClassDefFoundError` が発生し、`tasks.test.get()` での参照は Configuration Cache 違反となった。代わりに **`tasks.test` 自体に `-Pscope=<fast|perf|cli>` プロパティを受けて filter 適用** する設計に変更。詳細は `design.md` の "5. 実装メモ (revised)" を参照。

## セクション 1: tasks.test の scope-based filter 化

- [x] `build.gradle.kts` の `tasks.test {}` に `-Pscope` 分岐 (fast / perf / cli / null / else) を追加
- [x] `./gradlew test -Pscope=perf` で perf テスト 4 件のみ実行されることを確認
- [x] `./gradlew ktlintCheck` が緑
- [x] コミット: `🔧 Add -Pscope=fast|perf|cli filter to the test task`

## セクション 2: perf テストを baseline + slack モデルへ

- [x] `RescriptInteropClassifierPerfTest.kt` を baseline + slack + warmup に変更
- [x] `RescriptInteropScannerPerfTest.kt` を baseline + slack + warmup に変更
- [x] `RescriptSwitchArmCollectorPerfTest.kt` を baseline + slack + warmup に変更
- [x] `RescriptVariantFlowModelPerfTest.kt` を baseline + slack + warmup に変更
- [x] `./gradlew test -Pscope=perf` で 4 件全 PASS (cold-start を warmup で吸収後、ratio 0.01–0.09)
- [x] コミット: `♻️ Convert perf tests to baseline + slack ratchet with warmup`

## セクション 3: ドキュメント更新

- [x] `CLAUDE.md` のビルドコマンド表に `test -Pscope=fast|perf|cli` を追加
- [x] `README.md` の Quick reference に `test -Pscope=fast` を追加 (steering 003 と同時に取り込み)
- [x] コミット: `📝 Document test -Pscope filter in CLAUDE.md`

## セクション 4: マージ

- [x] `./gradlew ktlintCheck` 緑
- [x] `./gradlew clean buildPlugin test` 緑 — 最終確認 (1m 41s, Configuration Cache stored)
- [x] tasklist 全項目を `[x]` に更新してコミット
- [ ] `main` にマージ
