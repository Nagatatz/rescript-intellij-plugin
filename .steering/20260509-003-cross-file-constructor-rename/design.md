# Cross-file Variant Constructor Rename — Design

## アーキテクチャ概要

新規パッケージは作らず、既存の `intention/` パッケージに intention を追加する。プロジェクト全体走査は `impact/` の `FileTypeIndex` ベースのパターンを再利用し、constructor 文脈の判定は新規 `RescriptConstructorOccurrenceClassifier` に切り出す。LSP は呼び出さない。

```
caret on UIDENT
  ↓
RescriptRenameVariantConstructorIntention.isAvailable
  ↓ (current file lexer scan)
classifier.classify(file tokens, caret offset) ∈ { Constructor, Pattern }
  ↓ (Alt+Enter triggered)
RescriptConstructorOccurrenceFinder.findAll(project, name)
  ↓ FileTypeIndex(.res) + word-index "name" → per-file token scan
  ↓
List<ConstructorOccurrence> (file, range, kind)
  ↓ confirm dialog ("N occurrences across M files. Rename all?")
  ↓ WriteCommandAction
text replacement at each range with newName
```

## 新規クラス

| クラス | 責務 |
|--------|------|
| `intention/RescriptRenameVariantConstructorIntention` | Intention Action; isAvailable + invoke |
| `intention/RescriptConstructorOccurrenceClassifier` | トークン列から `(offset → kind)` を返す pure object。kind: `CONSTRUCTOR` / `PATTERN` / `MODULE_QUALIFIED_TAIL` / `OTHER` |
| `intention/RescriptConstructorOccurrenceFinder` | プロジェクト全体走査; UIDENT word-index で候補を絞り、各候補をクラシファイア通して `Constructor` / `Pattern` のみ拾う |
| `intention/RescriptConstructorOccurrence` | データクラス: `(VirtualFile, TextRange, Kind)` |

テスト免除対象 (Intention / Action) ではなく、classifier と finder は **pure object なので必須テスト**。

## Intention の `isAvailable`

```kotlin
override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
    if (file.fileType != RescriptFileType && file.fileType != RescriptInterfaceFileType) return false
    val caret = editor.caretModel.offset
    val word = extractWord(editor.document.text, caret) ?: return false
    if (!startsWithUppercase(word)) return false
    val kind = RescriptConstructorOccurrenceClassifier.classifyAt(file.text, caret)
    return kind == Kind.CONSTRUCTOR || kind == Kind.PATTERN
}
```

## Classifier ヒューリスティック

入力: ソース全体 + caret offset。
出力: `Kind`

トークン列を 1 度走査し、caret offset を含む UIDENT トークンを特定。その UIDENT の **直前 / 直後** のトークンを見て分類:

| 直前トークン | 直後トークン | Kind |
|-----------|-----------|------|
| `\|` | (任意) | `PATTERN` |
| `.` | (任意) | `MODULE_QUALIFIED_TAIL` |
| (上記以外) | `(` | `CONSTRUCTOR` |
| `=` , `,` , `(` , `[` , 行頭 | (上記の `(` 以外) | `CONSTRUCTOR` (引数なし constructor として使用) |
| その他 | その他 | `OTHER` |

「直前 / 直後」は **コメント・空白を飛ばした** non-trivia トークンで判定。

例:

| ソース | 結果 |
|--------|------|
| `\| Foo(_) =>` | PATTERN (直前 `\|`) |
| `\| Foo =>` | PATTERN |
| `let x = Foo(1)` | CONSTRUCTOR (直前 `=`, 直後 `(`) |
| `let x = Foo` | CONSTRUCTOR |
| `Module.Foo(x)` | MODULE_QUALIFIED_TAIL (直前 `.`) |
| `<Foo />` (JSX) | OTHER (直前 `<`) |
| `type t = Foo` | OTHER (直前 `=` だが直後がトークン末端) — 実は CONSTRUCTOR 扱いになり得る; v1 は許容 |
| `type t = \| Foo` | PATTERN (直前 `\|`) — type arm として認識 |

