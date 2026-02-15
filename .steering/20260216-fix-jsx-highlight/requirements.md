# Requirements: JSX ハイライト修正

## 背景

IntelliJ 2025.3 の Islands テーマ（デフォルト）で JSX ハイライトが正常に表示されない問題が発生している。

### 問題 1: Islands テーマでカスタムカラーが読み込まれない

- プラグインは `additionalTextAttributes` を `scheme="Darcula"` と `scheme="Default"` のみで登録
- Islands テーマではスキーム名が "Dark" / "Light" であり、カスタムカラー定義が適用されない
- JSX トークンが `Defaults.MARKUP_TAG` にフォールバックし、Dark テーマでデフォルトテキスト色に近いため視認不能

### 問題 2: JSX 属性付きタグの閉じ `>` が `GT` になるバグ

- `<Component attr=value>` のような属性付き JSX タグで、閉じ `>` がレクサーで `GT` として認識される
- `IN_JSX_TAG_NAME` 状態が空白で `INITIAL` に遷移した後、`>` が通常の `GT` として処理されるため

## 受け入れ条件

1. Islands テーマ (Dark/Light) で JSX タグのハイライトが正しく表示される
2. 従来の Darcula/Default テーマでの動作に影響がない
3. JSX 属性付きタグの閉じ `>` が `TAG_GT` として正しくトークナイズされる
4. ブレース内の `>` は `GT` のまま維持される
5. `./gradlew clean buildPlugin` が成功する
6. `./gradlew test` が全パスする
