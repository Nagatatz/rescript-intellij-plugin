# タスクリスト: S 優先度機能実装

## 事前準備

- [ ] ブランチ作成 (`feature/s-priority-features`)

## #110 GitHub エラーレポート（低難易度）

- [ ] `RescriptErrorReporter` 実装
- [ ] `RescriptErrorReporterTest` テスト作成
- [ ] `plugin.xml` に `errorHandler` 登録
- [ ] コミット: `✨ Add GitHub error report submitter`

## #84 Parameter Info Handler（中難易度）

- [ ] IntelliJ 2025.3+ LSP API が Signature Help を自動サポートするか確認
- [ ] 自動サポートされている場合: 動作確認のみ → スキップ（テスト省略理由: LSP API が自動処理するため追加実装不要）
- [ ] 自動サポートされていない場合: `RescriptParameterInfoHandler` 実装 + テスト + `plugin.xml` 登録
- [ ] コミット（実装が必要な場合）: `✨ Add parameter info handler for labeled arguments`

## #45 Go to Implementation（中難易度）

- [ ] `RescriptGotoSuperHandler` を拡張: `.resi` → `.res` 方向のナビゲーション追加
  - 既にハンドラ自体は双方向対応済み（`"resi" -> "res"` ケースあり）
  - Navigate > Go to Implementation メニュー項目の追加が主な作業
- [ ] `RescriptGotoImplementationAction` 作成（Navigate メニュー登録用アクション）
- [ ] `RescriptGotoImplementationActionTest` テスト作成
- [ ] `plugin.xml` に `<action>` 登録
- [ ] コミット: `✨ Add Go to Implementation action for .resi to .res navigation`

## #70 Pipe ⇔ 関数呼び出し変換（中難易度）

- [ ] `RescriptConvertPipeToFunctionCallIntention` 実装
- [ ] `RescriptConvertFunctionCallToPipeIntention` 実装
- [ ] `RescriptConvertPipeToFunctionCallIntentionTest` テスト作成
- [ ] `RescriptConvertFunctionCallToPipeIntentionTest` テスト作成
- [ ] `plugin.xml` に `intentionAction` 2件登録
- [ ] コミット: `✨ Add pipe to function call conversion intentions`

## #76 インターフェース公開/非公開（中難易度）

- [ ] `RescriptAddToInterfaceIntention` 実装
- [ ] `RescriptRemoveFromInterfaceIntention` 実装
- [ ] `RescriptAddToInterfaceIntentionTest` テスト作成
- [ ] `RescriptRemoveFromInterfaceIntentionTest` テスト作成
- [ ] `plugin.xml` に `intentionAction` 2件登録
- [ ] コミット: `✨ Add interface publish/unpublish intentions`

## #83 型ミスマッチインラインヒント（中難易度）

- [ ] `RescriptTypeMismatchParser` 実装（ReScript コンパイラのエラーメッセージから expected/actual 型を抽出）
- [ ] `RescriptErrorLensRenderer` を拡張（型ミスマッチの場合に構造化表示）
- [ ] `RescriptTypeMismatchParserTest` テスト作成
- [ ] `RescriptErrorLensRendererTest` 既存テストを拡張
- [ ] コミット: `✨ Add type mismatch inline hints to Error Lens`

## ドキュメント更新

- [ ] `docs/product-requirements.md` から実装済み6件を「将来機能」テーブルから削除し「実装済み機能」セクションに移動
- [ ] `CLAUDE.md` のアーキテクチャセクション更新（必要に応じて）
- [ ] コミット: `📝 Update docs for S-priority features`

## 完了

- [ ] `./gradlew buildPlugin` でビルド成功確認
- [ ] `./gradlew test` でテスト全通過確認
- [ ] `main` にマージ
