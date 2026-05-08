# JS Interop Risk Map — Design

## 1. アーキテクチャ概要

```
┌──────────────────────────────────────────────────────┐
│ Tools > Show JS Interop Risk Map                     │
└─────────────────────┬────────────────────────────────┘
                      │ activate
                      ▼
┌──────────────────────────────────────────────────────┐
│ RescriptInteropRiskToolWindowFactory                 │
│ (com.intellij.toolWindow extension point)            │
└─────────────────────┬────────────────────────────────┘
                      │ creates
                      ▼
┌──────────────────────────────────────────────────────┐
│ RescriptInteropRiskPanel                             │
│  - Refresh button                                    │
│  - JBList of InteropRiskEntry                        │
└──────────┬───────────────────────────┬───────────────┘
           │ scan project              │ render
           ▼                           ▼
┌─────────────────────────┐ ┌─────────────────────────┐
│ RescriptInteropScanner  │ │ EntryRenderer (in panel)│
│  - walks ReScript files │ └─────────────────────────┘
│  - delegates to         │
│    RescriptInteropClass │
│    ifier per match      │
└─────────┬───────────────┘
          │
          ▼
┌─────────────────────────┐
│ RescriptInteropClassifier│
│  - kind + risk score    │
│  - pure function        │
└─────────────────────────┘
```

## 2. パッケージ構成

新規パッケージ `interop/` を追加する。

```
src/main/kotlin/com/rescript/plugin/interop/
├── RescriptInteropRiskToolWindowFactory.kt   # ToolWindow 登録
├── RescriptInteropRiskPanel.kt                # JBList + Refresh
├── RescriptInteropRiskAction.kt               # Tools メニュー
├── RescriptInteropModel.kt                    # InteropKind / RiskLevel / Entry
├── RescriptInteropScanner.kt                  # プロジェクトスキャン
└── RescriptInteropClassifier.kt               # トークン分類器（pure）

src/test/kotlin/com/rescript/plugin/interop/
├── RescriptInteropClassifierTest.kt
└── RescriptInteropScannerTest.kt              # pure helper のみテスト
```

## 3. 主要クラス設計

### 3.1 RescriptInteropModel

```kotlin
enum class InteropKind { RAW, EXTERNAL, OBJ_MAGIC, BS_ATTR, UNKNOWN }
enum class RiskLevel { HIGH, MEDIUM, LOW }

data class InteropEntry(
    val file: VirtualFile,
    val offset: Int,
    val lineNumber: Int,
    val previewLine: String,
    val kind: InteropKind,
    val risk: RiskLevel,
)
```

### 3.2 RescriptInteropClassifier (pure)

```kotlin
object RescriptInteropClassifier {
    fun classify(snippet: String): Pair<InteropKind, RiskLevel>?
}
```

ヒューリスティック:
- 入力 snippet（マッチしたトークンを含む短い行コンテキスト）を見て:
  - `Obj.magic` → `OBJ_MAGIC` / `HIGH`
  - `%raw` / `%%raw` で始まる → `RAW` / `HIGH`
  - `external ` で始まり、行内に `@bs.send` `@bs.module` `@module` `@send` のいずれかが含まれる → `EXTERNAL` + `BS_ATTR` 共存 → `MEDIUM`
  - `external ` で始まる → `EXTERNAL` / `LOW`
  - `@bs.send` `@bs.module` 等のアノテーションのみ → `BS_ATTR` / `LOW`
  - その他 → `null`（除外）

### 3.3 RescriptInteropScanner

```kotlin
object RescriptInteropScanner {
    fun scan(project: Project): Result
    
    data class Result(val entries: List<InteropEntry>, val truncated: Boolean)
}
```

実装:
- `FileTypeIndex.getFiles(RescriptFileType, GlobalSearchScope.projectScope)` でファイル一覧取得
- 各ファイルテキストを行単位で走査し、`Obj.magic` / `%raw` / `%%raw` / `external` / `@bs.` / `@send` / `@module` を含む行を候補にする
- 候補行を `RescriptInteropClassifier` に渡し、`null` でない結果を `InteropEntry` として収集
- 1 ファイルあたり 50 件、プロジェクト全体で 500 件のソフトキャップ
- 結果を `risk` 順（HIGH → MEDIUM → LOW）でソート

### 3.4 RescriptInteropRiskPanel

`SimpleToolWindowPanel` + `JBList<InteropEntry>` + Refresh ボタン + 専用 `ListCellRenderer`:
- 表示: `[kind/risk] file:line  preview`
- ダブルクリックで `OpenFileDescriptor.navigate(true)`

### 3.5 ToolWindow / Action

既存パターンに従う。
- ToolWindow ID: `ReScript Interop Risk`
- 右側アンカー
- Tools メニューに `Show JS Interop Risk Map`

## 4. 既存資産の再利用

- `RescriptFileType` / `RescriptInterfaceFileType` — スキャン対象判定
- 既存 ToolWindow パターン（dependency-diagram、impact、flow）

## 5. テスト戦略

| テスト種別 | 対象 | 手法 |
|-----------|------|------|
| Unit | `RescriptInteropClassifier.classify` | 5 種類の入力（raw / external / Obj.magic / @bs.send / 何でもない行）+ エッジケースをスナップショット |
| Unit | `RescriptInteropScanner.collectCandidatesFromText` | テキストから候補行を抽出する pure helper のみテスト |
| 免除 | `RescriptInteropRiskPanel` | Swing UI |
| 免除 | `RescriptInteropRiskToolWindowFactory` / `Action` | IDE ライフサイクル |
| 免除 | `RescriptInteropScanner.scan` の FileTypeIndex 経路 | IntelliJ Platform fixture が必要 |

## 6. プラグイン互換性

- IntelliJ Platform 2025.3+ の `FileTypeIndex` API
- LSP 不要
- Deprecated API なし

## 7. ドキュメント更新

- `CLAUDE.md` レイヤー 3 に `interop/` パッケージを追記
- `docs/repository-structure.md` パッケージ表に `interop/` を追加
- `docs/functional-design.md` に ToolWindow + Action を追加
- `README.md` Features セクションに「JS interop risk map」追加
- `sphinx-docs/user/features/advanced.md` に新セクション
- 日本語訳同時更新
- `docs/lsp-fallback-matrix.md` に「LSP 不要」行を追加
