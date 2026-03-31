# タスクリスト: コード品質改善バッチ

## T1: Kotlin モダナイゼーション
- [x] 6つの `object` を `data object` に変換
- [x] `getService()` を `serviceOrNull<T>()` に移行
- [x] コミット

## T2: バージョンカタログ導入
- [ ] `gradle/libs.versions.toml` 作成
- [ ] `build.gradle.kts` のバージョン参照を置換
- [ ] `settings.gradle.kts` のプラグインバージョンを置換
- [ ] コミット

## T3: @Suppress DEPRECATION 見直し
- [ ] 4件の DEPRECATION 抑制を調査
- [ ] 代替 API 移行または理由コメント追加
- [ ] コミット

## T4: パラメータ化テスト導入
- [ ] RescriptLexerTest の反復テストを @ParameterizedTest に変換
- [ ] コミット

## T5: Kover 除外リスト精緻化
- [ ] テスト済みパッケージを個別クラス除外に変更
- [ ] カバレッジ確認
- [ ] コミット

## T6: CONTRIBUTING.md 拡充
- [ ] 前提条件・CI コマンド・品質チェック情報追加
- [ ] コミット

## T7: 検証・マージ
- [ ] `./gradlew ktlintCheck clean buildPlugin test koverHtmlReport` 成功
- [ ] main にマージ
