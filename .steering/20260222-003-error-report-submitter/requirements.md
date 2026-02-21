# 要求定義: GitHub エラーレポート (#110)

## 概要

プラグインの未処理例外発生時に、IDE のエラーダイアログから GitHub Issues へバグレポートを送信できる機能を実装する。`com.intellij.errorHandler` Extension Point を使用し、ブラウザベース方式で実現する。

## 背景・目的

- 現状、プラグインで例外が発生した場合、ユーザーは IDE のエラーダイアログを見るだけで、開発者に報告する手段がない
- ブラウザベース方式（GitHub New Issue URL を開く）は依存ゼロ・シークレット不要で、detekt、idea-clang-format 等の著名プラグインが採用している実績ある手法
- ユーザーが Issue を送信する前に内容を確認・編集できるため、プライバシー面でも安全

## 機能要件

### FR-01: エラーレポートボタン

- プラグインのクラスから発生した未処理例外のエラーダイアログに "Report to GitHub" ボタンを表示する
- ボタン押下時、GitHub Issues の New Issue ページをユーザーのデフォルトブラウザで開く

### FR-02: Issue URL の生成

以下の情報を含む GitHub Issue URL を生成する:

- **タイトル**: 例外クラス名とメッセージの先頭行
- **ラベル**: `bug`, `auto-report`
- **本文**:
  - ユーザーが入力した追加説明（`additionalInfo`）
  - スタックトレース（コードブロック形式、Windows URL 制限を考慮し最大 1,400 文字で切り詰め）
  - 環境情報:
    - プラグインバージョン
    - IDE 名・バージョン
    - OS 名・バージョン
    - JDK バージョン

### FR-03: スタックトレース取得

- `IdeaLoggingEvent.throwableText` を使用する（`event.throwable.stackTrace` は `TextBasedThrowable` ラッパーのため使用禁止）
- URL 長制限（Windows で約 2,000 文字）を考慮し、スタックトレースは最大 1,400 文字で切り詰める

## 非機能要件

- 外部ライブラリへの依存を追加しない（IntelliJ Platform の `BrowserUtil` のみ使用）
- GitHub API トークン等のシークレット管理は不要
- EDT 上で `submit()` が呼ばれるため、ブラウザを開くだけで重い処理は行わない

## スコープ外

- GitHub API を使った自動 Issue 作成（トークン管理が必要なため）
- Sentry/Bugsnag 等のエラートラッキングサービス連携
- 重複 Issue の自動検出
