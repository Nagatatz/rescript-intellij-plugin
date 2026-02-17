# Requirements: ビルドステータスウィジェット

## 概要

ステータスバーに ReScript コンパイラのビルド状態をリアルタイム表示するウィジェットを追加する。

## 背景

- VSCode 拡張では、ReScript コンパイラの状態（コンパイル中、成功、エラー、警告）がステータスバーに表示される
- IntelliJ プラグインでは未実装 → VSCode とのギャップを埋める

## 機能要件

### FR-1: ビルド状態のリアルタイム表示
- LSP サーバーからの `rescript/compilationStatus` カスタム通知を受信（共有インフラで実装済み）
- 状態に応じたテキスト表示:
  - `"compiling"` → `"ReScript: Compiling..."`
  - `"success"` → `"ReScript: ✓"`
  - `"error"` → `"ReScript: N error(s)"`
  - `"warning"` → `"ReScript: N warning(s)"`
  - デフォルト → `"ReScript"`

### FR-2: ツールチップ詳細表示
- ウィジェットホバー時にツールチップで詳細情報（エラー数・警告数）を表示

### FR-3: 表示条件
- ReScript プロジェクト（`rescript.json` が存在する）でのみウィジェットを表示

## 非機能要件

- UI 更新は EDT（Event Dispatch Thread）で行う
- リスナーの登録/解除はウィジェットのライフサイクルに連動する

## 制約事項

- 共有インフラ（`RescriptCompilationStatusService`, `RescriptLsp4jClient`）は実装済み
- 新規作成は StatusBarWidget のみ

## テスト方針

- `getText()` / `getTooltipText()` の状態別テストを作成
- `isAvailable()` の条件テストを作成
