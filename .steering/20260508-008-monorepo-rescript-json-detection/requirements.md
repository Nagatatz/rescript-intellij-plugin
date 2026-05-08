# Requirements: モノレポ対応（rescript.json がサブディレクトリにある場合）

## 背景

ユーザーが pnpm/npm/yarn ワークスペース構成のリポジトリを IntelliJ で開いた際、ルート直下に `rescript.json` がなく、`packages/<name>/rescript.json` のようにサブディレクトリだけに存在するケースで以下の問題が発生している:

- `RescriptMissingConfigInspection` が「rescript.json not found in project root. LSP features may not work correctly.」を誤って表示する。
- `RescriptLspDetector.isRescriptProject()` は親方向にしか走査しないため、startup notification・status bar widget・reanalyze server・file type recovery など複数機能の判定が「ReScript プロジェクトではない」と誤認する。
- `RescriptLspServerDescriptor.findLanguageServer()` は親方向と global PATH しか見ないため、pnpm モノレポで `packages/core/node_modules/.bin/rescript-language-server` を見つけられない。

実例: `~/Documents/repos/rescript-tauri/`（root に `pnpm-workspace.yaml` のみ、`packages/core/rescript.json` あり）。

## ユーザーストーリー

**ReScript モノレポ開発者として**、ワークスペース root を IntelliJ で開いたときに、サブディレクトリにある `rescript.json` を自動で検出してほしい。誤った警告を出されたくない。複雑なレイアウトでも明示的に package root を指定できる「逃げ道」がほしい。

## 受け入れ条件

- [x] root に `pnpm-workspace.yaml` + `packages/core/rescript.json` がある場合、Inspection 警告が出ない
- [x] 同レイアウトで startup balloon の判定が正しく動作する（LSP がインストールされていれば通知が出ず、無ければ通知が出る）
- [x] 同レイアウトでステータスバー widget が表示される
- [x] root に `package.json#workspaces` (npm/yarn/bun) がある場合も同様に検出される
- [x] workspace ファイルがなくても、depth ≤ 4 の再帰スキャンで `nested/path/rescript.json` を発見する
- [x] Settings > ReScript に "Project package roots"（複数行入力）を追加。非空のときは自動検出を上書きする
- [x] 既存の単純構成（root に `rescript.json` 直接配置）は引き続き動作する
- [x] サブディレクトリで開いて親に `rescript.json` がある既存挙動を破壊しない
- [x] LSP バイナリ探索が、検出済み package root の `node_modules/.bin/rescript-language-server` を優先順位に組み込む
- [x] 既存テスト全件 PASS、新規ユニットテストを追加

## 非機能要件

- ワークスペース全体の depth-limited スキャンが大規模リポジトリでも数百ミリ秒以内に終わる（除外ディレクトリで `node_modules` 等を確実にスキップ）
- YAML パースは pnpm-workspace.yaml の `packages:` リスト形式のみをサポート（複雑な YAML には非対応で問題ない）
- セキュリティ: glob 展開時に project.basePath の外に出ないことを保証

## 範囲外

- `lerna.json` のサポート（必要になったら追加）
- glob の否定パターン (`!packages/foo`) や brace 展開
- 各 package root に対する個別の LSP working directory 設定（v1 ではワークスペース root のまま）
