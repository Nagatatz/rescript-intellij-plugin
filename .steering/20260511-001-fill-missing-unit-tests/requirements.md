# 要求内容

## 背景

2026-05-11 のリポジトリ audit で、`.claude/rules/testing.md` の「テスト必須なのにテストが無い」9 件のプロダクションファイルが特定された。いずれも純粋なデータモデル・enum・sealed class・関数型インターフェースで、UI / LSP / IDE ライフサイクル依存に該当しない。免除対象外のためテストを作成する。

## スコープ

以下 9 ファイルに対する単体テストを `src/test/kotlin/com/rescript/plugin/<同パッケージ>/<クラス名>Test.kt` として追加する:

1. `interop/RescriptInteropModel.kt` — `InteropKind` / `RiskLevel` / `InteropEntry`
2. `impact/RescriptTypeImpactModel.kt` — `TypeTarget` / `TypeRefKind` / `ReferenceEntry`
3. `migration/RescriptMigrationModel.kt` — `MigrationCandidate` / `ConversionStatus` / `ConversionResult`
4. `narrowing/RescriptHoverTypeResolver.kt` — `fun interface` の SAM 動作・`forFile` ファクトリ
5. `intention/RescriptConstructorOccurrence.kt` — `ConstructorOccurrenceKind` / `RescriptConstructorOccurrence`
6. `navigation/RescriptTypeAst.kt` — `sealed class` 全 7 ノードの構築と equality
7. `navigation/RescriptTypeSignatureSearchHit.kt` — `data class` のフィールド保持
8. `RescriptLanguage.kt` — `isCaseSensitive` / `readResolve` / シングルトン保証
9. `lsp/RescriptWorkspaceLayout.kt` — `isRescriptProject` / `nodeModulesDirs` / `lspBinCandidates` / `lspPackageDirs` / `EMPTY`

## 受け入れ条件

- 9 件のテストファイルがそれぞれ作成され、対応するパッケージに配置されている
- すべてのテストが `./gradlew test` でグリーン
- `./gradlew ktlintCheck` でも違反ゼロ
- `VirtualFile` を要するクラスは `LightVirtualFile`（既存テスト `RescriptMigrationFinderTest` と同じパターン）で代替する
- IDE 結合（PSI / Project / LSP サーバー実通信）が必要なテストは含めない（純粋ロジックのみ）
- 新規プロダクションコードは追加しない（テスト追加のみ）

## 非スコープ

- `Action` / `Panel` / `ToolWindowFactory` 系のロジック分離リファクタは別ステアリング（タスク 4）
- 既存テストの強化やカバレッジ率の数値ラチェット更新は本作業の対象外（必要なら別途）

## 参照

- `.claude/rules/testing.md`
- `.claude/rules/code-comments.md`
- 2026-05-11 audit の "HIGH 優先度" 一覧
