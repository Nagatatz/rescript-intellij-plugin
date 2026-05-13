# 要求: Migration Pilot 機能の削除

## 背景

`migration/` パッケージは Reason → ReScript の一括変換 ToolWindow として導入されたが、`rescript convert` サブコマンドが ReScript 12 (rewatch ベース CLI) で削除されたため、本機能の前提が崩れている。先行の 20260513-007 でも「v12 を検出したら早期にエラーを返す」というガードを入れたが、ユーザーが rescript@12 を使う環境ではいずれにせよ実用上動作しない。

「Pin rescript@^11 して使ってください」という運用は実質的に新規プロジェクトでは選択肢にならず、保守コスト（コード・テスト・E2E heavy fixture・ドキュメント・日本語訳）に見合わない。

## 目的

Migration Pilot 機能をリポジトリから完全に削除する。コード・テスト・登録・ドキュメント (EN/JA) のすべてから痕跡を除去し、`./gradlew clean buildPlugin test` が緑のまま `main` にマージできる状態にする。

## 受け入れ条件

- [ ] `src/main/kotlin/com/rescript/plugin/migration/` 配下のすべての `.kt` が削除されている
- [ ] `src/test/kotlin/com/rescript/plugin/migration/` 配下のすべての `.kt` が削除されている
- [ ] `src/test/kotlin/com/rescript/plugin/cli/RescriptMigrationConverterCliTest.kt` が削除されている
- [ ] `src/main/resources/icons/rescript-migration.svg` が削除されている
- [ ] `src/main/resources/META-INF/plugin.xml` から Migration Pilot 関連の `<toolWindow>` と `<action>` が削除されている
- [ ] `ExternalCliAvailability.isRescriptCliAvailable` が削除されている（他に呼び出し元がない）
- [ ] `CLAUDE.md`, `README.md`, `docs/repository-structure.md`, `docs/functional-design.md`, `docs/lsp-fallback-matrix.md` から Migration Pilot の記述が削除されている
- [ ] `sphinx-docs/user/features/advanced.md` から該当セクションが削除されている
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po` が `make update-po` で再生成され、削除に追随している
- [ ] `make build-ja` が成功する
- [ ] `./gradlew ktlintCheck clean buildPlugin test` が成功する
- [ ] 単一の `🗑️` コミットで完結している

## 非対象

- Reason → ReScript の独自コンバータの新規実装は本作業の対象外
- `docs/product-requirements.md` への新規 US 追加は不要（Migration Pilot に対応する US は存在しない）

## リスク

- Kover カバレッジ `minBound` への影響 → 削除後の実測値で `koverVerify` が落ちないか確認が必要。落ちる場合は `minBound` を下げない方針なので、別途テスト補強で対処する
