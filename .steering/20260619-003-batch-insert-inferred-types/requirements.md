# 要求内容: inferred 型注釈の一括挿入

ロードマップ機能 **#117**（`docs/product-requirements.md`）の実装。

## 背景

Type Coverage Heat Map (`coverage/`) は `.res` ファイルごとに「トップレベル `let` のうちどれだけが明示的型注釈を持つか」を可視化する。低 coverage と判定されたファイルに対し、型注釈を1件ずつ手で足すのは手間が大きい。本機能は、ファイル内の未注釈トップレベル `let` をまとめて検出し、LSP hover から推論型を取得して `: T` を一括挿入する。

純ロジック層 `RescriptBatchAnnotationPlanner`（対象 let の列挙 + 編集計画生成）は既に実装・テスト済み（本ブランチの先行コミット）。本ステアリングは残りの実行グルー・UI エントリ・登録・ドキュメント同期を対象とする。

## 受け入れ条件

- [ ] エディタで `.res` ファイルを開き Alt+Enter から「Insert inferred type annotations (file)」相当の Intention を起動できる
- [ ] Type Coverage Heat Map のテーブル行から同等の一括注釈アクションを起動できる
- [ ] 未注釈トップレベル `let` のみが対象になる（注釈済み / タプル・レコード分解 / `_` ワイルドカード / depth>0 は対象外）
- [ ] hover 解決は EDT 外（背景タスク + 進捗 + キャンセル）で行い、IDE をブロックしない
- [ ] 注釈の挿入は単一 `WriteCommandAction` で行い、undo 1回で全取り消しできる
- [ ] 背景解決中にドキュメントが編集された場合（modificationStamp 変化）は適用を中断し通知する
- [ ] weak type variable（`'_weak…`）など構文的に挿入不可能な型は skip される（挿入後に構文エラーを生まない）
- [ ] hover が返さなかった / 正規化に失敗した binding は skip し、件数を結果通知（Annotated X / Skipped Y）に含める
- [ ] LSP サーバ未起動時は Intention を非表示（または明示メッセージで中断）
- [ ] 純ロジック（planner の挿入可能性フィルタ含む）にユニットテストがある
- [ ] CLAUDE.md / README / sphinx EN+JA / product-requirements（#117 行削除）を同期する

## 非対象（既知の限界）

- `let x = 1 and y = 2` の `and` 連結束縛は対象外
- 別モジュール定義の非修飾型が hover に現れた場合、稀に挿入後コンパイルが通らないことがある（機械的挿入の本質的限界。単一 undo で可逆なため許容）
- `.resi` インターフェースファイルは対象外（通常すでに注釈済み）

## セキュリティ要件

- hover レスポンス由来の型文字列は挿入前に sanitize する（制御文字除去・空白圧縮・長さ上限）。planner の `normalizeType` で実施済み。挿入可能性フィルタを追加で重ねる
- ユーザー向け通知・ダイアログに絶対パスを露出させない（ファイル名のみ）
