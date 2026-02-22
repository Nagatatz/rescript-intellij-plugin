# Requirements: External Annotator (Format Check)

## 概要

`rescript format` CLI を利用して、エディタ上で未フォーマットのコードに対して警告アノテーションを表示する ExternalAnnotator を実装する。

## 背景

- 既に `RescriptFormattingService` が `rescript format --stdin` を使用したフォーマット機能を提供している
- `RescriptReanalyzeAnnotator` が ExternalAnnotator パターンの実装例として存在する
- ユーザーはフォーマットされていないコードを IDE 上で即座に認識し、ワンクリックで修正できるべき

## 機能要件

### FR-1: フォーマットチェック

- `.res` / `.resi` ファイルに対して `rescript format --stdin` を実行し、出力と現在のエディタ内容を比較する
- 差分がある場合、ファイル先頭に INFO レベルのアノテーションを表示する
- メッセージ: `"Code is not formatted. Use Cmd+Option+L to format."` (macOS) / `"Code is not formatted. Use Ctrl+Alt+L to format."` (Windows/Linux)

### FR-2: Quick Fix

- アノテーションに Quick Fix を付与する
  - **"Format this file"**: 既存の `RescriptFormattingService` を呼び出してファイルをフォーマットする

### FR-3: 設定

- `Settings > Languages & Frameworks > ReScript` に ON/OFF トグルを追加する
  - 設定名: `Format check enabled` (デフォルト: OFF)
  - OFF の場合、ExternalAnnotator は `collectInformation` で null を返しスキップする

### FR-4: パフォーマンス

- プロセスには `RescriptSecurityUtils.PROCESS_TIMEOUT_SECONDS` のタイムアウトを適用する
- `doAnnotate` はバックグラウンドスレッドで実行する（ExternalAnnotator の標準動作）
- CLI バイナリが見つからない場合は静かにスキップする（エラー表示なし）

## 非機能要件

- 既存の `RescriptCliDetector` を使用して CLI パスを検出する
- `RescriptSecurityUtils` のセキュリティパターンに従う
- `RescriptReanalyzeAnnotator` と同様の3フェーズパターンを採用する

## 受け入れ条件

1. フォーマット済みファイルではアノテーションが表示されない
2. 未フォーマットファイルでは INFO レベルのアノテーションが表示される
3. Quick Fix でファイルがフォーマットされ、アノテーションが消える
4. 設定で OFF にするとアノテーションが表示されない
5. rescript CLI が見つからない場合はエラーなくスキップする
6. ビルドが通る (`./gradlew clean buildPlugin`)
7. ユニットテストがすべてパスする
