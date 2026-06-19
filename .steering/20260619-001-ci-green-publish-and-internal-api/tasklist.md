# タスクリスト

## セクション 1: 内部 API 依存の解消(1 コミット)

- [x] `RescriptModuleHierarchyNodeDescriptor` に `SmartPsiElementPointer` + `rescriptElement` を追加し `update()` を更新
- [x] `RescriptCallHierarchyNodeDescriptor` に同様の変更
- [x] `RescriptModuleHierarchyTreeStructure` / `RescriptModuleDependencyTreeStructure` の `buildChildren` を `rescriptElement` キャストに更新
- [x] `RescriptCalleeTreeStructure` / `RescriptCallerTreeStructure` の `buildChildren` を更新
- [x] `RescriptModuleHierarchyBrowser` / `RescriptCallHierarchyBrowser` の `getElementFromDescriptor` を更新
- [x] `plugin-verifier-ignored-problems.txt` の `SmartElementDescriptor.getPsiElement` エントリを削除
- [x] `RescriptModuleHierarchyNodeDescriptorTest` の `.psiElement` 参照を `rescriptElement` に更新
- [x] `RescriptCallHierarchyNodeDescriptorTest` の `.psiElement` 参照を `rescriptElement` に更新

## セクション 2: ドキュメント乖離の修正(1 コミット)

- [x] `CLAUDE.md` CI/CD 表に CodeQL / Integration Tests / OS Matrix を追加
- [x] `docs/repository-structure.md` の `.github/workflows/` 一覧を 7 ファイルに更新

## セクション 3: publish に CI グリーン必須化(1 コミット)

- [x] `release.yml` に `require-ci-green` ジョブを追加
- [x] `publish.needs` に `require-ci-green` を追加

## セクション 4: 検証・マージ

- [x] feature ブランチを push し PR #42 で CI を起動
- [x] CI の `verifyPlugin`(Verify plugin (binary compatibility))が green になることを確認(EAP 含む 3 IDE で INTERNAL_API_USAGES ゼロ)
- [x] CI 全ジョブ green を確認(build / mutation-test / security / actionlint / template-integration、Docs / CodeQL も success)
- [ ] ユーザーにマージ可否を確認
- [ ] `main` にマージしブランチ削除
