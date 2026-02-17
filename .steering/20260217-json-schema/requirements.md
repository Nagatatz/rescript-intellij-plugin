# Requirements: JSON Schema for rescript.json

## 概要

`rescript.json` / `bsconfig.json` に対して JSON Schema を提供し、プロパティ補完・バリデーションを有効にする。

## ユーザーストーリー

ReScript 開発者として、`rescript.json` を編集する際にプロパティ名の補完候補が表示され、不正なプロパティに対してバリデーション警告が表示されることで、設定ファイルの編集を効率的かつ正確に行いたい。

## 受け入れ条件

- `rescript.json` でプロパティ名の補完が効く
- 不正なプロパティに対してバリデーション警告が表示される
- `bsconfig.json` にも同じスキーマが適用される
- JSON プラグインが無効な環境でもプラグイン全体が正常に動作する（optional dependency）

## 制約事項

- JSON プラグイン（`com.intellij.modules.json`）への依存は optional とする
- スキーマは rescript-compiler 公式リポジトリの `build-schema.json` をベースとする
