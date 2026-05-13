# 設計: アイコンの色調整 + ToolWindow 13×13

## 1. グラデ起点色の差し替え

8 ファイル、各 1 行差し替え。

### Light variant 4 ファイル

`src/main/resources/icons/{rescript-file,rescript-interface,rescript-config,rescript-repl}.svg`:

```diff
-      <stop stop-color="#E84F4F"/>
+      <stop stop-color="#E6484F"/>
```

### Dark variant 4 ファイル

`src/main/resources/icons/{rescript-file,rescript-interface,rescript-config,rescript-repl}_dark.svg`:

```diff
-      <stop stop-color="#EF5E5E"/>
+      <stop stop-color="#ED5B58"/>
```

`#ED5B58` は `#E6484F` を lighten-by-7 した値で、Dark テーマでの視認性確保。

## 2. ToolWindow 13×13 単色アイコン

`src/main/resources/icons/rescript-toolwindow.svg`:

```xml
<svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 13 13" fill="none">
  <!-- ReScript R, stylised as a single vertical bar + dot to echo the FileType icon. -->
  <rect x="3" y="2.5" width="2" height="8" rx="0.5" fill="currentColor"/>
  <circle cx="9" cy="3.5" r="1.5" fill="currentColor"/>
</svg>
```

`rescript-toolwindow_dark.svg` も同内容。IntelliJ Platform は ToolWindow アイコンの色を theme から差し替えるため `currentColor` を使う。

## 3. `RescriptIcons.kt` の更新

```kotlin
@JvmField val TOOL_WINDOW = IconLoader.getIcon("/icons/rescript-toolwindow.svg", RescriptIcons::class.java)
```

## 4. `plugin.xml` 差し替え

2 行差し替え:

```diff
-        <toolWindow id="ReScript Module Diagram" anchor="right" icon="AllIcons.FileTypes.Diagram"
+        <toolWindow id="ReScript Module Diagram" anchor="right" icon="/icons/rescript-toolwindow.svg"
```

```diff
-        <toolWindow id="ReScript Switch Flow" anchor="right" icon="AllIcons.FileTypes.Diagram"
+        <toolWindow id="ReScript Switch Flow" anchor="right" icon="/icons/rescript-toolwindow.svg"
```

他の ToolWindow（Type Impact / Coverage / Interop Risk / Migration Pilot / PPX / REPL）は今回スコープ外。

## テスト

`RescriptIconsTest.kt` を新規作成:

- `RescriptIcons.TOOL_WINDOW` が null でないこと
- `RescriptIcons.FILE`, `INTERFACE_FILE`, `CONFIG_FILE` 既存も null でないこと（リグレッション防止）

SVG 中身は静的アセットなのでテスト対象外。`./gradlew buildPlugin` でアイコンが resource として梱包されているかが事実上の検証。

## 後方互換性

- 既存アイコンの形状は変えていないので、ユーザーから見た FileType アイコンの形は同じ
- ToolWindow アイコンの差し替えは 2 つだけで、残り 7 つは無変更
- public シグネチャ無変更
