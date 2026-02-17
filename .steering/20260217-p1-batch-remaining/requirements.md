# Requirements: P1 残り全6機能バッチ実装

## 概要

P1（高優先度）の残り全6機能を git worktree による並列実装で一括追加する。

## 対象機能

### 1. JSON Schema 提供（難易度: 低〜中）

**説明:** `rescript.json` / `bsconfig.json` に対して JSON Schema を提供し、補完・バリデーションを有効にする。

**受け入れ条件:**
- [ ] `rescript.json` を開くとプロパティ名の補完が効く
- [ ] 不正なプロパティに対してバリデーション警告が表示される
- [ ] `bsconfig.json` にも同じスキーマが適用される
- [ ] スキーマは rescript-lang 公式の定義に基づく

### 2. `%raw()` JS ハイライト（難易度: 中）

**説明:** `%raw()` 内の JavaScript コードをハイライトする（言語インジェクション）。

**受け入れ条件:**
- [ ] `%raw("...")` 内の文字列が JavaScript としてハイライトされる
- [ ] `%raw(\`...\`)` テンプレートリテラル内も同様
- [ ] JS の補完・シンタックスチェックが `%raw()` 内で動作する
- [ ] `%%raw(\`...\`)` にも対応する

### 3. Postfix Completion（難易度: 低）

**説明:** `.switch`, `.pipe`, `.log`, `.some`, `.ok` 等の式後方補完を提供する。

**受け入れ条件:**
- [ ] `expr.switch` → `switch expr { | _ => }` に展開
- [ ] `expr.pipe` → `expr->` に展開
- [ ] `expr.log` → `Console.log(expr)` に展開
- [ ] `expr.some` → `Some(expr)` に展開
- [ ] `expr.ok` → `Ok(expr)` に展開
- [ ] `expr.error` → `Error(expr)` に展開
- [ ] `expr.ignore` → `expr->ignore` に展開
- [ ] Settings > Editor > General > Postfix Completion で確認可能

### 4. Console Filter（難易度: 低）

**説明:** コンパイルエラー出力からファイル:行へのクリックジャンプを提供する。

**受け入れ条件:**
- [ ] ReScript コンパイラのエラー出力に含まれるファイルパスと行番号がクリック可能なリンクになる
- [ ] クリックで該当ファイルの該当行にジャンプする
- [ ] Run/Debug ウィンドウの出力に対して動作する

### 5. Editor Notification Bar（難易度: 低）

**説明:** LSP サーバー未検出時に設定案内バーをエディタ上部に表示する。

**受け入れ条件:**
- [ ] `@rescript/language-server` が未インストールの環境で `.res` ファイルを開くと、エディタ上部に警告バーが表示される
- [ ] バーに「Install @rescript/language-server」等のアクションリンクが含まれる
- [ ] LSP が利用可能な場合はバーが表示されない
- [ ] バーを閉じた後、再表示しない選択肢がある

### 6. Go to Related（難易度: 低）

**説明:** `.res` ↔ `.resi` ↔ `.js` 間の関連ファイルジャンプを提供する。

**受け入れ条件:**
- [ ] `Navigate > Related Symbol` (Ctrl+Alt+Home) で関連ファイル一覧がポップアップ表示される
- [ ] `.res` から: 対応する `.resi` と生成 `.js` ファイルが候補に表示
- [ ] `.resi` から: 対応する `.res` ファイルが候補に表示
- [ ] 候補を選択すると該当ファイルが開く

## 実装アプローチ

| 機能 | ブランチ名 | worktree パス |
|------|-----------|--------------|
| JSON Schema | `feature/json-schema` | `../rescript-wt-json-schema` |
| %raw() JS ハイライト | `feature/raw-js-highlight` | `../rescript-wt-raw-js` |
| Postfix Completion | `feature/postfix-completion` | `../rescript-wt-postfix` |
| Console Filter | `feature/console-filter` | `../rescript-wt-console-filter` |
| Editor Notification Bar | `feature/editor-notification` | `../rescript-wt-notification` |
| Go to Related | `feature/goto-related` | `../rescript-wt-goto-related` |

## 制約事項

- 各機能は完全に独立しており、互いに依存しない
- すべてのブランチは `main` から分岐する
- 各ブランチで `./gradlew buildPlugin` が成功すること
