# tasklist: ウィザードテンプレートの npm 脆弱性解消

## 前提と依存関係

- **セクション 1（例外リストの仕組み）とセクション 2（ピン更新）は独立している。**
  どちらを先にマージしても機能する。ただしセクション 1 だけでは `sharp` の high が残るため、
  監査が green になるのは両方が揃ってから
- セクション 3 はセクション 2 の完了を前提とする（astro 7 の実ビルド検証のため）
- 作業は worktree 内で行う（ブランチ `worktree-template-npm-vulnerabilities`）
- **`^` レンジのため監査結果は日々変わる。** 各検証時点で再実測し、日付付きで記録すること

## セクション 0: 準備

- [ ] `git fetch origin` を実行し、ローカル `main` が `origin/main` と同期していることを確認
      （並列セッションが活動中のため、`git log --oneline origin/main..main` と逆方向の両方を確認する）
- [ ] `EnterWorktree` で worktree を作成
- [ ] worktree 内で `pwd` と `git rev-parse --show-toplevel` を実行し、編集対象が worktree 内であることを確認
- [ ] worktree が `origin/main` 起点で作られるため、ステアリングを取り込む必要があれば
      `git merge --ff-only main` を実行する
- [ ] `.steering/20260808-004-template-npm-vulnerabilities/` をコミット

## セクション 1: 監査例外リストの仕組み

このセクションはテンプレートのバージョンを一切変更しない。
仕組みだけを先に入れ、単体で正しく動くことを確認する。

### 1-1: 判定スクリプト

- [ ] `.github/scripts/check-npm-audit.mjs` を新規作成する
  - [ ] `npm audit --json` を実行する部分と、判定を行う `evaluate()` を分離する（D-2）
  - [ ] `vulnerabilities[*].via[]` のオブジェクト要素から GHSA ID を抽出し一意化する
  - [ ] severity 閾値（既定 `high`）以上を対象とする
  - [ ] allowlist の `id` に一致するものを除外する
  - [ ] 残りが 1 件でもあれば **exit 1** し、詳細を stdout に出力する
  - [ ] `expires` 切れのエントリを **警告**として報告する（exit code に影響させない）
  - [ ] どの advisory にも一致しない allowlist エントリを **警告**として報告する
  - [ ] `GITHUB_STEP_SUMMARY` が設定されていれば同内容を追記する
  - [ ] `--allowlist <path>` と `--severity <level>` を引数で受け取る

### 1-2: 例外リスト

- [ ] `.github/scripts/npm-audit-allowlist.json` を新規作成する
- [ ] `image-size` の 2 advisory を登録する
      （`GHSA-w3rx-r6r6-pgpr` / `GHSA-5p2g-fcmc-qvqq`）
  - [ ] `reason` に「上流に修正版が存在しない」根拠を書く
        （image-size の最新公開版 2.0.2 自体が脆弱レンジ内、metro 0.87.0 も `^1.0.2` 依存）
  - [ ] `url` / `reviewed: 2026-08-09` / `expires: 2027-02-09` を設定する
- [ ] **`sharp` を allowlist に入れないこと**（セクション 2 で解消するため）

### 1-3: テスト（AC-3 の担保）

- [ ] `.github/scripts/__tests__/check-npm-audit.test.mjs` を新規作成する（`node:test` を使用）
- [ ] high 1 件 / allowlist 空 → `blocking` 1 件
- [ ] high 1 件 / その ID が allowlist にある → `blocking` 空
- [ ] moderate のみ / allowlist 空 → `blocking` 空（閾値未満）
- [ ] `expires` が過去 → `expired` 1 件、`blocking` には影響しない
- [ ] allowlist の ID がどの advisory にも一致しない → `stale` 1 件
- [ ] 同一 advisory が複数パッケージ経由で出現 → 重複排除されて 1 件
- [ ] `node --test .github/scripts/__tests__/` が成功する

### 1-4: workflow と手順書

- [ ] `.github/workflows/monthly-verify.yml` の audit ステップを新スクリプト呼び出しに置き換える
- [ ] `.claude/rules/release.md` の「ローカル再現」コマンドを更新する
- [ ] `actionlint` 相当の検証として workflow の YAML がパースできることを確認する
- [ ] コミット（`✨ Add an allowlist-aware npm audit gate for template versions`）

> この時点ではまだ監査は **fail する**（`sharp` が未解消のため）。それが正しい状態である。
> セクション 1 完了時に実際に fail することを確認し、下記に記録すること。

### セクション 1 の実測ログ（実装時に記入）

