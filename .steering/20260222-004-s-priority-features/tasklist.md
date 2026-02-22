# タスクリスト: S 優先度機能実装

## 事前準備

- [x] ブランチ作成 (`feature/s-priority-features`)

## #110 GitHub エラーレポート（低難易度）

- [x] `RescriptErrorReporter` 実装
- [x] `RescriptErrorReporterTest` テスト作成
- [x] `plugin.xml` に `errorHandler` 登録
- [x] コミット: `✨ Add GitHub error report submitter`

## #84 Parameter Info Handler（中難易度）

- [x] IntelliJ 2025.3+ LSP API が Signature Help を自動サポートするか確認 → **自動サポート確認済み**
- [x] 自動サポートされている場合: 動作確認のみ → スキップ（テスト省略理由: LSP API が自動処理するため追加実装不要）
- ~~自動サポートされていない場合: `RescriptParameterInfoHandler` 実装 + テスト + `plugin.xml` 登録~~ → N/A
- ~~コミット（実装が必要な場合）: `✨ Add parameter info handler for labeled arguments`~~ → N/A

## #45 Go to Implementation（中難易度）

- [x] `RescriptGotoSuperHandler` を拡張: `.resi` → `.res` 方向のナビゲーション追加
  - 既にハンドラ自体は双方向対応済み（`"resi" -> "res"` ケースあり）
  - Navigate > Go to Implementation メニュー項目の追加が主な作業
- [x] `RescriptGotoImplementationAction` 作成（Navigate メニュー登録用アクション）
- [x] `RescriptGotoImplementationActionTest` テスト作成
- [x] `plugin.xml` に `<action>` 登録
- [x] コミット: `✨ Add Go to Implementation action for .resi to .res navigation`

## #70 Pipe ⇔ 関数呼び出し変換（中難易度）

- [x] `RescriptConvertPipeToFunctionCallIntention` 実装
- [x] `RescriptConvertFunctionCallToPipeIntention` 実装
- [x] `RescriptConvertPipeToFunctionCallIntentionTest` テスト作成
- [x] `RescriptConvertFunctionCallToPipeIntentionTest` テスト作成
- [x] `plugin.xml` に `intentionAction` 2件登録
- [x] コミット: `✨ Add pipe to function call conversion intentions`

## #76 インターフェース公開/非公開（中難易度）

- [x] `RescriptAddToInterfaceIntention` 実装
- [x] `RescriptRemoveFromInterfaceIntention` 実装
- [x] `RescriptAddToInterfaceIntentionTest` テスト作成
- [x] `RescriptRemoveFromInterfaceIntentionTest` テスト作成
- [x] `plugin.xml` に `intentionAction` 2件登録
- [x] コミット: `✨ Add interface publish/unpublish intentions`

## #83 型ミスマッチインラインヒント（中難易度）

- [x] `RescriptTypeMismatchParser` 実装（ReScript コンパイラのエラーメッセージから expected/actual 型を抽出）
- [x] `RescriptErrorLensRenderer` を拡張（型ミスマッチの場合に構造化表示）
- [x] `RescriptTypeMismatchParserTest` テスト作成
- [x] `RescriptErrorLensRendererTest` 既存テストを拡張
- [x] コミット: `✨ Add type mismatch inline hints to Error Lens`

## ドキュメント更新

- [x] `docs/product-requirements.md` から実装済み6件を「将来機能」テーブルから削除し「実装済み機能」セクションに移動
- [x] `CLAUDE.md` のアーキテクチャセクション更新（必要に応じて）
- [x] コミット: `📝 Update docs for S-priority features`

## 完了

- [x] `./gradlew buildPlugin` でビルド成功確認
- [x] `./gradlew test` でテスト全通過確認
- [x] `main` にマージ
