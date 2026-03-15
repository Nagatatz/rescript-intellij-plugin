# Requirements: Code Quality Quick Wins

## 概要

JUnit 5 マイグレーション完了後のリファクタリング分析で発見された、2つのクイックウィンを実装する。

## 要求内容

### 1. 空の catch ブロックにコメント追加

`RescriptFormattingService.kt` と `RescriptFormatCheckAnnotator.kt` の 4 つの空 catch ブロックに、なぜ例外を無視しているかのコメントを追加する。

**対象:**
- `RescriptFormattingService.kt:69` — stdin thread IOException
- `RescriptFormattingService.kt:84` — stderr thread IOException
- `RescriptFormatCheckAnnotator.kt:159` — stdin thread IOException
- `RescriptFormatCheckAnnotator.kt:172` — stderr thread IOException

### 2. 重複 Regex パターンの集約

`RescriptRegexPatterns.kt` に以下のパターンを追加し、使用箇所を参照に置き換える:

- **CONSTRUCTOR_PATTERN** — `^([A-Z]\w*)(?:\((.+)\))?\s*$` — 2 ファイルで重複
- **LABELED_PARAM_NAME** — `~(\w+)` — 基本形が 1 ファイルで使用、集約価値あり
- **INCLUDE_STATEMENT** — `^include\s+([A-Z][\w.]*)` — open パターンとの一貫性

## 受け入れ条件

- [ ] 4 つの空 catch ブロックすべてに意図を説明するコメントがある
- [ ] CONSTRUCTOR_PATTERN が 2 ファイルで共通パターンを使用している
- [ ] INCLUDE_STATEMENT が RescriptRegexPatterns.kt に集約されている
- [ ] LABELED_PARAM_NAME が RescriptRegexPatterns.kt に集約されている
- [ ] 既存テストがすべてパスする
- [ ] ビルドが成功する

## スコープ外

- ProcessBuilder パターンの統一（大規模、別ユニットで対応）
- 大規模ファイル分割（別ユニットで対応済み or 予定）
- 新規テストの追加（既存パターンの移動のみでロジック変更なし）
