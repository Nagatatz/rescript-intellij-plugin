# Requirements: rescript.json サポート + Run Configuration

## 概要

ReScript プロジェクトの設定ファイル `rescript.json` の認識と、IDE 内からの ReScript ビルドコマンド実行機能を提供する。

## 背景

現在のプラグインは `.res` / `.resi` ファイルの編集機能（ハイライト、LSP 連携等）を提供しているが、プロジェクトのビルド設定ファイルである `rescript.json` の認識や、IDE 内からのビルド実行はサポートしていない。ReScript 開発者は現在、ターミナルに切り替えて `rescript build` コマンドを手動実行する必要がある。

## ユーザーストーリー

### US-R1: rescript.json のファイル認識

**ユーザーとして**、`rescript.json` ファイルが ReScript のプロジェクト設定ファイルとして認識され、専用アイコンで表示されることで、プロジェクト構造を直感的に把握したい。

**受け入れ条件:**
- [ ] `rescript.json` ファイルが専用アイコン（ReScript ロゴ）で表示される
- [ ] レガシーの `bsconfig.json` も同様に認識される
- [ ] JSON としてのシンタックスハイライトは IDE 標準の JSON サポートに委譲する

### US-R2: ReScript ビルドの実行

**ユーザーとして**、IDE の Run Configuration から ReScript プロジェクトのビルドを実行し、結果をツールウィンドウで確認できることで、ターミナルに切り替えることなく開発を進めたい。

**受け入れ条件:**
- [ ] Run Configuration のタイプ一覧に「ReScript」が表示される
- [ ] 以下のコマンドテンプレートが選択できる:
  - `rescript build` — 通常ビルド
  - `rescript build -w` — ウォッチモード（ファイル変更時に自動リビルド）
  - `rescript clean` — ビルド成果物のクリーン
- [ ] `rescript` CLI を `node_modules/.bin/` から自動検出する
- [ ] ビルド出力が Run ツールウィンドウに表示される

### US-R3: Run Configuration の設定 UI

**ユーザーとして**、Run Configuration の設定画面でコマンドやオプションをカスタマイズできることで、プロジェクトに合わせたビルド設定を柔軟に管理したい。

**受け入れ条件:**
- [ ] 設定画面に以下のフィールドがある:
  - コマンド選択（Build / Build Watch / Clean）
  - Working directory（プロジェクトルート）
  - 追加引数（任意）
- [ ] 設定は IDE 再起動後も永続化される
- [ ] 複数の Run Configuration を作成・保存できる

### US-R4: rescript.json からのプロジェクト検出

**ユーザーとして**、`rescript.json` が存在するディレクトリが ReScript プロジェクトルートとして自動的に認識されることで、Run Configuration の作業ディレクトリが適切に設定されてほしい。

**受け入れ条件:**
- [ ] `rescript.json`（または `bsconfig.json`）の存在を検出してプロジェクトルートを特定する
- [ ] Run Configuration 作成時に Working directory がプロジェクトルートにデフォルト設定される

## スコープ

### 含むもの

- `rescript.json` / `bsconfig.json` のファイルアイコン表示
- ReScript Run Configuration タイプ（Build / Build Watch / Clean）
- 設定 UI（コマンド選択、作業ディレクトリ、追加引数）
- `rescript` CLI の自動検出（`node_modules/.bin/`）
- ビルド出力の Run ツールウィンドウ表示

### 含まないもの（将来の拡張）

- `rescript.json` のスキーマバリデーションや補完（JSON Schema 提供で対応予定）
- ビルドエラーからソースファイルへのクリッカブルリンク（将来の Console Filter で対応）
- Before-Run タスク（ビルド→実行の自動チェーン）
- Run Line Marker（エディタガターの実行ボタン）
- `rescript.json` の内容に基づく高度なプロジェクト設定の自動構成

## 制約事項

- `rescript` CLI は Node.js プロジェクトの `node_modules/.bin/` に存在することを前提とする
- JSON ファイルの編集サポート（スキーマ補完等）は IDE 標準の JSON プラグインに委譲する
- Run Configuration は `DefaultRunExecutor`（通常実行）のみをサポートし、デバッグ実行はスコープ外とする
