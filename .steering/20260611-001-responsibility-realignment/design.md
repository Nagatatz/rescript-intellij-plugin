# 設計: 責務再配置 (Phase 3)

3 セクション + docs。リスク低→中の順。各セクション = 1 コミット = 独立マージ可能。

## セクション 0: ロードマップ追補

`docs/product-requirements.md` のリファクタリング候補表に追加 (既存最大 #128 + 1 から採番):

- `| 🚧 129 | RescriptLspUtils facade 解体 | リファクタリング | パース系委譲メソッド・typealias を削除し呼び出し側をパーサー直接参照に書き換え (実体は分離済み) | 低 | B |`
- `| 🚧 130 | TypeDeclarationParser の lang/ 移動 | リファクタリング | lsp→generate / util→generate の逆方向依存を解消し型宣言パースを言語基盤に配置 | 低〜中 | B |`

最初のコミットに含める。

## セクション 1: dead code 削除 (リスク極小)

- `src/test/kotlin/com/rescript/plugin/IntelliJPlatformExtensionWithContentRoot.kt` を削除 (削除直前に grep で参照 0 を三たび確認)
- `docs/repository-structure.md` §2.2 の heavy fixture 段落を削除 (`IntelliJPlatformExtensionWithContentRoot` への言及)
- 検証: `./gradlew test` green
- コミット: `🗑️ Remove unused IntelliJPlatformExtensionWithContentRoot test fixture`

## セクション 2: `RescriptLspUtils` facade 解体 (リスク低)

### 削除対象 (`RescriptLspUtils` L146-193)

- `typealias LabeledParam` / `typealias VariantInfo` / `typealias DiagnosticKind` / `typealias DiagnosticInfo`
- 委譲 fun: `parseSignatureLabels` / `parseVariantConstructors` / `parseDiagnosticMessage` / `extractParenContent` / `splitByComma`
- KDoc の delegation 言及 (L23-24, @see はパーサー 2 クラスを残す)

### 呼び出し側書き換え (機械的)

- intention 5 ファイル: `RescriptLspUtils.parseXxx(...)` → `RescriptLspSignatureParser.parseXxx(...)` 等、型参照 `RescriptLspUtils.VariantInfo` → `RescriptLspSignatureParser.VariantInfo` 等。import 追加。`getHoverType` 等 LSP 通信系の参照はそのまま
- `RescriptMissingArmsBuilderTest`: 型参照の書き換えのみ (ロジック不変)

### テスト整理

- `RescriptLspUtilsTest`: パース系 16 ケースを削除する**前に**、各ケースの入力・期待値が `RescriptLspSignatureParserTest` / `RescriptLspDiagnosticParserTest` に等価ケースとして存在するか突合する。欠落分はパーサーテスト側へ移設してから削除。URI 変換 4 ケースは残す
- 検証: `./gradlew ktlintCheck test` green
- コミット: `♻️ Dissolve RescriptLspUtils parse facade into direct parser references`

## セクション 3: `RescriptTypeDeclarationParser` の `lang/` 移動 (リスク低〜中)

### 移動

- `generate/RescriptTypeDeclarationParser.kt` → `lang/RescriptTypeDeclarationParser.kt` (package 行と KDoc の文脈以外は不変。`git mv` 後に package 修正)
- `generate/RescriptTypeDeclarationParserTest.kt` → `lang/RescriptTypeDeclarationParserTest.kt` (同様)

### import 書き換え (7 ファイル)

- generate 5: `RescriptJsonCodeGenerator` / `RescriptGenerateSwitchAction` / `RescriptGenerateMakeAction` / `RescriptGenerateJsonCodecAction` / `RescriptGenerateRecordValueAction`
- `lsp/RescriptVariantTypeResolver` (lsp → generate 依存の解消)
- `util/RescriptRegexPatterns` (util → generate 依存の解消)
- テスト 2: `RescriptJsonCodeGeneratorTest` / `RescriptGenerateSwitchActionTest`

### kover (本フェーズ最大の注意点)

- generate のクラス除外パターンに `RescriptTypeDeclarationParser` が含まれている場合は build.gradle.kts から該当行を削除 (lang 移動後は除外不要 — テスト同梱で対象化する)。含まれていない (generate 全体除外でない) 場合は確認のみ
- マージ前に `koverHtmlReport` で `lang/RescriptTypeDeclarationParser` の line カバレッジと総合 86% 維持を実測確認
- 検証: `./gradlew ktlintCheck test koverVerify` green
- コミット: `♻️ Move RescriptTypeDeclarationParser from generate to lang`

## セクション 4: docs 同期

- `docs/repository-structure.md`: lang/ 行に `RescriptTypeDeclarationParser` 追記、generate/ 行から「型宣言 RHS の再パース」を削除、lsp/ 行の説明調整 (variant 型の bare-name 解決は残る)
- CLAUDE.md: 「Add Missing Switch Arms Intention」段落の `RescriptTypeDeclarationParser` 言及はパッケージ非依存の記述なら変更不要 (実装時確認)。repository-structure.md の参照で吸収
- `docs/product-requirements.md`: #129 / #130 を削除
- コミット: `📝 Sync docs for Phase 3 responsibility realignment`

## リスクと緩和

| リスク | 緩和策 |
|---|---|
| facade 削除でテストカバレッジが下がる (委譲行が消えるのは分子減だが lsp は除外パッケージ) | lsp は `com.rescript.plugin.lsp.*` ごと kover 除外のため影響なし。それでも koverVerify をセクションごとに実行 |
| parser 移動で kover 分母が増え 86% を割る | 既存テスト (RescriptTypeDeclarationParserTest) を同時移動。マージ前に実測。万一不足ならテスト追補で回復 (ラチェットは下げない) |
| テスト突合の見落とし (等価ケースなしで削除) | 16 ケースを 1 件ずつ入力文字列で grep 突合し、結果を tasklist に記録 |
