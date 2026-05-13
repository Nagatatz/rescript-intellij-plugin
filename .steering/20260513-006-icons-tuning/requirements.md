# 要求: アイコンの色を ReScript ブランドに寄せる

## 背景

サンドボックステストで「プラグイン関係のアイコンは色を ReScript 風に寄せてくれないとわからない」と要望があった。

現状の FileType アイコン:

- `rescript-file.svg` / `_dark.svg` — グラデ start `#E84F4F` (light) / `#EF5E5E` (dark)
- `rescript-interface.svg` / `_dark.svg` — 同上
- `rescript-config.svg` / `_dark.svg` — 同上
- `rescript-repl.svg` / `_dark.svg` — 同上

ReScript 公式ブランドの赤は **#E6484F**。すでに近いが、起点色を **正確に揃える** ことでブランド統一感を確保したい。

加えて、ReScript 専用 ToolWindow の多くが `AllIcons.FileTypes.Diagram` などのプラットフォーム標準アイコンを使っており、ReScript 由来であることが視覚的に伝わりにくい。**13×13 単色のシンプルな ReScript R マークアイコン** を新規作成し、主要 ToolWindow（Switch Flow / Module Diagram）に適用する。

## ユーザーストーリー

**JetBrains IDE でアイコンを多用する開発者として**、ReScript プラグインのアイコンが公式ブランドと一致した赤系で揃うことで、Project View・FileType・ToolWindow いずれでも一目で ReScript 関連であると認識したい。

## 受け入れ条件

- [ ] `rescript-file.svg`, `rescript-interface.svg`, `rescript-config.svg`, `rescript-repl.svg` の light 版で gradient 起点色を `#E6484F` に書き換え
- [ ] 同じ 4 ファイルの `_dark` 版で gradient 起点色を `#E6484F` に近い lighter シェード `#ED5B58` に書き換え
- [ ] `icons/rescript-toolwindow.svg` + `icons/rescript-toolwindow_dark.svg` を新規作成（13×13、ReScript の R を単色アウトラインで）
- [ ] `RescriptIcons.kt` に `TOOL_WINDOW` 定数を追加
- [ ] `plugin.xml` の `ReScript Module Diagram` と `ReScript Switch Flow` の `icon=` を新しい SVG パスに差し替え
- [ ] 既存のテスト（アイコンを参照しないもの含めて）がグリーン

## 制約

- 既存の rescript-* SVG の path 形状は変更しない（色 stop だけの差し替え）
- ToolWindow 13×13 アイコンは「単色 + currentColor」スタイルにして、theme から色を引き継ぐ
- 他の ToolWindow（Type Impact, Type Coverage, Migration Pilot 等）は今回は触らない（過剰スコープ回避、別タスク化）
- 形状を変えていないので、テストは plugin.xml のアイコンパス記載と `RescriptIcons.kt` の定数追加のみを対象にする
