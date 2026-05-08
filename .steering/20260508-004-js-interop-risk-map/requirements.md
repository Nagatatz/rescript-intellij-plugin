# JS Interop Risk Map — Requirements

## 背景

ReScript は JS 生態系との相互運用のため、複数の **型保証されない出口** を提供している:

- `%raw("...")` / `%%raw(\`...\`)` — 任意の JS をそのまま埋め込む
- `external name : type = "jsName"` — JS 関数を ReScript の型で宣言
- `@bs.send` / `@bs.module` / `@module` 等の interop アノテーション
- `Obj.magic` — 型を強制的にキャストする

これらは「型システムの外」へ抜け出す入口で、誤用すると実行時エラーや型保証の崩壊を招く。プロジェクトの保守者は **どこに interop リスクがあるか** を一覧で把握し、レビュー時にリスクの高い箇所を優先したい。

本機能はこのギャップを埋めるため、**プロジェクト全体の interop 使用箇所を ToolWindow に集約表示する** Risk Map を提供する。各エントリは「種別」「ファイル」「行」「プレビュー」「ヒューリスティック・リスクスコア」で構成される。

## ユーザーストーリー

### US-Risk-01: プロジェクト全体の interop 使用箇所一覧

**ReScript 開発者として**、プロジェクト全体の `%raw`、`external`、`Obj.magic` 等の使用箇所を ToolWindow で一覧確認したい。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] `Tools > Show JS Interop Risk Map` メニューから ToolWindow を開ける
- [x] ToolWindow を開くと、現在のプロジェクト内の interop 使用箇所が `[risk/kind] file:line  preview` 形式で一覧表示される
- [x] 各エントリには interop の種別（`raw`, `external`, `obj-magic`, `bs-attr`）が表示される
- [x] ダブルクリックで該当ファイル・該当行にジャンプできる
- [x] ステータスバーに合計件数と種別ごとの内訳を表示する
- [x] Refresh ボタンで再スキャンできる

### US-Risk-02: ヒューリスティックなリスクスコア

**保守者として**、各 interop 使用箇所の「リスクの高さ」を簡単に判別したい。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] 各エントリに `high` / `medium` / `low` のリスクラベルを付与する
- [x] 判定ヒューリスティック（Phase 1）:
  - `Obj.magic` → `high`
  - `%raw` / `%%raw` → `high`（実行時の任意 JS 実行）
  - `external ... = "..."` で `@bs.send` などの bs アノテーションを伴う → `medium`
  - 単純な `external` 宣言（モジュール参照のみ）→ `low`
  - 単独の `@bs.*` / `@send` / `@module` → `low`
- [x] リスクラベル順（high → medium → low）でソートされる

### US-Risk-03: スコープ制限と除外

**ライブラリの境界を意識したい開発者として**、テストファイルや bindings ディレクトリを除外して risk map を見たい。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] スキャン対象は `GlobalSearchScope.projectScope`（プロジェクト直下の `.res` `.resi` のみ）
- [x] `node_modules/` は IntelliJ の標準 exclude 機構で自動除外される（`projectScope` の挙動に依存）
- [x] 1 ファイルあたり 50 件、プロジェクト全体で 500 件のソフトキャップ

## スコープ外（Phase 1）

- リスクスコアのカスタマイズ UI（Phase 2 以降）
- リスクの自動修正提案（Phase 2 以降、Quick Fix で対応）
- LSP との連携で「型保証範囲」を計算する（Phase 2 以降、現状はトークンベース）
- 静的型推論結果との突合（Phase 2 以降）
- CSV / JSON エクスポート

## 受け入れ確認

- [x] 4 種類の interop（`%raw`、`external`、`Obj.magic`、`@bs.send`）が分類される（Classifier ユニットテスト）
- [x] 100KB 規模のスキャンが 500ms 以内に完了することを `RescriptInteropScannerPerfTest` および `RescriptInteropClassifierPerfTest`（20260508-006）で自動検証（FileTypeIndex 経由の populated 100 ファイルケースは content-root 付き fixture が必要なため Phase 2）
- [x] ジャンプが該当行を開く（Panel の double-click navigation 実装で対応）
- [x] ユニットテストで分類器のヒューリスティックをスナップショット検証する（Classifier 8 / Scanner pure helper 5）

## 非機能要件

- ToolWindow 描画は既存パターン（`SimpleToolWindowPanel` + `JBList`）を踏襲する
- スキャンはバックグラウンドスレッド（`executeOnPooledThread` + `runReadAction`）で実行する
- 大量 interop（500 件超）はソフトキャップで切り捨て、ステータスバーに通知
