# B 優先度機能 — タスクリスト

## #93 常時型表示パネル (Type Info ToolWindow)

- [x] `RescriptTypeInfoToolWindowFactory.kt` を作成
- [x] `RescriptTypeInfoPanel.kt` を作成（CaretListener + debounce + LSP hover）
- [x] `plugin.xml` に toolWindow を登録
- [x] テスト: 免除（Swing UI + LSP 結合）— 理由を本タスクリストに記載済み
- [x] コミット: `✨ Add Type Info tool window for ReScript (#93)`

## #96 レコードスタブ生成 (Record Value Generation)

- [x] `RescriptGenerateRecordValueAction.kt` を作成
- [x] `RescriptGenerateGroup` に追加
- [x] `RescriptGenerateRecordValueActionTest.kt` を作成
- [x] コミット: `✨ Add Record Value generation action (#96)`

## #54 IntelliLang 連携強化 (`%re` 正規表現インジェクション)

- [x] `RescriptRawJsInjector.kt` を拡張して `%re` → RegExp 注入を追加
- [x] `RescriptRawJsInjectorTest.kt` を更新（`%re` テスト + `getRegexPatternRange` テスト追加）
- [x] コミット: `✨ Add RegExp injection for %re expressions (#54)`

## #82 分割代入の解除 (Expand Destructuring)

- [x] `RescriptExpandDestructuringIntention.kt` を作成
- [x] `plugin.xml` に intentionAction を登録
- [x] `RescriptExpandDestructuringIntentionTest.kt` を作成
- [x] コミット: `✨ Add expand destructuring intention (#82)`

## ドキュメント更新

- [x] `CLAUDE.md` — アーキテクチャセクション（レイヤー 3）に 4 機能を追加
- [x] `README.md` — Features セクションに 4 機能を追加
- [x] `sphinx-docs/` — 該当ディレクトリが存在しないためスキップ
- [x] `docs/product-requirements.md` — 4 機能を「実装済み」セクションに移動
- [ ] コミット: `📝 Update docs for B-priority features (#93, #96, #54, #82)`

## コミット前検証

- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] 全テストがパスする
- [ ] KDoc が全クラスに付与されている
- [ ] セキュリティ: 外部入力のバリデーション確認

## マージ

- [ ] tasklist.md の全タスクが `[x]` になっている
- [ ] ユーザーにマージ可否を確認
- [ ] main にマージしブランチ削除
