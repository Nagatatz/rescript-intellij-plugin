# Docs Lint — 手順書

`.claude/rules/documentation.md` の「機能実装時のドキュメント更新」と「日本語訳の同時更新」を staged 差分に対して機械チェックする。

## 実行手順

### 0. 対象の特定

```bash
# staged changes
git diff --cached --name-only > /tmp/docs-lint-staged.txt
# staged diff で新規追加された Kotlin クラス
git diff --cached --diff-filter=A --name-only -- 'src/main/**/*.kt' > /tmp/docs-lint-new-classes.txt
```

### 1. Sphinx `.md` ↔ `.po` 同期チェック

対象: `sphinx-docs/**/*.md`（`locale/` 配下は除く）

```bash
for md in $(grep '^sphinx-docs/' /tmp/docs-lint-staged.txt | grep '\.md$' | grep -v '/locale/'); do
  # 対応する ja 側 .po パスを導出
  po="sphinx-docs/locale/ja/LC_MESSAGES/${md#sphinx-docs/}"
  po="${po%.md}.po"
  if grep -Fxq "$po" /tmp/docs-lint-staged.txt; then
    echo "[OK]  $md ↔ $po (both staged)"
  else
    echo "[FAIL] $md staged but $po not staged"
  fi
done
```

staged `.po` に翻訳欠落が残っていないかチェックする。

**行単位の `grep '^msgstr ""'` で判定してはならない。** gettext では複数行の翻訳が

```
msgstr ""
"1 行目"
"2 行目"
```

という形を取るため、`msgstr ""` という行は「未翻訳」と「複数行翻訳の開始行」の両方を意味する。
行だけを見ると翻訳済みエントリを大量に誤検出する（実測で 20 件以上の偽陽性を確認）。
**継続行 (`"..."`) を連結してから空判定すること**:

```bash
for po in $(grep 'locale/ja.*\.po$' /tmp/docs-lint-staged.txt); do
  python - "$po" <<'PY'
import io, sys
path = sys.argv[1]
lines = io.open(path, encoding="utf-8").read().splitlines()
i = 0
while i < len(lines):
    if lines[i].startswith("msgstr "):
        value, j = lines[i][7:], i + 1
        while j < len(lines) and lines[j].startswith('"'):
            value += lines[j].strip()
            j += 1
        if value.replace('"', "").strip() == "":
            k = i - 1
            while k >= 0 and not lines[k].startswith("msgid"):
                k -= 1
            msgid = lines[k] if k >= 0 else "?"
            # msgid "" はヘッダーエントリ。msgstr にメタデータを持つので対象外
            if msgid.strip() != 'msgid ""':
                print("[FAIL] %s:%d untranslated: %s" % (path, i + 1, msgid[:70]))
        i = j
    else:
        i += 1
PY
done
```

awk で書く場合は **`FNR`（ファイル内行番号）を使うこと**。`NR` は複数ファイルを跨いで累積するため、
2 ファイル目以降で存在しない行番号を報告する。

### 2. 4-target sync matrix チェック（新規 Kotlin クラス）

対象: `/tmp/docs-lint-new-classes.txt` に列挙された新規 `*.kt` 内のトップレベル `class` / `object` 宣言。

新規クラスごとに以下の 4 ターゲットを grep する:

```bash
for kt in $(cat /tmp/docs-lint-new-classes.txt); do
  # トップレベルの class/object 名を抽出
  classes=$(grep -oE '^(internal |public )?(class|object|enum class|sealed class) [A-Z][A-Za-z0-9]+' "$kt" | awk '{print $NF}')
  for cls in $classes; do
    # Target 1: functional-design.md（CLAUDE.md レイヤー 3 のポインタ先）
    grep -q "$cls" docs/functional-design.md || echo "[WARN] $cls not mentioned in docs/functional-design.md"
    # Target 2: repository-structure.md（CLAUDE.md レイヤー 3 のポインタ先）
    grep -q "$cls" docs/repository-structure.md || echo "[INFO] $cls not in docs/repository-structure.md (パッケージ単位で暗黙的に該当する場合あり)"
    # Target 3: README の Features セクション
    grep -q "$cls" README.md || echo "[WARN] $cls not mentioned in README.md"
    # Target 4: sphinx-docs の feature ページ
    grep -rq "$cls" sphinx-docs/user/features/ 2>/dev/null || echo "[WARN] $cls not mentioned in sphinx-docs/user/features/"
    # Target 5: product-requirements.md（ロードマップから実装済みへ移動済みかの確認）
    grep -q "$cls" docs/product-requirements.md docs/archive/implemented-features.md 2>/dev/null && echo "[INFO] $cls mentioned in product-requirements or archive"
  done
done
```

`[WARN]` は advisory（手動レビューで省略可否を判断）、`[FAIL]` のみブロッキングとする。

### 3. Sphinx ビルドの成否

オプション（ユーザーが「ビルドも確認して」と指示した場合のみ実行。時間がかかる）:

```bash
cd sphinx-docs && make build-ja
```

`make build-ja` が非 0 で終了した場合は FAIL として報告し、ログの最終 20 行を出力する。

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

- 翻訳品質の判定（日本語表現の良し悪し）
- Sphinx ReST 文法の検証（`make build-ja` に委譲）
- `.po` の `msgstr` 自動補完（`sphinx-po-ja-sync` スキルの役割）

## 参考

- ルール: `.claude/rules/documentation.md`（4-target matrix と `.po` 同期）
- 関連スキル: `.claude/skills/sphinx-po-ja-sync/`（書き込み側）、`.claude/skills/definition-of-done-check/`（DoD 全体チェック）
