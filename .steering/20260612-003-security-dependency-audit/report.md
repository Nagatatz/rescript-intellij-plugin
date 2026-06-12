# セキュリティ・依存鮮度監査報告 (2026-06-12)

## 調査体制 (audit-tasks.md の二段検証)

- **一次調査** (並列 subagent): ① 依存インベントリ = sonnet Explore、② コードセキュリティ監査 = opus、③ 最新版/CVE 照合 = sonnet + Web
- **二次検証** (Fable): Next.js のピン値と `isWithinProject` 呼び出し箇所をソース実体で確認、npm registry で最新版を再取得 (agent 報告 16.2.7 → 実際は **16.2.9** に補正)、GHSA-26hh-7cqf-hhc6 の影響範囲を本文 fetch で確定、pitest 1.25.4 の実在を GitHub releases で確認

## 結論サマリ

| 区分 | 件数 | 内容 |
|---|---|---|
| **要対応 (High)** | 1 | テンプレートの Next.js ピン `^16.2.4` が既知 High 脆弱性の影響範囲内 |
| 検討推奨 (低) | 3 | pitest minor 5 遅れ / Expo SDK 56 移行 / dependabot 死角の運用 |
| コード要修正 | 0 | — |
| コード懸念 (許容) | 2+1 | 診断アクションの絶対パス表示 (意図的) / JSON パースのサイズ上限なし (project-local) / wizard projectName のコンテンツ非エスケープ (低リスク) |

## 1. 要対応: Next.js テンプレートのピン引き上げ

- `TemplateVersions.kt:51` `NEXTJS = "^16.2.4"` — **GHSA-26hh-7cqf-hhc6 (High, CVSS 7.5、App Router の middleware/proxy bypass) の影響範囲 `>=16.0.0 <16.2.6` に下限が該当** (二次検証でアドバイザリ本文から確定)。2026-05 には同種の High がもう 1 件 (GHSA-267c-6grr-h53f) と Moderate XSS (GHSA-ffhc-5mcf-pf4q) も公開されている
- caret 範囲なので新規 `install` 時は 16.2.9 が解決されるが、下限が脆弱版である以上、lockfile やオフラインミラー環境では脆弱版が選択されうる
- **推奨対応**: `NEXTJS` を `"^16.2.9"` (調査時点の最新) へ引き上げ。nextjs テンプレートの golden 3 件 (`NEXTJS__pnpm-zod` / `bun-zod` / `pnpm-sury`) の再生成が必要 (`WIZARD_GOLDEN_UPDATE=true`)
- agent 報告の「16.2.6 で 13 CVE 修正」という件数は**未検証** (確認できたのは上記 3 件)。対応判断には High 1 件の確定で十分

## 2. 検討推奨 (緊急性なし)

- **pitest 1.20.4 → 1.25.4** (`libs.versions.toml:15`): minor 5 遅れ。ビルドツールのみで本番影響なし。1.25.4 の実在は二次検証で確認済み
- **Expo SDK ^55 → 56** (`TemplateVersions.kt:58`): テンプレート追従。SDK メジャー移行は React Native 系定数 (RN 0.85 系) と整合させて別途実施
- **plugin-verifier 1.403**: 意図的 pin (2026.2 EAP layout 非対応、product-requirements.md 記録済み)。1.404+ の対応状況は未確認 — 月次 verify で追う

## 3. dependabot の死角 (一次調査①で特定、運用メモ)

自動追跡されない依存源: (a) **TemplateVersions.kt の npm 定数** (ユーザー生成物に直結 — 今回の Next.js のように手動監査が必要)、(b) CI インラインの `npm install -g` 群 (mermaid-cli 11.14.0 / rescript 12.2.0 / pa11y-ci 4.1.0)、(c) build.gradle.kts 直書きの remote-robot 0.11.23 / verifier 1.403、(d) `sphinx-docs/package.json` (npm ecosystem 未登録)、(e) テンプレートの Docker イメージタグ (postgres:18-alpine / mysql:8.4)。
→ **(a) は定期的な手動監査をリリースフローに組み込む価値あり** (例: リリース前チェックに「TemplateVersions の CVE 照合」を追加)。

## 4. コードセキュリティ (opus 監査、Fable スポットチェック済み)

CLAUDE.md のセキュリティ規約は遵守されている:

- **プロセス実行 (10 箇所)**: 全件 `ProcessBuilder`/`GeneralCommandLine` の明示的引数リスト。シェル文字列連結・`bash -c` なし
- **LSP 由来パス (3 箇所)**: `openCompiled` / `createInterface` / rename WorkspaceEdit のすべてで `RescriptSecurityUtils.isWithinProject` 検証 (スポットチェックで実在確認)
- **パストラバーサル**: package 名は `isValidPackageName` で `..` 拒否、settings の packageRoots は `startsWith(baseAbs)` 検証、`TemplateResourceLoader` のパスはハードコードのみ
- **JSON パース**: 全箇所例外安全。シークレットのコミットなし。`robot.sh` は loopback 限定の開発ツールで問題なし

許容済みの懸念 (対応不要と判断):
1. `RescriptDumpLspStateAction` が通知に絶対パスを表示 — ユーザー起動の診断アクションで意図的
2. rescript.json / .resnb 等のパースにサイズ上限なし — project-local の信頼済みファイル
3. wizard の projectName が生成 package.json 等にエスケープなしで入る — パスには流れずローカル生成物のみ

## 残存する不確実性

- IntelliJ Platform バンドルの lsp4j バージョンと CVE 状況は公開インベントリから確認できず (Platform 側管理)
- ktlint-gradle 14.2.0 / remote-robot 0.11.23 の最新版照合は Web 情報が不確定 (dependabot の gradle 監視でカバーされる範囲)
- Next.js 以外のテンプレート npm 依存の CVE 照合は主要 ~15 件のみ実施 (全 ~60 定数の網羅ではない)
