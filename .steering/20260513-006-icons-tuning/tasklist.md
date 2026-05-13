# タスクリスト: アイコン色調整 + ToolWindow

## セクション A: グラデ起点色を #E6484F に揃える

- [x] 4 つの light SVG (`rescript-file`, `rescript-interface`, `rescript-config`, `rescript-repl`)
- [x] 4 つの dark SVG (同上)

## セクション B: ToolWindow 13×13 アイコン

- [x] `rescript-toolwindow.svg` + `rescript-toolwindow_dark.svg` を新規作成
- [x] `RescriptIcons.kt` に `TOOL_WINDOW` 追加
- [x] `plugin.xml` の Module Diagram / Switch Flow ToolWindow icon を差し替え
- [x] `RescriptIconsTest.kt` を新規作成

## セクション C: 検証 + コミット

- [x] `./gradlew ktlintCheck` グリーン
- [x] `./gradlew clean buildPlugin && ./gradlew test --rerun-tasks` グリーン
- [x] `🎨 Align icon gradients to ReScript brand color and add tool window mark` でコミット

## セクション D: マージ

- [x] tasklist の全項目を `[x]` に更新（このコミットに含める）
- [x] `main` にマージ、worktree クリーンアップ
