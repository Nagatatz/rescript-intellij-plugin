# Design: プラグイン設定ページ

## 1. 実装アプローチ

IntelliJ Platform の `Configurable` API と `PersistentStateComponent` を使用し、プロジェクト単位の設定ページを追加する。

### 設定の永続化

`PersistentStateComponent<State>` を実装したプロジェクトサービスを作成する。設定値は XML ファイル（`.idea/rescriptSettings.xml`）に自動シリアライズされる。

### 設定 UI

`Configurable` を実装し、Settings > Languages & Frameworks > ReScript に設定パネルを表示する。`FormBuilder` を使ってフォームを構築する（既存の `RescriptSettingsEditor` と同じパターン）。

### LSP サーバーとの連携

`RescriptLspServerDescriptor.createCommandLine()` で設定値を参照し、カスタムパスが設定されている場合は自動検出をスキップする。

## 2. コンポーネント設計

### 2.1 RescriptProjectSettings（永続化サービス）

```
パッケージ: com.rescript.plugin.settings
ファイル: RescriptProjectSettings.kt
```

| 項目 | 内容 |
|---|---|
| 継承元 | `PersistentStateComponent<RescriptProjectSettings.State>` |
| スコープ | `@Service(Service.Level.PROJECT)` |
| 永続化 | `@State(name = "RescriptSettings", storages = [Storage("rescriptSettings.xml")])` |
| 役割 | LSP サーバーパス、Node.js パスの設定値を保持・永続化 |

**State データクラス:**

| フィールド | 型 | デフォルト | 説明 |
|---|---|---|---|
| `lspServerPath` | `String` | `""` | LSP サーバーの絶対パス（空 = 自動検出） |
| `nodePath` | `String` | `""` | Node.js の絶対パス（空 = PATH 上の node） |

**companion object:**

```kotlin
fun getInstance(project: Project): RescriptProjectSettings =
    project.service<RescriptProjectSettings>()
```

### 2.2 RescriptConfigurable（設定 UI）

```
パッケージ: com.rescript.plugin.settings
ファイル: RescriptConfigurable.kt
```

| 項目 | 内容 |
|---|---|
| 継承元 | `Configurable` |
| 配置場所 | Settings > Languages & Frameworks > ReScript |
| Extension Point | `projectConfigurable` |

**UI レイアウト:**

```
┌─────────────────────────────────────────────┐
│ ReScript Settings                           │
├─────────────────────────────────────────────┤
│                                             │
│ Language server path:                       │
│ ┌─────────────────────────────────────┐ ┌─┐│
│ │ (auto-detect)                       │ │…││
│ └─────────────────────────────────────┘ └─┘│
│ Leave empty to auto-detect from             │
│ node_modules or PATH.                       │
│                                             │
│ Node.js interpreter path:                   │
│ ┌─────────────────────────────────────┐ ┌─┐│
│ │ (use PATH)                          │ │…││
│ └─────────────────────────────────────┘ └─┘│
│ Leave empty to use "node" from PATH.        │
│                                             │
└─────────────────────────────────────────────┘
```

**メソッド:**

| メソッド | 処理内容 |
|---|---|
| `getDisplayName()` | `"ReScript"` を返す |
| `createComponent()` | `FormBuilder` で UI パネルを構築 |
| `isModified()` | 現在の UI 値と保存済み設定を比較 |
| `apply()` | UI 値を `RescriptProjectSettings` に書き込み |
| `reset()` | 保存済み設定を UI に反映 |

**バリデーション:**

`apply()` 時に以下を検証する:
- パスが空でない場合、指定されたファイルが存在するか確認
- 存在しない場合、`ConfigurationException` をスローしてエラー表示

### 2.3 RescriptLspServerDescriptor の変更

**変更内容:**

`createCommandLine()` メソッドで設定値を参照するように修正:

```
1. RescriptProjectSettings から設定値を取得
2. lspServerPath が設定されている場合 → そのパスを使用
3. lspServerPath が空の場合 → 従来の自動検出ロジック
4. nodePath が設定されている場合 → .js 実行時にそのパスを使用
5. nodePath が空の場合 → 従来通り "node" を使用
```

## 3. 変更するコンポーネント

| ファイル | 変更種別 | 内容 |
|---|---|---|
| `settings/RescriptProjectSettings.kt` | **新規** | 設定の永続化サービス |
| `settings/RescriptConfigurable.kt` | **新規** | 設定 UI |
| `lsp/RescriptLspServerDescriptor.kt` | **修正** | 設定値を参照するよう変更 |
| `resources/META-INF/plugin.xml` | **修正** | `projectConfigurable` と `projectService` を登録 |

## 4. plugin.xml への追加

```xml
<!-- Project settings -->
<projectConfigurable
    parentId="language"
    instance="com.rescript.plugin.settings.RescriptConfigurable"
    id="com.rescript.plugin.settings"
    displayName="ReScript"/>

<projectService
    serviceImplementation="com.rescript.plugin.settings.RescriptProjectSettings"/>
```

## 5. 影響範囲

- LSP サーバー起動ロジックに影響（設定値によるパス上書き）
- 既存の自動検出ロジックは引き続きフォールバックとして機能
- 設定未変更の場合、従来と同一の動作を保証
