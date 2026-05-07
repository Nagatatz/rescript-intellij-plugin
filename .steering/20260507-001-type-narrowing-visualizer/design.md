# Type Narrowing Visualizer — Design

## 1. アーキテクチャ概要

```
┌────────────────────────────────────────────┐
│           Editor (.res / .resi)            │
└──────────────────┬─────────────────────────┘
                   │ PSI 走査
                   ▼
┌────────────────────────────────────────────┐
│  RescriptNarrowingHintProvider             │
│  (InlayHintsProvider<NoSettings>)          │
└────────┬───────────────────────────┬───────┘
         │ switch 式検出              │ 設定参照
         ▼                            ▼
┌──────────────────────┐   ┌────────────────────┐
│ RescriptSwitchArm    │   │ RescriptProjectSet │
│ Collector            │   │ tings              │
└──────────┬───────────┘   └────────────────────┘
           │ arm 情報（offset, pattern, scrutinee）
           ▼
┌──────────────────────┐
│ RescriptHoverTypeRe  │
│ solver               │
│ (LSP textDocument/   │
│  hover を非同期で問い │
│  合わせ、200ms デバウ │
│  ンス)                │
└──────────┬───────────┘
           │ Type 文字列
           ▼
┌──────────────────────┐
│ RescriptNarrowingPre │
│ senter               │
│ (ヒント文字列の整形)  │
└──────────────────────┘
```

## 2. パッケージ構成

`docs/repository-structure.md` に従い、新規パッケージ `narrowing/` を追加する。既存 `lsp/` の `RescriptLspUtils` を再利用する。

```
src/main/kotlin/com/rescript/plugin/narrowing/
├── RescriptNarrowingHintProvider.kt   # InlayHintsProvider 実装
├── RescriptSwitchArmCollector.kt      # PSI から switch arm を抽出
├── RescriptHoverTypeResolver.kt       # LSP hover を呼び出し型情報を取得
├── RescriptNarrowingPresenter.kt      # 表示文字列の組み立て
└── RescriptNarrowingSettings.kt       # 設定永続化（既存 settings/ と統合）
```

```
src/test/kotlin/com/rescript/plugin/narrowing/
├── RescriptSwitchArmCollectorTest.kt
├── RescriptNarrowingPresenterTest.kt
└── RescriptHoverTypeResolverTest.kt   # LSP モックを利用
```

## 3. 主要クラス設計

### 3.1 RescriptNarrowingHintProvider

IntelliJ Platform の `com.intellij.codeInsight.hints.declarative.InlayHintsProvider` を実装する。`InlayTreeSink` を用いて各 `switch` arm の先頭にヒントを presentation する。

- `providerId = "com.rescript.plugin.narrowing"`
- グループ: `com.intellij.codeInsight.hints.declarative.HintFormat`
- 表示位置: arm の `=>` 直後（同一行末尾、disabled when too long）
- ヒント本体は `presentation` を `text("=> ${typeStr}")` で構築

### 3.2 RescriptSwitchArmCollector

**方針確認:** 既存の `RescriptParser` は switch 式を PSI として表現していない（`skipNonTopLevel` で素通り）。パーサーに switch 認識を追加すると影響範囲が大きく、既存テストへのリグレッションリスクが高い。本機能では **PSI を拡張せず、トークンウォーカー方式** を採る。

`RescriptLexer` を直接走らせて `switch` キーワードとその後続の `{ ... }` ブロック内の `|` `=>` を構文的に検出し、以下の情報を返す:

```kotlin
data class SwitchArm(
    val scrutineeRange: TextRange,   // switch X の X の範囲（hover の対象）
    val patternOffset: Int,          // | の直後（パターン開始 offset）
    val arrowOffset: Int,            // => の終了 offset（ヒント挿入点）
    val patternSummary: String,      // "Some(_)" / "Ok(_)" 等の表示用要約
)
```

走査ロジック:

1. `switch` トークンを検出
2. 続く非空白トークン列をスクラティニーとして読み取り（`{` まで）
3. `{` 内で `|` トークンを arm 区切りとして識別。ネスト `switch` は brace 深度で区別
4. `=>` 直前までをパターン、`=>` 直後を arm body と認識
5. 各 arm の `patternSummary` は最初の構成要素 + 末尾 `(_)` ヒューリスティックで生成

**メリット:** PSI 変更不要。既存 PSI / パーサーへの影響ゼロ。テスト容易（純粋関数として `String → List<SwitchArm>`）。
**デメリット:** PSI ベースのキャッシュ機構が使えない。代わりに `(VirtualFile, modCount)` 単位で結果をキャッシュする。

