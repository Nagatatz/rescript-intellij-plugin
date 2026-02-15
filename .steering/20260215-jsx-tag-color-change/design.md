# Design: JSX タグ名の色変更

## 変更対象
- `src/main/resources/colorSchemes/RescriptDarcula.xml` - Darcula テーマ用カラースキーム
- `src/main/resources/colorSchemes/RescriptDefault.xml` - Default テーマ用カラースキーム

## 変更内容
各ファイルの `RESCRIPT_MARKUP_TAG` の `FOREGROUND` 値を変更するのみ。
コードロジックの変更は不要。

## 影響範囲
カラースキーム XML のみ。レクサー、パーサー、PSI には影響なし。
