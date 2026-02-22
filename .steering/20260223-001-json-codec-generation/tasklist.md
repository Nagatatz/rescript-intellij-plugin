# タスクリスト: JSON エンコーダ/デコーダ生成 (#81)

## Step 1: 型分類器

- [x] `RescriptJsonTypeClassifier.kt` の実装
- [x] `RescriptJsonTypeClassifierTest.kt` の実装
- [x] ビルド確認

## Step 2: コード生成エンジン

- [x] `RescriptJsonCodeGenerator.kt` の実装
- [x] `RescriptJsonCodeGeneratorTest.kt` の実装
- [x] ビルド確認

## Step 3: Generate アクション + グループ統合

- [x] `RescriptGenerateJsonCodecAction.kt` の実装
- [x] `RescriptGenerateJsonCodecActionTest.kt` の実装（静的メソッド isValidShape のテストを実装。actionPerformed/update は AnAction の IDE ライフサイクル依存だが、ロジックは RescriptJsonCodeGenerator のテストで網羅済み）
- [x] `RescriptGenerateGroup.kt` の変更（actions 配列に追加）
- [x] `RescriptGenerateGroupTest.kt` の更新
- [x] ビルド確認

## Step 4: ドキュメント更新

- [x] `CLAUDE.md` — レイヤー 3 の Generate エントリ更新
- [x] `README.md` — Features セクション更新
- [x] `sphinx-docs/user/features/code-editing.md` — 機能説明追加
- [x] `docs/product-requirements.md` — #81 を実装済みに移動

## Step 5: コミット前検証

- [x] KDoc コメント — 全 class/object に英語 KDoc が付与されている
- [x] テスト — 全テストクラスが存在する（免除対象は理由明記）
- [x] ドキュメント同期 — 4 つのドキュメントが更新されている
- [x] plugin.xml — 変更不要（Generate グループのハードコード配列で自動検出）
- [x] tasklist.md — 全タスク [x]
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 全テストパス

## Step 6: コミット + マージ

- [x] 機能コミット: `✨ Add JSON encoder/decoder generation (Cmd+N)`
- [x] ドキュメントコミット: `📝 Update docs for JSON codec generation (#81)`
- [x] main にマージ
