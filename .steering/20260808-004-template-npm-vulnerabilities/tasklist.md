# tasklist: ウィザードテンプレートの npm 脆弱性解消

## 前提と依存関係

- **セクション 1（例外リストの仕組み）とセクション 2（ピン更新）は独立している。**
  どちらを先にマージしても機能する。ただしセクション 1 だけでは `sharp` の high が残るため、
  監査が green になるのは両方が揃ってから
- セクション 3 はセクション 2 の完了を前提とする（astro 7 の実ビルド検証のため）
- 作業は worktree 内で行う（ブランチ `worktree-template-npm-vulnerabilities`）
- **`^` レンジのため監査結果は日々変わる。** 各検証時点で再実測し、日付付きで記録すること

## セクション 0: 準備

- [x] `git fetch origin` を実行し、ローカル `main` が `origin/main` と同期していることを確認（0 ahead / 0 behind）
- [x] `.steering/20260808-004-template-npm-vulnerabilities/` を `main` にコミット（`c1e4c314`）
- [x] `EnterWorktree` で worktree を作成（ブランチ `worktree-template-npm-vulnerabilities`）
- [x] worktree 内で `pwd` と `git rev-parse --show-toplevel` を実行し、編集対象が worktree 内であることを確認
- [x] worktree は `origin/main`（`d508dd62`）起点だったため、`git merge main` でステアリングを取り込み

### セクション 0 の記録

- 並列セッションが活動中で、`origin/main` は本作業の着手中にも進んでいた（`dc5df892` → `d508dd62`）
- 並列セッションは `.steering/20260809-001-lsp-client-api-migration/` を作成済み。
  **LSP API 移行（本リポジトリの後続作業として切り出したもの）に着手している**
- 前回の作業（20260808-001）では、`main` に直接コミットしたステアリングが
  並列セッションの main 再構築で失われた。今回も同じ経路のため、
  **マージ時に `c1e4c314` が残っているかを確認すること**

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

### セクション 1 の実測ログ

| 項目 | 結果 |
|---|---|
| 実測日 | 2026-08-09 |
| スクリプト単体テスト | **10 件すべて pass** |
| 監査の判定（`sharp` 未解消の状態） | **exit 1**（期待値どおり） |
| blocking として報告された advisory | `GHSA-f88m-g3jw-g9cj` (`sharp`, high) の 1 件のみ |
| allowed として報告された advisory | `GHSA-w3rx-r6r6-pgpr` / `GHSA-5p2g-fcmc-qvqq`（`image-size`, high）の 2 件 |

**AC-3 はこの実測で満たされている。** 除外していない `sharp` が blocking として報告され exit 1 に
なっており、「例外リストを入れたら何でも通る」状態ではないことが実データで確認できた。
加えて単体テストの「allowlist 空で high が blocking になる」ケースでも担保している。

#### 実装中に遭遇した問題

**1. `node --test <ディレクトリ>` が Node 24 で動作しない。**
`Cannot find module ...__tests__` となる。テストファイルを直接指定する形にした
（workflow も同様）。

**2. Windows で `npm` を `execFileSync` できない。**
最初は `spawnSync npm ENOENT`（Windows の npm は `npm.cmd`）、
`npm.cmd` に変えると `EINVAL`。後者は CVE-2024-27980 の緩和策として
Node 20 以降が `.cmd` / `.bat` のシェル無し起動を拒否するため。
Windows でのみ `shell: true` を有効にした。引数は固定リテラルのみでインジェクション面が無く、
CI (Linux) はシェル無しの経路を通る。

## セクション 2: バージョンピンの更新

- [x] `TemplateVersions.kt` の `ASTRO` を `^6.3.1` → `^7.2.0`
- [x] `TemplateVersions.kt` の `ASTROJS_REACT` を `^5.0.0` → `^6.0.2`
- [x] `TemplateVersions.kt` の `ASTROJS_NODE` を `^10.0.0` → `^11.1.0`
- [x] `TemplateVersions.kt` の `ESBUILD` を `^0.28.0` → `^0.28.1`
- [x] 各定数のコメントを実態に合わせて更新し、**advisory ID を根拠として明記**した
      （将来この行を戻そうとした人が理由に辿り着けるようにするため）
- [x] `TemplateVersionsTest.kt` が緑（semver 形式の検証のみで追従不要だった）
- [x] `AstroTemplateFilesTest.kt` が緑（`TemplateVersions.ASTRO` を記号参照しているため追従不要だった）
- [x] **golden マニフェストの再生成**（下記参照 — 事前に想定していなかったタスク）
- [x] `./gradlew ktlintCheck buildPlugin test` が成功する
- [x] 監査を再実測し、**blocking が 0 件**になることを確認する
- [x] コミット（`⬆ Move the Astro template to Astro 7`）

### セクション 2 の記録

**golden テストの再生成が必要だった。** `TemplateGoldenTest` が 22 テンプレートの生成物を
SHA-256 マニフェストで固定しており、ピン変更で 5 ケースが失敗した
（ASTRO ×2、AWS_LAMBDA ×3 — 後者は `ESBUILD` 変更の影響）。
`WIZARD_GOLDEN_UPDATE=true ./gradlew test --tests "*TemplateGoldenTest*"` で再生成した。

**再生成の差分を検証した。** golden 更新は「壊れた出力をそのまま正解にしてしまう」危険があるため、
差分の中身を確認した。5 ファイルとも **変更は 1 行のみ、いずれも `package.json` のハッシュ**で、
`astro.config.mjs` / `README.md` / `rescript.json` 等のハッシュは不変だった。
これは design F-1（astro 7 でテンプレート実体の変更は不要）の裏付けになる。

