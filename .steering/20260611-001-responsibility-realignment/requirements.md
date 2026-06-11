# 要求内容: 責務再配置 (完全リファクタリング Phase 3)

## 背景

完全リファクタリング計画の Phase 3。lsp パッケージの facade 解体・型宣言パーサーの配置是正・dead code 削除により、責務配置の歪みを解消する。ロードマップ未登録のため、着手時に候補表へ追補行を追加してから 🚧 を付ける。

## 対象 (実コードで再検証済み・2026-06-11)

### 1. `lsp/RescriptLspUtils` の facade 解体

- 実体は既に `RescriptLspSignatureParser` / `RescriptLspDiagnosticParser` に分離済みで、`RescriptLspUtils` L146-193 は委譲メソッド + typealias のみの間接層
- パース系参照は intention 5 ファイル (`RescriptCaseSplitIntention` / `RescriptMissingArmsBuilder` / `RescriptConvertToLabeledArgsIntention` / `RescriptAddMissingSwitchArmsIntention` / `RescriptInsertLabeledArgsIntention`) + テスト 2 ファイル
- `RescriptLspUtilsTest` は 20 ケース中 16 件が facade 経由のパース系 (URI 変換 4 件のみ固有)。パーサーテスト側は 43 + 16 ケース

### 2. `generate/RescriptTypeDeclarationParser` の `lang/` 移動

- 「ReScript 型宣言 RHS のパース」は言語処理であり、現配置は以下の歪んだ依存を生んでいる:
  - `lsp/RescriptVariantTypeResolver` → `generate/` (lsp → generate の逆方向依存)
  - `util/RescriptRegexPatterns` → `generate/` (基盤 util が機能パッケージに依存)
- 参照: main 7 ファイル (generate 5 + lsp 1 + util 1) + テスト 3 ファイル
- kover: generate はクラス単位除外下、lang は対象 → 移動でカバレッジ分母に入る (既存テスト同梱で中立〜微増見込み、マージ前に実測確認)

### 3. dead code 削除

- `src/test/.../IntelliJPlatformExtensionWithContentRoot.kt` (99 行): 参照 0 を 2026-06-10 と本日の 2 回確認済み
- `docs/repository-structure.md` の heavy fixture 言及 (§2.2) も同時更新

## 要求

1. `RescriptLspUtils` から委譲メソッド・typealias を削除し、LSP 通信専任 (`getServer` / URI 変換 / `getHoverType` 等) に縮小する。呼び出し側はパーサー直接参照に書き換える
2. `RescriptTypeDeclarationParser` を `generate/` から `lang/` へ移動する (クラス名不変、パッケージのみ変更)
3. `IntelliJPlatformExtensionWithContentRoot.kt` を削除する
4. ロードマップ候補表に #129 (facade 解体) / #130 (parser 移動) を追加 → 🚧 → 完了時に削除

## 受け入れ条件

- [ ] intention / generate / lsp / util の既存テストが green (パーサー直接参照への書き換えと import 変更以外、テストロジックは不変)
- [ ] `RescriptLspUtilsTest` のパース系 16 ケースは、パーサーテスト側に等価ケースが存在することを突合してから削除 (欠落があれば移設)
- [ ] `./gradlew ktlintCheck clean buildPlugin test koverVerify verifyPluginStructure` green (minBound 86 維持)
- [ ] `koverHtmlReport` で `lang/RescriptTypeDeclarationParser` のカバレッジを確認 (移動で 86% を毀損しない)
- [ ] docs (repository-structure.md / product-requirements.md) 同期。sphinx 更新なし (機能不変)

## スコープ外

- `RescriptLspSignatureParser` / `RescriptLspDiagnosticParser` 自体の lang/ 移動 (LSP 応答のパースなので lsp 所属で正当)
- typeinfo の Alarm 置換 (Phase 4)
- runIde 手動スモーク (UI 変更なし・コンパイラとテストで担保できるため不要)

## 実装時のスコープ変更 (2026-06-11 追記)

要求 3 (`IntelliJPlatformExtensionWithContentRoot.kt` の削除) は**中止**した。削除直前の再 grep を docs/ まで広げたところ、`docs/good-first-issues.md` Issue #9 が本 fixture を使う heavy-fixture テスト追加をコントリビュータタスクとして明示参照していることが判明 (Phase 0 で v1-followups からマージされた文書)。コード参照 0 だが文書化された利用予定があるため dead code ではない。詳細は tasklist.md セクション 1 を参照。
