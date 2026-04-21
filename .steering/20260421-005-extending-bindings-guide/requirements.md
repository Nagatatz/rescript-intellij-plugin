# 要求内容: Extending Bindings ガイドの追加

## 背景

Wizard が生成する 15 種類のプロジェクトテンプレートは、外部 JS ライブラリ (Hono、drizzle-orm、react-native 等) へのバインディングを各テンプレート内に手書きの最小 `@module` / `@send` / `@val` external で同梱している。サードパーティ ReScript バインディングパッケージ (例: `@rescript/webapi`、`rescript-hono`) は意図的に採用していない。

この方針は依存が最小で学習教材として機能する反面、利用者がテンプレートを拡張する段階で「hono/cors を追加したい」「drizzle-orm の where 句を使いたい」「fetch を型付けしたい」といった場面に遭遇し、同じパターンで externals を書き足す必要がある。`@rescript/webapi` は experimental ステータス (0.1.x) でまだ安定していないため依存追加は見送る。

## 目的

各テンプレートが生成する `README.md` に共通の「Extending Bindings」リファレンスセクションを自動的に含め、利用者が自力で externals を書き足せるように以下を提供する:

- バインディング属性 (`@module` / `@send` / `@val` / `@new` / `@get` / `@set` / `@scope`) のクイックリファレンス
- 代表的な 3 パターンの実装レシピ (typed fetch / Hono middleware / drizzle-orm フィルタ)
- 参考になるコミュニティパッケージの一覧 (安定度コメント付き)
- ReScript 公式リソース (forum、binding manual) へのリンク

## 受け入れ条件

- [ ] `src/main/resources/templates/common/readme/extending-bindings.md` が存在し、上記 4 セクションを含む
- [ ] `CommonFiles.readme(...)` が生成する全テンプレートの README に `## Extending Bindings` 見出しが含まれる
- [ ] セクションは `extraSections` の後、`## Learn More` の前に配置される
- [ ] 新規セクションに `@rescript/webapi` の experimental ステータスに関する注意書きが含まれる
- [ ] `CommonFilesTest` が新セクションの存在を検証する
- [ ] `ProjectTemplateTest` が全 15 テンプレートの README に新セクションが含まれることを検証する
- [ ] `./gradlew ktlintCheck clean buildPlugin test` が成功する

## スコープ外

- テンプレート `*.res` ファイルの変更
- 新規依存パッケージ (`@rescript/webapi` 等) の追加
- sphinx-docs / CLAUDE.md / README.md / docs/ の変更
- ガイドの日本語訳 (テンプレート出力はすべて英語)
