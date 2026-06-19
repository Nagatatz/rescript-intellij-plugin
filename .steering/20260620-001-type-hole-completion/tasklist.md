# タスクリスト: Type Hole Completion (#115)

各セクションは「マージ可能な単位」= 1 コミット。純ロジック 3 クラスは独立にビルド・テスト可能なので
先にコミットを刻み、最後に Contributor + plugin.xml + ドキュメントをまとめる。

依存関係: セクション 4 (Contributor) はセクション 1〜3 の純ロジックに依存する。
セクション 5 (ドキュメント) は全機能完成後。

## セクション 0: 準備

- [ ] worktree 作成前に MAIN repo で `git fetch origin` / ローカル main の ahead-behind 確認
- [ ] ローカル main が origin より遅れていれば `git pull --ff-only origin main`
- [ ] `.claude/worktrees/type-hole-completion/` を最新 main から作成し EnterWorktree
- [ ] worktree 内で `pwd` / `git rev-parse --show-toplevel` で編集パスを確認

## セクション 1: RescriptTypeHoleContext（型穴検出）

- [ ] `completion/RescriptTypeHoleContext.kt` 作成
  - [ ] `data class TypeHoleDetection(expectedTypeHead, bindingName)`
  - [ ] `object RescriptTypeHoleContext.detect(textBeforeCaret): TypeHoleDetection?`
  - [ ] 末尾 `_` / 直前 `=` / `let [rec] <name>: <T>` 検証 / depth-0 head 抽出
  - [ ] KDoc（クラス + detect）
- [ ] `test/completion/RescriptTypeHoleContextTest.kt` 作成（fixture 不要）
  - [ ] `let x: color = _` → `(color, x)`
  - [ ] `let x: color = ` / `let x: color = R` / `let x = _`（注釈なし）→ null
  - [ ] `rec` / `option<color>` / `Belt.Map.t` の head 抽出
- [ ] `./gradlew test` グリーン確認
- [ ] コミット `✨ Add type hole detector for completion`（tasklist 更新含む）

## セクション 2: RescriptLocalBindingScanner（ローカル束縛走査）

- [ ] `completion/RescriptLocalBindingScanner.kt` 作成
  - [ ] `data class LocalBinding(name, typeHead)`
  - [ ] `object RescriptLocalBindingScanner.scan(source): List<LocalBinding>`
  - [ ] brace-depth 0 の `let [rec] IDENT : <T> =` 収集 / 後勝ちシャドウ / depth-0 head
  - [ ] KDoc
- [ ] `test/completion/RescriptLocalBindingScannerTest.kt` 作成（fixture 不要）
  - [ ] depth-0 列挙 / depth>0 除外 / 同名後勝ち / 注釈なし無視 / `colors` vs `color` 非一致
- [ ] `./gradlew test` グリーン確認
- [ ] コミット `✨ Add local binding scanner for type hole completion`（tasklist 更新含む）

## セクション 3: RescriptCaseSplitBuilder（case split 雛形生成）

- [ ] `completion/RescriptCaseSplitBuilder.kt` 作成
  - [ ] `object RescriptCaseSplitBuilder.build(constructors, lineIndent): String?`
  - [ ] 空 → null / arm = `Ctor(_) => _` or `Ctor => _` / インデント反映 / 先頭 `_` がスクラティニー
  - [ ] KDoc
- [ ] `test/completion/RescriptCaseSplitBuilderTest.kt` 作成（fixture 不要）
  - [ ] payload あり/なし混在 / 空 → null / インデント / 先頭 `_` 位置
- [ ] `./gradlew test` グリーン確認
- [ ] コミット `✨ Add case split builder for type hole completion`（tasklist 更新含む）

## セクション 4: RescriptTypeHoleCompletionContributor（EP 統合）

- [ ] `completion/RescriptTypeHoleCompletionContributor.kt` 作成
  - [ ] `fillCompletionVariants`: BASIC / language guard / detect / `withPrefixMatcher("")`
  - [ ] (A) local binding fill（head 一致 & self 除外）
  - [ ] (B) case split（`RescriptPlaceholderTypeResolver.resolve` → Variant のとき）
  - [ ] `applyInsertion`（#117 と同一 / 最初の `_` へ caret park）
  - [ ] KDoc（EP: `completion.contributor` 言及）
- [ ] `plugin.xml` に `<completion.contributor language="ReScript" .../>` 登録（#117 の隣）
- [ ] `test/completion/RescriptTypeHoleCompletionContributorTest.kt` 作成（light fixture）
  - [ ] `type color = Red | Green` 配置 / `let x: color = _<caret>` で case split & local binding 候補
  - [ ] 選択後テキスト・caret 位置 / #117 と共存
- [ ] `./gradlew test` グリーン確認
- [ ] コミット `✨ Add Wingman-style type hole completion`（tasklist 更新含む）

## セクション 5: ドキュメント更新

- [ ] `docs/repository-structure.md` — `completion/` 行に 4 クラス追記
- [ ] `docs/functional-design.md` — 補完カテゴリに Type Hole Completion 解説 + EP マップ
- [ ] `README.md` — Features 補完カテゴリに 1 項目追加
- [ ] `sphinx-docs/user/features/code-completion.md` — 機能説明 + 変換例（EN）
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` — `make gettext` / `make update-po` / msgstr 日本語 / `make build-ja`
- [ ] `docs/product-requirements.md` — ロードマップ #115 行を削除
- [ ] CLAUDE.md は更新不要（新カテゴリ無し）を確認
- [ ] コミット `📝 Document Wingman-style type hole completion`（tasklist 更新含む）

## セクション 6: 最終検証・マージ

- [ ] `./gradlew ktlintCheck` グリーン
- [ ] `./gradlew clean buildPlugin` グリーン
- [ ] `./gradlew test` グリーン
- [ ] 新規 `.kt` すべてに対応テスト存在を確認
- [ ] requirements.md の受け入れ条件 8 項目をすべて確認
- [ ] tasklist 全項目 `[x]` 化（このセクション含む）をマージ前最終コミットに含める
- [ ] AskUserQuestion でマージ可否確認
- [ ] 承認後 main にマージ → 作業ブランチ削除 → セッション終了（worktree 自動クリーンアップ）
