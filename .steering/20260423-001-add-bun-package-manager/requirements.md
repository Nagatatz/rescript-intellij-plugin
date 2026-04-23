# requirements.md — `PackageManager.BUN` の追加

## 背景

現行の `PackageManager` enum は NPM / PNPM / YARN の 3 値。res-x テンプレートは Bun ランタイム前提だが Wizard で Bun を PM として選択できないため、scripts に `bun` をハードコードしつつ install/readme だけは PNPM などで切り替えるという歪な状態。`20260422-003-res-x-followup-polish` のレビューで Bun を正式な PM 選択肢に昇格する案を独立 PR 候補として保留していた。

## スコープ

### 対象

- `PackageManager` enum に `BUN("bun")` を 4 つ目として追加し、既存 3 値と同等の扱いにする
- `TemplateContext` の 5 メソッド（`installCmd` / `runCmd` / `execCmd` / `lockfileName` / `packageManagerSpec`）に BUN 分岐を追加する
- `CommonFiles.packageManagerName` に `BUN -> "Bun"` を追加し、BUN のとき Prerequisites の文言を「Corepack 経由」ではなく「install from https://bun.sh」相当に切り替える
- `CommonFiles.ciWorkflow` が `ctx.packageManager == BUN` のとき自動的に `oven-sh/setup-bun@v2` を出力する（`setupBun` 明示フラグは維持、論理和で統合）
- `MonorepoTemplateFiles` の workspace ヘルパー（4 メソッド）に BUN 分岐を追加
- `GoogleCloudRunTemplateFiles.dockerfile()` に BUN 用ベースイメージ `oven/bun:1-slim` と `bun install` の分岐を追加
- `TemplateResourcesSmokeTest` の placeholder 残留テストに BUN コンテキストを追加

### スコープ外

- Wizard UI (`RescriptProjectWizardStep`) の変更 — `JComboBox(PackageManager.entries.toTypedArray())` が自動反映するため触らない
- `RescriptModuleBuilder` のデフォルト PM 変更 — PNPM のまま維持
- res-x テンプレートの scripts 修正 — `bun` はランタイム呼び出しであり PM 抽象化対象ではない
- `TemplateIntegrationTest` の BUN マトリクス化 — Bun + pnpm 共存問題や rescript-bun の未知数のため、BUN 検証は手動に留める
- Bun v1.0 の `bun.lockb`（バイナリ）対応 — v1.2+ の `bun.lock`（テキスト）のみ前提とする

## 受け入れ条件

- [ ] `PackageManager` enum が 4 値 (NPM/PNPM/YARN/BUN) になる
- [ ] `TemplateContext.packageManagerSpec()` が BUN で `"bun@${TemplateVersions.BUN}"` を返す
- [ ] `TemplateContext.installCmd()` が BUN で `"bun install"` を返す
- [ ] `TemplateContext.runCmd("dev")` が BUN で `"bun run dev"` を返す
- [ ] `TemplateContext.execCmd("rescript")` が BUN で `"bunx rescript"` を返す
- [ ] `TemplateContext.lockfileName()` が BUN で `"bun.lock"` を返す
- [ ] `CommonFiles.packageManagerName()` が BUN で `"Bun"` を返し、README Prerequisites が Bun 向け文言を出す
- [ ] `CommonFiles.ciWorkflow(ctx, …)` が `ctx.packageManager == BUN` のとき `oven-sh/setup-bun@v2` ステップを自動出力する
- [ ] BUN を選んだ monorepo 生成で workspace 関連 scripts が `bun --filter` / `workspace:*` を使う
- [ ] BUN を選んだ Google Cloud Run 生成で Dockerfile が `FROM oven/bun:1-slim` と `bun install --production` を使う
- [ ] `TemplateResourcesSmokeTest` が BUN コンテキストでも `{{placeholder}}` 残留ゼロを通過
- [ ] `./gradlew ktlintCheck clean buildPlugin test integrationTest verifyPluginStructure` すべて成功
- [ ] 既存 16 テンプレートの PNPM 系挙動に変化なし（既存テストが変わらず通る）
- [ ] `tasklist.md` の全項目が `[x]` になる
