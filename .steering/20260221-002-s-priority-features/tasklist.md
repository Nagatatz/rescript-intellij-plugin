# タスクリスト: S 優先度機能一括実装

## 実装タスク

- [x] 1. Bundled Dictionary（スペルチェック辞書）
  - [x] 1-1. `src/main/resources/dictionaries/rescript.dic` を作成
  - [x] 1-2. `RescriptBundledDictionaryProvider.kt` を実装
  - [x] 1-3. `plugin.xml` に登録
  - [x] 1-4. `RescriptBundledDictionaryProviderTest.kt` を作成

- [x] 2. Test Source Filter（テストファイル認識）
  - [x] 2-1. `RescriptTestSourcesFilter.kt` を実装
  - [x] 2-2. `plugin.xml` に登録
  - [x] 2-3. `RescriptTestSourcesFilterTest.kt` を作成

- [x] 3. Context Info（Declaration Range Handler）
  - [x] 3-1. `RescriptDeclarationRangeHandler.kt` を実装
  - [x] 3-2. `plugin.xml` に登録
  - [x] 3-3. `RescriptDeclarationRangeHandlerTest.kt` を作成

- [x] 4. FindUsagesProvider + WordsScanner
  - [x] 4-1. `RescriptFindUsagesProvider.kt` を実装
  - [x] 4-2. `plugin.xml` に登録
  - [x] 4-3. `RescriptFindUsagesProviderTest.kt` を作成

- [x] 5. Unwrap/Remove
  - [x] 5-1. `RescriptUnwrapDescriptor.kt` を実装（全7種の unwrapper）
  - [x] 5-2. `plugin.xml` に登録
  - [x] 5-3. `RescriptUnwrapDescriptorTest.kt` を作成

- [x] 6. Typed Handler（JSX 閉じタグ自動挿入）
  - [x] 6-1. `RescriptTypedHandler.kt` を実装
  - [x] 6-2. `plugin.xml` に登録
  - [x] 6-3. `RescriptTypedHandlerTest.kt` を作成

- [x] 7. Go to Test / Create Test
  - [x] 7-1. `RescriptTestCreator.kt` を実装
  - [x] 7-2. `plugin.xml` に登録
  - [x] 7-3. `RescriptTestCreatorTest.kt` を作成

- [x] 8. Tree Structure Provider（.resi ネスト）
  - [x] 8-1. `RescriptTreeStructureProvider.kt` を実装（設定フラグは省略、常時有効）
  - [x] 8-2. `plugin.xml` に登録
  - [x] 8-3. `RescriptTreeStructureProviderTest.kt` を作成

## ビルド・検証

- [x] 9. `./gradlew buildPlugin` でビルド成功を確認
- [x] 10. ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md, README.md）

## Git

- [x] 11. tasklist 更新 + コミット（`✨ Add S-priority features: unwrap, go-to-test, resi-nesting, typed-handler, dictionary, context-info, test-filter, find-usages`）
- [x] 12. main にマージして worktree を削除
