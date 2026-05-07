# Type Narrowing Visualizer — Requirements

## 背景

ReScript の `switch` 式や `if let` パターンは強力な型絞り込み（type narrowing）を行うが、各 arm でカーソル変数が「どの型に絞り込まれているか」はホバー操作（LSP `textDocument/hover`）でしか確認できない。複数の arm を持つ `switch` で型の流れを把握するには、各 arm でホバーを繰り返す必要があり、認知負荷が高い。

本機能は、ReScript の型推論の強みを直接ユーザー体験に変換することを目的に、`switch` 式の各 arm におけるパターン変数・スクラティニー（被検査式）の絞り込み後の型を **インラインで常時表示** する。

## ユーザーストーリー

### US-Narrow-01: switch arm での絞り込み型の常時表示

**ReScript 開発者として**、`switch` 式の各 arm におけるパターン変数とスクラティニーの絞り込み後の型を、ホバー操作なしで常時確認したい。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] `switch` 式の各 arm（`| Pattern => body`）の `=>` 直後に、スクラティニーの型を InlayHints で表示する
- [x] LSP（rescript-language-server）が利用可能なときのみ動作し、未接続時は何も表示しない
- [x] 型情報が長すぎる場合（64 文字超）は末尾を `…` で省略する
- [x] 設定画面（IDE 標準の **Settings > Editor > Inlay Hints > ReScript** および project-level の `RescriptProjectSettings.narrowingHintsEnabled`）でオン・オフを切り替えられる
- [x] デフォルトはオン

**Phase 2 以降（次回ステアリング）:**

- パターン変数（`Some(x)` の `x`）の narrowing 後の型表示。これは collector が arm 内のバインド変数を識別する必要があり、現在の collector 実装の拡張が必要
- ホバーで省略前の全文を表示する tooltip

### US-Narrow-02: 型推論の理由表示（補足）

**型推論結果に納得できない開発者として**、なぜこの型に絞り込まれたかの根拠（マッチしたパターン名）を簡潔に確認したい。

**受け入れ条件:**

- [x] 内部表現（`SwitchArm.patternSummary`）として `Some(_)` / `Ok(_)` / `None` 等のパターンサマリーを生成し、将来 UI に露出できるよう保持する
- [x] スクラティニー式が複雑（型が変わらないトリビアルなマッチ）な場合は表示を抑制する（`unit`、自由型変数、空文字列）

**Phase 2 以降:** UI 表示形式の検討（インラインに併記するか、tooltip にするか）。現状の実装ではパターンサマリーは UI には露出していない。

## スコープ外（本ステアリングでは扱わない）

- `if let` 構文の絞り込み（ReScript v11 でも `if let` は限定的なため次フェーズ）
- `try ... catch` 内の例外型絞り込み
- 推論の "なぜ" を遡るインタラクティブなウォーカ（提案 #2 の派生機能、別 steering で扱う）
- Type Narrowing 結果のテストカバレッジへの連携

## 受け入れ確認

- [x] 5 種類の Variant パターン（`option`, `result`, `list`, polymorphic variant, custom variant）が `RescriptSwitchArmCollector` のテストでカバーされる
- [x] `RescriptHoverTypeResolver` 経由で LSP 未起動時に `null` が返り、HintProvider が hint を発行しないことを `RescriptNarrowingHintProviderTest` の "skips arms when LSP returns null" で検証
- [ ] 1000 行超のサンプルファイルで体感遅延がない（編集後 200ms 以内に反映）— `./gradlew runIde` で手動検証
- [ ] 設定 OFF 時、ヒントが完全に消える — `RescriptConfigurable` 経由で `narrowingHintsEnabled = false` にして手動検証
- [x] ユニットテスト（`narrowing/`）が green（Collector 12 件 / Presenter 8 件 / HintProvider 6 件 = 26 件）。LSP 結合テストは LSP fixture が必要なため免除（tasklist 参照）

## 非スコープ・制約

- 本機能は LSP `textDocument/hover` のレスポンスをパース・再利用する。LSP が型情報を返さないケース（推論失敗）では当該 arm のヒントをスキップする
- パフォーマンス予算: 1 ファイルあたり最大 50 個の `switch` arm までヒントを表示。それ以上は警告ログを出してスキップ
- Hover レスポンスのキャッシュは編集ごとに無効化。同一テキストオフセットへの再リクエストは 500ms デバウンス
