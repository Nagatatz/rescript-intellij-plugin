# タスクリスト: 監査残アクション対応

## セクション 1: pitest バンプ

- [x] `libs.versions.toml` pitest 1.20.4 → 1.25.4
- [x] `./gradlew pitest` 実行で互換検証 — 1.25.4 と 1.20.4 (stash でベースライン比較) の両方で「No mutations found」= **バンプによる退行なし**。ただし PIT が以前から no-op だったことを発見 (failWhenNoMutations=false で green に見えていた)。**別課題として記録** — 原因調査 (instrumentCode との classes ディレクトリ不整合疑い) は本 steering のスコープ外
- [x] コミット: `⬆ Bump pitest to 1.25.4`

## セクション 2: Expo SDK 56

- [x] `TemplateVersions.kt` EXPO → `^56.0.11` (REACT_NATIVE 等は据え置き = SDK 56 同梱の RN 0.85 と整合)
- [x] `compileTestKotlin --rerun` → golden 再生成 → 差分は REACT_NATIVE の 3 件のみ
- [x] wizard テスト green
- [x] コミット: `⬆ Move the Expo template to SDK 56`

## セクション 3: TemplateVersions の CVE 照合自動化

- [x] `.github/scripts/audit-template-versions.mjs` 新規 — マッピングは手書きせず **Kotlin ソースの `"pkg" to TemplateVersions.X` パターンから抽出** (74 パッケージ)。小文字限定の正規表現でテンプレート変数キー (htmxVersion 等) を除外、抽出数 <30 で fail する自己診断付き
- [x] ローカル実行で検証 — **即座に critical 2 件 (concurrently 9.x → shell-quote GHSA-w7jw-789q-3m8p) を検出**し、CONCURRENTLY を ^10.0.3 にバンプして解消 (golden 26 件再生成、🐛 コミット)。残る moderate 21 件は最新ピンの transitive 依存で上流待ち (audit-level=high のため fail しない)
- [x] `monthly-verify.yml` に template-versions-audit ジョブ追加 (--legacy-peer-deps で lock 生成 — 全テンプレート混載の peer 衝突回避。actionlint OK)。生成物ディレクトリを .gitignore に追加
- [x] CLAUDE.md の CI 表 (Monthly Verify 行) と `.claude/rules/release.md` の前提条件に反映 (ローカル再現コマンド付き)
- [x] コミット: `🔧 Audit template npm versions monthly via a generated manifest`

## マージ前検証

- [x] `./gradlew ktlintCheck test --rerun` green
- [x] tasklist 全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認 → main マージ → push

## テスト免除の記載

- audit スクリプト (.mjs): Kotlin テスト規約の対象外 (CI 補助スクリプト)。ローカル実行 + CI での実走で担保
