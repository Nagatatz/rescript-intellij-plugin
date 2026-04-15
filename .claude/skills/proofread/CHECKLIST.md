# Proofread Checklist — Qodana 指摘傾向ベース

過去に Qodana が指摘してきた問題パターンを 12 カテゴリに整理した校正チェックリスト。
**ktlint / Kotlin コンパイラが既に拾う指摘は除外**し、意味論的・設計的な指摘に絞っている。

## 手順

### ステップ1: 対象ファイルの特定

引数に応じて対象を決定する:

| 引数 | 対象ファイル |
|------|-------------|
| なし | `git diff --name-only HEAD` + `git diff --name-only --cached` の和集合 |
| ファイルパス | そのファイル |
| コミット範囲（例: `main...HEAD`） | `git diff --name-only <range>` |

**除外対象:**
- `src/main/java/com/rescript/plugin/lang/RescriptFlexLexer.java`（JFlex 自動生成）
- `build/`, `.gradle/` 配下
- 拡張子が `.kt` / `.kts` / `.java` 以外のファイル

### ステップ2: 各ファイルを Read し、下記カテゴリを適用

カテゴリごとに「検出方法」を使って機械的にチェックし、ヒットしたら「判定基準」で本当に問題かを判断する。

**Grep を優先活用**するカテゴリ（5, 7, 10, 等）は最初に横断 Grep を走らせて候補を絞ってから個別に Read する。

### ステップ3: レポート生成

後述のレポートフォーマットで出力する。各指摘には **修正方針**（コード修正 / `@Suppress` / 無視）を明示する。

---

## カテゴリ

### 1. 非推奨 / 削除予定 API（高優先）

**検出方法:** Grep `@Deprecated|DEPRECATION|deprecated for removal`。IntelliJ SDK のバージョンアップで壊れやすい API を特に注視。

**典型パターン:**
- `addBrowseFolderListener(title, desc, project, descriptor)` → `addBrowseFolderListener(project, descriptor.withTitle(...).withDescription(...))`
- `IdAndTodoScannerBasedOnFilterLexer`（2025.3 で削除）
- `ApplicationManager.getApplication().runReadAction { }` の古いシグネチャ

**判定基準:** 代替 API があれば移行を提案。移行不可なら `@Suppress("DEPRECATION")` + 理由コメント。

### 2. 未使用宣言（既知の誤検出に注意）

**検出方法:** Grep `@Suppress\("unused"|"UNUSED_PARAMETER"\)` で既存の抑制を確認しつつ、新規コードで未参照フィールド/パラメータがないか Read でチェック。

**誤検出しやすい正当な未使用:**
- JFlex レクサーから間接参照されるトークン定数（`RescriptTokenTypes.SHARP` 等）
- LSP4J の `@JsonNotification` リフレクション起動メソッド
- IntelliJ DI コンテナが注入するコンストラクタ引数
- PSI Stub ベースの name-resolution API
- ToolWindow 公開定数（プログラマティックに参照）

**判定基準:**
- 本当に未使用 → 削除
- 上記の「正当な未使用」 → `@Suppress("unused")` + 1 行コメントで理由明記

### 3. Null 安全（冗長 nullable / UNSAFE_CALL）

**検出方法:** Grep `fun \w+\([^)]*\): \w+\?` で `?` 戻り値を持つ関数を列挙。`.node\.elementType` など PSI ツリー走査の直接アクセスを検索。

**典型パターン:**
- 実装が常に non-null を返すのに戻り値が `Type?`（例: `getLineIndent(): String?` で `""` を返す）→ 戻り値を `String` に narrow
- `PsiElement.node.elementType`（`node` は nullable） → `element.node?.elementType`

**判定基準:** 実装を Read して確認。常に non-null なら narrow、nullable の可能性があれば `?.` を挿入。

### 4. 文字列キャピタライゼーション（UI 文字列）

**検出方法:** Grep `"[A-Z][a-z]+ [A-Z]"` で UI 文字列（ダイアログタイトル、エラーメッセージ等）を列挙。

**典型パターン:**
- ツール名の扱い: 「rescript」（コマンド名）は小文字、「ReScript」（言語名）は PascalCase
- ファイル名: `package.json`, `rescript.json` は原文どおり
- 製品名は Proper: 「Node.js Interpreter Path」

**判定基準:**
- Dialog タイトル（Properties Window 等）は Title Case
- ボタン/メニュー項目は Sentence case
- 意図的な場合のみ `@Suppress("DialogTitleCapitalization")`

### 5. Kotlin イディオム（冗長コード）

**検出方法:** Grep で以下のアンチパターンを検索:
- `if \(\w+ == null\) \{ throw` → `?:`
- `\$\{'\\$'\}` → multi-dollar string `$$"""..."""`（Kotlin 2.1+）
- `start >= end \|\| start < 0` 的な範囲チェック → `x !in 0 until end`
- `"\\\\\\$"` 冗長エスケープ

**典型変換:**
```kotlin
// Before
if (x == null) { throw IllegalStateException("...") }
// After
x ?: throw IllegalStateException("...")
```

**判定基準:** ktlint は拾わない。Qodana 固有の提案。可読性が向上する場合のみ変換。

### 6. セーフナビゲーション（nullable API の直接アクセス）

**検出方法:** 以下の nullable API への `.` 直接アクセスを Grep:
- `PsiElement.node`（nullable）
- `PsiElement.reference`（nullable）
- `VirtualFile.parent`（nullable）
- `Editor.project`（nullable）

