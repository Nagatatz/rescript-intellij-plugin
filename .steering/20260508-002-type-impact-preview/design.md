# Type Impact Preview — Design

## 1. アーキテクチャ概要

```
┌────────────────────────────────────────────────────────┐
│ Editor (.res / .resi)                                  │
└─────────────────────┬──────────────────────────────────┘
                      │ Caret position
                      ▼
┌────────────────────────────────────────────────────────┐
│ RescriptTypeImpactToolWindowFactory                    │
│ (com.intellij.toolWindow extension point)              │
└─────────────────────┬──────────────────────────────────┘
                      │ creates
                      ▼
┌────────────────────────────────────────────────────────┐
│ RescriptTypeImpactPanel                                │
│  - listens to FileEditorManager / Caret events         │
│  - debounces 200ms before recalculation                │
└──────────┬───────────────────────────┬─────────────────┘
           │ resolve target            │ render
           ▼                           ▼
┌────────────────────────┐   ┌─────────────────────────┐
│ RescriptTypeTarget     │   │ JBList<ReferenceEntry>  │
│ Resolver               │   │ + ReferenceEntryRenderer│
│ (PSI → type name)      │   └─────────────────────────┘
└──────────┬─────────────┘
           │ type name
           ▼
┌────────────────────────────────────────────┐
│ RescriptTypeReferenceFinder                │
│  - Stub Index lookup (RescriptNameIndex)   │
│  - LSP textDocument/references fallback    │
│  - merges and dedupes results              │
└──────────┬─────────────────────────────────┘
           │ List<ReferenceEntry>
           ▼
┌────────────────────────────────────────────┐
│ RescriptReferenceClassifier                │
│  - inspects PSI around each reference      │
│  - tags with TypeRefKind enum              │
└────────────────────────────────────────────┘
```

## 2. パッケージ構成

新規パッケージ `impact/` を追加する。

```
src/main/kotlin/com/rescript/plugin/impact/
├── RescriptTypeImpactToolWindowFactory.kt   # ToolWindow 登録
├── RescriptTypeImpactPanel.kt                # JBList + Caret listener
├── RescriptTypeImpactAction.kt               # Tools メニュー
├── RescriptTypeTargetResolver.kt             # 型宣言の検出と名前抽出 (pure)
├── RescriptTypeReferenceFinder.kt            # Stub Index + LSP fallback
├── RescriptReferenceClassifier.kt            # PSI/トークン分類 (pure)
└── RescriptTypeImpactModel.kt                # 表示用データクラス

src/test/kotlin/com/rescript/plugin/impact/
├── RescriptTypeTargetResolverTest.kt
├── RescriptReferenceClassifierTest.kt
└── RescriptTypeReferenceFinderTest.kt        # Stub Index は fixture 内で動作確認
```

## 3. 主要クラス設計

### 3.1 RescriptTypeTargetResolver

カーソル位置の PSI 要素から「対象となる型宣言」を識別する。

```kotlin
data class TypeTarget(
    val name: String,              // "User.t", "Result.result", ...
    val declarationFile: VirtualFile,
    val declarationOffset: Int,    // for "go to declaration" use
)

object RescriptTypeTargetResolver {
    fun resolveAt(psiFile: PsiFile, offset: Int): TypeTarget?
}
```

実装方針:
- PSI を offset で取得し、`RescriptDeclarationPsiElement` のうち `elementType == TYPE_DECLARATION` を上向きに探す
- 見つかった要素から名前を抽出（`type t = ...` の `t`）
- モジュールパスを再構築（包含する `module Foo = { type t = ... }` から `Foo.t`）

### 3.2 RescriptTypeReferenceFinder

