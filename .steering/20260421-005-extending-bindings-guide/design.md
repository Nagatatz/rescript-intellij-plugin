# 設計: Extending Bindings ガイドの追加

## 変更対象

1. **新規:** `src/main/resources/templates/common/readme/extending-bindings.md`
2. **変更:** `src/main/kotlin/com/rescript/plugin/wizard/templates/CommonFiles.kt` (`readme()` 関数本体)
3. **変更:** `src/test/kotlin/com/rescript/plugin/wizard/templates/CommonFilesTest.kt` (アサーション追加)
4. **変更:** `src/test/kotlin/com/rescript/plugin/wizard/ProjectTemplateTest.kt` (アサーション追加)

## 配置方針

- 共通 markdown ファイルを単一ソースとし、全テンプレートで同一内容を使う
- `src/main/resources/templates/common/readme/` という新規サブディレクトリを作成。既存の `TemplateResourceLoader.load("common/readme/extending-bindings.md")` が `templates/common/readme/extending-bindings.md` にマップされるため、ローダ本体の変更は不要
- プレースホルダ (`{{key}}`) は使わない。15 テンプレートで同一文言を提供する
- `CommonFiles.readme()` が既存の `extraSections` ループ終了後・`## Learn More` 追加前に固定で `## Extending Bindings` 見出しと本文を `appendLine` する

## markdown コンテンツ構成

見出し階層:

```
## Extending Bindings   (CommonFiles.readme が挿入)
(intro prose)
### Binding attributes at a glance
(table)
### Pattern: typed fetch wrapper (frontend)
(rescript code)
### Pattern: adding a Hono middleware (backend)
(rescript code)
### Pattern: filtering with drizzle-orm
(rescript code)
### Community binding packages
(bulleted list with stability notes)
### Further reading
(bulleted list of URLs)
```

- コード例は `rescript` および `bash` 言語指定のコードフェンス
- `@rescript/webapi` の行に `⚠ experimental` バッジ相当の注記を含める
- スタイルは `hono/readme/database.md` など既存ファイルに倣う (短いプロース + 具体例)

## Kotlin 実装

`CommonFiles.readme()` 内、`extraSections.forEach { ... }` ブロック直後、`## Learn More` の前に以下を追加:

```kotlin
appendLine("## Extending Bindings")
appendLine()
appendLine(TemplateResourceLoader.load("common/readme/extending-bindings.md").trimEnd())
appendLine()
```

- 関数シグネチャは変更しない
- 15 テンプレートの `*TemplateFiles.kt` は変更不要 (全て `CommonFiles.readme` 経由)
- `TemplateResourceLoader.load` はクラスパスから読み込むため、リソースが `buildPlugin` で jar に含まれることを確認する必要がある

## テスト戦略

### CommonFilesTest.kt

既存テストのパターンに倣い、以下を追加:

```kotlin
@Test
fun `readme appends Extending Bindings section with recipes`() {
    val readme = CommonFiles.readme(
        ctx = pnpmCtx,
        description = "x",
        scripts = emptyList(),
    )
    assertTrue(readme.contains("## Extending Bindings"))
    assertTrue(readme.contains("### Pattern: typed fetch wrapper"))
    assertTrue(readme.contains("### Pattern: adding a Hono middleware"))
    assertTrue(readme.contains("### Pattern: filtering with drizzle-orm"))
    assertTrue(readme.contains("@rescript/webapi"))
}
```

### ProjectTemplateTest.kt

全 15 テンプレートを走査し README が当該セクションを含むことを検証:

```kotlin
@Test
fun `every template README contains Extending Bindings section`() {
    for (template in ProjectTemplate.entries) {
        val files = template.generateFiles("demo")
        val readme = files["README.md"]
        assertTrue(readme != null && readme.contains("## Extending Bindings"),
            "${template.name} README should include Extending Bindings section")
    }
}
```

## リスク

- `TemplateResourceLoader.load` が新規 `common/` サブディレクトリから正しく読み込めるかは既存ロジック (`templates/$path` プレフィックス付与) に依存。ビルド後にリソース jar で確認する
- markdown が長すぎると `readme` 文字列全体が膨張する。70 行以内に抑える
- コミュニティパッケージのリンクは外部サイトに依存するため、URL 切れの可能性あり。公式 npm / GitHub の canonical URL を使う

## ドキュメント影響

- `CLAUDE.md` / `README.md` / `docs/` / `sphinx-docs/` — 変更なし (テンプレート出力であり、プラグイン機能ではない)
- `docs/product-requirements.md` — ロードマップ影響なし
