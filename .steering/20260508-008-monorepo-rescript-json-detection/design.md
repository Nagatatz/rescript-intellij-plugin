# Design: モノレポ対応（rescript.json がサブディレクトリにある場合）

## アーキテクチャ概要

ワークスペース構造の検出を新しい `RescriptWorkspaceDiscovery` に集約する。各 caller（Inspection, Detector, ServerDescriptor, StatusWidget, StartupActivity 群）はこの単一エントリポイントを利用する。

```
Caller --> RescriptWorkspaceDiscovery.discover(project) --> RescriptWorkspaceLayout(packageRoots: List<Path>)
                          ↓
                  ┌───────┴────────┐
                  │  4 段の検出    │
                  │  ロジック      │
                  └───────┬────────┘
                          ↓
        ┌─────────────────┼─────────────────┐
        ↓                 ↓                 ↓
  manual override    workspace files   depth-limited scan
  (settings)         (pnpm/npm/yarn)   (depth ≤ 4)
                                              ↓
                                        parent walk
                                        (fallback)
```

## 検出ロジック

### Layer 1: 手動上書き

```kotlin
val settings = RescriptProjectSettings.getInstance(project)
if (settings.packageRoots.isNotEmpty()) {
    return settings.packageRoots
        .mapNotNull { resolveAgainstBase(it, basePath) }
        .filter { hasConfigFile(it) }
        .let { RescriptWorkspaceLayout(it) }
}
```

無効なパス（base 配下でない、ディレクトリでない、config ファイルがない）はサイレントに除外する（Settings 側のバリデータが警告表示を担当）。

### Layer 2: Workspace ファイル

`pnpm-workspace.yaml` を最初に探し、なければ `package.json` の `workspaces` フィールドを探す。

#### pnpm-workspace.yaml

最小限の行ベースパーサ:

```
packages:
  - "packages/*"
  - "examples/*"
  - "!packages/foo"   ← v1 ではサポートせず無視
```

実装方針: `packages:` 行を見つけたら、それ以降のインデントされた `- "..."` または `- '...'` または `- ...` 行を集める。次の非インデント行で打ち切り。クォートは strip。

#### package.json#workspaces

JSON パースに `org.json.JSONObject` を使う（IntelliJ Platform に同梱）。

```json
// 配列形式
{ "workspaces": ["packages/*", "apps/*"] }

// オブジェクト形式 (yarn classic)
{ "workspaces": { "packages": ["packages/*"] } }
```

両形式を吸収して `List<String>` を返す。

### Layer 3: Glob 展開

`packages/*` のような単純パターンを `Path` リストに展開:

```kotlin
class RescriptGlobExpander {
    fun expand(base: Path, glob: String): List<Path> {
        val parts = glob.split("/")
        return walk(base, parts, 0)
    }

    private fun walk(current: Path, parts: List<String>, idx: Int): List<Path> {
        if (idx == parts.size) return listOf(current)
        val part = parts[idx]
        return when (part) {
            "*" -> Files.list(current).use { stream ->
                stream.filter { Files.isDirectory(it) && !isExcluded(it) }
                    .flatMap { walk(it, parts, idx + 1).stream() }
                    .toList()
            }
            "**" -> walkRecursive(current, parts, idx + 1, depth = 0)
            else -> {
                val next = current.resolve(part)
                if (Files.isDirectory(next)) walk(next, parts, idx + 1) else emptyList()
            }
        }
    }
}
```

- 除外ディレクトリ: `node_modules`, `.git`, `build`, `lib`, `dist`, `.pnpm`, `.bs`, `target`, `out`
- セキュリティ: `Path.normalize()` 後に base 配下であることを再確認

### Layer 4: Depth-limited フォールバックスキャン

workspace ファイルがない、または上記で 0 件の場合に発動:

```kotlin
fun scanDepthLimited(base: Path, maxDepth: Int = 4): List<Path> {
    val roots = mutableListOf<Path>()
    Files.walkFileTree(base, EnumSet.noneOf(FileVisitOption::class.java), maxDepth, object : SimpleFileVisitor<Path>() {
        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
            if (dir != base && isExcluded(dir)) return FileVisitResult.SKIP_SUBTREE
            if (hasConfigFile(dir)) {
                roots.add(dir)
                return FileVisitResult.SKIP_SUBTREE  // 子の rescript.json は重複扱い
            }
            return FileVisitResult.CONTINUE
        }
    })
    return roots
}
```

