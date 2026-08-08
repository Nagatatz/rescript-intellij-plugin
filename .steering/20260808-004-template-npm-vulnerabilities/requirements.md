# requirements: ウィザードテンプレートの npm 脆弱性解消

## 背景

Monthly Verify ワークフローの `template-versions-audit` ジョブが 2026-08-01 の実行以降 **fail** している。
このジョブは `TemplateVersions.kt`（ウィザードがユーザープロジェクトに書き込む npm バージョンピン）から
合成 `package.json` を生成し、`npm audit --audit-level=high` を実行する。
Dependabot はこれらを Kotlin ソース内の文字列としか認識できないため、この仕組みで補っている。

`.claude/rules/release.md` の「前提条件」に **「直近の Monthly Verify の `template-versions-audit` ジョブが
green であること」** と明記されており、**このジョブが赤い限りリリースできない**。

## 調査結果（2026-08-09 にローカルで実測）

`node .github/scripts/audit-template-versions.mjs` → `npm i --package-lock-only --legacy-peer-deps`
→ `npm audit --json` を実行して確認した。合成マニフェストの依存は 74 件。

**8 月 1 日の CI ログとは内容が異なる。** ピンの多くが `^` レンジであり、解決される実バージョンが
日々変わるためである。以下は 2026-08-09 時点の実測値であり、着手時に再実測すること。

### high の root advisory は 2 件のみ

`npm audit` は依存チェーンの上流パッケージにも最大 severity を伝播させるため、
「high」と表示されるパッケージは 14 件あるが、**根本原因の advisory は 2 件**である。

