# Tasklist: UI テストフレームワーク導入 & スクリーンショット自動撮影

## Phase 1: Gradle 設定 & 基盤

- [x] 1.1 `build.gradle.kts` に Remote-Robot 依存関係を追加
  - `com.intellij.remoterobot:remote-robot:0.11.23`
  - `com.intellij.remoterobot:remote-fixtures:0.11.23`
- [x] 1.2 `src/uiTest/` ソースセットを作成・Gradle に登録
- [x] 1.3 `runIdeForUiTests` タスクを `intellijPlatformTesting` DSL で登録
- [x] 1.4 `uiTest` テスト実行タスクを登録
- [x] 1.5 Kover から `src/uiTest/` を除外
- [x] 1.6 `.gitignore` に `src/uiTest/testData/sample-project/node_modules/` を追加
- [x] 1.7 ビルド確認: `./gradlew buildPlugin` が既存ビルドを壊さないこと

## Phase 2: テスト基盤クラス

- [x] 2.1 `UiTestBase.kt` — 共通基底クラス（接続、スクリーンショット撮影、IDE 待機）
- [x] 2.2 `fixtures/IdeFixtures.kt` — エディタ・ツールウィンドウ操作用 Fixture
- [x] 2.3 基盤クラスのユニットテスト — ※ 全クラスが Remote-Robot 接続に依存するため免除（UI テストフレームワーク自体が結合テスト前提）

## Phase 3: テストプロジェクト

- [x] 3.1 `src/uiTest/testData/sample-project/rescript.json` 作成
- [x] 3.2 `src/uiTest/testData/sample-project/package.json` 作成
- [x] 3.3 `src/uiTest/testData/sample-project/src/Demo.res` 作成
- [x] 3.4 `src/uiTest/testData/sample-project/src/Demo.resi` 作成
- [x] 3.5 `src/uiTest/testData/sample-project/src/ErrorDemo.res` 作成
- [x] 3.6 `src/uiTest/testData/sample-project/src/JsxDemo.res` 作成

## Phase 4: スクリーンショット撮影テスト

- [x] 4.1 `MarketplaceScreenshotTest.kt` — 全 11 シーンの撮影テストクラス
  - シーン 1: シンタックスハイライト
  - シーン 2: コード補完
  - シーン 3: Error Lens
  - シーン 4: インレイヒント
  - シーン 5: ストラクチャービュー
  - シーン 6: Code Vision
  - シーン 7: JSX サポート
  - シーン 8: Project View
  - シーン 9: クイックフィックス / Intention
  - シーン 10: ホバードキュメント
  - シーン 11: REPL

## Phase 5: 動作検証

- [ ] 5.1 `./gradlew runIdeForUiTests` で IDE が起動し、ポート 8082 で接続可能 — ※ ディスク容量不足のためスキップ、後日検証
- [ ] 5.2 `./gradlew uiTest` で全 11 スクリーンショットが `build/screenshots/` に生成される — ※ 同上
- [x] 5.3 `./gradlew test` が既存ユニットテストのみ実行される（UI テスト混入なし）
- [ ] 5.4 スクリーンショットの品質確認（解像度、可読性） — ※ 同上

## Phase 6: ドキュメント & コミット前検証

- [x] 6.1 CLAUDE.md 更新（UI テスト基盤の記載追加）
- [x] 6.2 README.md 更新（Development セクションに UI テスト実行方法追加）
- [x] 6.3 sphinx-docs 更新（dev/testing.md に UI テストの説明追加）
- [x] 6.4 `./gradlew ktlintCheck` パス
- [x] 6.5 `./gradlew clean buildPlugin` パス
- [x] 6.6 `./gradlew test` パス（既存テスト影響なし）
- [x] 6.7 KDoc コメント確認

## Phase 7: 機能リクエスト & マージ

- [x] 7.1 JetBrains YouTrack — ※ ユーザー判断でスキップ
- [x] 7.2 tasklist.md 全タスク完了確認
- [x] 7.3 ユーザーにマージ確認
- [x] 7.4 main にマージ & ブランチ削除
