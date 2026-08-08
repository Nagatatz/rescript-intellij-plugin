# タスクリスト: Windows で残存する POSIX 前提テスト失敗の解消

## セクション間の依存

各セクション（1〜4）は**互いに独立**しており、任意の順序で着手・コミットできる。いずれも他セクションのコードに依存しない。セクション 0 のみ全体の前提となる。

セクション 5 は 1〜4 の完了後に実施する。

---

## セクション 0: 準備

- [x] メインリポジトリで `git fetch origin` を実行する
- [x] `git log --oneline origin/main..HEAD` と `git log --oneline HEAD..origin/main` で main が origin と同期していることを確認する（遅れていれば `git pull --ff-only origin main`）
- [x] steering ドキュメント（requirements / design / tasklist）を main に直接コミットする（`📝 Add steering docs for Windows POSIX test failures`）
- [x] `EnterWorktree` で worktree を作成する
- [x] worktree 直後に `pwd` と `git rev-parse --show-toplevel` を実行し、編集対象が worktree 内であることを確認する

> 注: worktree は `origin/main` から切られるため未 push の steering コミットが含まれない。worktree 内で `git merge main` を実行して取り込んだ。

---

## セクション 1: CliDetectorTest のパス比較を Path API に置き換える（手法 A-1）

- [x] `src/test/kotlin/com/rescript/plugin/run/RescriptCliDetectorTest.kt` の 2 テストを修正する
  - `findCli returns path when CLI exists in workingDirectory`
  - `findCli returns path when CLI exists in projectBasePath`
  - `assertTrue(result!!.contains("node_modules/.bin/rescript"))` → `assertTrue(Path.of(result!!).endsWith(Path.of("node_modules/.bin/rescript")))`
- [x] `findCli prefers workingDirectory over projectBasePath` の `contains("work")` が Windows でも成立しているか確認する（現状 pass だが区切り文字非依存か目視確認）
  - 区切り文字を含まない単一ディレクトリ名の照合であり、プラットフォーム非依存。変更不要と判断
- [x] `./gradlew test --tests 'com.rescript.plugin.run.RescriptCliDetectorTest'` で全件成功を確認する（6 件成功 / スキップ 0 / 失敗 0）
- [x] tasklist を更新してコミットする（`✅ Make CLI detector path assertions platform-agnostic`）

---

## セクション 2: ReanalyzeServerServiceTest のパス比較を分解する（手法 A-2）

- [x] `src/test/kotlin/com/rescript/plugin/analysis/RescriptReanalyzeServerServiceTest.kt` の `getSocketPath returns correct path` を修正する
  - `assertEquals(".rescript-reanalyze.sock", socketPath.fileName.toString())`
  - `assertEquals(Path.of("/project/root"), socketPath.parent)`
- [x] 同クラスの `getSocketPath handles trailing slash` が Windows で成功し続けることを確認する（現状 pass。必要なら同方式に揃える）
  - `endsWith(".rescript-reanalyze.sock")` は区切り文字を含まないため非依存。変更不要と判断
- [x] `./gradlew test --tests 'com.rescript.plugin.analysis.RescriptReanalyzeServerServiceTest'` で全件成功を確認する（14 件成功 / スキップ 0 / 失敗 0）
- [x] tasklist を更新してコミットする（`✅ Assert socket path by components instead of a POSIX string`）

---

## セクション 3: FormatCheckAnnotatorTest の常時失敗コマンドを生成する（手法 B）

- [x] `src/test/kotlin/com/rescript/plugin/analysis/RescriptFormatCheckAnnotatorTest.kt` に private helper `createAlwaysFailingCommand(dir: Path): Path` を追加する
  - Windows: `alwaysfail.bat` に `@echo off` / `exit /b 1`
  - POSIX: `alwaysfail.sh` に `#!/bin/sh` / `exit 1` + `setExecutable(true)`
  - プラットフォーム判定は `com.intellij.openapi.util.SystemInfo.isWindows` を使う
  - helper に KDoc を付与し、`/usr/bin/false` を置き換えた理由を明記する
- [x] `@TempDir` で一時ディレクトリを受け取るよう該当 2 テストを修正する
  - `runFormatCheck returns null for command with non-zero exit`
  - `runFormatCheck cleans up process on non-zero exit`
- [x] `cliPath = "/usr/bin/false"` を helper の戻り値に差し替える
- [x] 残存する `/usr/bin/false` 参照がないか `grep -rn "usr/bin/false" src/test/` で確認する
  - 残存は helper の KDoc 内の説明文 1 件のみ。実行パスとしての使用はゼロ
- [x] `./gradlew test --tests 'com.rescript.plugin.analysis.RescriptFormatCheckAnnotatorTest'` で全件成功を確認する（15 件成功 / スキップ 0 / 失敗 0）
- [x] tasklist を更新してコミットする（`✅ Generate a cross-platform always-failing command in format check tests`）

---

## セクション 4: 実行ビット依存テストを Windows でスキップする（手法 C）

- [x] `src/test/kotlin/com/rescript/plugin/util/RescriptSecurityUtilsTest.kt` の `isValidExecutable returns false for non-executable file` に `@DisabledOnOs` を付与する
- [x] `src/test/kotlin/com/rescript/plugin/settings/RescriptSettingsValidatorTest.kt` の 2 テストに `@DisabledOnOs` を付与する
  - `validateNodePath throws when file is not executable`
  - `validateLspPath throws when non-js file is not executable`
- [x] いずれも `disabledReason` に「Windows に実行ビットがなく `File.canExecute()` が常に true」である旨を英語で明記する
- [x] `org.junit.jupiter.api.condition.DisabledOnOs` / `OS` を import する
- [x] 該当 2 クラスを実行し、Windows で `skipped` として集計される（silently pass ではない）ことを確認する
  - `RescriptSecurityUtilsTest`: 23 件中 skipped 1 / 失敗 0
  - `RescriptSettingsValidatorTest`: 24 件中 skipped 2 / 失敗 0
- [x] tasklist を更新してコミットする（`✅ Skip execute-bit assertions on Windows`）

---

## セクション 5: 全体検証と仕上げ

- [ ] `./gradlew ktlintCheck` が成功する
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] `./gradlew test` を実行し、**Windows で失敗 0 件**を確認する
- [ ] スキップ件数が手法 C の 3 件のみであることを確認する
- [ ] `.claude/rules/definition-of-done.md` の Phase 3 を通過することを確認する
- [ ] 記憶 `windows-known-test-failures` を実態に合わせて更新する（8 件の既知失敗は解消済み、Windows でのスキップ 3 件のみが残る旨）
- [ ] tasklist の全タスクが `[x]` であることを確認し、最終コミットに含める
- [ ] `AskUserQuestion` でユーザーにマージ可否を確認する
- [ ] 承認後、worktree 内で `git checkout main && git merge <作業ブランチ>` を実行し、作業ブランチを削除する
- [ ] セッションを終了して worktree の自動クリーンアップを発動させる

---

## ドキュメント更新について

本作業はテストコードのみの変更であり、ユーザー向け機能に変更がない。そのため `.claude/rules/documentation.md` の同期対象表（`docs/repository-structure.md` / `docs/functional-design.md` / `README.md` / `sphinx-docs/`）はいずれも**更新不要**と判断する。

`docs/product-requirements.md` のロードマップにも該当項目はない。

## テスト作成の免除について

本作業は既存テストの修正そのものが成果物であり、新規プロダクションクラスを追加しないため、`.claude/rules/testing.md` が求める「新規クラスに対するテスト作成」は発生しない。
