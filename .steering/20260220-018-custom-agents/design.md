# Design: Custom Agents

## 概要

`.claude/agents/` ディレクトリに2つのカスタムサブエージェント定義ファイルを配置する。

## ディレクトリ構成

```
.claude/
└── agents/
    ├── code-reviewer.md    # コード品質レビューエージェント
    └── build-resolver.md   # ビルドエラー修正エージェント
```

## エージェント設計

### 1. code-reviewer.md

**目的:** コード変更のレビューを自動化し、プロジェクト規約への準拠を確認する。

**YAML フロントマター:**
```yaml
---
allowed-tools:
  - Read
  - Glob
  - Grep
model: sonnet
---
```

**プロンプト構成:**
1. ロール定義（IntelliJ Plugin コード品質レビュアー）
2. レビュー観点の列挙（5項目）
3. 出力フォーマットの指定（マークダウン表）

**レビュー項目:**
| # | チェック項目 | 確認方法 |
|---|------------|---------|
| 1 | KDoc コメント | `class`, `object`, `fun` 定義に KDoc があるか Grep |
| 2 | Extension Point 登録 | 新規クラスが `plugin.xml` に登録されているか確認 |
| 3 | テストファイル | 対応する `*Test.kt` が存在するか Glob |
| 4 | 自動生成ファイル保護 | `RescriptFlexLexer.java` の変更がないか確認 |
| 5 | パッケージ構成 | `com.rescript.plugin.*` 配下にあるか確認 |

### 2. build-resolver.md

**目的:** ビルドエラーの原因を特定し、具体的な修正提案を行う。

**YAML フロントマター:**
```yaml
---
allowed-tools:
  - Read
  - Glob
  - Grep
  - Bash
model: sonnet
---
```

**プロンプト構成:**
1. ロール定義（Gradle ビルドエラー修正スペシャリスト）
2. エラー分類カテゴリの定義
3. 解析手順の指定
4. 出力フォーマットの指定

**エラー分類:**
| カテゴリ | 対象 |
|---------|------|
| Kotlin コンパイルエラー | 型エラー、未解決参照、構文エラー |
| Gradle 設定エラー | build.gradle.kts の設定問題 |
| 依存関係エラー | ライブラリバージョン不整合 |
| IntelliJ Platform API 互換性 | API 変更による非互換 |

## 設計上の判断

- **code-reviewer は読み取り専用:** コード変更はユーザーが判断して行うべきため、`Bash` や `Edit` を許可しない
- **build-resolver に Bash を許可:** ビルドコマンドの再実行やエラーログの取得に必要
- **モデルは sonnet:** レビューやエラー解析は sonnet で十分な精度が得られ、コスト効率が良い
