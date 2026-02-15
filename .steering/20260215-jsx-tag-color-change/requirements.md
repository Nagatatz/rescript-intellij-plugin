# Requirements: JSX タグ名の色変更

## 概要
JSX タグ名（`div`, `span` 等）のハイライト色を赤系から Teal/Cyan 系に変更する。

## 背景
現在の赤色ハイライトが IntelliJ のエラー表示と紛らわしいため、視認性を改善する。

## 受け入れ条件
- Darcula テーマ: `RESCRIPT_MARKUP_TAG` の色が `E06C75`（赤）→ `56B6C2`（Teal/Cyan）
- Default テーマ: `RESCRIPT_MARKUP_TAG` の色が `800000`（暗赤）→ `008080`（Teal）
- `./gradlew buildPlugin` が成功すること
