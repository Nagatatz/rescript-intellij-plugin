# 設計書: GitHub エラーレポート (#110)

## アーキテクチャ

### コンポーネント構成

```
com.rescript.plugin.error/
└── RescriptErrorReportSubmitter.kt  # ErrorReportSubmitter 実装
```

1 ファイル・1 クラスの最小構成。

### クラス設計

#### `RescriptErrorReportSubmitter`

`com.intellij.openapi.diagnostic.ErrorReportSubmitter` を継承し、以下をオーバーライドする:

| メソッド | 役割 |
|---------|------|
| `getReportActionText()` | エラーダイアログのボタンラベル ("Report to GitHub") |
| `submit(events, additionalInfo, parentComponent, consumer)` | Issue URL を組み立て、ブラウザで開く |

#### ヘルパーメソッド（private）

| メソッド | 役割 |
|---------|------|
| `buildGitHubIssueUrl(events, additionalInfo)` | URL パラメータ付きの GitHub New Issue URL を構築 |
| `buildIssueTitle(events)` | 例外メッセージから Issue タイトルを生成 |
| `buildIssueBody(events, additionalInfo)` | Markdown 形式の Issue 本文を生成 |
| `getStacktraceText(events)` | スタックトレースを取得・切り詰め（最大 1,400 文字） |
| `getEnvironmentInfo()` | IDE/プラグイン/OS/JDK バージョン情報を収集 |

### Issue 本文テンプレート

```markdown
### Description

{ユーザー入力の追加説明}

### Stacktrace

```
{スタックトレース（最大 1,400 文字）}
```

### Environment

- Plugin version: {プラグインバージョン}
- IDE: {IDE名} {ビルド番号}
- OS: {OS名} {OSバージョン}
- JDK: {JDKバージョン}
```

### Extension Point 登録

`plugin.xml` に以下を追加:

```xml
<extensions defaultExtensionNs="com.intellij">
    <errorHandler implementation="com.rescript.plugin.error.RescriptErrorReportSubmitter"/>
</extensions>
```

### 使用する IntelliJ Platform API

| API | 用途 |
|-----|------|
| `ErrorReportSubmitter` | エラーレポート Extension Point の基底クラス |
| `IdeaLoggingEvent.throwableText` | スタックトレースの安全な取得 |
| `SubmittedReportInfo` | レポート送信結果のコールバック |
| `BrowserUtil.browse(url)` | デフォルトブラウザで URL を開く |
| `ApplicationInfo.getInstance()` | IDE バージョン情報の取得 |
| `PluginManagerCore.getPlugin()` | プラグインバージョンの取得 |
| `SystemInfo` | OS 情報の取得 |

### 注意事項

- `event.throwable` は `TextBasedThrowable` ラッパーを返すため、スタックトレースは必ず `event.throwableText` から取得する
- URL エンコードには `URLEncoder.encode(text, "UTF-8")` を使用する
- Windows の URL 長制限（約 2,000 文字）を考慮し、スタックトレースは 1,400 文字で切り詰める
- GitHub リポジトリ URL はコンパニオンオブジェクトの定数として定義する

### テスト方針

- URL 構築ロジック（タイトル生成、本文生成、スタックトレース切り詰め）をユニットテストする
- `BrowserUtil.browse()` の呼び出しや `ErrorReportSubmitter` のライフサイクルは IDE 統合テストが必要なため、ユニットテストの対象外とする
