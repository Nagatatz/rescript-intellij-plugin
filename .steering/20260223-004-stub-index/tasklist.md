# Tasklist: #51 Stub Index

## Phase A: コア型定義

- [x] `RescriptDeclarationStub.kt` — スタブデータクラス作成
- [x] `RescriptDeclarationElementType.kt` — IStubElementType 実装
- [x] `RescriptDeclarationPsiElement.kt` — StubBasedPsiElementBase 継承 PSI
- [x] `RescriptFileStub.kt` — ファイルスタブ実装

## Phase B: インデックス定義

- [x] `RescriptNameIndex.kt` — 全宣言名インデックス
- [x] `RescriptModuleIndex.kt` — モジュール専用インデックス

## Phase C: PSI 基盤変更

- [x] `RescriptPsi.kt` — 5つの宣言型を `RescriptDeclarationElementType` に置換（`RescriptStubElementTypes` に委譲）
- [x] `RescriptParserDefinition.kt` — `IStubFileElementType` + `createElement` 分岐

## Phase D: 消費者書き換え

- [x] `RescriptSymbolContributor.kt` — StubIndex ベースに書き換え

## Phase E: 登録・テスト

- [x] `plugin.xml` — stubElementTypeHolder（externalIdPrefix なし）+ stubIndex 登録
- [x] `RescriptStubElementTypes.kt` — スタブ型専用ホルダーオブジェクト（stubElementTypeHolder 用）
- [x] `RescriptDeclarationElementTypeTest.kt` — externalId、型チェック、委譲テスト
- [x] `RescriptNameIndexTest.kt` — インデックスキー定数テスト
- [x] `RescriptModuleIndexTest.kt` — モジュールインデックステスト
- [x] テスト免除記録:
  - `RescriptFileStub` — 純粋なデータコンテナ（ロジックなし）
  - `RescriptDeclarationPsiElement` — IDE ライフサイクル依存（`StubBasedPsiElementBase`）
  - `RescriptSymbolContributor` — StubIndex クエリは IDE 結合テスト必要

## Phase F: ドキュメント更新

- [x] CLAUDE.md アーキテクチャ更新（レイヤー 1 に PSI Stub Index 追加）
- [x] README.md Features 更新（Go to Symbol / Search Everywhere に stub-indexed 追記）
- [x] sphinx-docs/user/features/navigation.md 更新（Go to Symbol / Search Everywhere セクション）
- [x] docs/product-requirements.md — #51 実装済みに移動

## Phase G: コミット前検証

- [x] KDoc コメント確認
- [x] テスト確認
- [x] ドキュメント同期確認
- [x] plugin.xml 登録確認
- [x] tasklist.md 進捗確認

## Phase H: ビルド・マージ

- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 全テスト通過
- [x] コミット
- [x] main マージ
