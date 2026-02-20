# Tasklist: ReScript公式ブランドマークアイコンの16x16最適化

## SVGアセット作成

- [x] `rescript-file.svg` を公式ブランドマーク簡略版に置換
- [x] `rescript-file_dark.svg` を新規作成（ダークテーマ用）
- [x] `rescript-interface.svg` を75%透過バリアントに置換
- [x] `rescript-interface_dark.svg` を新規作成（ダークテーマ用）
- [x] `rescript-config.svg` を`{}`パス版に置換
- [x] `rescript-config_dark.svg` を新規作成（ダークテーマ用）

## 検証

- [x] `./gradlew buildPlugin` でビルド成功を確認

## コミット・マージ

- [x] 変更をコミット
- [x] `main` にマージ

## テスト

SVGアセットの置換のみでKotlinコード変更がないため、ユニットテスト作成は省略。
`./gradlew buildPlugin` による統合確認とrunIdeでの目視確認で検証する。