### 3.3 RescriptHoverTypeResolver

LSP `textDocument/hover` を呼び出し、`scrutineeOffset` に対する型情報を取得する。`RescriptLspUtils` の既存 helper を活用する。

- 入力: `(Project, VirtualFile, offset: Int)`
- 出力: `CompletableFuture<String?>`
- LSP 未接続時は `null` を即返す
- 200ms デバウンス（`com.intellij.util.Alarm`）
- 同一 (file, offset, document version) のキャッシュを保持し、document 変更で無効化

### 3.4 RescriptNarrowingPresenter

Hover レスポンスから型のみを抽出し、表示用に整形する。

- ReScript LSP の hover は `type signature\n\n---\n\ndoc` 形式を返す。先頭ブロックのみ抽出
- 80 文字超は `...` 省略、tooltip で全体表示
- `_unit` / `unknown` のようなトリビアル型は表示抑制

### 3.5 RescriptNarrowingSettings

既存 `RescriptProjectSettings` に `narrowingHintsEnabled: Boolean = true` を追加する。`RescriptConfigurable` に対応するチェックボックスを追加。

## 4. パーサー拡張（不要）

調査の結果、既存 `RescriptParser` は `switch` 式を素通りしており、PSI を介した arm 取得は不可能。本実装ではパーサーに手を入れず、`RescriptLexer` を `RescriptSwitchArmCollector` 内で直接インスタンス化して走らせる。これにより:

- 既存 PSI / パーサーテストへのリグレッションリスクをゼロにできる
- `RescriptSwitchArmCollector` を純粋関数（入力: `String` → 出力: `List<SwitchArm>`）として実装でき、テスト容易性が高い
- レクサーのみへの依存で、JFlex の変更も発生しない

## 5. パフォーマンス戦略

| 項目 | 方針 |
|------|------|
| InlayHints 計算 | バックグラウンド実行（`InlayHintsProvider.PreparedFactory`）、UI スレッドをブロックしない |
| LSP hover 呼び出し | 200ms デバウンス、最大 50 arms/file |
| キャッシュ | `(VirtualFile, modCount, offset)` → `String?` の WeakHashMap |
| 編集時の無効化 | `PsiTreeChangeListener` で switch を含むファイルのキャッシュをクリア |
| 失敗時のフォールバック | LSP エラー時は当該 arm のヒントをスキップ、他の arm は表示継続 |

## 6. LSP 連携の境界

LSP 未接続時の挙動は `docs/lsp-fallback-matrix.md` に従い、**機能完全停止**（ヒントを一切出さない）とする。「型情報が取得できない arm にだけ警告を出す」のような部分動作は混乱の元なので採用しない。

## 7. テスト戦略

| テスト種別 | 対象 | 手法 |
|-----------|------|------|
| Unit | `RescriptSwitchArmCollector` | 既存 PSI fixture を使った golden test（5 種類のパターン） |
| Unit | `RescriptNarrowingPresenter` | 入力文字列のスナップショットテスト |
| Unit | `RescriptHoverTypeResolver` | LSP モック（`MockLanguageServer` 既存パターンを踏襲） |
| Integration | `RescriptNarrowingHintProvider` | `LightJavaCodeInsightFixtureTestCase` の `testInlays` で表示位置・内容を検証 |

UI コンポーネント（`RescriptConfigurable` のチェックボックス追加）はテスト免除（`testing.md` の免除カテゴリ）。

## 8. プラグイン互換性

- IntelliJ Platform 2025.3+ の `InlayHintsProvider` v2 API を使用（declarative API）
- LSP API は `com.intellij.platform.lsp` の既存利用に準拠
- Deprecated API なし（既存の `RescriptCodeVisionProvider` と同等の API レベル）

## 9. ドキュメント更新

- `CLAUDE.md` レイヤー 3: IDE 統合機能に `narrowing/` パッケージを追加
- `docs/repository-structure.md` パッケージ表に `narrowing/` を追加
- `docs/functional-design.md` の Extension Point マップに `RescriptNarrowingHintProvider` を追加
- `README.md` Features セクションに「Type Narrowing Visualizer」を追加
- `sphinx-docs/user/features/code-analysis.md` に新セクション追加（日本語訳 `.po` も同時更新）
- `docs/lsp-fallback-matrix.md` に本機能の依存度を追加
