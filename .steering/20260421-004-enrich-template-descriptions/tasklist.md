# タスクリスト: プロジェクトテンプレート説明の拡充

## 実装タスク

- [x] ステアリングドキュメント作成 (requirements / design / tasklist)
- [x] 各 `*TemplateFiles.kt` の KDoc・dependencies を読み、15 テンプレート分の Includes/Requires を洗い出す
- [x] `ProjectTemplate.kt` の 15 エントリの `description` を `trimIndent()` 付き raw string で差し替え

## 検証タスク

- [x] `./gradlew ktlintCheck` が成功する
- [x] `./gradlew buildPlugin` が成功する（`rescript-intellij-plugin-0.1.12.zip` 生成確認）
- [x] `./gradlew test --tests ProjectTemplateTest` が全 31 件成功（`description.isNotBlank()` を含む）
- [x] 既存の `RescriptModuleBuilderTest.module type is not null` の NPE 失敗は clean `main` でも再現するため本変更とは無関係
- [x] 既存 enum の値差し替えのみで新規クラス追加なし → 追加テスト不要

## ドキュメントタスク

- [x] ドキュメント影響なし（ユーザー向けドキュメントは Wizard UI 内の表示内容であり、README / sphinx-docs には転記されていない）

## コミット・マージタスク

- [x] `ProjectTemplate.kt` + steering ディレクトリを個別ファイル指定でステージング
- [x] `📝 Enrich project template descriptions` で単一コミット
- [x] tasklist の全タスクを `[x]` に更新してコミットに含める
- [x] ユーザーにマージ可否を確認
- [x] 承認後 `main` にマージ
