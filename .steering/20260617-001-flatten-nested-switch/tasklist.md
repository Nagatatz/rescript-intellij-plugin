# タスクリスト: ネスト switch 平坦化 Intention (#114)

各セクション = マージ可能な単位。セクション 1 が緑になってからセクション 2 に進む
（intention は flattener に依存するため）。

## セクション 1: 純ロジック (RescriptNestedSwitchFlattener) + テスト

- [ ] `intention/RescriptNestedSwitchFlattener.kt` を作成
  - [ ] lexer 走査ユーティリティ（空白/改行/コメント除去のトークン列）
  - [ ] キャレットを含む switch・アームの特定
  - [ ] 適用可能性判定 5 条件（when なし / 束縛 1 個 / 本体が内側 switch のみ / scrutinee 一致 / 内側 or-pattern・when なし）
  - [ ] 束縛位置でのパターン置換 + 置換テキスト生成（インデント踏襲・body verbatim）
  - [ ] `FlattenPlan` データクラス + KDoc（英語）
- [ ] `test/.../intention/RescriptNestedSwitchFlattenerTest.kt` を作成
  - [ ] option ネスト → replacementText 文字列一致
  - [ ] result ネスト
  - [ ] bare 束縛
  - [ ] 束縛 0 / 2 個 → null
  - [ ] scrutinee 不一致 → null
  - [ ] 内側 or-pattern → null
  - [ ] 内側 when / 外側 when → null
  - [ ] 本体に他式混在 → null
  - [ ] インデント踏襲
  - [ ] replaceStart/replaceEnd が外側アームの範囲を指す
- [ ] `./gradlew ktlintCheck test` 緑を確認
- [ ] コミット: `✨ Add nested switch flattener logic`

## セクション 2: Intention ラッパー + plugin.xml 登録 + 結線テスト

- [ ] `intention/RescriptFlattenNestedSwitchIntention.kt` を作成（`RescriptBaseIntention` 派生、KDoc 英語）
- [ ] `plugin.xml` に `<intentionAction>` を既存 Intention の並びに従い登録
- [ ] `test/.../intention/RescriptFlattenNestedSwitchIntentionTest.kt`（light fixture で isAvailable true/false + invoke 後ドキュメント 1〜2 ケース）
- [ ] `./gradlew ktlintCheck clean buildPlugin test` 緑を確認
- [ ] コミット: `✨ Add flatten nested switch intention`

## セクション 3: ドキュメント更新

- [ ] `CLAUDE.md` レイヤー 3 Intention 一覧に追記
- [ ] `docs/repository-structure.md` の `intention/` 行に代表クラス追記
- [ ] `README.md` Features（Intention カテゴリ）に追記
- [ ] `sphinx-docs/user/features/code-editing.md` に変換例つきで追記
- [ ] `sphinx-docs` の JA `.po` 同期（make gettext / update-po / 翻訳 / build-ja）
- [ ] `docs/product-requirements.md` 将来機能テーブルから #114 行削除
- [ ] コミット: `📝 Document flatten nested switch intention`

## セクション 4: マージ

- [ ] 全タスク `[x]` 確認、`./gradlew clean buildPlugin test` 緑
- [ ] tasklist の全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] AskUserQuestion でマージ可否確認
- [ ] worktree 内で main にマージ → 作業ブランチ削除 → セッション終了
