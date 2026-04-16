# Tasklist: UI テストスクリーンショット品質修正

## 実装

- [x] UiTestBase.kt: `ideFrame` フィールド追加 + `takeScreenshot` を IDE ウィンドウのみキャプチャに変更
- [x] IdeFixtures.kt: `openFileByName` を `runJs` + `FileEditorManager.openFile()` に置換
- [x] MarketplaceScreenshotTest.kt: `ideFrame` を基底クラスに移行 + REPL `invokeAndWait` 修正
- [x] MarketplaceScreenshotTest.kt: IDE エラー通知の消去 + LSP 未検出バーの非表示処理を追加
- [x] MarketplaceScreenshotTest.kt: LSP 利用可能前提でテスト内容を調整（補完・Error Lens・Intention 等）

## 検証

- [ ] `./gradlew buildPlugin` が成功する
- [ ] `./gradlew runIdeForUiTests` で IDE 起動 → `./gradlew uiTest` で全 11 テストがパスする
- [ ] スクリーンショットを目視確認:
  - IDE ウィンドウのみ（デスクトップ背景なし）
  - 各テストで意図したファイルが表示されている
  - Search Everywhere ダイアログが残っていない
  - IDE 内部エラー通知（赤いバルーン/アイコン）がない
  - LSP 未検出の黄色い通知バーがない

## コミット・マージ

- [ ] 機能単位でコミット
- [ ] main にマージ

## スコープ外（別タスク）

- Stub Builder 互換性バグの根本原因調査・修正（15個の SEVERE エラー）
