# Definition of Done (DoD)

**以下は強制的な行動指示であり、例外なく従うこと。**

コードの変更を伴う作業を「完了」とみなすには、以下のすべての条件を満たす必要がある。各条件の詳細は参照先のルールファイルを確認すること。

## 1. コード品質

- [ ] すべての `class` / `object` / `enum class` / `sealed class` / `interface` に英語 KDoc が付与されている
- [ ] KDoc がクラスの責務を 1〜3 文で説明している
- [ ] IntelliJ Extension Point を実装するクラスは、対応するインターフェース名に言及している

**詳細:** `.claude/rules/code-comments.md`

## 2. テスト

- [ ] 新規・変更したすべてのクラスに対応する `<ClassName>Test.kt` が `src/test/` に存在する
- [ ] 免除対象（UI/LSP 結合/IDE ライフサイクル依存等）の場合、tasklist.md に省略理由を明記した
- [ ] すべてのテストがパスする

**詳細:** `.claude/rules/testing.md`

## 3. ビルド

- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] ビルド警告が新たに増加していない（既存警告は許容）

## 4. ドキュメント同期

- [ ] `CLAUDE.md` — アーキテクチャセクション（レイヤー 3）に新機能が反映されている
- [ ] `README.md` — Features セクションに新機能が反映されている
- [ ] `sphinx-docs/user/features/` — 該当する機能ページに説明・使用例が記載されている
- [ ] `docs/product-requirements.md` — ロードマップ記載機能の場合、「実装済み」セクションに移動した

**詳細:** `.claude/rules/documentation.md`

## 5. Extension Point 登録

- [ ] Extension Point を実装するクラスは `plugin.xml`（または `META-INF/rescript-*.xml`）に登録されている

**詳細:** `.claude/rules/plugin-xml-rules.md`

## 6. ステアリング進捗

- [ ] tasklist.md のすべてのタスクが `[x]` になっている
- [ ] requirements.md の受け入れ条件をすべて満たしている

**詳細:** `.claude/rules/steering-workflow.md`

## 7. Git コミット

- [ ] コミットは機能単位で分割されている（独立した機能は個別コミット）
- [ ] コミットメッセージに絵文字プレフィックスが付与されている
- [ ] `git add .` / `git add -A` ではなく、個別ファイル指定で `git add` している

**詳細:** `.claude/rules/git-conventions.md`

## 8. セキュリティ

- [ ] 外部入力（LSP レスポンス、ファイルパス、JSON 設定）はバリデーション済み
- [ ] コマンド実行は `ProcessBuilder` + 明示的引数リスト（文字列連結禁止）
- [ ] 絶対パスが UI やエラーメッセージに露出していない

## 適用タイミング

| タイミング | 確認する DoD 項目 |
|-----------|-----------------|
| コミット前 | 1〜5, 7, 8 |
| tasklist 完了時 | 6 |
| ブランチマージ前 | 全項目 (1〜8) |

## 例外

以下の変更は DoD の一部を免除してよい:

| 変更種別 | 免除項目 |
|---------|---------|
| タイポ修正・1 行の設定変更 | 2 (テスト), 4 (ドキュメント), 6 (ステアリング) |
| ドキュメントのみの変更 | 1〜3, 5, 8 |
| ステアリングドキュメントのみ | 1〜5, 7, 8 |
