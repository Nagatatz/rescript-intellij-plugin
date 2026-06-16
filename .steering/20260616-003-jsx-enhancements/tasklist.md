# tasklist — JSX 改良プログラム (C→E→B→A→D)

各セクション = 1 機能 + テスト + plugin.xml 登録 + EN/JA docs = 1 コミット（独立にマージ可能）。
**依存**: E が共有ヘルパ `RescriptJsxTagPairUtil` を新設するため、B・A は E の後に着手する。C は独立（ヘルパ非依存）。

## セクション 0: worktree 準備

- [x] `git fetch origin` で worktree 作成前の同期確認（0/0 を確認）
- [x] `EnterWorktree` で `jsx-enhancements` worktree に入り `pwd` / `git rev-parse --show-toplevel` を確認

## セクション C: Surround with JSX

- [x] `surround/RescriptSurroundDescriptor.kt` に `RescriptJsxElementSurrounder`（`<div>$SEL</div>`、caret を `div` に）と `RescriptJsxFragmentSurrounder`（`<>$SEL</>`）を追加し `surrounders` 配列へ登録
- [x] 両クラスに英語 KDoc を付与
- [x] `surround/RescriptSurroundDescriptorTest.kt` に `generateTemplate` / `getCursorRange` の純ロジックテストを追加
- [x] plugin.xml は既存 `surroundDescriptor` 登録で足りる（新規 EP 登録不要）ことを確認
- [x] docs: CLAUDE.md(layer3 編集系は Surround を列挙せず docs/functional-design.md に委譲のため変更不要) / README(Code Editing) / sphinx code-editing.md(EN) + JA .po
- [x] `./gradlew ktlintCheck test` 緑を確認し `✨ Add Surround with JSX element/fragment` でコミット

## セクション E: 閉じタグ不一致 Inspection（共有ヘルパ新設）

- [x] `lang/psi/RescriptJsxTagPairUtil.kt` を新設: `JsxTagNames` data class / `TagSide` enum / `findEnclosingJsxElement` / `extractTagNames` / `sideAt`（英語 KDoc 全付与）
- [x] `inspection/RescriptMismatchedJsxTagInspection.kt`（`LocalInspectionTool`）を新設、`openName != closeName` の閉じタグ範囲に WARNING 登録
- [x] plugin.xml に `localInspection` を既存 inspection 群の並びに従って登録
- [x] `lang/psi/RescriptJsxTagPairUtilTest.kt`: 名前抽出 / dotted path / fragment / self-closing / 閉じ欠落 / sideAt を網羅
- [x] `inspection/RescriptMismatchedJsxTagInspectionTest.kt`: `<div></span>` 検出 / `<Foo></Bar>` 検出 / `<div></div>` 非検出 / ネスト同名 非検出 / fragment 非検出 / self-closing 非検出
- [x] docs: README(Code Analysis) / sphinx code-analysis.md(EN) + JA .po / repository-structure.md / functional-design.md(localInspection EP マップ) にヘルパ・Inspection クラス追記（CLAUDE.md は C と同様 layer3 分析系に個別 inspection を列挙せず functional-design.md へ委譲のため変更不要）
- [x] `./gradlew ktlintCheck test` 緑を確認し `✨ Add mismatched JSX close-tag inspection` でコミット

## セクション B: タグペアハイライト（E のヘルパ依存）

- [x] `highlight/RescriptHighlightUsagesHandlerFactory.kt` の `createHighlightUsagesHandler` 冒頭に「caret token が JSX_TAG_NAME / JSX_COMPONENT_NAME なら JSX ハンドラを返す」分岐を追加
- [x] `highlight/RescriptJsxTagHighlightHandler.kt`（`HighlightUsagesHandlerBase`）を新設、`extractTagNames` で開き/閉じ両範囲をハイライト（英語 KDoc）
- [x] `highlight/RescriptJsxTagHighlightHandlerTest.kt`: 開きタグ名 caret → 2 範囲 / 閉じタグ名 caret → 2 範囲 / 閉じ欠落 → 1 範囲 / ネスト同名で正しい対応
- [x] plugin.xml は既存 `highlightUsagesHandlerFactory` 登録で足りることを確認
- [x] docs: README(Code Editing) / sphinx code-editing.md(EN) + JA .po / functional-design.md(EP マップ) / repository-structure.md にハンドラ追記（CLAUDE.md layer3 は C/E と同じく functional-design.md へ委譲）
- [x] `./gradlew ktlintCheck test` 緑を確認し `✨ Add JSX tag-pair highlighting` でコミット

