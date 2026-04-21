# タスクリスト: Extending Bindings ガイドの追加

## 実装タスク

- [x] ステアリングドキュメント作成 (requirements / design / tasklist)
- [x] `src/main/resources/templates/common/readme/extending-bindings.md` を作成
- [x] `CommonFiles.readme()` に `## Extending Bindings` セクション挿入ロジックを追加

## テストタスク

- [x] `CommonFilesTest` に `readme appends Extending Bindings section with recipes` + `Extending Bindings is placed between extra sections and Learn More` テストを追加
- [x] `ProjectTemplateTest` に `every template README contains Extending Bindings section` テストを追加

## 検証タスク

- [x] `./gradlew ktlintCheck` が成功する
- [x] `./gradlew clean buildPlugin` が成功する (リソース jar `templates/common/readme/extending-bindings.md` を `unzip -p` で確認済)
- [x] `./gradlew test --tests ProjectTemplateTest --tests CommonFilesTest` が成功 (`tests=32/14`, `failures=0`)

## ドキュメントタスク

- [x] CLAUDE.md / README.md / sphinx-docs への影響なしを確認 (テンプレート出力であり機能追加ではない)

## コミット・マージタスク

- [x] `CommonFiles.kt` + 新規 markdown + 両テスト + steering ディレクトリを個別ファイル指定でステージング
- [x] `✨ Add Extending Bindings guide to template READMEs` で単一コミット
- [x] tasklist の全タスクを `[x]` に更新してコミットに含める
- [ ] ユーザーに push 可否を確認
- [ ] 承認後 `origin/main` に push
