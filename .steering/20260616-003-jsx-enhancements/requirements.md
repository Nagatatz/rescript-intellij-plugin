# requirements — JSX 改良プログラム (C→E→B→A→D)

## 背景

JSX サポートの徹底調査（二段検証済み）の結果、prop 補完・コンポーネント補完・hover・定義ジャンプは `@rescript/language-server` が既に担当しており native 再実装は無駄、タグ自動クローズ・Backspace タグペア削除・HTML→JSX paste・Extract Component は実装済みであることが確定した。

一方で、以下の **純構文・LSP 非重複・LSP 不在でも動作する** native 機能が欠けている。本ステアリングではこれらを ROI とデリスク順（共有インフラを軽い機能で先に作って検証 → 最も fiddly な A を最後に乗せる）で実装する。

実装順: **C → E → B → A → D**。各機能は独立にマージ可能なチェックポイントとして扱う。

- F（Emmet-in-JSX）は高工数・独自展開が必要なため本ステアリング対象外（別ステアリングに後回し）。
- G（JSX 属性 PSI 化）は LSP 非カバーの punning Intention 等とセットでのみ価値が出るため対象外。

## 対象機能と受け入れ条件

### C: Surround with JSX

`surround/RescriptSurroundDescriptor` を拡張し、選択範囲を JSX で囲む surrounder を追加する。

- [ ] エディタで式・JSX を選択して Surround With（Ctrl+Alt+T）すると「Surround with JSX element `<tag>...</tag>`」と「Surround with Fragment `<>...</>`」が候補に出る
- [ ] JSX element surrounder は `<div>$SELECTION</div>` を挿入し、`div` 部分にキャレット/選択を置いてタグ名をすぐ編集できる
- [ ] Fragment surrounder は `<>$SELECTION</>` を挿入する
- [ ] LSP 不在でも動作する

### E: 閉じタグ不一致 Inspection

開きタグ名と閉じタグ名が一致しない JSX 要素を警告する LocalInspectionTool を追加する。

- [ ] `<div>...</span>` のように開き/閉じタグ名が異なる JSX_ELEMENT に WARNING を表示する
- [ ] 大文字コンポーネント（`<Foo>...</Bar>`）と小文字 HTML 要素（`<div>...</span>`）の双方で検出する
- [ ] 正しくネストした一致タグ・フラグメント（`<>...</>`）・自己閉鎖要素では誤検知しない
- [ ] LSP 不在でも動作する
- [ ] 本機能の実装で **共有ヘルパ「JSX_ELEMENT の開き/閉じタグ名・範囲を取得する純関数」を新設**し、B・A から再利用する

### B: タグペアハイライト

カーソルを JSX 開きタグ名（または閉じタグ名）に置くと、対応する反対側のタグ名をハイライト表示する。

- [ ] カーソルを `<div>` のタグ名に置くと対応する `</div>` のタグ名がハイライトされる（逆も同様）
- [ ] ネストした同名タグでも正しい対応先をハイライトする
- [ ] E で新設した共有ヘルパを再利用する
- [ ] LSP 不在でも動作する

### A: ペアタグ同期リネーム

開きタグ名を編集すると、対応する閉じタグ名が同期して追従する（逆も同様）。

- [ ] `<div>` の `div` を `span` に書き換えると `</div>` が `</span>` に追従する
- [ ] 閉じタグ側を編集した場合も開きタグ側が追従する
- [ ] ネストした同名タグでも正しい対応先のみ追従する
- [ ] 自己閉鎖タグ・フラグメントでは何も壊さない
- [ ] E/B で確立した共有ヘルパを再利用する
- [ ] Undo が破綻しない（編集が1ステップで取り消せる）
- [ ] LSP 不在でも動作する

### D: 構造ビュー JSX ノード

構造ビュー（および breadcrumb / navbar が共有する `NAVIGABLE_TYPES`）に JSX 要素を含めるかを検討し、ノイズにならない形で表示する。

- [ ] 構造ビューに JSX 要素がタグ名付きで表示される（または「価値より表示ノイズが上回る」と判断した場合はスキップ理由を design.md / tasklist.md に明記）
- [ ] 表示する場合、breadcrumb / navbar への影響（過剰なノードでの可読性低下）を確認する

## 非機能要件

- 既存の JSX 機能（折りたたみ・自動クローズ・Extract Component・paste）に回帰を起こさない
- 各機能はテスト必須（`testing.md` の免除対象に該当するのは Surrounder の UI 表示部分等のみ。タグペア探索ヘルパ・不一致判定・タグ名比較ロジックは純関数としてテスト必須）
- KDoc を全新規クラスに付与（英語）
- セキュリティ: 外部入力なし（純構文機能）だが、PSI 範囲操作で範囲外アクセスを起こさないこと

## スコープ外

- F（Emmet-in-JSX）: 別ステアリング
- G（JSX 属性 PSI 化）: punning Intention 等とセットでのみ着手
- LSP が担当する prop 補完・hover・定義ジャンプの native 再実装

## マージ単位

C / E / B / A / D をそれぞれ独立チェックポイント（1 機能 + テスト + ドキュメント = 1 コミット）とし、緑になった順に commit する。E は共有ヘルパを含むため B・A の前提（tasklist 冒頭に依存を明記）。
