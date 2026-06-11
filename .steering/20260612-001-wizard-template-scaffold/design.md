# 設計: wizard テンプレートの scaffold 化 (Phase 5)

## セクション 1: キャラクタリゼーション (golden) テスト

### 仕組み

- `src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateGoldenTest.kt`
- 各 (template, ctx) 組について `ProjectTemplate.generateFiles(ctx)` を呼び、`fileKey -> SHA-256(content)` のソート済み一覧を生成
- golden は `src/test/testData/wizard-golden/<template>-<comboKey>.txt` (1 行 = `hash  fileKey`) にコミット
- 比較は完全一致。差分時は「再生成手順」(システムプロパティ `-Pwizard.golden.update=true` 等で golden を書き直すモード) をテスト失敗メッセージに表示
- `year` はハッシュを揺らすため ctx に固定値 (例: 2026) を渡す

### ctx マトリクス (分岐網羅、過剰組合せはしない)

- 全 22: `PNPM+ZOD` (default) と `BUN+ZOD` (PM 分岐)
- `supportsValidationSelection` な 18: `PNPM+SURY`
- DB 対応 5 (hono / hono-graphql / hono-inertia / full-stack / monorepo): `POSTGRES` と `MYSQL` (LIBSQL は default で被覆)
- full-stack: `apiStrategy=GRAPHQL`
- monorepo: `NPM` (workspace 分岐) を追加
- 概算 22×2 + 18 + 5×2 + 1 + 1 = ~74 combo

### 委譲

- ハーネス設計 (この節) は Fable。実装は **opus subagent** に委譲し、Fable が実行・検収

## セクション 2: scaffold 基盤

`wizard/templates/TemplateScaffold.kt` (internal object) — Fable が実装 (基盤は要の設計なので委譲しない):

```kotlin
internal object TemplateScaffold {
    /** common tail 7 ファイル (.nvmrc / LICENSE / dependabot / README / .gitignore / .editorconfig / ci.yml) を一括生成 */
    fun commonTail(
        ctx: TemplateContext,
        readme: String,                       // CommonFiles.readme(...) の結果を受ける (構築は呼び出し側)
        gitignoreExtra: List<String> = emptyList(),
        ciHasBuild: Boolean = false,
        ciHasTest: Boolean = false,
        ciSetupBun: Boolean = false,
    ): Map<String, String>

    /** ターゲットキー == リソース相対パスの一括 load (支配的パターンを 1 行/ファイル化) */
    fun resourceFiles(
        resourceRoot: String,
        keys: List<String>,
        vars: Map<String, String> = emptyMap(),
    ): Map<String, String>

    /** <root>/variants/<variantKey()>/<path> から Validation.res 等を引く */
    fun validationVariant(
        ctx: TemplateContext,
        resourceRoot: String,
        targetPath: String = "src/Validation.res",
        sourcePath: String = targetPath,
    ): Pair<String, String>

    /** zod/sury 切替付きの dependencies 組み立て (~18 クラスの xxxDependencies を置換) */
    fun standardDependencies(
        ctx: TemplateContext,
        base: Map<String, String>,
        zodVersion: String = TemplateVersions.ZOD,
        suryVersion: String = TemplateVersions.SURY,
    ): Map<String, String>
}
```

- 注意: `commonTail` の挿入順は既存の linkedMapOf 順 (`.nvmrc`→`LICENSE`→`dependabot`→`README`→`.gitignore`→`.editorconfig`→`ci.yml`) を保持… **ただし既存クラス間で順序が微妙に違う場合がある** (例: ReactNativeCli は README→.gitignore→.editorconfig→.nvmrc...)。Map の iteration 順はファイル書き出し順に影響しうるため、golden は「ソート済み一覧」で比較し (順序非依存)、**書き出し順への依存が呼び出し側にないことを実装時に確認** (ProjectTemplate の利用箇所を確認し、依存があれば commonTail に順序パラメータを足す)
- `TemplateVersions` に ZOD/SURY 定数がない場合は各クラスの実値を調査して合わせる (バイト等価優先)
- 単体テスト `TemplateScaffoldTest.kt` (キー集合・variant パス・依存切替)

## セクション 3〜6: バッチ移行 (subagent 委譲)

各バッチ = 1 コミット。**sonnet subagent** に「対象クラスを scaffold 利用に書き換え、golden テスト green を確認してから報告」を委譲し、Fable が diff レビュー + 再実行で検収。バッチ内のクラスは互いに独立ファイルのため 1 agent が直列で安全に処理する。

- **バッチ A (単純 6)**: basic, vite-react, electron, cli-tool, npm-library, react-native
- **バッチ B (サーバー系 5)**: nextjs, react-native-cli, aws-lambda, cloudflare-workers, google-cloud-run
- **バッチ C (modern + 特殊 6)**: tanstack-start, remix-v7, astro, waku, res-x, tauri
- **バッチ D (動的 5)**: hono, hono-graphql, hono-inertia, monorepo, full-stack — scaffold は標準フレーム部分にのみ適用し、DB 分岐 / apiStrategy 分岐 / PM ヘルパは現状の Kotlin のまま残す

### 移行ルール (subagent プロンプトに明記)

1. 生成される Map の **キー集合と各値はバイト等価** を維持 (golden テストが審判)
2. 既存のクラス固有テスト (`*TemplateFilesTest`) は無変更で green
3. 後方互換 overload `generate(projectName)` は維持
4. KDoc 必須、ktlint 通過
5. 判断に迷う差異 (バイト等価にできない等) は変更せずに報告へ記載

## セクション 7: docs + DoD + スモーク

- repository-structure.md: wizard/templates/ 行に `TemplateScaffold` 追記
- product-requirements.md: #131 削除 (セクション 0 で追補した行)
- DoD フルチェーン + `test --rerun` + koverHtmlReport (wizard は kover 除外パッケージだが golden テストは test で走る)
- runIde スモーク: New Project から Basic / Hono / Tauri を実生成して起動確認

## リスクと緩和

| リスク | 緩和策 |
|---|---|
| 移行でテンプレート出力が微妙に変わる (改行・順序) | golden hash テストを先行整備し全バッチで不変を強制。順序非依存比較 + 書き出し順依存の事前確認 |
| subagent が「等価にできない」差異を勝手に吸収する | 移行ルール 5 で禁止し、バッチごとに Fable が diff レビュー |
| golden の組合せ漏れ (分岐を踏まない combo) | ctx マトリクスを分岐調査 (セクション 0) に基づき設計。カバレッジは golden テスト実行時の分岐網羅で副次確認 |
| year 等の非決定値で golden が揺れる | ctx に固定 year を注入。他に非決定値がないことを 2 回連続実行の一致で確認 |
