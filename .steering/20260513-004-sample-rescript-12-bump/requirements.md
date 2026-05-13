# 要求: 手動テスト用サンプルプロジェクトの ReScript を 11→12 に上げる

## 背景

サンドボックステストで `manual-test-projects/main` を開いたところ、`package.json` の `rescript` 依存が `^11.1.4` のままで、プラグインが本気で想定する ReScript 12 系の挙動が再現できていない。プラグイン側のテンプレート生成器 (`TemplateVersions.RESCRIPT`) はすでに `^12.2.0`、UI テストフィクスチャ (`src/uiTest/testData/sample-project/package.json`) も `@rescript/language-server: ^1.72.0` に上がっており、ここだけ取り残されている。

## 受け入れ条件

- [ ] `manual-test-projects/main/package.json` の `rescript` を `^12.2.0` に更新
- [ ] `manual-test-projects/main/package.json` の `@rescript/language-server` を `^1.72.0` に更新
- [ ] `./gradlew clean buildPlugin test` がグリーン（プラグイン側のテストには影響しないはずだが、念のため確認）

## 制約

- `manual-test-projects/monorepo/` には rescript 依存がないので変更不要
- lockfile はリポジトリにコミットされていないため、`pnpm-lock.yaml` / `package-lock.json` の同期コミットは発生しない
- 既存テストが旧バージョン文字列（"11.0.0" など）を含む箇所は、それらは「11 系プロジェクトを検出できるか」を確認するテストであって fixture ではないため、触らない
