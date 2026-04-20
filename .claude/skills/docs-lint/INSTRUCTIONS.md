# Docs Lint — Instructions

`.claude/rules/documentation.md` の「機能実装時のドキュメント更新」と「日本語訳の同時更新」を staged 差分に対して機械チェックする。

## 実行手順

### 0. 対象の特定

```bash
# staged changes
git diff --cached --name-only > /tmp/docs-lint-staged.txt
# new Kotlin classes added in staged diff
git diff --cached --diff-filter=A --name-only -- 'src/main/**/*.kt' > /tmp/docs-lint-new-classes.txt
```

### 1. Sphinx `.md` ↔ `.po` 同期チェック

対象: `sphinx-docs/**/*.md`（`locale/` 配下は除く）

```bash
for md in $(grep '^sphinx-docs/' /tmp/docs-lint-staged.txt | grep '\.md$' | grep -v '/locale/'); do
  # derive corresponding ja .po path
  po="sphinx-docs/locale/ja/LC_MESSAGES/${md#sphinx-docs/}"
  po="${po%.md}.po"
  if grep -Fxq "$po" /tmp/docs-lint-staged.txt; then
    echo "[OK]  $md ↔ $po (both staged)"
  else
    echo "[FAIL] $md staged but $po not staged"
  fi
done
```

staged `.po` に `msgstr ""`（翻訳欠落）が残っていないかチェック。ヘッダーエントリ（最初の空 msgid 直後の msgstr "" は許容）を除外する:

```bash
for po in $(grep 'locale/ja.*\.po$' /tmp/docs-lint-staged.txt); do
  # skip header entry (first msgstr ""), flag subsequent empty msgstr
  empty=$(awk '/^msgstr ""/{c++; if (c>1) print NR": "$0}' "$po")
  [ -n "$empty" ] && echo "[FAIL] $po has untranslated entries:" && echo "$empty"
done
```

### 2. 4-target sync matrix チェック（新規 Kotlin クラス）

対象: `/tmp/docs-lint-new-classes.txt` に列挙された新規 `*.kt` ファイル内のトップレベル `class` / `object` 宣言。

新規クラスごとに以下の 4 ターゲットを grep:

```bash
for kt in $(cat /tmp/docs-lint-new-classes.txt); do
  # extract top-level class/object names
  classes=$(grep -oE '^(internal |public )?(class|object|enum class|sealed class) [A-Z][A-Za-z0-9]+' "$kt" | awk '{print $NF}')
  for cls in $classes; do
    # Target 1: functional-design.md (CLAUDE.md レイヤー 3 の pointer 先)
    grep -q "$cls" docs/functional-design.md || echo "[WARN] $cls not mentioned in docs/functional-design.md"
    # Target 2: repository-structure.md (CLAUDE.md レイヤー 3 の pointer 先)
    grep -q "$cls" docs/repository-structure.md || echo "[INFO] $cls not in docs/repository-structure.md (may be implicit under package)"
    # Target 3: README Features セクション
    grep -q "$cls" README.md || echo "[WARN] $cls not mentioned in README.md"
    # Target 4: sphinx-docs feature pages
    grep -rq "$cls" sphinx-docs/user/features/ 2>/dev/null || echo "[WARN] $cls not mentioned in sphinx-docs/user/features/"
    # Target 5: product-requirements.md (if was in roadmap, should now be in 実装済み)
    grep -q "$cls" docs/product-requirements.md docs/archive/implemented-features.md 2>/dev/null && echo "[INFO] $cls mentioned in product-requirements or archive"
  done
done
```

`[WARN]` は advisory（手動レビューで省略可能かを判断）、`[FAIL]` のみ blocking とする。

### 3. Sphinx build 成否

オプション（ユーザーが「build も確認して」と言った場合のみ実行。時間がかかる）:

```bash
cd sphinx-docs && make build-ja
```

`make build-ja` が非 0 終了 → Fail として報告、ログの最終 20 行を出力。

### 4. 出力フォーマット

```
## Docs Lint Report — <branch>

### Sphinx .md ↔ .po sync
- [OK]   sphinx-docs/user/features/foo.md ↔ locale/ja/.../foo.po
- [FAIL] sphinx-docs/dev/bar.md staged but locale/ja/.../bar.po not staged

### Empty msgstr check
- [FAIL] sphinx-docs/locale/ja/.../foo.po has 3 untranslated entries at lines 45, 62, 89

### 4-target sync (new classes)
- RescriptFoo (src/main/.../RescriptFoo.kt)
  - [WARN] not in docs/functional-design.md
  - [WARN] not in README.md Features
  - [OK]   in sphinx-docs/user/features/code-editing.md

### Summary
FAIL: 2  WARN: 2  OK: 1
```

## 非ゴール

- 実際の翻訳品質判定（Japanese wording quality）
- Sphinx ReST 文法の検証（`make build-ja` に委譲）
- `.po` の `msgstr` 自動補完（これは `sphinx-po-ja-sync` スキルの役割）

## Reference

- Rule: `.claude/rules/documentation.md`（4-target matrix と `.po` 同期）
- 関連スキル: `.claude/skills/sphinx-po-ja-sync/`（write 側）、`.claude/skills/definition-of-done-check/`（DoD 全体チェック）
