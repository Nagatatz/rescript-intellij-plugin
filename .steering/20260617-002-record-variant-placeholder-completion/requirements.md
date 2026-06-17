# 要求内容: Record / Variant Placeholder 補完 (#116)

## 背景

ロードマップ #116「record / variant placeholder 補完」(カテゴリ: 補完 / 優先度: B)。
record literal を書くとき全フィールドを手で並べるのは退屈で、フィールドの抜け漏れも起きやすい。
variant 値を構築するときも、どの constructor があるか・payload が要るかを思い出す手間がある。
型注釈が分かっている文脈では、IDE がフィールド/constructor の雛形を `_` 付きで一括挿入できると入力コストが下がる。

既存資産:
- `lang/RescriptTypeDeclarationParser` … `type T = ...` のテキストを `TypeShape`(`Record`/`Variant`/`Unknown`) にパースし、`RecordField`(name/typeAnnotation/isMutable) と `VariantConstructor`(name/payload) を抽出済み。
- `lsp/RescriptVariantTypeResolver` … bare type name を stub index (`RescriptNameIndex`) 経由で `type <name> = ...` 宣言まで辿り、RHS を再パースするパターンを実装済み。
- `lsp/RescriptLspSignatureParser.parseVariantConstructors` … option / result の built-in variant をハードコードで返す。
- `completion/RescriptDecoratorCompletionContributor` … 既存の `CompletionContributor` 実装パターン (plugin.xml `completion.contributor` 登録)。
- 純ロジック分離パターン … `intention/RescriptMissingArmsBuilder` (pure builder) + thin IDE wrapper。

## スコープ (v1)

### トリガー
ReScript ファイルで、`let <name>: <TypeName> = ` の **型注釈付き let 束縛の値位置** にキャレットがある補完。
判定はキャレット直前テキストの純構文走査で行う (LSP 非依存)。`=` の直後 (値をまだ書いていない / 識別子先頭を書き始めた) 位置を対象とする。

### 解決
抽出した `TypeName` の head 識別子 (型引数 `<...>` は除く) を解決する:
1. built-in: `option` / `result` は variant としてハードコード解決。
2. プロジェクト宣言: stub index で `type TypeName = ...` を検索し `RescriptTypeDeclarationParser.parse` で `TypeShape` 化。

### 補完候補の生成
- `TypeShape.Record`: 単一候補 `{ field1: _, field2: _, ... }` を挿入。
- `TypeShape.Variant`: constructor ごとに候補を生成。payload 有りは `Ctor(_)`、無しは `Ctor`。

### 非対象 (v1 では実装しない)
- 「variant matching wrapper (switch スケルトン生成)」は既存の Add Missing Switch Arms Intention と重複するため対象外。v1 は **値構築位置の** record literal / variant constructor 雛形に限定する。
- `let` 以外の値位置 (関数引数、record field 値、`->` 後など) の型推論ベース補完。
- ネストした record/variant の再帰展開 (フィールド値は常に `_` のまま)。
- polymorphic variant・ラベル付き型引数のパース (既存パーサの v1 制約を踏襲)。
- 既にキャレット直前に `{` を入力済みのケース (二重ブレース回避のためスキップ)。

## 受け入れ条件

- [ ] `let p: person = ` (person が record 型) の位置で補完すると `{ name: _, age: _ }` 候補が出て、選択するとフィールド雛形が挿入される
- [ ] `let c: color = ` (color が variant 型) の位置で補完すると各 constructor 候補 (`Red`, `Green`, `Rgb(_)` など payload 有無に応じた形) が出る
- [ ] `let x: option<int> = ` で `Some(_)` / `None` 候補が出る (built-in 解決)
- [ ] 型注釈が無い `let x = ` や、解決できない型名では候補を出さない (誤爆しない)
- [ ] キャレット直前に既に `{` がある場合は record 候補を出さない
- [ ] LSP 未起動環境でも動作する (純構文 + stub index のみ)
- [ ] 純ロジック (注釈検出 / 雛形生成) は fixture 不要のユニットテストで網羅される
- [ ] ドキュメント (CLAUDE.md / README.md / sphinx-docs EN+JA / product-requirements.md のロードマップ行削除) を更新する

## 制約

- LSP 非依存 (FR/NFR: LSP 未接続でもネイティブ機能は動作する)
- 外部入力 (stub index 由来の宣言テキスト) はパース時に検証する
- 既存 `RescriptTypeDeclarationParser` / `RescriptVariantTypeResolver` を再利用し、重複ロジックを作らない
