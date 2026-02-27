# Design: CI/CD パイプライン整備

## 1. CI/CD ドキュメント整備

### 1.1 CLAUDE.md 更新

「ビルド・実行コマンド」セクションに CI/CD 情報を追加する。

**追加内容:**
- CI ワークフロー一覧（ci.yml, release.yml, docs.yml, qodana）
- ローカルでの CI 再現コマンド
- テスト・カバレッジコマンド

### 1.2 Sphinx ドキュメント: CI/CD ページ追加

`sphinx-docs/dev/ci-cd.md` を新規作成し、開発者向け CI/CD ガイドを記載する。

**構成:**
1. パイプライン概要（4 ワークフローの説明）
2. ci.yml の詳細（ジョブ構成、各ステップの役割）
3. release.yml の詳細（リリースフロー）
4. docs.yml の詳細（ドキュメントビルド・デプロイ）
5. ローカルでの CI 再現手順
6. トラブルシューティング（よくある CI 失敗とその対処）

`sphinx-docs/dev/index.md` の toctree に `ci-cd` を追加する。

## 2. インテグレーションテスト追加

### 2.1 テストデータディレクトリ

`src/test/testData/` を新規作成し、各テスト種別ごとにサブディレクトリを設ける。

```
src/test/testData/
├── highlighting/    # ハイライトテスト用 .res ファイル
├── folding/         # 折りたたみテスト用 .res ファイル
├── structure/       # 構造ビューテスト用 .res ファイル
└── indent/          # インデントテスト用 .res ファイル
```

### 2.2 テストクラス設計

すべてのインテグレーションテストは `BasePlatformTestCase` を継承する。`getTestDataPath()` をオーバーライドして testData ディレクトリを参照する。

#### 2.2.1 ハイライトテスト

**クラス:** `RescriptHighlightingIntegrationTest`
**パッケージ:** `com.rescript.plugin.highlight`
**検証内容:**
- .res ファイルをパースし、レクサーが正しいトークンタイプを生成するか検証
- `myFixture.configureByText()` + `myFixture.doHighlighting()` パターン
- キーワード、文字列、コメント、数値、JSX タグなどの基本トークンを検証

#### 2.2.2 折りたたみテスト

**クラス:** `RescriptFoldingIntegrationTest`
**パッケージ:** `com.rescript.plugin.folding`
**検証内容:**
- `myFixture.testFolding()` で折りたたみ領域を検証
- testData ファイルに `<fold text='...'>...</fold>` マーカーを使用
- モジュール、関数、コメント、リージョンの折りたたみを検証

#### 2.2.3 構造ビューテスト

**クラス:** `RescriptStructureViewIntegrationTest`
**パッケージ:** `com.rescript.plugin.structure`
**検証内容:**
- `myFixture.configureByText()` でファイルを開く
- `StructureViewBuilder` 経由でツリーモデルを取得
- ツリーの階層構造（モジュール > 関数/型）を検証

#### 2.2.4 インデントテスト

**クラス:** `RescriptIndentIntegrationTest`
**パッケージ:** `com.rescript.plugin.codestyle`
**検証内容:**
- `myFixture.configureByText()` でキャレット位置を設定
- `myFixture.type('\n')` で改行を挿入
- 挿入後のインデントレベルを検証
- モジュール内、関数内、switch ケース内のインデントを検証

#### 2.2.5 パーサー統合テスト

**クラス:** `RescriptParserIntegrationTest`
**パッケージ:** `com.rescript.plugin.lang`
**検証内容:**
- `myFixture.configureByText()` で複雑な .res コードをパース
- PSI ツリー内のトップレベル宣言数を検証
- モジュールのネスト構造を検証
- エラーノードが存在しないことを検証

#### 2.2.6 レクサー統合テスト

**クラス:** `RescriptLexerIntegrationTest`
**パッケージ:** `com.rescript.plugin.lang`
**検証内容:**
- `myFixture.configureByText()` で実際のファイルをロード
- `PsiFile` からトークンを走査し、トークンタイプを検証
- 複数行にまたがるコード（文字列リテラル、テンプレートリテラル）の正確なトークナイズを検証

### 2.3 testData ファイル

各テストに対応する `.res` ファイルを testData ディレクトリに配置する。

**highlighting/Basic.res** — 基本的な ReScript コード（キーワード、関数、型、文字列、コメント、JSX）
**folding/Folding.res** — `<fold>` マーカー付き折りたたみテスト
**structure/ModuleStructure.res** — ネストされたモジュール・関数・型定義
**indent/Indent.res** — インデント検証用コード

### 2.4 既存テストとの関係

- 既存のユニットテスト（`RescriptLexerTest`, `RescriptParserTest`, `RescriptFoldingBuilderTest` 等）はそのまま維持
- インテグレーションテストは IDE プラットフォーム上での動作を検証する補完的なテスト
- 両方のテストレイヤーが `./gradlew test` で実行される

## 3. 技術的な留意点

- `BasePlatformTestCase` は JUnit 3 スタイル（`fun testXxx()` メソッド命名）が必要
- `getTestDataPath()` はプロジェクトルートからの相対パスを返す
- `myFixture.testFolding()` は testData ファイル内の `<fold>` マーカーと実際の折りたたみ領域を照合する
- CI では headless モードで実行されるため、UI 依存のテストは避ける
