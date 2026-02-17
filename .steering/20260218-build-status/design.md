# Design: ビルドステータスウィジェット

## アーキテクチャ

### データフロー

```
LSP Server → rescript/compilationStatus → RescriptLsp4jClient
  → RescriptCompilationStatusService.updateStatus()
    → listener callback
      → RescriptCompilerStatusWidget が statusBar.updateWidget() で UI 更新
```

## 新規ファイル

### `src/main/kotlin/com/rescript/plugin/statusbar/RescriptCompilerStatusWidgetFactory.kt`

- `StatusBarWidgetFactory` を実装（ファクトリ）
- 内部クラスとして `RescriptCompilerStatusWidget` を実装
  - `StatusBarWidget` + `StatusBarWidget.TextPresentation` を実装
  - `install()` で `RescriptCompilationStatusService.addListener()` を購読
  - `getText()` で状態に応じたテキストを返す
  - `getTooltipText()` で詳細情報を返す
- `isAvailable()`: プロジェクトルートに `rescript.json` が存在するか確認

## 変更ファイル

### `plugin.xml`

```xml
<statusBarWidgetFactory id="RescriptCompilerStatus"
    implementation="com.rescript.plugin.statusbar.RescriptCompilerStatusWidgetFactory"/>
```

## テスト

### `src/test/kotlin/com/rescript/plugin/statusbar/RescriptCompilerStatusWidgetFactoryTest.kt`

- `getText()` の各状態テスト（compiling, success, error, warning, unknown）
- `getTooltipText()` の各状態テスト
- テスト省略: `isAvailable()` は `VirtualFile` 操作が必要で単体テスト困難
