# Reason → ReScript Migration Pilot — Requirements

## 背景

ReasonML / OCaml シンタックスから ReScript シンタックスへの移行は、ReScript 公式の `rescript convert` CLI（旧 `rescript format`）でファイル単位に変換できる。しかし、既存の `.re` ファイルがプロジェクトに 100 個ある場合、開発者は手動で:

1. 各ファイルに対して `rescript convert path/to/file.re` を実行する
2. 結果を `.res` ファイルとして保存する
3. 元の `.re` ファイルを削除する
4. import 文や bsconfig.json を修正する

を繰り返す必要があり、エラー時の対応も手間が大きい。reasonml-idea-plugin がメンテナンス停止状態なので、移行を支援するツールが現在は存在しない。

本機能はこのギャップを埋めるため、**プロジェクト内の `.re` ファイルを一覧表示し、選択した複数ファイルを一括で `.res` に変換する** 移行支援 ToolWindow を提供する。

## ユーザーストーリー

### US-Migrate-01: `.re` ファイルの検出と一覧表示

**Reason → ReScript 移行を始める開発者として**、プロジェクト内の `.re` ファイルを ToolWindow で一覧確認したい。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] `Tools > Show Reason Migration Pilot` メニューから ToolWindow を開ける
- [x] ToolWindow には現在のプロジェクト内の `.re` / `.rei` ファイルが一覧表示される
- [x] 各エントリには `path/to/file.re` 形式のパスが表示される
- [x] チェックボックスで変換対象を選択できる
- [x] 「Select All」「Clear」ボタンで一括選択／解除できる
- [x] Refresh ボタンで再スキャンできる
- [x] ファイルが見つからない場合は「No Reason files found」プレースホルダーを表示する

### US-Migrate-02: 一括変換

**移行を進める開発者として**、選択した `.re` ファイルを一括で `.res` に変換したい。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] ツールバーに「Convert Selected」ボタンがあり、チェックされたファイルを順次 `rescript convert` で変換する
- [x] 各変換は `ProcessBuilder` でサブプロセスとして実行される（`buildCommand` で argv を組み立てる）
- [x] 変換成功時: 元の `.re` ファイルを `.res` にリネーム + 内容を変換結果で置き換え（write action 内で実行）
- [x] 変換失敗時: stderr を結果領域に表示し、元ファイルは保持する
- [x] 進行状況をステータスバーに表示する（`Converting i/N…`）
- [ ] Cancel ボタンで残りの変換を中止できる — Phase 2 以降に持ち越し（現状はファイル数 N に対して逐次実行のみ）

### US-Migrate-03: 変換結果のサマリー

**移行が終わった後に何が起きたかを把握したい開発者として**、変換成功/失敗のサマリーを確認したい。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] 変換完了時にステータスバーに「N succeeded, M failed」を表示する
- [x] 失敗したファイルは結果リストにエラーメッセージ付きで残る
- [ ] サマリーは `Copy` ボタンでクリップボードにコピーできる — Phase 2 以降に持ち越し

## スコープ外（Phase 1）

- import 文・bsconfig.json の自動更新（変換後のファイル整合性は手動で確認）
- ロールバック機能（変換前のスナップショット保持）— Git に任せる
- Reason 構文のシンタックスハイライト（プロジェクト全体で利用しない前提）
- バッチサイズの自動調整・並列変換
- 変換中の差分プレビュー
- Cancel 機能と結果サマリーのコピー（Phase 2 以降）

## 受け入れ確認

- [ ] サンプル `.re` ファイル 3 つを選択して一括変換できる — マージ後手動検証
- [ ] 不正な `.re` ファイル（構文エラー）でも他のファイルの変換が継続される — マージ後手動検証
- [x] `rescript` CLI が見つからない場合、stderr / `Error:` メッセージが結果領域に表示される（実装上、ProcessBuilder の例外を `ConversionResult.message` に転載）
- [x] ユニットテストでファイル列挙ロジックと argv 組み立てをスナップショット検証する（Finder 5 / Converter 4 ケース）

## 非機能要件

- ToolWindow 描画は既存パターン（`SimpleToolWindowPanel` + `JBList`）を踏襲する
- 変換は `executeOnPooledThread` で実行し、UI スレッドをブロックしない
- 変換タイムアウトは 30 秒/ファイル（既存 `RescriptReplExecutor` の値に合わせる）
