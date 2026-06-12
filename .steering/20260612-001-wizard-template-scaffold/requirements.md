# 要求内容: wizard テンプレートの scaffold 化 (完全リファクタリング Phase 5)

## 背景

完全リファクタリング計画の最終フェーズ。wizard/templates/ の 22 個の `*TemplateFiles` クラス (~4,680 行) は、静的内容こそ `TemplateResourceLoader` + リソース 339 ファイルに外出し済みだが、各クラスが「common tail 7 ファイルの列挙」「リソース load の 1 ファイル 5〜8 行の定型」「zod/sury 依存切替」を個別に繰り返している。

## go/no-go 調査結果 (2026-06-12、sonnet ×3 並列調査 + Fable 判定、ユーザー承認済み)

- **エントリポイント**: `generate(ctx): Map<String, String>` + 後方互換 overload — **22/22 同形**
- **common tail** (`.nvmrc` / `LICENSE` / `dependabot.yml` / `README.md` / `.gitignore` / `.editorconfig` / `ci.yml`): **22/22** が CommonFiles 経由で同順列挙 (引数差: gitignore extras / ci フラグ)
- **rescript.json + package.json**: 21/22 が ProjectFileBuilders (res-x のみリソース load)
- **Validation variants** (`variantKey()` → `<root>/variants/<key>/...`): 18/22 で同一パターン (modern 4 種は意図的に非対応)
- **zod/sury 依存切替** (`xxxDependencies(ctx)`): ~18 クラスが同形のプライベート関数を複製
- **動的ロジック** (manifest 化に不向き): hono/hono-graphql/hono-inertia/monorepo/full-stack の DB 分岐・apiStrategy 分岐・PM workspace ヘルパ、tauri の Rust 連携、hono の条件付き ZodOpenapi.res

**判定: go (scaffold 形式)** — 標準フレームは 17/22 で同形 (ゲート 15 以上を充足)。純データ manifest は動的 5 クラスの表現で劣化 DSL 化するため、Kotlin 宣言形の scaffold ビルダー + 共通ヘルパとし、動的部分は各クラスに残す。

## 要求

1. **キャラクタリゼーションテストの先行整備**: 22 テンプレート × 分岐網羅 ctx (PM / validation / database / apiStrategy) の生成結果を「ファイル名一覧 + 内容ハッシュ」の golden ファイルに固定し、移行の回帰検出器とする。移行完了後も軽量回帰網として維持する
2. **scaffold 基盤の新設** (`wizard/templates/TemplateScaffold.kt` 想定):
   - common tail 7 ファイルを一括生成する scaffold エントリ
   - 「ターゲットキー = リソースパス」型の一括リソース load ヘルパ
   - Validation variant 選択ヘルパ
   - zod/sury 依存切替の共通化 (`standardDependencies`)
3. **22 クラスの段階移行** (4 バッチ、各バッチ独立コミット)。**生成される Map の内容は 1 byte も変えない** (golden hash 不変が受け入れ条件)
4. ロードマップに #131 を追補 → 🚧 → 完了時に削除

## 実施体制 (ユーザー指示)

Fable が大枠 (設計・バッチ分割・レビュー・検証) を担当し、調査・バッチ移行の実作業は opus / sonnet subagent に委譲する。各バッチ完了時に Fable が diff レビュー + golden テスト実行で検収する。

## 受け入れ条件

- [ ] golden テストが移行前に整備され、全バッチで **golden hash 不変** (= 生成内容のバイト等価)
- [ ] 22 クラスすべてが scaffold を利用し、重複の common tail 列挙・リソース load 定型・依存切替複製が解消されている
- [ ] wizard/templates の純減 ~800 行以上 → **未達 (実測 +39 行: 既存 22 クラス −111、scaffold 本体 +150)**。見積り誤りの分析と価値の再評価は「実装結果の評価 (2026-06-12 追記)」を参照
- [ ] 新基盤に KDoc + ユニットテスト (golden テストとは別に scaffold 単体の検証)
- [ ] `./gradlew ktlintCheck clean buildPlugin test koverVerify verifyPluginStructure` green
- [ ] runIde スモーク: New Project ウィザードから代表 2〜3 テンプレートを実生成し、生成物が従来どおりであること
- [ ] docs 同期 (repository-structure.md の wizard/templates 行、#131 削除)。sphinx 更新なし (生成物不変のため)

## スコープ外

- 生成内容の変更・改善 (バイト等価が原則。気付いた改善候補は記録のみ)
- 静的リソース (resources/templates/) の再編成
- ProjectTemplate enum / Wizard UI の変更

## 実装結果の評価 (2026-06-12 追記)

行数削減目標は未達。原因は go/no-go 調査時の見積り誤り — 静的内容のリソース外出しと CommonFiles 集約が過去に完了しており、残存定型は各クラス十数行 (tail 7 行 + load 数行 + when 4 行) しかなかった。バイト等価制約と ktlint の 120 桁改行がさらに削減を圧縮した。

実際に得られた価値:
- golden キャラクタリゼーションテスト 74 combo — テンプレート出力の恒久回帰網 (従来は部分文字列 assert のみ)
- 単一情報源化: common tail 22 箇所 → 1、validation 依存切替 18 箇所 → 1、variant パス解決 18 箇所 → 1。第 8 の tail ファイル追加や第 3 の validation ライブラリ追加が 1 ファイル編集になる

マージ可否はこの再評価を明示してユーザーに確認する。