**ピン解決の実測（2026-08-09）**

| パッケージ | 変更前 | 変更後 |
|---|---|---|
| `astro` | 6.4.8 | **7.2.0** |
| `sharp`（astro 経由） | 0.34.5（脆弱） | **0.35.3** |
| `@astrojs/node` | 10.1.4 | **11.1.0** |
| `@astrojs/react` | 5.0.7 | **6.0.2** |
| `esbuild` | 0.28.1 | 0.28.1（下限のみ引き上げ） |

**監査ゲートの結果: exit 0。** blocking 0 件、allowed 2 件（`image-size`）。
セクション 1 時点の「`sharp` が blocking で exit 1」から期待どおり遷移した。

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

### 3-1 の検証結果

**実施日: 2026-08-09**

AC-8 は既存の結合テストで実施した。ただし到達までに 2 つの障害があり、
いずれも **本作業のスコープ外の問題**だったため、ユーザー承認のうえ修正した。

#### 障害 1: 結合テストが Windows で常にスキップされていた

`IntegrationTestSupport.requireBinary` が `which <binary>` を `ProcessBuilder` で実行して
存在確認していたが、Windows では成立しない:

- `PNPM_BIN=pnpm`（既定）→ `which pnpm` は成功するが、`ProcessBuilder("pnpm")` が
  拡張子なしファイルを起動できず `IOException`
- `PNPM_BIN=<絶対パス>/pnpm.CMD` → Git の POSIX `which` は `PATHEXT` を解釈しないため
  存在確認に失敗し、テストが **skip** される

**両方を満たす値が存在しない。** その結果、Windows では
`tests=4 skipped=4` となり、緑に見えて実際には何も検証していなかった。

対応: `which` への外部依存をやめ、`PATH` と `PATHEXT` を自前で走査する
`resolveExecutable()` を追加し、`exec()` が起動前にコマンドを解決するようにした。
呼び出し側は変更していない。

> 途中で `CreateProcess error=193`（有効な Win32 アプリケーションではない）を踏んだ。
> Windows では `Files.isExecutable` が拡張子なしファイルにも true を返すため、
> `pnpm`（POSIX シェルラッパー）を先に拾っていた。**PATHEXT 候補を先に試す**順序に修正した。

#### 障害 2: どのテンプレートも `pnpm build` を実行していなかった

`templatesWithBundle` が `emptySet()` になっており、**バンドラを起動する検証が
スイート全体で 1 件も無かった**。コメントによれば Vite+ (vite-plus) が pre-1.0 で
`ERR_MODULE_NOT_FOUND` になるためだが、一律に空にされていた。

Astro は vite-plus を使わないため巻き添えである。astro 7 への更新は
**Vite 8 / Rolldown への移行**を含み、これは `pnpm install` と `rescript` だけでは
一切踏まない経路なので、`templatesWithBundle` に `ASTRO` を戻した。

#### 検証結果

| 項目 | 結果 |
|---|---|
| `NewReactTemplatesIntegrationTest`（install → rescript → vitest） | **4/4 pass**（skipped 0） |
| `TemplateIntegrationTest` 全体 | 82 件中 pass 42 / skip 38 / **fail 2**（TAURI のみ。下記参照） |
| **ASTRO (zod)** — install → rescript → vitest → **`astro build`** | **pass**（41.4 秒） |
| **ASTRO (sury)** — 同上 | **pass**（20.1 秒） |
| テンプレート修正の要否 | **不要**。design F-1 / D-4 のとおり、テンプレート実体には一切手を入れていない |

**AC-8 は達成。** astro 7 + Vite 8 / Rolldown で `pnpm build` が実際に成功した。
静的検査では判断できないと design で留保した唯一の項目がこれで解消した。

#### 付随して露出した既存不具合: TAURI テンプレート（本作業のスコープ外）

`TAURI (zod)` / `TAURI (sury)` の 2 件が Windows で失敗する。原因は依存パッケージ内の
ReScript コンパイルエラー:

```
We've found a bug for you!
  node_modules/.pnpm/@rescript-tauri+plugin-noti.../PluginNotification.res
  Could not find the .cmi file for interface ... PluginNotification.resi.
```

**本作業の変更が原因ではないことを確認済み:**

- `TauriTemplateFiles.kt` は変更した定数（`ASTRO` / `ASTROJS_*` / `ESBUILD`）を 1 つも参照していない
- ピン更新コミット `e76531e8` は Tauri 関連ファイルを 1 つも変更していない
- 失敗箇所は生成物ではなく **`node_modules` 内のサードパーティ `@rescript-tauri/*` パッケージ**

**これまで表面化しなかったのは、Windows では全件がスキップされていたため。**
CI (Linux) は green なので、Windows 固有の問題（パス長・大文字小文字・`.cmi` 解決）と推定される。

> 注意: 本作業の `resolveExecutable` 修正により、Windows でも
> `./gradlew integrationTest` が実際に走るようになった結果、**この 2 件が新たに赤くなる**。
> 「以前は緑だったのに」ではなく「以前は何も検証していなかった」が正しい理解である。
> 別作業として送る。

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
- **TAURI テンプレートが Windows の結合テストで失敗する件**（セクション 3-1 の記録参照）。
  `@rescript-tauri/*` の `.cmi` 解決エラー。CI (Linux) は green で、本作業の変更とは無関係
- **`templatesWithBundle` に Vite+ 以外のテンプレートを戻す検討**。本作業では `ASTRO` のみ戻した。
  `pnpm build` を通していないテンプレートが残っており、バンドラ起因の破損を検出できない
