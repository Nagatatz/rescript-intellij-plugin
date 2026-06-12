# CI 復旧対応 (2026-06-12〜13、goal 駆動)

main の CI が 3 push 連続で failure。原因 2 系統と対処:

## 1. CI ワークフロー: テンプレート定数テストの失敗

- 症状: `MonorepoTemplateFilesTest` 等 5 件が「定数をシンボル参照しているのに不一致」で fail
- 原因: `TemplateVersions` の `const val` はコンパイル時に参照側へインライン化される。CI の Gradle リモートビルドキャッシュが `compileTestKotlin FROM-CACHE` (ログで確認) で**旧定数値入りのテストクラス**を配布し、新しい main クラスと不一致になる。ローカルで遭遇した stale インライン問題 (memory 記録済み) の CI 版
- 対処: `const val` → `val` 化 (~60 定数)。インライン化が消え、参照は実行時解決になるため再発不能。const→val は ABI 変更なので既存の stale キャッシュも自然に無効化される
- 注: 採番 005 は goal モード (確認なし) のため AskUserQuestion を省略した

## 2. Docs ワークフロー: Link check タイムアウト

- 症状: `https://v2.tauri.app/security/csp/` が CI ランナーから timeout (60s×3)。ローカル curl は 200
- 対処: `sphinx-docs/conf.py` の `linkcheck_ignore` に `v2.tauri.app` を追加 (理由コメント付き)
- follow-up (警告のみ・本対応では見送り、.po 同期が必要なため): 恒久リダイレクト 4 件の URL 更新 — docs.anthropic.com/claude-code → code.claude.com/docs (dev/claude-code.md:9)、rescript-association/reanalyze → rescript-lang/reanalyze (user/features/code-analysis.md:89)、nodejs.org/api/corepack.html → github.com/nodejs/corepack#readme (user/features/advanced.md:582)、vite.plus → viteplus.dev (advanced.md:591 / templates/index.md:58 / templates/vite-react.md:232)

## 解決確認 (2026-06-13)

- 修正 push (a26856c5) 後も CI は failure — 両 job で `compileTestKotlin FROM-CACHE` が継続し、復元された Gradle home 内のビルドキャッシュが stale テストクラスを供給し続けていた (失敗 5 テストはすべて直近バンプ定数 NEXTJS/EXPO/CONCURRENTLY の比較で完全一致 — 汚染の確証)
- `gh cache delete --all` で Actions キャッシュ 27 件を全削除 → `gh run rerun --failed` → **CI success**
- 最終状態: CI / Docs / CodeQL すべて success (commit a26856c5)
- 再発防止: const→val 化によりインライン汚染は構造的に発生しない。Docs 側は linkcheck_ignore で恒久化