```kotlin
data class ReferenceEntry(
    val file: VirtualFile,
    val offset: Int,
    val lineNumber: Int,           // 1-based
    val previewLine: String,       // trimmed line of source
    val kind: TypeRefKind,
)

enum class TypeRefKind { TYPE_REF, CONSTRUCTOR, PATTERN, FIELD_ACCESS, UNKNOWN }

object RescriptTypeReferenceFinder {
    fun findReferences(project: Project, target: TypeTarget): List<ReferenceEntry>
}
```

実装:
1. **Stub Index ステージ:** `RescriptNameIndex.KEY` で `target.name` のローカル部分（`User.t` の `t`）を検索 → 候補 PSI 群を取得
2. **LSP フォールバック:** Stub Index で件数が 0 か、project setting で `forceLspReferences = true` の場合に LSP `textDocument/references` を呼ぶ
3. **マージ:** `(file, offset)` ペアでデデュープ
4. **件数制限:** 200 件超は切り捨ててサマリーエントリを末尾に追加

### 3.3 RescriptReferenceClassifier

各参照を PSI コンテキストで分類する。

```kotlin
object RescriptReferenceClassifier {
    fun classify(file: PsiFile, offset: Int): TypeRefKind
}
```

ヒューリスティック:
- 直前のトークンが `:`（型注釈）→ `TYPE_REF`
- 直前のトークンが `|` 直後の identifier → `CONSTRUCTOR`
- `switch` 内で `|` の後 + コンテキスト → `PATTERN`
- 直前のトークンが `.` → `FIELD_ACCESS`
- それ以外 → `UNKNOWN`

純粋関数として `String → TypeRefKind` の helper でテストし、PSI ベース版は薄いラッパーにする。

### 3.4 RescriptTypeImpactPanel

`SimpleToolWindowPanel` を継承。`JBList<ReferenceEntry>` に専用の `ListCellRenderer` を設定。`CaretListener` で 200ms debounce。

ツールバー:
- Refresh
- Jump to Declaration
- (将来) Filter by kind

### 3.5 ToolWindow / Action

既存パターン（dependency-diagram、variant-flow）と同じ。
- ToolWindow ID: `ReScript Type Impact`
- 右側アンカー
- Tools メニューに `Show Type Impact` を追加

## 4. 既存資産の再利用

- `RescriptNameIndex.KEY` を使用して名前ベースの参照検索
- `RescriptLspUtils` の hover 同様、`textDocument/references` 呼び出し用の helper（既存があれば使う、無ければ追加）
- `RescriptOffsetUtils` で offset ↔ Position 変換
- `flow/` で確立した CaretListener + 200ms debounce + ApplicationManager.runReadAction パターン

## 5. テスト戦略

| テスト種別 | 対象 | 手法 |
|-----------|------|------|
| Unit | `RescriptTypeTargetResolver` | カーソル位置別に PSI fixture（`@ExtendWith(IntelliJPlatformExtension)`） |
| Unit | `RescriptReferenceClassifier` (純粋関数版) | 入力文字列のスナップショット |
| Unit | `RescriptTypeReferenceFinder` | Stub Index は IntelliJ Platform fixture 経由で動作確認、LSP は資料免除 |
| 免除 | `RescriptTypeImpactPanel` | Swing UI |
| 免除 | `RescriptTypeImpactToolWindowFactory` | IDE ライフサイクル依存 |
| 免除 | `RescriptTypeImpactAction` | AnAction 単発呼び出し |

## 6. プラグイン互換性

- IntelliJ Platform 2025.3+ の Stub Index API
- LSP 4j (既存と同じレベル)
- Deprecated API なし

## 7. ドキュメント更新

- `CLAUDE.md` レイヤー 3 に `impact/` パッケージを追記
- `docs/repository-structure.md` パッケージ表に `impact/` を追加
- `docs/functional-design.md` に ToolWindow + Action を追加
- `README.md` Features セクションに「Type impact preview」追加
- `sphinx-docs/user/features/advanced.md` に新セクション（日本語訳同時）
- `docs/lsp-fallback-matrix.md` に「LSP 不要（フォールバックあり）」行を追加
