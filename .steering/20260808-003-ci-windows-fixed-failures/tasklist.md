# タスクリスト: CI Windows で固定的に失敗する 5 件の解消

## セクション間の依存

セクション 1（A: ProcessUtilsTest）と セクション 2（B: JsonSchemaProviderFactoryTest）は**互いに独立**で、任意の順に着手・コミットできる。セクション 0 が全体の前提。

セクション 3 の CI 検証は 1・2 の push 後にまとめて実施する。**B はローカルで再現しないため、セクション 3 を通過するまで完了とみなさない。**

---

## セクション 0: 準備

- [x] `git fetch origin` を実行し、main が origin と同期していることを確認する
- [x] steering ドキュメント 3 点を main に直接コミットする（`📝 Add steering docs for CI Windows fixed failures`）
- [x] `EnterWorktree` で worktree を作成する
- [x] `pwd` と `git rev-parse --show-toplevel` で編集対象が worktree 内であることを確認する
- [x] worktree は `origin/main` から切られるため、未 push コミットがある場合は `git merge main` で取り込む

---

## セクション 1: ProcessUtilsTest の bash 依存を解消（手法 A）

- [x] `src/test/kotlin/com/rescript/plugin/util/RescriptProcessUtilsTest.kt` に private helper `script(dir, name, win, posix): Path` を追加する
  - Windows: `<name>.bat` に `@echo off` + win 本体
  - POSIX: `<name>.sh` に `#!/bin/sh` + posix 本体 + `setExecutable(true)`
  - プラットフォーム判定は `com.intellij.openapi.util.SystemInfo.isWindows`
  - KDoc に「bash が CI Windows では WSL ランチャーに解決される」ことを英語で明記する
- [x] `executeWithStdin captures stderr` を書き換える（`echo err 1>&2` / `echo err >&2`）
- [x] `executeWithStdin reports non-zero exit code` を書き換える（`exit /b 42` / `exit 42`）
- [x] `testRunSimpleCommandTimesOut` を書き換える（`echo started` + `ping -n 61 127.0.0.1 >nul` / `echo started; sleep 60`）
- [x] `executeWithStdin handles timeout` に `@DisabledOnOs(WINDOWS)` を付与し、`disabledReason` に cmd.exe が stdout を閉じられない旨を明記する
- [x] 対象テストに `@TempDir tempDir: Path` を追加する
- [x] `grep -rn '"bash"' src/test/` で bash 直呼びが残っていないことを確認する
  - 残存 1 件は `handles timeout` のみ。Windows ではスキップされ POSIX では正常動作するため設計どおり
- [x] `./gradlew test --tests 'com.rescript.plugin.util.RescriptProcessUtilsTest'` で全件成功を確認する（11 件 / skipped 1 / 失敗 0）
- [x] Windows で skipped が 1 件（`handles timeout`）として集計されることを確認する
- [x] tasklist を更新してコミットする（`✅ Replace direct bash calls in process utils tests`）

---

## セクション 2: JsonSchemaProviderFactoryTest の VFS 依存を分離（手法 B）

- [x] `src/test/kotlin/com/rescript/plugin/config/RescriptJsonSchemaProviderFactoryTest.kt` に新規テスト `testSchemaResourceExistsOnClasspath` を追加する
  - `assertNotNull(RescriptJsonSchemaProviderFactory::class.java.getResource("/schemas/rescript.schema.json"))`
  - コメントで「元テストの本来の検証意図」であることを明記する
- [x] 既存 `testProviderSchemaFileResolves` に `@DisabledOnOs(WINDOWS)` を付与する
  - `disabledReason` に「プラグイン jar が sandbox 配下にあり許可 VFS ルート外」である旨を英語で明記する
- [x] `org.junit.jupiter.api.condition.DisabledOnOs` / `OS` を import する
- [x] `./gradlew test --tests 'com.rescript.plugin.config.RescriptJsonSchemaProviderFactoryTest'` で全件成功を確認する（10 件 / skipped 1 / 失敗 0）
- [x] tasklist を更新してコミットする（`✅ Verify bundled schema via classpath instead of VFS`）

---

## セクション 3: ローカル検証・CI 検証・マージ

### ローカル検証

- [ ] `./gradlew ktlintCheck` が成功する
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] `./gradlew test` を実行し、Windows で失敗 0 件を確認する
- [ ] スキップ総数が 17 件（従来 15 + 本作業の 2）であることを確認する
- [ ] `.claude/rules/definition-of-done.md` の Phase 3 を通過する

### CI 検証（**B の完了判定に必須**）

- [ ] main にマージして push する
- [ ] `gh workflow run os-matrix.yml --ref main` で OS Matrix を実行する
- [ ] CI Windows のテストレポートを取得する
- [ ] **対象 2 クラスが失敗リストから消えたことをクラス単位で確認する**
  - `util.RescriptProcessUtilsTest`（従来 4 件固定失敗）→ 0 件
  - `config.RescriptJsonSchemaProviderFactoryTest`（従来 1 件固定失敗）→ 0 件
  - 総失敗数は 21〜37 の範囲で揺れるため、**総数では判定しない**
- [ ] ubuntu が success を維持していることを確認する
- [ ] macOS で新たな固定失敗が発生していないことを確認する

### 仕上げ

- [ ] 記憶 `windows-known-test-failures` を実態に合わせて更新する
- [ ] tasklist の全タスクが `[x]` であることを確認し、最終コミットに含める
- [ ] `AskUserQuestion` でユーザーにマージ可否を確認する（マージは CI 検証の前に必要なため、確認タイミングはセクション 3 冒頭）
- [ ] worktree とブランチをクリーンアップする

---

## 手順上の既知の注意点

`20260808-002` で判明した以下の落とし穴を繰り返さないこと。

- worktree 内で `git checkout main` は `fatal: 'main' is already used by worktree` で失敗する。`ExitWorktree(action: keep)` でメインリポジトリへ戻ってからマージする
- worktree の `git worktree remove` は Windows のロングパス制限で失敗することがある。その場合は PowerShell の `\\?\` プレフィックス付き `Remove-Item` で削除する
- SSH エージェントが不通のため、push は `git -c credential.helper='!gh auth git-credential' push https://github.com/...` を使う

## ドキュメント更新について

テストコードのみの変更でユーザー向け機能に変更がないため、`.claude/rules/documentation.md` の同期対象表はいずれも更新不要。`docs/product-requirements.md` にも該当項目はない。

## テスト作成の免除について

既存テストの修正が成果物であり、新規プロダクションクラスを追加しないため、`.claude/rules/testing.md` が求める新規クラス向けテストは発生しない。セクション 2 で追加する `testSchemaResourceExistsOnClasspath` は既存クラスに対するテスト追加である。
