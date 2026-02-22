---
paths:
  - "src/**/*.kt"
---

# テスト規約

**以下は強制的な行動指示であり、例外なく従うこと。**

コードの変更を行う場合、**対応するユニットテストを必ず作成・更新すること**。すべてのコード変更にはテストを含めること。

- **新機能追加時:** 新しいクラス・メソッドに対するテストを作成する
- **バグ修正時:** 修正内容を検証するリグレッションテストを作成する
- **リファクタリング時:** 既存テストが通ることを確認し、必要に応じてテストも更新する
- **テスト配置:** `src/test/kotlin/com/rescript/plugin/` 配下に、対象クラスと同じパッケージ構成で配置する（例: `highlight/RescriptBraceMatcherTest.kt`）
- **テスト命名:** `<対象クラス名>Test.kt`（例: `RescriptFoldingBuilderTest.kt`）
- **カバレッジ確認:** `./gradlew koverHtmlReport` でレポートを生成し、新規コードが十分にカバーされていることを確認する
- **tasklist.md への記載:** ステアリングワークフローの tasklist.md には、実装タスクとセットでテスト作成タスクを必ず含めること

**例外:** UI コンポーネント（Swing ベースの設定画面等）や、LSP サーバーとの結合が必須で単体テストが困難なクラスは、テスト作成を省略してよい。ただし、その場合は tasklist.md にテスト省略の理由を明記すること。

## コミット前の検証

**以下は強制的な行動指示であり、例外なく従うこと。**

コミット前に、新規作成したすべての `.kt` ファイルについて以下を確認すること:

1. `src/test/kotlin/com/rescript/plugin/<パッケージ>/<クラス名>Test.kt` が存在するか
2. 存在しない場合、免除対象に該当するか（該当する場合は tasklist.md に理由を明記）
3. 免除対象に該当しない場合、**テストを作成するまでコミットしない**

### 免除対象の判定基準

以下のすべてを満たすクラスのみ免除可:

| 免除カテゴリ | 具体例 |
|-------------|--------|
| Swing UI コンポーネント | `SettingsEditor`, `Configurable`, `ToolWindowPanel`, `WizardStep` |
| LSP サーバー結合必須 | `LspServerDescriptor`, `LspServerSupportProvider`, `Lsp4jClient` |
| IDE ライフサイクル依存 | `StartupActivity`, `ProjectManagerListener` |
| 純粋なインターフェース定義 | ロジックを持たない `interface` 宣言のみ |
| 実行構成 UI | `RunConfiguration`, `ConfigurationOptions`, `SettingsEditor` |

**上記に該当しないクラス（ユーティリティ、パーサー、データオブジェクト、テンプレート等）はテスト必須。**