**判定基準:** コンテキストで non-null が保証される場合は `!!` ではなく `checkNotNull` / 早期 return で明示。保証されない場合は `?.`。

### 7. ホットパス・オブジェクトアロケーション

**検出方法:** Grep `setOf\(|listOf\(|\.toRegex\(\)` を **頻繁に呼ばれるメソッド内部** で検索（パーサー、インスペクション、アノテーター、Intention action 等）。

**典型パターン:**
```kotlin
// Bad: 呼び出しごとに Regex を新規生成
fun check(s: String) = s.matches("\\s+".toRegex())

// Good: companion object で 1 回だけコンパイル
companion object { private val WS = Regex("\\s+") }
fun check(s: String) = s.matches(WS)
```

**既存共通化:** `RescriptRegexPatterns`, `RescriptLspUtils` に既存パターンがある。重複していないか確認。

**判定基準:** ループ内・毎フレーム呼ばれるメソッド内 → companion object 定数化。1 回しか呼ばれないなら放置可。

### 8. 例外処理（リソースリーク・飲み込み）

**検出方法:** Grep 以下:
- `runCatching \{` でその中で `InterruptedException` に触れる箇所 → 再スローが必要
- `\.waitFor\(\)`（引数なし）→ タイムアウト追加
- `catch \(\w+: Exception\) \{ \}` 空 catch
- `InputStream`, `Reader` 系の取得で `.use { }` なし

**典型修正:**
```kotlin
// Bad
process.waitFor()
// Good
process.waitFor(30, TimeUnit.SECONDS)
```

**判定基準:** 必ず修正。セキュリティ/安定性直結。

### 9. 冗長オーバーライド / デッドコード

**検出方法:** Grep `override fun` 付近で `super\.\w+\(\)` のみを呼ぶ実装を Read で確認。

**典型パターン:**
- `Language` サブクラスの `getDisplayName()` で `super.getDisplayName()` のみ返す
- 空 body の `override fun`

**判定基準:** 何も付け加えていないなら削除。

### 10. Serializable コントラクト（readResolve）

**検出方法:** Grep `object \w+ : \w+` で Serializable を継承する親クラス（`Language`, `FileType`, etc.）を持つ `object` を列挙。

**典型修正:**
```kotlin
object RescriptLanguage : Language("ReScript") {
    private fun readResolve(): Any = RescriptLanguage  // 必須
}
```

**判定基準:** Serializable な親を持つ singleton `object` で `readResolve()` がないなら追加必須。

### 11. 重複コード（既知の誤検出あり）

**検出方法:** 同じパッケージ内のファイルを横断 Read し、類似する構造パターンを認識。

**正当な重複（指摘しない）:**
- `wizard/templates/*TemplateFiles.kt`（テンプレートごとに意図的に独立）
- `Configurable`, `StartupActivity` 等の IntelliJ Platform 定型実装
- Intention/Generate/QuickFix の action クラス（Platform 契約で構造が固定）
- Hierarchy browser 実装

**判定基準:**
- 上記の「正当な重複」 → 指摘しない
- それ以外で 3 箇所以上の類似 → 共通関数 / 基底クラスへの抽出を提案（既存の `RescriptBaseIntention`, `RescriptBaseGenerateAction`, `RescriptEditorUtils` の活用を優先検討）

### 12. セキュリティ（追加重点項目）

**検出方法:** Grep 以下:
- 絶対パスの UI/エラーメッセージ露出: `\.absolutePath|e\.message.*Path`
- コマンド実行の文字列連結: `ProcessBuilder\([^)]*\+|exec\(".*\$`
- ユーザー入力の未検証フラグ: LSP レスポンス / JSON 設定値の直接使用

**判定基準:** 必ず修正。`CLAUDE.md` のセキュリティセクション準拠。
- 絶対パス → ファイル名のみに sanitize
- コマンド実行 → `ProcessBuilder(listOf(...))` で明示引数リスト化

---

## レポートフォーマット

```
# Proofread Report

**対象:** {引数 or "uncommitted changes"}
**対象ファイル数:** N
**検出件数:** M （Critical: X / High: Y / Moderate: Z / Low: W）

## カテゴリ別サマリー

| # | カテゴリ | 件数 |
|---|---------|------|
| 1 | 非推奨 API | 2 |
| 2 | 未使用宣言 | 3 |
| ... |

## 指摘一覧（重要度順）

### Critical

#### 1. [カテゴリ番号] 短い見出し
- **ファイル:** `path/to/File.kt:L42`
- **問題:** 現状の説明
- **修正方針:** コード修正 / `@Suppress(...)` / 無視
- **Before/After:**
  \`\`\`kotlin
  // Before
  ...
  // After
  ...
  \`\`\`

### High
...

### Moderate
...
```

---

## 注意事項

- **このスキルはヒューリスティック**。Qodana の静的解析エンジンを完全には代替しない。網羅性より「よくある指摘を素早く拾う」ことを目的とする。
- **誤検出が出たら即座にこの CHECKLIST.md を更新**する（カテゴリ 2, 11 の「誤検出しやすい」リスト追加）。
- **既存の `@Suppress` を尊重**する。抑制の理由がコメントで明記されていれば再指摘しない。
- **JFlex 自動生成ファイル**（`RescriptFlexLexer.java`）への指摘は常に無視する。
- **ktlint / コンパイラ警告**との重複は避ける。このスキルは意味論的指摘に特化。