| # | advisory | 脆弱レンジ | 実際の解決版 | 依存経路 |
|---|---|---|---|---|
| V-1 | [GHSA-f88m-g3jw-g9cj](https://github.com/advisories/GHSA-f88m-g3jw-g9cj) `sharp` libvips CVE | `<0.35.0` | `0.34.5` | `astro 6.4.8` → `sharp ^0.34.0` |
| V-2 | [GHSA-w3rx-r6r6-pgpr](https://github.com/advisories/GHSA-w3rx-r6r6-pgpr) / [GHSA-5p2g-fcmc-qvqq](https://github.com/advisories/GHSA-5p2g-fcmc-qvqq) `image-size` DoS | `<=2.0.2` | `1.2.1` | `react-native 0.85.3` → `metro 0.84.4` → `image-size ^1.0.2` |

### V-1 は修正可能

`astro@7.2.0` の `optionalDependencies.sharp` は `^0.34.0 || ^0.35.0` であり、
npm は最高版を選ぶため `sharp 0.35.3`（修正済み）に解決される。
現行の `astro@6.4.8` は `sharp ^0.34.0` のみで 0.34.5 に留まる。

### V-2 は上流に修正版が存在しない

- `image-size` の **最新公開版は 2.0.2** であり、これ自体が脆弱レンジ `<=2.0.2` に含まれる
- `metro` は **最新の 0.87.0 でも `image-size ^1.0.2`** に依存している
- `npm audit` の `fixAvailable` は `react-native@0.72.17` を提示するが、これは 0.85.3 からの
  大幅なダウングレードであり、採用できない（テンプレートが対象とする RN の世代を割る）

したがって V-2 は **バージョン変更では解消できない**。

### high 未満で修正可能なもの

`--audit-level=high` のゲートは通るが、安価に直せるものは併せて対応する。

| # | パッケージ | severity | 脆弱レンジ | 対応 |
|---|---|---|---|---|
| V-3 | `astro` XSS ×3 | moderate / low | `<=7.0.9` 他 | V-1 の astro 7 化で同時に解消 |
| V-4 | `@astrojs/node` | low | `>=8.1.0 <11.0.2` | `^11.1.0` へ（astro 7 の peer 要件でもある） |
| V-5 | `esbuild` 開発サーバのファイル読み取り | low | `>=0.27.3 <0.28.1` | ピンの下限を `^0.28.1` に引き上げ |
| V-6 | `waku` CSRF / Open Redirect | moderate ×2 | `<=1.0.0-beta.0` | **今回は対象外**（下記「対象外」参照） |

### 修正できないもの（今回は対象外）

| パッケージ | severity | 理由 |
|---|---|---|
| `uuid` `<11.1.1` | moderate | `xcode` の推移的依存（React Native ツールチェーン）。直接ピンが無く、`overrides` を使わない限り制御できない。ゲート未満のため今回は許容する |

## 目的

1. `template-versions-audit` を green にし、リリースのブロッカーを解消する
2. 上流に修正版が無い advisory を「見なかったことにする」のではなく、
   **理由と再検討期限を伴って明示的に除外する仕組み**を用意する

## スコープ

### 対象

| # | 項目 | 内容 |
|---|------|------|
| R-1 | astro 系のメジャー更新 | `ASTRO` `^6.3.1` → `^7.2.0` / `ASTROJS_REACT` `^5.0.0` → `^6.0.2` / `ASTROJS_NODE` `^10.0.0` → `^11.1.0` |
| R-2 | Astro テンプレートの追従 | astro 7 の破壊的変更に対する `AstroTemplateFiles.kt` の修正 |
| R-3 | esbuild ピンの引き上げ | `ESBUILD` `^0.28.0` → `^0.28.1` |
| R-4 | 監査例外リストの導入 | 上流に修正版が無い advisory を理由・期限付きで除外する仕組み |
| R-5 | テストの更新 | `TemplateVersions` およびテンプレート生成のテストを新バージョンに追従させる |
| R-6 | ドキュメント同期 | テンプレートのバージョン表記があるドキュメントの更新 |

### 対象外

- **Waku の更新（`1.0.0-alpha.10` → `1.0.0-beta.9`）** — ユーザー判断により 2026-08-09 に除外。
  解消されるのは V-6 の moderate 2 件のみで `--audit-level=high` のゲートは通る一方、
  alpha → beta の移行はテンプレートコードの追従量が読めず、本作業の目的（ゲートを green にする）に
  対して risk が見合わない。別作業として切り出す
- `uuid` の推移的脆弱性（上表の理由により許容）
- `npm overrides` の全面導入（テンプレートが生成するユーザープロジェクトの依存解決に
  副作用を持ち込むため、本作業では採用しない）
- Expo / React Native 本体のメジャー更新（advisory の解消に寄与しない）
- LSP API 移行・`0.1.17` リリース（それぞれ別作業）

## 監査例外リストの設計方針（R-6）

`plugin-verifier-ignored-problems.txt` の運用に揃える。すなわち:

- 除外は **advisory ID 単位**で行う（パッケージ名単位にすると将来の別の脆弱性まで隠す）
- 各エントリに **理由・出典 URL・`Reviewed`・`Expires`** を必須とする
- `Expires` 切れは警告として可視化する（Monthly Verify の Step Summary）
- 除外が 0 件になったらファイルごと削除できる形にする

実装方式は design で決める。候補: `npm audit --json` の結果を除外リストと突き合わせて
判定する小さな Node スクリプトを `.github/scripts/` に追加し、`npm audit --audit-level=high` を置き換える。

## 受け入れ条件

- [ ] AC-1: ローカルで監査を再現し、**high/critical の未除外 advisory が 0 件**になる
- [ ] AC-2: 除外されているのは V-2（`image-size`）のみであり、理由・URL・`Reviewed`・`Expires` が記載されている
- [ ] AC-3: 除外リストに載っていない high が出た場合、監査が **fail する**ことを確認する（意図的に偽エントリを入れて検証）
- [ ] AC-4: `./gradlew ktlintCheck` が成功する
- [ ] AC-5: `./gradlew clean buildPlugin` が成功する
- [ ] AC-6: `./gradlew test` が全件成功する
- [ ] AC-7: `./gradlew test -Pscope=cli` および Integration Tests が対象とするテンプレート生成が壊れていない
- [ ] AC-8: Astro テンプレートが生成するプロジェクトが、新バージョンで実際に
      `npm install` および build を通ることを確認する
- [ ] AC-9: ドキュメントのバージョン表記が更新されている
- [ ] AC-10: Monthly Verify を `workflow_dispatch` で手動実行し、`template-versions-audit` が green になる

## リスク

| リスク | 影響 | 緩和策 |
|---|---|---|
| astro 6 → 7 の破壊的変更 | Astro テンプレートが生成直後にビルドできない | AC-8 で実際に生成して `npm install` + build を通す |
| 例外リストが「とりあえず除外」の温床になる | 監査ゲートが形骸化する | AC-3 で fail することを実証し、`Expires` を必須にする |
| `^` レンジのため実測が日々変わる | 着手時と検証時で結果が変わる | 各検証時点で再実測し、tasklist に日付付きで記録する |
| 監査スクリプト置き換えの不具合 | 脆弱性を検出できなくなる | AC-3 の偽エントリ検証を必須とする |

## 参照

- `.github/workflows/monthly-verify.yml` — `template-versions-audit` ジョブ
- `.github/scripts/audit-template-versions.mjs` — 合成マニフェスト生成
- `.claude/rules/release.md` — リリース前提条件
- `plugin-verifier-ignored-problems.txt` — 例外リスト運用の先例
