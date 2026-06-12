# 要求: ツールウィンドウの EDT スレッディング修正

## 背景

v0.1.14 との差分に対するスモークテストで、2 つのツールウィンドウが
現行プラットフォーム (2026.1.2) で SEVERE ログを出して機能不全になる
ことを確認した。いずれも本セッションのリファクタリングによる退行では
なく、**v0.1.14 時点から存在する潜在バグ**（`git show v0.1.14` で同一
スレッディングを確認済み）。プラットフォームのスレッディング表明が
SEVERE 化されたことで顕在化した。

## 対象 defect

### DEFECT 1: Module Dependency Diagram が壊れる (ThreadingAssertions SEVERE)

- `RescriptDependencyDiagramPanel.doRefresh()` が
  `RescriptDependencyDiagramProvider.buildDiagram(project)` を **EDT 上で
  read-action なしに** 呼ぶ。
- `buildDiagram` は `FileTypeIndex.getFiles` / `PsiManager.findFile` /
  `psiFile.text` を read-action 外で触るため `ThreadingAssertions` が
  SEVERE を投げ、グラフが構築されない。
- 安全な既存パターン (`coverage/` `interop/`) は共有ユーティリティ
  `RescriptProjectFileScanner.scanFiles`（内部で `runReadAction`）経由で
  アクセスしており、本 provider だけがこのガードを欠いている。

### DEFECT 2: Type Impact が遅延操作で落ちる (SlowOperations SEVERE)

- `RescriptTypeImpactPanel.doRefresh()` が
  `RescriptTypeReferenceFinder.findReferences(project, target)` を **EDT 上で**
  呼ぶ。
- `findReferences` は内部で `runReadAction` を取得済みだが、word-index 走査
  (`PsiSearchHelper.processElementsWithWord`) という遅延操作を EDT 上で
  実行するため `SlowOperations` が SEVERE を投げる。
- finder の KDoc は既に「Must be invoked off the EDT」と明記しており、
  呼び出し側がこの契約を破っている。

## 受け入れ条件

- [ ] Module Dependency Diagram ツールウィンドウを開いてもログに
      `ThreadingAssertions` SEVERE が出ず、グラフが描画される
- [ ] Type Impact ツールウィンドウで caret を動かしてもログに
      `SlowOperations` SEVERE が出ず、参照一覧が更新される
- [ ] 両パネルとも重い処理を pooled thread に逃がし、UI 更新は
      `invokeLater` で EDT に戻す（`coverage/` `interop/` と同じ形）
- [ ] Type Impact は caret 連打時に古い結果で UI を上書きしない
      （staleness ガード）
- [ ] `./gradlew ktlintCheck clean buildPlugin test` がすべて緑
- [ ] 既存の provider / finder のユニットテストが引き続き緑

## スコープ外

- 他のツールウィンドウ（Variant Flow, Type Coverage, Interop Risk は
  既に安全パターン）への変更
- 機能仕様・UI レイアウトの変更（純粋なスレッディング修正のみ）
