# タスクリスト: コード品質改善バッチ

## T1: Kotlin モダナイゼーション
- [x] 6つの `object` を `data object` に変換
- [x] `getService()` を `serviceOrNull<T>()` に移行
- [x] コミット

## T2: バージョンカタログ導入
- [x] `gradle/libs.versions.toml` 作成
- [x] `build.gradle.kts` のバージョン参照を置換
- [x] `settings.gradle.kts` — foojay プラグインは settings では catalog 不可、維持
- [x] コミット

## T3: @Suppress DEPRECATION 見直し
- [x] 6件の DEPRECATION 抑制を調査 — すべて代替 API なし、正当
- [x] 理由コメントが欠けていた3箇所に追加
- [x] コミット

## T4: パラメータ化テスト導入
- [x] RescriptLexerTest の反復テストを @ParameterizedTest に変換（37個→2パラメータ化テスト）
- [x] コミット

## T5: Kover 除外リスト精緻化
- [x] `documentation` パッケージを除外リストから削除（全3クラスにテスト有）
- [x] `analysis`/`inspection`/`intention` は IDE 結合クラスが多く現状維持
- [x] カバレッジ確認 — koverVerify パス
- [x] コミット

## T6: CONTRIBUTING.md 拡充
- [x] Prerequisites セクション追加（JDK 21, Node.js, IntelliJ, Gradle）
- [x] Quality Checks セクション追加（6項目のチェックコマンド一覧）
- [x] Submitting Changes を全CI再現コマンドに更新
- [x] コミット

## T7: 検証・マージ
- [ ] `./gradlew ktlintCheck clean buildPlugin test koverHtmlReport` 成功
- [ ] main にマージ
