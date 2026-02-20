# Tasklist: プロジェクトテンプレートのバージョン更新

## 実装タスク

- [x] ブランチ `chore/update-template-versions` を作成
- [x] `RescriptProjectGenerator.kt` の `generateRescriptJson` を更新
  - `package-type` → `package-specs` 形式に変更
  - `sources` をオブジェクト形式に変更
- [x] `RescriptProjectGenerator.kt` の `generatePackageJson` を更新
  - `rescript` を `^12.0.0` に更新
  - `@rescript/core` を削除
  - scripts を `res:build` / `res:clean` / `res:dev` に変更
  - React 依存を `react ^19.0.0` / `react-dom ^19.0.0` / `@rescript/react ^0.14.0` に更新
- [x] `RescriptProjectGenerator.kt` の `generateStarterModule` を簡略化
- [x] `RescriptProjectGeneratorTest.kt` を更新
- [x] ビルド確認 (`./gradlew buildPlugin`)
- [x] tasklist.md を更新してコミット
- [x] `main` にマージ
