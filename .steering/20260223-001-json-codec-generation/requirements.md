# 要求定義: JSON エンコーダ/デコーダ生成 (#81)

## 概要

ReScript の型定義（record / variant）から JSON encoder/decoder 関数を自動生成する Generate アクションを追加する。

## 背景

- ReScript の JSON 処理はボイラープレートが多く、型定義に対応する encoder/decoder を手書きするのは非効率
- `@rescript/core` の `JSON` モジュール（外部依存なし）をターゲットとする
- ロードマップ #81（B 優先度）

## 機能要件

### FR-1: Generate メニューからの呼び出し

- Cmd+N (Generate) メニューに "JSON Encoder/Decoder" アクションを追加
- カーソルが型宣言内にある場合のみ有効化
- Record 型または Variant 型（コンストラクタ 1 個以上）で有効

### FR-2: Record 型の encoder/decoder 生成

- 各フィールドの型に基づいて適切な encode/decode 式を生成
- サポートする型: `string`, `int`, `float`, `bool`, `option<T>`, `array<T>`
- 不明な型は `/* TODO */` プレースホルダを生成

### FR-3: Variant 型の encoder/decoder 生成

- ペイロードなし（enum）: `String` ベースのエンコーディング
- ペイロード付き: tagged union パターン (`tag` + `_0`, `_1`, ...)

### FR-4: 型名の命名規則

- `encode` + capitalize(typeName), `decode` + capitalize(typeName)
- 型名が `t` の場合は `encode` / `decode` のみ

## 受け入れ条件

- [ ] Record 型から正しい encoder/decoder が生成される
- [ ] Variant 型（enum、ペイロード付き）から正しい encoder/decoder が生成される
- [ ] `option<T>`, `array<T>` のネストが正しく処理される
- [ ] 型名 `t` の場合、関数名に型名が付かない
- [ ] Generate メニューに表示され、型宣言外では無効化される
- [ ] 全テストパス、ビルド成功
