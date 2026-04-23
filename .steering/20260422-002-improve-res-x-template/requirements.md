# requirements.md — res-x テンプレートの堅牢化

## 背景

2026-04-22 に Project Wizard の 16 番目のテンプレートとしてマージした **res-x (HTMX on Bun)** について、事後レビューで次の 2 カテゴリの問題が判明した。

1. **ReScript コンパイル失敗級の correctness バグ**（3 件）: テンプレート生成直後の `pnpm exec rescript` が失敗する可能性がある
2. **Bun 前提条件が README / CI / dev スクリプトに現れていない UX 問題**（3 件）: 生成された雛形を Wizard ユーザーがそのまま `pnpm install && pnpm dev` で起動できない

いずれも `./gradlew integrationTest` を本テンプレートに対して通すことで機械的に surface できるため、まずは integrationTest 統合を行い、検出されるバグを (A) で修正し、さらに UX 改善 (B) を重ねる。

## スコープ

### 対象

1. **(ウ) integrationTest 統合**
   - `.github/workflows/integration-tests.yml` に `oven-sh/setup-bun` セットアップステップを追加
   - `TemplateIntegrationTest.kt` は `@EnumSource(ProjectTemplate::class)` で既に RES_X を巡回しているため追加変更不要
   - ローカルで `./gradlew integrationTest --tests "*TemplateIntegrationTest*"` を走らせ、RES_X で失敗するエラーを列挙する

2. **(A) correctness バグ修正**
   - TodoForm.res: `let onSubmit = ... and renderFormWithError = ...` の非 λ RHS 相互参照（`let … and …` 構文は `rec` 必須かつ非 λ RHS 不可）を解消
   - Layout.res: `<main> children </main>` を `<main>{children}</main>` に修正（JSX 変数補間には波括弧が必須）
   - variants/sury/src/Validation.res: `%raw(\`{name: trimmedName, description: trimmedDescription}\`)` はテンプレート文字列ではなくただの文字列リテラルのため変数が展開されない。ReScript オブジェクトリテラルで置き換え
   - vite.config.js: `clientDirs: ["client"]` が実在しないディレクトリを指すため、`clientDirs: []` に変更

3. **(B) Bun 前提条件の UX 改善**
   - `CommonFiles.readme(...)` に `extraPrerequisites: List<String> = emptyList()` を追加し、README の Prerequisites セクションに Bun の注記を載せられるようにする
   - `CommonFiles.ciWorkflow(...)` に `setupBun: Boolean = false` を追加し、true のとき `uses: oven-sh/setup-bun@v1` ステップを挿入する
   - res-x の `package.json` の `dev` スクリプトを `concurrently "rescript -w" "bun --watch run src/App.res.mjs"` に変更し、ReScript watcher と Bun watcher を並走させる。`concurrently` は devDependencies に追加（`TemplateVersions.CONCURRENTLY` を参照）
   - ResXTemplateFiles から新しい引数を利用するよう呼び出し側を更新

### スコープ外

- res-x テンプレートの機能追加（新しいコンポーネント、新しいエンドポイント等）
- 他テンプレート（Hono, Vite+React, Next.js 等）の変更。ただし `CommonFiles` の共通化変更は他テンプレートへの影響が 0 になるよう「デフォルト値付きオプショナル引数」で追加する
- sphinx-docs / README.md / CLAUDE.md の機能紹介文の変更（既存テンプレートのバグ修正 + UX 整備であり、機能の追加ではない）
- res-x v1.5+ への追従やバインディング追加
- Docker / devcontainer 配布

## 受け入れ条件

- [ ] `.github/workflows/integration-tests.yml` に `oven-sh/setup-bun@v2`（またはバージョン同等）ステップが追加され、Bun が CI runner で利用可能な状態になっている
- [ ] `./gradlew integrationTest --tests "*TemplateIntegrationTest*"` が RES_X を含む全パラメータで成功する（ローカル、Bun 導入済み環境で実測）
- [ ] `TodoForm.res` が `let onSubmit` と `renderFormWithError` の非正規 `let … and …` を使用していない。検証エラー時も ReScript がコンパイルできる構造になっている
- [ ] `Layout.res` が `<main>{children}</main>` の形で `children` を JSX 式として埋め込んでいる
- [ ] `variants/sury/src/Validation.res` が `%raw` テンプレート文字列誤用を排除し、ReScript オブジェクトリテラルで payload を構築している
- [ ] `vite.config.js` の `clientDirs` が空配列（またはテンプレートで生成される実在ディレクトリのみ）になっている
- [ ] `CommonFiles.readme` が新しい `extraPrerequisites` 引数を受け取り、指定時に Prerequisites セクションに箇条書きで追記する
- [ ] `CommonFiles.ciWorkflow` が新しい `setupBun` 引数を受け取り、`true` のとき `oven-sh/setup-bun@v1` ステップを YAML に出力する
- [ ] ResXTemplateFiles が上記 2 引数を利用し、README に Bun 行、CI YAML に setup-bun、package.json に concurrently を記載している
- [ ] `CommonFilesTest.kt` と `ResXTemplateFilesTest.kt` に対応するユニットテストが追加・更新されている
- [ ] `./gradlew ktlintCheck clean buildPlugin test koverHtmlReport verifyPluginStructure` が全て成功する
- [ ] 既存の 17 テンプレート（res-x 以外）の generate 出力と既存テストの結果に変化がない（デフォルト値のオプショナル引数追加であり、既存呼び出しは no-op）
- [ ] `tasklist.md` の全タスクが `[x]` になっている
