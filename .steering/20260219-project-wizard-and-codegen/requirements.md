# Requirements: Project Wizard + Code Generation

## 概要

P3 残りの2機能を実装する:
1. **Project Wizard** — New Project ダイアログに ReScript テンプレートを追加
2. **Code Generation** — Code > Generate メニューに variant switch / module type 生成を追加

## Feature 1: Project Wizard

### ユーザーストーリー
- 開発者として、IntelliJ の New Project ダイアログから ReScript プロジェクトを作成したい
- パッケージマネージャ（npm/pnpm/yarn）を選択したい
- React プロジェクトかどうかを選択したい

### 受け入れ条件
- New Project に "ReScript" エントリが表示される
- パッケージマネージャ選択 UI が表示される
- React チェックボックスが表示される
- 選択に応じて rescript.json, package.json, App.res が生成される
- src/ がソースルートとして設定される

## Feature 2: Code Generation

### ユーザーストーリー
- 開発者として、variant 型から switch arms を自動生成したい (Cmd+N)
- 開発者として、module から module type スケルトンを自動生成したい (Cmd+N)

### 受け入れ条件
- Code > Generate メニューに "ReScript" サブメニューが表示される
- ReScript ファイルでのみ表示される
- variant 型の TYPE_DECLARATION にカーソルがあるとき、switch arms が生成できる
- MODULE_DECLARATION にカーソルがあるとき、module type が生成できる
- テキストベースの型解析が正しく動作する（simple variant, payload variant, record）

## 制約事項
- 軽量パーサーは型の内部構造を解析しないため、テキストベースの解析を使用
- UI コンポーネント（Swing）はテスト省略可
