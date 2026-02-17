# Design: Console Filter

## 実装アプローチ

IntelliJ Platform の `ConsoleFilterProvider` extension point を使用して、コンソール出力テキストからファイルパス:行番号パターンを検出し、クリック可能なハイパーリンクに変換する。

## 新規ファイル

- `src/main/kotlin/com/rescript/plugin/run/RescriptConsoleFilterProvider.kt`

## 変更ファイル

- `src/main/resources/META-INF/plugin.xml` — `consoleFilterProvider` の登録追加

## コンポーネント設計

### RescriptConsoleFilterProvider

| 項目 | 内容 |
|---|---|
| 継承元 | `com.intellij.execution.filters.ConsoleFilterProvider` |
| 役割 | プロジェクトに紐づく `Filter` インスタンスを返す |

### RescriptConsoleFilter（内部クラス）

| 項目 | 内容 |
|---|---|
| 継承元 | `com.intellij.execution.filters.Filter` |
| 役割 | コンソール出力の各行をスキャンし、ファイルパスパターンをハイパーリンクに変換 |

### 正規表現パターン

```
\s*((?:[A-Za-z]:)?[^\s:]+\.resi?):(\d+):(\d+)
```

- グループ1: ファイルパス（`.res` / `.resi` で終わる）
- グループ2: 行番号
- グループ3: 列番号

### 動作フロー

1. コンソール出力の各行に対して `applyFilter()` が呼ばれる
2. 正規表現でファイルパス:行:列パターンをマッチ
3. マッチした場合、ファイルパスを解決（相対パスの場合は `basePath` を基準に）
4. `VirtualFileManager` でファイルを検索
5. ファイルが存在すれば `OpenFileHyperlinkInfo` を作成
6. `Filter.Result` にハイパーリンク情報を設定して返す

## plugin.xml 変更

```xml
<consoleFilterProvider implementation="com.rescript.plugin.run.RescriptConsoleFilterProvider"/>
```
