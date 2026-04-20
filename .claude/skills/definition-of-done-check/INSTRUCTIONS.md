# Definition of Done Check — Instructions

`.claude/rules/definition-of-done.md` を索引として読み、各 Phase が参照する canonical rule に対応する機械的チェックを実行する。最後に Phase ごとの Pass/Fail/Manual 表を出力する。

## 実行手順

### 0. 前提確認

- CWD がプロジェクトルート（`rescript-intellij-plugin`）か確認。そうでなければ中断。
- `git diff --name-only --cached` と `git diff --name-only`（unstaged）を取得して、以降のチェック対象ファイル集合を決める。

### 1. DoD インデックスの読み込み

`Read .claude/rules/definition-of-done.md`。各 Phase セクションを解析し、`→ .claude/rules/<file>.md` 形式のリンクを抽出する。

### 2. Phase 2 — 実装品質チェック

| 参照ルール | 機械的チェック | Fail 条件 |
|---|---|---|
| `code-comments.md` | staged `*.kt` のうち `class\|object\|enum class\|sealed class\|interface` 定義を含むファイルごとに `/** ... */` KDoc が直前行に存在するか grep | KDoc 欠落あり |
| `testing.md` | 新規 `src/main/**/*.kt` の `class/object` に対し、同パッケージの `src/test/**/<ClassName>Test.kt` が存在するか glob | 対応テスト欠落あり（免除対象でない場合。免除対象判定は手動 MANUAL） |
| `deprecated-api.md` | staged `*.kt` のうち `@Deprecated` 付き API 利用を grep（`@Suppress("DEPRECATION")` 注釈の有無もセットで報告） | `@Suppress` なしの deprecated 参照あり（CI `./gradlew verifyPlugin` でも最終検出） |
| `flex-rules.md` | staged ファイルに `RescriptFlexLexer.java`（生成物）を含むか、`Rescript.flex` と `RescriptTokenTypes.kt` が対で更新されているか | `.java` 直編集 or `.flex` のみ更新で `TokenTypes` 未更新 |
| `plugin-xml-rules.md` | 新規 `com.intellij.*` Extension Point 実装クラスに対し `plugin.xml`（または `META-INF/rescript-*.xml`）への登録 grep | 登録行欠落 → MANUAL（自動抽出が難しい場合） |

### 3. Phase 3 — コミット前チェック

#### 3-1. 自己検証（DoD-owned）

以下を直列実行。Fail があれば即フラグ。

```bash
./gradlew ktlintCheck
./gradlew clean buildPlugin
./gradlew test
```

各コマンドの終了コードを記録。ビルド警告の新規増加は `gradle.log` 差分で検出（ベースラインを `.claude/.dod-check/baseline-warnings.txt` にキャッシュしている場合のみ）。

#### 3-2. ドキュメント同期（→ `documentation.md`）

staged 差分に新規 Kotlin クラスがある場合、以下の 4 ターゲットに言及があるか grep:

- `CLAUDE.md` レイヤー 3 のポインタ先（`docs/functional-design.md`, `docs/repository-structure.md`）
- `README.md` Features セクション
- `sphinx-docs/user/features/**`
- `docs/product-requirements.md`（ロードマップ記載分は実装済みセクションへの移動）

staged 差分に `sphinx-docs/**/*.md`（`locale/` 外）がある場合、対応する `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` も staged かつ `msgstr ""` 残存なしを確認。

#### 3-3. Git コミット（→ `git-conventions.md`）

- HEAD へのコミット直前検証ではなく、ドラフト中のコミットメッセージを検査する場合: 絵文字プレフィックスの正規表現 `^(✨|🐛|♻️|📝|🎨|⚡|🔧|⬆|✅|🗑️) ` でマッチするか
- `git add .` / `git add -A` を最近の操作で使っていないか（`git reflog` で検出困難 → MANUAL）

#### 3-4. セキュリティ（→ `CLAUDE.md` セキュリティ）

staged `*.kt` に対して以下の簡易スキャン:

- `Runtime.getRuntime().exec(` / `ProcessBuilder(...)` + 文字列連結パターン → Fail（`ProcessBuilder` は配列引数必須）
- `.absolutePath` / `.canonicalPath` を含むログ出力・例外メッセージ・ユーザー可視 UI 文字列 → Fail
- LSP レスポンス（`org.eclipse.lsp4j.*`）をバリデーションなしで `File(...)` / `Path.of(...)` に渡している → MANUAL（文脈判断が必要）

### 4. Phase 4 — マージ前（DoD-owned）

- `.steering/<current>/tasklist.md` が存在し、すべてのチェックボックスが `[x]` か確認
- `.steering/<current>/requirements.md` の受け入れ条件章を抽出し、MANUAL として提示

### 5. 出力フォーマット

```
## Definition of Done Check — <branch>

### Phase 2: 実装
- [x] KDoc (code-comments.md)           PASS
- [ ] Test placement (testing.md)       FAIL — missing: src/test/kotlin/.../FooTest.kt
- [!] Deprecated API (deprecated-api.md) MANUAL — 2 @Suppress annotations, review justifications
- [x] plugin.xml registration           PASS

### Phase 3: コミット前
- [x] ktlintCheck                       PASS (exit 0)
- [x] clean buildPlugin                 PASS (no new warnings)
- [x] test                              PASS (134 passed)
- [!] Doc sync (documentation.md)       MANUAL — 2 new classes, manual verification: README + sphinx
- [x] Security scan                     PASS

### Phase 4: マージ前
- [ ] tasklist完了                       FAIL — 3 unchecked items
- [!] Acceptance criteria              MANUAL — review requirements.md 受け入れ条件

### Summary
FAIL: 2  MANUAL: 3  PASS: 7
```

## 非ゴール

- ルール本文の書き換え（これは DoD の索引パターンが担う）
- KDoc の「責務を 1-3 文で説明」などの質的判定（LLM 判定可能だがこのスキルでは MANUAL に分類）
- `git commit` の実行（本スキルはあくまで検証で、コミットはユーザー責任）

## Reference

- DoD index: `.claude/rules/definition-of-done.md`
- 関連スキル: `.claude/skills/docs-lint/`（ドキュメント sync の機械チェック専用）
