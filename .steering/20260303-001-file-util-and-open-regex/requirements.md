# 要件定義書: ファイルユーティリティ統一 + open Regex 統一リファクタリング

## 概要

直近の infrastructure refactoring (#115, #116, #117) 完了後、残存する重複パターンを統一する。

## 背景

- `isRescriptFile()` が 10+ ファイルで独自実装されている
- `.res↔.resi` 検索が 6 箇所で繰り返されている
- `open` 文 Regex が 5 ファイルに散在している

## 対象範囲

### 1. RescriptFileUtil の新設

- ファイル拡張子チェック（`isRescriptFile`, `isResFile`, `isResiFile`）の集約
- ファイル名ベースのチェック（`isRescriptFileName`, `isResFileName`, `isResiFileName`）の提供
- `.res↔.resi` 対応ファイル検索（`findCounterpartFile`, `findInterfaceFile`）の集約

### 2. open 文 Regex パターンの統一

- `OPEN_STATEMENT`, `OPEN_MODULE_CAPTURE`, `OPEN_MODULE_STRICT`, `OPEN_LINE_TEST` の 4 パターンを `RescriptRegexPatterns` に追加
- 5 ファイルの重複パターンを置換

## 受け入れ条件

1. `RescriptFileUtil` が 20 ファイルで使用されている
2. open 文 Regex が `RescriptRegexPatterns` に集約されている
3. `./gradlew clean buildPlugin` が成功する
4. 既存テストがすべてパスする
5. 新規テスト: `RescriptFileUtilTest` + `RescriptRegexPatternsTest` 追加分がパスする
6. 機能的な振る舞いに変更がない（純粋なリファクタリング）
