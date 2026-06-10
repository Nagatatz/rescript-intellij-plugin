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
- [x] `./gradlew ktlintCheck test` green
- [x] コミット: `♻️ Extract EditorTextFieldFactory for shared panel editor settings` (d2f6be4)

## セクション 2: RescriptProjectFileScanner

- [x] `util/RescriptProjectFileScanner.kt` 新規作成 (KDoc 付き)
- [x] `util/RescriptProjectFileScannerTest.kt` 新規作成 (全件走査 / truncated / 読取不能 skip / 空プロジェクト smoke)
  - 設計変更: light fixture は content root を持たず FileTypeIndex にファイルが載らないため、走査ループを `visitFiles(files, ...)` internal ヘルパに分離して LightVirtualFile でテストする方式に変更 (既存 scanner の「pure helper を internal 公開」パターンに準拠)。「複数 fileTypes」のインデックス結合は flatMap 1 行のため空プロジェクト smoke + 既存 interop テストで担保
- [x] `coverage/RescriptTypeCoverageScanner.scan` のループを置換 (公開シグネチャ不変)
- [x] `interop/RescriptInteropScanner.scan` のループを置換 (公開シグネチャ不変)
- [x] 既存 scanner テスト + interop IntegrationTest が**無変更で** green であることを確認
- [x] `./gradlew ktlintCheck test` green
- [x] コミット: `♻️ Extract RescriptProjectFileScanner for shared file scan loop` (588daf1)

## セクション 3: ドキュメント同期

- [x] `docs/repository-structure.md` の util/ 行に EditorTextFieldFactory / RescriptProjectFileScanner を追記
- [x] `docs/product-requirements.md` の #126 を将来機能テーブルから削除 (実装済み扱い)
- [x] sphinx-docs: 更新なしの確認のみ (機能不変・ユーザー向け挙動変更なし)
- [x] コミット: `📝 Sync docs for Phase 1 util extractions` (cfd68e8)

## 追加セクション (スコープ外バグ修正): sandbox jar purge の範囲限定

DoD フルチェーン検証中に main 既存のバグを発見したため修正 (リファクタ起因ではない):

- [x] `build.gradle.kts` の stale-jar 除去フックが sandbox root 全体を walk しており、`buildPlugin test` の同一呼び出しで `prepareSandbox` が `plugins-test/` の現行 jar を削除 → `:test` が NoClassDefFoundError (junit-vintage discovery 失敗) になるタスク順序依存の flake を確認
- [x] 各 `prepareSandbox*` タスクの purge を自身の `Sync.destinationDir` 配下に限定する修正
- [x] 修正後フルチェーン 2 回連続 green を確認
- [x] コミット: `🐛 Scope sandbox jar purge to each prepareSandbox task's own destination` (bc89bcd)
- テスト省略の理由: Gradle ビルドスクリプトの変更でありユニットテスト対象外。検証はフルチェーンの再現実行で実施

## マージ前検証 (DoD Phase 3〜4)

- [x] `./gradlew ktlintCheck clean buildPlugin test koverVerify verifyPluginStructure` green
- [x] `koverHtmlReport` で util/ 新クラスのカバレッジ確認 (EditorTextFieldFactory / RescriptProjectFileScanner とも 100%、minBound 86 維持)
- [x] tasklist 全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認
- [ ] main へマージ → ブランチ削除 → push

## テスト免除の記載

免除対象なし (新規 2 クラスともテスト必須対象、呼び出し側 panel は既存の免除済みクラスで変更は設定ブロックの置換のみ)。
