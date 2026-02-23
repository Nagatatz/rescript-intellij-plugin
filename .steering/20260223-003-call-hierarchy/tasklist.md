# Tasklist: Call Hierarchy (#44)

## 実装

- [x] `RescriptCallAnalyzer.kt` — 呼び出し関係分析ユーティリティ
- [x] `RescriptCallHierarchyProvider.kt` — HierarchyProvider EP 実装
- [x] `RescriptCallHierarchyBrowser.kt` — HierarchyBrowserBaseEx UI
- [x] `RescriptCallHierarchyNodeDescriptor.kt` — ノード描画
- [x] `RescriptCallerTreeStructure.kt` — 呼び出し元ツリー構造
- [x] `RescriptCalleeTreeStructure.kt` — 呼び出し先ツリー構造
- [x] `plugin.xml` に `callHierarchyProvider` 登録

## テスト

- [x] `RescriptCallAnalyzerTest.kt` — CallReference, トークン抽出ロジック
- [x] `RescriptCallHierarchyProviderTest.kt` — Provider 基本動作
  - 免除: PsiSearchHelper を用いるプロジェクト横断検索は IDE 結合テストのため免除
  - 免除: Browser/NodeDescriptor/TreeStructure は HierarchyBrowserBaseEx に依存する UI クラスのため免除

## ドキュメント更新

- [x] `CLAUDE.md` — レイヤー 3 に Call Hierarchy 追加
- [x] `README.md` — Navigation セクションに追加
- [x] `sphinx-docs/user/features/navigation.md` — Call Hierarchy セクション追加
- [x] `docs/product-requirements.md` — #44 を実装済みに移動、🚧 マーク除去

## コミット前検証

- [x] すべてのクラスに KDoc コメントがあること
- [x] テストが存在すること（免除対象は理由明記済み）
- [x] ドキュメントが同期されていること
- [x] plugin.xml に登録されていること
- [x] tasklist.md が最新であること

## ビルド・マージ

- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功
- [x] tasklist.md 全タスク完了
- [ ] main にマージ