| 項目 | 結果 |
|---|---|
| 実測日 | （未記入） |
| スクリプト単体テスト | （未記入） |
| 監査の判定（`sharp` 未解消の状態） | （未記入 — fail が期待値） |
| blocking として報告された advisory | （未記入） |

## セクション 2: バージョンピンの更新

- [ ] `TemplateVersions.kt` の `ASTRO` を `^6.3.1` → `^7.2.0`
- [ ] `TemplateVersions.kt` の `ASTROJS_REACT` を `^5.0.0` → `^6.0.2`
- [ ] `TemplateVersions.kt` の `ASTROJS_NODE` を `^10.0.0` → `^11.1.0`
- [ ] `TemplateVersions.kt` の `ESBUILD` を `^0.28.0` → `^0.28.1`
- [ ] 各定数のコメントを実態に合わせて更新する
      （現行コメントは「astro 6.x が active major、@astrojs/react 5.x と @astrojs/node 10.x が対応アダプタ」）
- [ ] `TemplateVersionsTest.kt` が緑（semver 形式の検証のみなので追従不要の見込み。要確認）
- [ ] `AstroTemplateFilesTest.kt` が緑（`TemplateVersions.ASTRO` を記号参照しているため追従不要の見込み。要確認）
- [ ] `./gradlew ktlintCheck clean buildPlugin test` が成功する
- [ ] 監査を再実測し、**blocking が 0 件**になることを確認する
- [ ] コミット（`⬆ Move the Astro template to Astro 7`）

## セクション 3: 実ビルド検証とドキュメント

### 3-1: AC-8 実ビルド検証

astro 7 が実際に動くかは静的検査では判断できない（design F-1 / D-4）。

- [ ] Astro テンプレートを生成する（`manual-test-projects/` 配下、または一時ディレクトリ）
- [ ] `npm install` が成功する
- [ ] `npm run build` が成功する（ReScript コンパイル → `astro build`）
- [ ] 生成物に明らかな破損がないことを確認する
- [ ] `compressHTML: 'jsx'` による表示崩れがないか確認する（design F-1 の「軽微」項目）
- [ ] 結果を下記に記録する
- [ ] 問題が出た場合のみ `AstroTemplateFiles.kt` / `templates/astro/` を修正する（D-4: 先回りしない）

### 3-1 の検証結果（実装時に記入）

| 項目 | 結果 |
|---|---|
| 実施日 | （未記入） |
| `npm install` | （未記入） |
| `npm run build` | （未記入） |
| テンプレート修正の要否 | （未記入） |

### 3-2: ドキュメント同期

- [ ] `sphinx-docs/user/templates/astro.md` に astro のバージョン表記があれば更新する
- [ ] `docs/` 配下にテンプレートのバージョン一覧があれば更新する
- [ ] 上記を変更した場合、`.po` を同一コミットで同期する
      （`make gettext && make update-po` → 空 `msgstr` を埋める → `make build-ja`）
- [ ] `docs-lint` スキルで同期崩れがないことを確認する
- [ ] コミット（`📝 ...`）

## セクション 4: 完了処理

- [ ] 監査を最終再実測し、blocking 0 件を確認する（日付を記録）
- [ ] requirements.md の AC-1〜AC-10 を更新する
- [ ] `.claude/rules/definition-of-done.md` で Phase 1〜5 を確認する
- [ ] `git status` と `git log --oneline origin/main..HEAD` を実行し、出力を確認したうえで報告する
- [ ] 本ファイルの全タスクが `[x]` であることを確認（このタスク自身を含む）
- [ ] tasklist 更新をマージ前の最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否を確認する
  - [ ] Astro テンプレート利用者にメジャー更新（astro 6 → 7）が及ぶことを明示する
  - [ ] `image-size` を意図的に除外したことと、その再検討期限を明示する
- [ ] 承認後、`main` にマージしブランチを削除する
- [ ] AC-10: マージ後に Monthly Verify を `workflow_dispatch` で手動実行し、
      `template-versions-audit` が green になることを確認する
- [ ] セッションを終了する

## 別作業に送る項目

- **Waku の更新**（`1.0.0-alpha.10` → `1.0.0-beta.9`）— moderate 2 件（CSRF / Open Redirect）を解消するが、
  alpha → beta の追従量が読めないためユーザー判断で本作業から除外（2026-08-09）
- `uuid <11.1.1`（moderate）— `xcode` の推移的依存で直接ピンが無い。`overrides` を使わない限り制御できない
- LSP API 移行（`.steering/20260808-001-*` から切り出したもの）
- `0.1.17` リリース（本作業の完了が前提条件）
- `.github/scripts/audit-template-versions.mjs` にテストが無い件（本作業では新規スクリプトのみ対象とした）
