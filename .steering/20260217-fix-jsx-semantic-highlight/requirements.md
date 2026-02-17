# Requirements: JSX タグの LSP セマンティックトークンによるハイライト消失修正

## 概要

JSX タグ（`<div>` や `<RescriptRelayReact.Context.Provider>` 等）が IDE 上で一瞬色がつくが、LSP セマンティックトークンが到着するとデフォルトテキスト色に戻る問題を修正する。

## 根本原因

1. セマンティックトークンの `TextAttributesKey` のフォールバック先が Platform デフォルトキー（`Defaults.MARKUP_TAG` 等）になっており、テーマによっては色が未定義
2. カラースキーム XML に `RESCRIPT_SEMANTIC_*` の色定義が存在しない
3. `getTextAttributesKey` の `else` ブランチが `super` を呼び出し、未知のトークンタイプにも Platform デフォルトを適用してしまう

## 受け入れ条件

- [ ] JSX タグのレクサーベースハイライトが LSP セマンティックトークン到着後も維持される
- [ ] Darcula / Default 両テーマでセマンティックトークンに適切な色が表示される
- [ ] `./gradlew clean buildPlugin` が成功する
