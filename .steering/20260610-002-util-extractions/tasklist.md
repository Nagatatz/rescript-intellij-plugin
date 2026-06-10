# タスクリスト: 低リスク util 抽出 (Phase 1)

セクション間依存: なし (1 と 2 は独立)。3 (docs) は 1・2 の完了後。
各セクション = 1 コミット = 独立にビルド・テスト通過可能な単位。

## セクション 0: セットアップ

- [x] `git fetch origin` + main の ahead/behind 確認
- [x] `EnterWorktree` で worktree 作成、`pwd` / `git rev-parse --show-toplevel` で編集パス確認
- [x] `docs/product-requirements.md` の #126 に 🚧 マーク (最初のコミットに含める)

## セクション 1: EditorTextFieldFactory (#126)

- [x] `util/EditorTextFieldFactory.kt` 新規作成 (KDoc 付き)
- [x] `util/EditorTextFieldFactoryTest.kt` 新規作成
- [x] `repl/RescriptReplPanel.kt` の addSettingsProvider を置換
- [x] `notebook/RescriptNotebookCellPanel.kt` の addSettingsProvider を置換
- [x] `typeinfo/RescriptTypeInfoPanel.kt` の addSettingsProvider を置換
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Extract EditorTextFieldFactory for shared panel editor settings`

## セクション 2: RescriptProjectFileScanner

- [ ] `util/RescriptProjectFileScanner.kt` 新規作成 (KDoc 付き)
- [ ] `util/RescriptProjectFileScannerTest.kt` 新規作成 (全件走査 / truncated / 複数 fileTypes)
- [ ] `coverage/RescriptTypeCoverageScanner.scan` のループを置換 (公開シグネチャ不変)
- [ ] `interop/RescriptInteropScanner.scan` のループを置換 (公開シグネチャ不変)
- [ ] 既存 scanner テスト + interop IntegrationTest が**無変更で** green であることを確認
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Extract RescriptProjectFileScanner for shared file scan loop`

## セクション 3: ドキュメント同期

- [ ] `docs/repository-structure.md` の util/ 行に EditorTextFieldFactory / RescriptProjectFileScanner を追記
- [ ] `docs/product-requirements.md` の #126 を将来機能テーブルから削除 (実装済み扱い)
- [ ] sphinx-docs: 更新なしの確認のみ (機能不変)
- [ ] コミット: `📝 Sync docs for Phase 1 util extractions`

## マージ前検証 (DoD Phase 3〜4)

- [ ] `./gradlew ktlintCheck clean buildPlugin test koverVerify` green
- [ ] `koverHtmlReport` で util/ 新クラスのカバレッジ確認 (minBound 86 維持)
- [ ] tasklist 全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認
- [ ] main へマージ → ブランチ削除 → push

## テスト免除の記載

免除対象なし (新規 2 クラスともテスト必須対象、呼び出し側 panel は既存の免除済みクラスで変更は設定ブロックの置換のみ)。