depth limit はディレクトリ階層数。`base` を depth 0 とする。

### Layer 5: 親方向走査（最終フォールバック）

既存の `RescriptLspDetector.findInAncestors()` を継続して使用。Layer 1〜4 ですべて 0 件のときに、ユーザーがサブディレクトリを開いた可能性に備えて親を見る。

## API

### `RescriptWorkspaceLayout`

```kotlin
data class RescriptWorkspaceLayout(val packageRoots: List<Path>) {
    fun isRescriptProject(): Boolean = packageRoots.isNotEmpty()
    fun nodeModulesPaths(): List<Path> = packageRoots.map { it.resolve("node_modules") }
}
```

### `RescriptWorkspaceDiscovery`

```kotlin
object RescriptWorkspaceDiscovery {
    fun discover(project: Project): RescriptWorkspaceLayout
    fun discover(basePath: String?): RescriptWorkspaceLayout  // settings なしバリアント（互換用）
}
```

## Settings 拡張

```kotlin
@State(...)
class RescriptProjectSettings : PersistentStateComponent<RescriptProjectSettings.State> {
    class State {
        // ...既存フィールド...
        var packageRoots: MutableList<String> = mutableListOf()
    }
}
```

UI: `RescriptConfigurable` に "Project package roots (one per line, relative to project root)" の `JBTextArea` を追加。空のときは "Auto-detect" のラベルを表示。

`RescriptSettingsValidator` には `validatePackageRoots(project, paths): List<ValidationIssue>` を追加。`WARNING` レベルで:
- パスが存在しない
- ディレクトリでない
- `rescript.json` / `bsconfig.json` が含まれない

## LSP Server 探索の優先順位

```kotlin
fun findLanguageServer(): String? =
    findInDetectedPackageRoots()  // 新規: workspace 検出結果から
        ?: findInProjectNodeModules()  // 既存: project root 直下
        ?: findInParentNodeModules()   // 既存: 親方向
        ?: findOnPath()                // 既存: global PATH
```

`findInDetectedPackageRoots()` は `RescriptWorkspaceDiscovery.discover(project)` の結果を `node_modules/.bin/rescript-language-server` にマッピングし、最初に存在するものを返す。

## ドキュメント

- `CLAUDE.md`: 「LSP 統合」セクションにモノレポ検出ロジックの一文を追加
- `README.md`: Features に "Monorepo support" 項目
- `sphinx-docs/user/features/advanced.md`: "ReScript package roots" 設定の使い方
- `sphinx-docs/user/troubleshooting.md`: 旧バージョンの誤警告に言及

## テスト戦略

ユニットテストは一時ディレクトリ (`@TempDir Path tempDir`) で疑似ワークスペースを構築し、`RescriptWorkspaceDiscovery.discover(tempDir.toString())` の結果を検証する。

カバーするケース:
1. シンプル: root に `rescript.json` のみ
2. pnpm: root に `pnpm-workspace.yaml` + `packages/<name>/rescript.json`
3. npm 配列形式: `package.json#workspaces: ["apps/*"]` + `apps/web/rescript.json`
4. yarn classic オブジェクト形式: `package.json#workspaces: {packages: [...]}`
5. 手動上書き: settings で指定したパスのみ採用
6. 手動上書きで指定が無効: layout は空
7. workspace ファイルあるが pkg なし → depth-limited にフォールバック
8. depth-limited 検出: `nested/dir/rescript.json`
9. node_modules 配下の rescript.json は無視
10. 親方向走査: 子ディレクトリを開いて親に config がある
11. 完全に空: layout 空

統合観点（既存テスト更新）:
- `RescriptMissingConfigInspectionTest`: モノレポでは警告なし、空ワークスペースでは警告あり
- `RescriptLspDetectorTest`: `isRescriptProject(project)` の新オーバーロードを各レイアウトで検証