> 注: `type` 宣言の **arm pattern** は `|` 直後パターンと同じ形なので PATTERN として扱う。これは intentional — `type t = | Foo | Bar` の `Foo` は実質「constructor 宣言」なので、PATTERN と CONSTRUCTOR の両方の出現と一緒にリネームしても問題ない。

## Finder の挙動

```kotlin
fun findAll(project: Project, name: String): List<RescriptConstructorOccurrence> {
    val results = mutableListOf<RescriptConstructorOccurrence>()
    val scope = GlobalSearchScope.projectScope(project)
    val helper = PsiSearchHelper.getInstance(project)
    helper.processElementsWithWord(
        { element, offsetInElement ->
            val file = element.containingFile?.virtualFile ?: return@processElementsWithWord true
            if (file.fileType !in arrayOf(RescriptFileType, RescriptInterfaceFileType)) return@processElementsWithWord true
            val text = element.containingFile.text
            val absoluteOffset = element.textRange.startOffset + offsetInElement
            val kind = RescriptConstructorOccurrenceClassifier.classifyAt(text, absoluteOffset)
            if (kind == Kind.CONSTRUCTOR || kind == Kind.PATTERN || kind == Kind.MODULE_QUALIFIED_TAIL) {
                val range = TextRange(absoluteOffset, absoluteOffset + name.length)
                results.add(RescriptConstructorOccurrence(file, range, kind))
            }
            true
        },
        scope,
        name,
        UsageSearchContext.IN_CODE,
        true,
    )
    return results
}
```

`MODULE_QUALIFIED_TAIL` も書き換え対象に含める (Module.Foo の Foo 部分)。`OTHER` は除外。

## 書き換え

```kotlin
WriteCommandAction.runWriteCommandAction(project) {
    val byFile = occurrences.groupBy { it.file }
    for ((file, list) in byFile) {
        val doc = FileDocumentManager.getInstance().getDocument(file) ?: continue
        // 末尾から先頭へ書き換え (offset shift を避ける)
        for (occ in list.sortedByDescending { it.range.startOffset }) {
            doc.replaceString(occ.range.startOffset, occ.range.endOffset, newName)
        }
    }
}
```

複数ファイルを 1 つの WriteCommandAction にまとめると Undo も 1 ステップ。

## 上限とエラー処理

- 500 件超: `Messages.showErrorDialog("Too many occurrences (>500). Use Shift+F6 (LSP rename) or narrow scope.")` で中止
- 0 件: caret 位置の UIDENT 自身も含むはずなので 0 件はあり得ない (defensive)
- 新名が大文字始まりで無い場合: 入力ダイアログでバリデーション、再入力を促す

## kover 影響

- Classifier + Finder + Occurrence は pure object → テスト必須・カバレッジ参加
- Intention 本体は IDE lifecycle (PsiElementBaseIntentionAction) → testing.md 免除、build.gradle.kts kover excludes に追加

## テスト方針

- `RescriptConstructorOccurrenceClassifierTest`: 30+ ケース (CONSTRUCTOR / PATTERN / MODULE_QUALIFIED_TAIL / OTHER の各分岐、コメント挟み、文字列内、JSX 内、ネスト、or-pattern)
- `RescriptConstructorOccurrenceFinderTest`: light fixture で複数ファイルを書いて name 検索 → 期待件数一致
- Intention 本体: testing.md 免除 — 動作検証は手動 (runIde) または将来の UI test

## 既存 LSP rename との関係

| 起動方法 | 対象 | 動作 |
|---------|------|------|
| Shift+F6 (RescriptRenameHandler) | 任意の identifier | LSP rename (型情報考慮、最も安全) |
| Alt+Enter → "Rename variant constructor" | UIDENT (constructor / pattern 文脈) | トークンヒューリスティック (LSP 不要) |

両者は co-exist。LSP が動く環境では Shift+F6 を推奨、LSP 不在では Alt+Enter intention に切り替える。
