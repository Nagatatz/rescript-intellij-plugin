# Native PSI Fallbacks for LSP Code Actions — 設計

## 全体方針

両 Intention とも `RescriptBaseIntention` を継承し、`isAvailableInRescript` で発動条件を判定、`invoke` で書き換えを実行する。`RescriptEditorUtils` 系の既存ヘルパー（`replaceInWriteAction` 等）を最大限活用し、独自書き換えロジックは最小限にする。

## A1: `RescriptApplyUncurriedIntention`

### 検出ロジック

カーソル位置の PSI 要素を起点に以下を順に判定:

1. **call site 検出**: カーソルが識別子上にあり、直後のテキストが `(` で始まり、既に `(.` (uncurried 構文) ではないこと
   - 行テキストを取得 → 識別子末尾位置から `(` までの間に空白のみが介在することを確認
   - `(. ` で始まる場合は false（既に uncurried）
2. **定義検出**: 識別子名で `RescriptStubIndexKey` の `let` インデックスを引く
   - 候補がない場合は false
   - 候補のうち、定義行のテキストに正規表現 `=\s*\(\s*\.\s*` (例: `let f = (. x) =>` `let f = (.x, y)` 等) が含まれるものを 1 つでも見つけたら true
   - 同名で複数定義がある場合は最初にマッチしたものを採用（false positive 許容範囲）

### 適用ロジック

1. 行テキストから call expression の `(` 位置を特定
2. `(` の直後に `. ` を挿入する（既存の引数があれば `. arg, arg2`、なければ `. `）
   - 0 引数 `f()` の場合は `f(.)` に変換（uncurried 0 引数構文）
3. `RescriptEditorUtils.replaceInWriteAction` で書き換え

### Intention テキスト

- `text` / `familyName`: `"Convert call to uncurried form"`

### false positive 対策

- 軽量パーサーの制約上、識別子の名前解決は行わない（同名関数が複数あり片方が curried、もう片方が uncurried というケースで誤検出する可能性）
- 受容理由: ReScript v10/v11 のレガシーコードでは uncurried 注釈は意識的に付与する設計が多い。同名で curried/uncurried が混在するケースは実用上稀
- ユーザーが意図しない箇所で適用した場合、Undo (`Cmd+Z`) で即座にロールバック可能

## A2: `RescriptExtractLocalModuleToFileIntention`

### 検出ロジック

1. **PSI 検出**: カーソルの PSI 要素から親をたどり、`MODULE_DECLARATION` （`RescriptElementTypes.MODULE_DECLARATION` 相当）の名前識別子上にあること
2. **ローカル検出**: その module 宣言が **トップレベル** にあること（同一ファイルの最上位スコープ。ネストした `module Inner = { module M = { ... } }` の場合は外側のスコープに M.res を作っても整合しない可能性が高いためスキップ）
3. **既存ファイル除外**: 同一ディレクトリに `M.res` がまだ存在しないこと
   - `containingFile.virtualFile.parent.findChild("M.res")` で確認

### 適用ロジック（`WriteCommandAction` 内）

1. **本体抽出**: PSI 要素のテキストから `{ ... }` の中身（外側の波括弧を除いた本体）を取得
   - 先頭・末尾の改行・インデントは整形して保存
2. **新規ファイル作成**:
   - `containingFile.virtualFile.parent.createChildData(this, "M.res")`
   - 本体テキストを書き込む（末尾に改行）
3. **元宣言の削除**:
   - `module M = { ... }` の PSI 範囲全体を空文字で置換
   - 直前の空行も合わせて削除（行数が増えないように）
4. **参照警告**:
   - 元ファイルのテキストを正規表現 `\bM\.\w` で検索
   - マッチがあれば IntelliJ の `Notifications.Bus.notify` で
     - `NotificationType.WARNING`
     - `"References to M in this file may need adjustment after extraction. Consider adding 'open M' or qualifying with the new module path."`
   - 通知グループ: `"ReScript"`（既存の `notificationGroup` を再利用）

### Intention テキスト

- `text`: `"Extract module to file"`（カーソル位置の M 識別子は動的に置き換え不要、シンプルに）
- `familyName`: `"Extract module to file"`

### エッジケース

- **`module M: SIG = { ... }`**: シグネチャ付き宣言。本タスクではシグネチャを `M.resi` に分離せず、`module M: SIG = include({...})` のような形にもしない。シンプルに `M.res` だけ作って元の宣言は削除する。SIG が外部から参照されている場合は警告通知に含める
- **`module M = OtherModule.Sub`**: 構造を持たない alias 宣言。本 Intention の対象外（PSI で `{` を含むかどうかで除外）
- **複数ファイルにまたがる影響**: 他ファイルから `Outer.M` のように参照している場合、抽出後は `M` だけで参照可能になるが、これも警告通知の範囲（自動書き換えはしない）

## Extension Point 登録

`src/main/resources/META-INF/plugin.xml` の `<extensions defaultExtensionNs="com.intellij">` 内に以下を追加:

```xml
<intentionAction>
    <language>ReScript</language>
    <className>com.rescript.plugin.intention.RescriptApplyUncurriedIntention</className>
    <category>ReScript</category>
</intentionAction>
<intentionAction>
    <language>ReScript</language>
    <className>com.rescript.plugin.intention.RescriptExtractLocalModuleToFileIntention</className>
    <category>ReScript</category>
</intentionAction>
```

並び順は既存エントリのアルファベット順に挿入する。

## テスト戦略

`BasePlatformTestCase` を継承する既存パターンに従う:

```kotlin
class RescriptApplyUncurriedIntentionTest : BasePlatformTestCase() {
    fun testCurriedCallToUncurriedDef_isAvailable() {
        myFixture.configureByText(
            "Foo.res",
            """
            let add = (. x, y) => x + y
            let result = ad<caret>d(1, 2)
            """.trimIndent(),
        )
        val intention = myFixture.findSingleIntention("Convert call to uncurried form")
        assertNotNull(intention)
    }

    fun testApply_insertsDotBeforeFirstArg() { /* ... */ }
    fun testNoCurriedDef_notAvailable() { /* ... */ }
    fun testAlreadyUncurried_notAvailable() { /* ... */ }
}
```

`extractLocalModuleToFile` のテストでは仮想ファイル作成を `myFixture.tempDirFixture` で扱う。

## ドキュメント反映

- **CLAUDE.md レイヤー 3**: `intention/` パッケージの代表クラスに 2 つを追記不要（既に「Wrap with」「@genType 追加等」とまとめられているため、既存記述で十分）。`docs/functional-design.md` に明示マップがあれば追加
- **README.md Features**: 「Code Editing > Intention Actions」セクションに 2 つを 1 行ずつ追加
- **sphinx-docs/user/features/code-editing.md**: Intention Actions の具体例として 2 つを before/after コードブロック付きで追加
- **sphinx-docs/locale/ja/LC_MESSAGES/user/features/code-editing.po**: 上記の翻訳

## コミット戦略

機能単位で分割（`.claude/rules/git-conventions.md`）:

1. `✨ Add applyUncurried PSI fallback intention` — 実装 + テスト + plugin.xml
2. `✨ Add extractLocalModuleToFile PSI fallback intention` — 実装 + テスト + plugin.xml
3. `📝 Document native LSP code action fallback intentions` — CLAUDE.md / README.md / sphinx + .po
4. `📝 Mark native fallback tasklist complete`（マージ前最終）