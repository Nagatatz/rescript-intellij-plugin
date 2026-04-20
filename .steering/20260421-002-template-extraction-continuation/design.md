# Design — Wizard Template Resource Extraction (Continuation)

前回 steering `20260416-002-wizard-template-resource-extraction` の抽出パターンを
そのまま踏襲する。設計は既存の `TemplateResourceLoader` 基盤を使うため、新規
コンポーネントは不要。

## 抽出パターン

各テンプレートで以下を機械的に適用する:

### 1. Pre-snapshot 採取

各 `PackageManager` 値（NPM / PNPM / YARN）で `<Name>TemplateFiles.generate(ctx)` を呼び、
全ファイルを一時ディレクトリへ書き出す:

```
/tmp/tpl-snap-pre/<name>/NPM/
/tmp/tpl-snap-pre/<name>/PNPM/
/tmp/tpl-snap-pre/<name>/YARN/
```

※ 採取にはローカル実装の Kotlin テストを 1 つ書くか、既存の
`<Name>TemplateFilesTest` を参考に ad-hoc スクリプトを使う。

### 2. 抽出対象の選定

**抽出する:**
- `.res` / `.resi` ファイル
- `vite.config.mjs`, `rolldown.config.ts` 等の設定
- `index.html`, `app.json` 等の静的ファイル
- `src/__tests__/*.test.mjs`

**抽出しない（Kotlin 側で動的生成）:**
- `package.json`（`ProjectFileBuilders.packageJson` 呼び出し）
- `rescript.json`（`ProjectFileBuilders.rescriptJson` 呼び出し）
- `README.md`（`CommonFiles.readme` 呼び出し）
- `.gitignore`, `.editorconfig`, `.nvmrc`, `LICENSE`, `dependabot.yml`, CI
  ワークフロー（すべて `CommonFiles.*`）
- PM 依存のコンテンツ（`ctx.runCmd(...)`, `ctx.installCmd()` を呼ぶもの）

### 3. リソース配置

`src/main/resources/templates/<kebab-name>/` に抽出。ディレクトリ構造は
generate() の map キーに対応:

```
src/main/resources/templates/vite-react/
├── index.html                        # from "index.html"
├── vite.config.mjs
├── src/
│   ├── App.res
│   ├── Main.res
│   ├── Api.res
│   └── __tests__/App.test.mjs
```

### 4. プレースホルダ

動的な値は `{{key}}` で埋める。`TemplateResourceLoader.load(path, vars)` が
置換する。典型的な値:

| プレースホルダ | 値の供給源 |
|--------------|-----------|
| `{{projectName}}` | `ctx.projectName` |
| `{{cmdDev}}`, `{{cmdBuild}}` 等 | `ctx.runCmd(...)` |
| `{{installCmd}}` | `ctx.installCmd()` |

新規プレースホルダを追加した場合、`TemplateResourcesSmokeTest` の許容リストを
更新する。

### 5. `<Name>TemplateFiles.kt` の書き換え

```kotlin
internal object ViteReactTemplateFiles {
    fun generate(ctx: TemplateContext): Map<String, String> {
        val vars = mapOf(
            "projectName" to ctx.projectName,
            "cmdDev" to ctx.runCmd("dev"),
            // ...
        )
        return mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(/* ... */),
            "package.json" to ProjectFileBuilders.packageJson(/* ... */),
            "index.html" to TemplateResourceLoader.load("templates/vite-react/index.html", vars),
            "vite.config.mjs" to TemplateResourceLoader.load("templates/vite-react/vite.config.mjs"),
            "src/App.res" to TemplateResourceLoader.load("templates/vite-react/src/App.res"),
            // ...
            "README.md" to CommonFiles.readme(/* ... */),
            ".gitignore" to CommonFiles.gitignore(),
            // ...
        )
    }
}
```

### 6. Post-snapshot 採取 + diff

抽出後に再度 3 PM で snapshot を取り、`diff -r` で pre と比較。差分 0 が必須。

### 7. 既存テスト確認

各テンプレートには `<Name>TemplateFilesTest.kt` が存在する。**無修正で** pass すること
を確認。修正が必要なら抽出に問題がある。

## コミット単位

前回 steering と同じく、テンプレート 1 つ = コミット 1 つ。コミットメッセージは:

```
♻️ Extract static content from <Name>TemplateFiles to resources
```

コミット内容:
- 新規リソースファイル（`src/main/resources/templates/<name>/**`）
- `<Name>TemplateFiles.kt` の書き換え
- `TemplateResourcesSmokeTest.kt` の許容プレースホルダ更新（必要時のみ）

## 処理順

シンプル（設定ファイル少・.res ファイル少）から複雑へ:

1. Basic (186 行)
2. AwsLambda (179 行)
3. CloudflareWorkers (181 行)
4. GoogleCloudRun (204 行)
5. CliTool (233 行)
6. ViteReact (240 行)
7. ReactNative (245 行)
8. Electron (248 行)
9. Nextjs (260 行)
10. NpmLibrary (260 行)

## リスク管理

- 各コミットで snapshot diff が一致しない場合、そのコミットを破棄して原因調査。
  **一致するまで次のテンプレートへ進まない**。
- 既存 `<Name>TemplateFilesTest` を絶対に修正しない（テストは抽出の正当性の最重要
  検証手段）。