## セクション A: ペアタグ同期リネーム（E のヘルパ依存）

- [x] `RescriptJsxTagPairUtil` に `computeSyncEdit(names, editedSide, preEditEditedName): SyncEdit?` と `SyncEdit` data class を追加（編集前同期時のみミラー、英語 KDoc）
- [x] `RescriptTypedHandler` / `RescriptBackspaceHandler` に JSX タグ名同期 wiring を追加（同一 WriteCommandAction で Undo 1 ステップ）
- [x] `RescriptJsxTagPairUtilTest.kt` に `computeSyncEdit` 網羅テスト（open→close / close→open / 編集前不一致 null / self-closing null / fragment null）
- [x] heavy fixture でタイピング同期スモーク 1 本（`<div>` の `div` 編集 → `</div>` 追従 / Undo 1 ステップ）。**wiring 自体は editor 結合のためスモーク 1 本に限定（理由: TypedHandlerDelegate の charTyped は light fixture で安定駆動できないため）**
- [x] docs: README(Code Editing) / functional-design.md(typedHandler + backspaceHandler 行) / repository-structure.md(lang/psi + editor/ 行) / sphinx code-editing.md(EN) + JA .po（CLAUDE.md は C/E/B 同様、機能列挙を functional-design.md に委譲するため per-feature 追記なし）
- [x] `./gradlew ktlintCheck test` 緑を確認し `✨ Add paired JSX tag synchronized rename` でコミット

## セクション D: 構造ビュー JSX ノード

- [x] **設計変更（承認済み tasklist からの逸脱）**: `NAVIGABLE_TYPES` には JSX を追加せず、構造ビュー専用のスーパーセット `STRUCTURE_VIEW_TYPES = NAVIGABLE_TYPES + {JSX_ELEMENT, JSX_FRAGMENT}` を新設。理由: `NAVIGABLE_TYPES` は Find Usages / Safe Delete / Search Everywhere / qualified-name copy / breadcrumb / navbar 等 ~14 の宣言指向機能を駆動しており、`<div>` を宣言扱いにすると誤って Find Usages / Safe Delete の対象になる。JSX を構造ビューに限定することで下流 3 テスト（QualifiedNameProvider / Breadcrumbs / StructureAwareNavbar）も無改変で緑のまま
- [x] `RescriptStructureViewElement.getChildren` のフィルタを `STRUCTURE_VIEW_TYPES` に切替（self-closing は葉ノードのため除外）
- [x] `extractName`（開きタグ名、fragment は `<>`、`RescriptJsxTagPairUtil` 再利用）/ `getIcon`（`AllIcons.Nodes.Tag`）/ `getElementDescription` に JSX 分岐を追加
- [x] `lang/psi/RescriptPsiUtilsTest.kt` に JSX ノードの `extractName` / `getElementDescription` / `STRUCTURE_VIEW_TYPES` 包含テストと `NAVIGABLE_TYPES` に JSX が漏れない退行テストを追加
- [x] breadcrumb / navbar への波及を設計判断で評価: `STRUCTURE_VIEW_TYPES` 分離により JSX は宣言指向機能へ波及しない。self-closing は構造ビューでも除外しノイズ抑制（runIde GUI 検証はヘッドレス環境のため未実施）
- [x] docs: README(Navigation) / sphinx navigation.md(EN) + JA .po / functional-design.md + repository-structure.md。CLAUDE.md layer3 は C/E/B/A の前例に倣い機能列挙を docs/functional-design.md に委譲（layer3 行追加なし）
- [x] `./gradlew ktlintCheck test` 緑を確認し `✨ Show JSX elements in structure view` でコミット

## セクション Z: マージ

- [x] 全セクション緑、`./gradlew clean buildPlugin test` 成功を確認
- [x] requirements.md の受け入れ条件をすべて満たしていることを確認
- [x] このファイルの全タスクを `[x]` 更新（マージ前最終コミットに同梱）
- [x] `AskUserQuestion` でマージ可否を確認
- [x] 承認後 worktree 内で `main` にマージ → 作業ブランチ削除 → セッション終了（worktree 自動クリーンアップ）

## テスト免除メモ

- `RescriptJsxElementSurrounder` / `RescriptJsxFragmentSurrounder`: `generateTemplate`/`getCursorRange` の純ロジックはテスト対象。UI 表示部（`getSurrounders` の Swing 連携）は既存 surrounder と同様に免除。
- A の TypedHandler/BackspaceHandler wiring: editor 結合のため heavy fixture スモーク 1 本に限定。ミラー計算 `computeSyncEdit` は純関数として網羅テスト必須。
