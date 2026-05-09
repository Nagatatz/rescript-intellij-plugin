# Type Coverage Heat Map — Tasklist

## 実装

- [x] T1: `RescriptTypeCoverageModel.kt` — `LetCoverage` enum, `FileCoverage` / `ProjectCoverage` data classes + テスト
- [x] T2: `RescriptTypeCoverageClassifier.kt` — pure object + classifier テスト (30+ ケース)
- [x] T3: `RescriptTypeCoverageScanner.kt` — `FileTypeIndex` 走査 + scanner テスト
- [x] T4: `RescriptTypeCoveragePanel.kt` — Swing UI (テスト免除: Swing UI)
- [x] T5: RefreshAction は Panel 内 inner class として実装 (テスト免除: IDE lifecycle)
- [x] T6: `RescriptTypeCoverageToolWindowFactory.kt` (テスト免除: ToolWindowFactory)
- [x] T7: `plugin.xml` に `<toolWindow>` 登録
- [x] T8: `build.gradle.kts` kover excludes に Panel / ToolWindowFactory を追加

## 検証

- [x] `./gradlew ktlintCheck` が成功する
- [x] `./gradlew checkKdoc` が成功する
- [x] `./gradlew test` が成功する
- [x] `./gradlew koverVerify` が minBound=86 を維持する
- [x] `./gradlew verifyPluginStructure` が成功する

## ドキュメント更新

- [ ] CLAUDE.md レイヤー 3 に Type Coverage Heat Map を追記
- [ ] docs/repository-structure.md に `coverage/` 行を追加
- [ ] README.md Features セクションに項目追加
- [ ] sphinx-docs/user/features/advanced.md に機能解説を追加
- [ ] 同一コミットで `make gettext && make update-po` を実行し `.po` 翻訳を埋める
- [ ] `make build-ja` が成功することを確認

## マージ

- [ ] 機能単位コミット粒度: (a) Model+Classifier+Tests / (b) Scanner+Panel+Action+Factory+plugin.xml+kover / (c) Docs
- [ ] AskUserQuestion でマージ可否確認
- [ ] main へ fast-forward マージ & origin に push
- [ ] CI 緑を確認
