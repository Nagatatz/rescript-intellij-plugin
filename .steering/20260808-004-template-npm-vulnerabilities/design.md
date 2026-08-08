# design: ウィザードテンプレートの npm 脆弱性解消

## 上流調査で判明した事実

requirements 承認後、Astro 7 と関連パッケージを実測・調査して以下を確定した。

### F-1: Astro テンプレートは astro 7 の破壊的変更のいずれにも該当しない

[Astro v7 アップグレードガイド](https://docs.astro.build/en/guides/upgrade-to/v7/)の破壊的変更を
テンプレート実体（`src/main/resources/templates/astro/`）と突き合わせた:

| 破壊的変更 | テンプレートへの影響 |
|---|---|
| 実験フラグ（`rustCompiler` / `queuedRendering` / `logger` / `advancedRouting` / `cache`）の削除 | **なし** — `astro.config.mjs` に実験フラグは 1 つも無い |
| `src/fetch.ts` のルーティング用途への転用 | **なし** — 該当ファイルが存在しない |
| `@astrojs/db` の完全削除 | **なし** — 依存していない |
| Markdown 処理系が remark/rehype → Sätteri | **なし** — remark/rehype プラグインを使っていない |
| `astro:transitions` の内部 API 削除 | **なし** — View Transitions を使っていない |
| Rust コンパイラの HTML 厳格化（未閉タグ・不正なネスト） | **なし** — `index.astro` は全タグが閉じられ、ネストも正しい |
| `compressHTML` の既定が `true` → `'jsx'`（インライン要素間の空白除去） | **軽微** — テンプレートはブロック要素中心。表示崩れが起きないか AC-8 で確認する |
| Vite 8 / Rolldown への移行 | **要検証** — 設定は既定のままだが、ビルドが通るかは AC-8 で実測する |

つまり **R-2（テンプレートの追従）は「変更なし」で済む可能性が高い**が、
Vite 8 / Rolldown の影響は静的検査では判断できないため AC-8 の実ビルドで確定させる。

### F-2: 統合パッケージのバージョンと peer 要件

| パッケージ | 現行ピン | 新ピン | peer 要件 |
|---|---|---|---|
| `astro` | `^6.3.1` | `^7.2.0` | — |
| `@astrojs/react` | `^5.0.0` | `^6.0.2` | `react` 17/18/19、`@types/react` 17/18/19（astro への peer 宣言は無し） |
| `@astrojs/node` | `^10.0.0` | `^11.1.0` | `astro ^7.0.0` |

`@astrojs/node@11.1.0` は low advisory（`>=8.1.0 <11.0.2`）の修正版でもある。

### F-3: Node.js 要件は既に満たしている

`astro@7.2.0` の `engines.node` は `>=22.12.0`。
テンプレートが書き込む `NODE_ENGINE` は既に `">=24"` であり、引き上げは不要。

### F-4: `astro.config.mjs` の API は変更不要

現行の設定は以下のみで、いずれも astro 7 で有効:

```js
export default defineConfig({
  output: "server",
  adapter: node({ mode: "standalone" }),
  integrations: [react()],
});
```

## 設計上の決定

### D-1: 監査例外リストは advisory ID 単位の JSON + 判定スクリプト

`npm audit` 自体には除外機能が無いため、`npm audit --json` の出力を解釈する
判定スクリプトを追加し、workflow の `npm audit --audit-level=high` を置き換える。

**ファイル構成**

| パス | 役割 |
|---|---|
| `.github/scripts/npm-audit-allowlist.json` | 除外エントリの正本（データ） |
| `.github/scripts/check-npm-audit.mjs` | 判定ロジック（`npm audit --json` を実行して評価） |

**allowlist の形式**

JSON にはコメントが書けないため、**理由をデータとして持たせる**。
`plugin-verifier-ignored-problems.txt` がコメントで担っていた情報をフィールド化する:

```json
{
  "note": "Advisories that cannot be fixed by changing TemplateVersions.kt. ...",
  "allow": [
    {
      "id": "GHSA-w3rx-r6r6-pgpr",
      "package": "image-size",
      "reason": "No fixed version exists upstream: the latest published image-size is 2.0.2, which is itself inside the vulnerable range. metro still depends on image-size ^1.0.2 as of 0.87.0.",
      "url": "https://github.com/advisories/GHSA-w3rx-r6r6-pgpr",
      "reviewed": "2026-08-09",
      "expires": "2027-02-09"
    }
  ]
}
```

**除外の粒度は advisory ID とする。** パッケージ名で除外すると、そのパッケージに将来
別の脆弱性が出たときに黙って隠れてしまう。

**スクリプトの判定ロジック**

1. `npm audit --json` を実行（終了コードは無視。脆弱性があると非 0 を返すため）
2. `vulnerabilities[*].via[]` からオブジェクト形式の advisory を全て収集し、
   `url` から GHSA ID を抽出して一意化する
3. `severity` が閾値（既定 `high`）以上のものを対象にする
4. allowlist の `id` に一致するものを除外する
5. **残りが 1 件でもあれば exit 1**（詳細を stdout に出力）
6. 追加で以下を **警告として**報告する（exit code には影響させない）:
   - `expires` が今日より過去のエントリ（再検討期限切れ）
   - どの advisory にも一致しなかった allowlist エントリ（陳腐化した除外）
7. GitHub Actions 上では `GITHUB_STEP_SUMMARY` にも同じ内容を書き出す

**なぜ「期限切れ」を fail にしないか**: 期限切れは「見直せ」という運用上の合図であって、
その時点で新たな危険が生じたわけではない。fail にすると、無関係のリリース作業が
突然ブロックされる。`plugin-verifier-ignored-problems.txt` の月次 verifyPlugin が
Step Summary への警告に留めている運用と揃える。

### D-2: スクリプトは fixture ベースのテストを持つ

AC-3（除外リストに載っていない high が出たら fail する）を、
**本番の `npm audit` 結果に依存せずに**検証できるようにする。

`npm audit --json` の実行部分と判定部分を分離し、判定関数を単体で呼べる構造にする:

```js
export function evaluate(auditJson, allowlist, { severityThreshold, today }) {
  return { blocking: [...], expired: [...], stale: [...] };
}
```

テストは `.github/scripts/__tests__/check-npm-audit.test.mjs` に置き、
Node 標準の `node:test` で実行する（新規の npm 依存を持ち込まない）。

| ケース | 期待 |
|---|---|
| high が 1 件、allowlist 空 | `blocking` に 1 件 |
| high が 1 件、その ID が allowlist にある | `blocking` 空 |
| moderate のみ、allowlist 空 | `blocking` 空（閾値未満） |
| allowlist の `expires` が過去 | `expired` に 1 件、`blocking` には影響しない |
| allowlist の ID がどの advisory にも一致しない | `stale` に 1 件 |
| 同一 advisory が複数パッケージ経由で出現 | 重複排除されて 1 件 |

> `.claude/rules/testing.md` は Kotlin テストの規約だが、
> 「すべてのコード変更にはテストを含める」という原則は言語を問わず適用する。
> 既存の `.github/scripts/audit-template-versions.mjs` にはテストが無いが、
> 本スクリプトは **セキュリティゲートの判定そのもの**であり、
> 誤って常に成功する実装になると脆弱性を検出できなくなるため、テストを必須とする。

### D-3: workflow の変更は最小限にする

`monthly-verify.yml` の該当ステップのみを置き換える:

```yaml
- name: npm audit (fail on high/critical, minus documented exceptions)
  working-directory: .github/scripts/template-audit
  run: node ../check-npm-audit.mjs --allowlist ../npm-audit-allowlist.json
```

`release.md` の「ローカル再現」手順も同じコマンドに更新する。

### D-4: R-2 は「変更が必要になったら行う」

F-1 のとおりテンプレート実体に手を入れる必要は見当たらない。
AC-8 の実ビルドで問題が出た場合にのみ修正する。**先回りして直さない**
（astro 7 で不要になった記述を推測で消すと、かえって壊すため）。

## 変更対象ファイル

### セクション 1: 監査例外リストの仕組み（テンプレート変更なし）

| ファイル | 変更 |
|---|---|
| `.github/scripts/check-npm-audit.mjs` | 新規 |
| `.github/scripts/npm-audit-allowlist.json` | 新規（`image-size` の 2 advisory を登録） |
| `.github/scripts/__tests__/check-npm-audit.test.mjs` | 新規 |
| `.github/workflows/monthly-verify.yml` | audit ステップの置き換え |
| `.claude/rules/release.md` | ローカル再現手順の更新 |

### セクション 2: バージョンピンの更新

| ファイル | 変更 |
|---|---|
| `src/main/kotlin/.../templates/TemplateVersions.kt` | `ASTRO` / `ASTROJS_REACT` / `ASTROJS_NODE` / `ESBUILD` |
| `src/test/kotlin/.../TemplateVersionsTest.kt`（存在すれば） | 追従 |

### セクション 3: 検証とドキュメント

| ファイル | 変更 |
|---|---|
| `docs/` / `sphinx-docs/user/templates/astro.md` 等 | バージョン表記があれば更新（要調査） |
| `sphinx-docs/locale/ja/**` | 対応する `.po` |

## テスト方針

- `check-npm-audit.mjs` — D-2 の fixture テスト（必須）
- `TemplateVersions.kt` — 既存テストの有無を実装時に確認し、あれば追従させる
- テンプレート生成 — 既存の生成テストが緑であることを確認する
- AC-8 の実ビルドは自動テストではなく手動検証として実施し、結果を tasklist に記録する

## 検証手順

```bash
# 監査の再現（着手時と完了時に実施し、日付付きで記録する）
node .github/scripts/audit-template-versions.mjs /tmp/ta
cd /tmp/ta && npm i --package-lock-only --legacy-peer-deps
node <repo>/.github/scripts/check-npm-audit.mjs --allowlist <repo>/.github/scripts/npm-audit-allowlist.json

# スクリプトのテスト
node --test .github/scripts/__tests__/

# プラグイン本体
./gradlew ktlintCheck clean buildPlugin test
```

AC-8 は生成したプロジェクトで実際に `npm install && npm run build` を実行する。

## ロールバック

セクション 1（例外リストの仕組み）とセクション 2（ピン更新）は独立している。
astro 7 化が AC-8 で通らなかった場合、セクション 2 のみ revert すれば
セクション 1 の仕組みは残せる。その場合 `sharp` の high を allowlist に
一時的に追加するか、作業自体を保留するかを再判断する。
