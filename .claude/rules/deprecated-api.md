---
globs: ["src/main/**/*.kt", "src/main/**/*.java"]
---

# Deprecated API 利用ルール

**以下は強制的な行動指示であり、例外なく従うこと。**

新規実装で **`@Deprecated`** が付与された API や JetBrains の "scheduled for removal" API を参照してはならない。Marketplace の Plugin Verifier レポートに警告が残り続けるため、新規コードでは必ず代替 API を使うこと。

## コード追加・変更時の必須手順

新しく import するクラス・メソッドや、`override fun` で実装するインタフェースメンバに対して以下を確認する:

1. **IDE 補完で打ち消し線になっていないか** — IntelliJ が deprecated を表示する。
2. **KDoc / Javadoc に `@deprecated` や "scheduled for removal" が含まれていないか** — Go to Declaration (Cmd+B) で確認する。
3. **代替 API が示されているか** — 「Use X instead」と書かれていれば即そちらを使う。
4. 代替 API が存在しない場合のみ、以下の抑制手順に従う。

## 抑制手順（代替 API が存在しない場合のみ）

やむを得ず deprecated API を使う場合、**すべて** を満たすこと:

1. 使用箇所に `@Suppress("DEPRECATION")`（または `"OVERRIDE_DEPRECATION"`）を付け、**1 行コメントで理由** を明記する。
2. Plugin Verifier が bytecode レベルで検出するため、`plugin-verifier-ignored-problems.txt` にエントリを追加する。
3. エントリには以下を含める:
   - API 名と使用理由（代替 API なし、LSP4J transitive、etc.）
   - 対象ソースファイル名
   - `Status: KEEP` と `Reviewed: YYYY-MM-DD`
   - `Expires: YYYY-MM-DD`（推奨: Reviewed の 12 ヶ月後）。月次 verifyPlugin ワークフローが期限切れエントリを Step Summary に警告として表示する
4. 実装の PR 本文で「deprecated API を意図的に使用した理由」を説明する。

## IntelliJ Platform バージョンアップ時

`platformVersion` を更新したら必ず以下を実行し、新たに deprecated になった API を棚卸しする:

```bash
./gradlew verifyPlugin
```

レポート (`build/reports/pluginVerifier/*/plugins/com.rescript.plugin/*/deprecated-usages.txt`) を確認し、新規 deprecated 利用箇所があれば **そのバージョンアップ PR に修正を含める**。後回しにしない。

## よくある代替 API

| 非推奨 | 代替 |
|-------|------|
| `ReadAction.compute(ThrowableComputable)` | `ApplicationManager.getApplication().runReadAction<T> { ... }` |
| `StubBasedPsiElementBase.getElementType()` | `node.elementType` (ASTNode 経由) |
| `FloatingToolbarProvider.getPriority()` | `plugin.xml` の `order=` 属性 |
| `ToolWindowFactory.isApplicable(Project)` | `shouldBeAvailable(Project)` |

## Kotlin コンパイラ設定

`build.gradle.kts` の `-jvm-default=no-compatibility` は DefaultImpls ブリッジメソッドの生成を抑え、Java インターフェースの deprecated default method への bytecode 参照を防ぐ。**この設定は外してはならない**。外すとブリッジ経由で deprecated 警告が再発生する。

## コミット前の検証

**以下は強制的な行動指示であり、例外なく従うこと。**

Kotlin / Java の本体コード (`src/main/`) を変更した場合、コミット前に以下を確認する:

1. 新規 import 行に deprecated クラスが含まれていないか
2. 新規 `override` が deprecated メンバでないか
3. deprecated を使用した場合、`@Suppress` コメントと ignored-problems エントリの両方が揃っているか

上記が満たせていない場合、**コミットしない**。
