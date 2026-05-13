# 20260513-007: 6件のフォローアップ修正

ユーザー報告 6 件（うち 1 件は質問への回答）の対応をまとめて行う。直近のステアリング 001-006 で着手済みだが、ユーザーの実環境では引き続き未解決のもの。

## 1. ReScript Migration Pilot の `unexpected argument` エラー

### 現象
manual-test-projects/main の rescript v12.2.0 環境で `[fail] src/LegacyReason.re  error: unexpected argument 'src/LegacyReason.re' found`。

### 根本原因
ReScript v12 (rewatch ベース) は `convert` サブコマンドを完全削除した。実機で確認済み:
- `npx rescript --version` → `12.2.0`
- `npx rescript convert --help` → `convert` がコマンド一覧に無い (build / watch / clean / format / compiler-args / help のみ)
- `npx rescript-legacy convert ...` → `Unknown command "convert"` (v12 では legacy も convert を持たない)
- `npx rescript-legacy format` は `.res` / `.resi` 拡張子のみサポート、`.re` は `Unsupported extension`

### 受け入れ条件
- [ ] `RescriptMigrationConverter` が CLI 起動前に `rescript --version` を取得し、v12 以上なら convert を実行せずに「ReScript 12 removed the `convert` subcommand. Pin `rescript@^11` to use Migration Pilot.」相当の actionable エラーを返す
- [ ] v11 系では従来通り `rescript convert <relative-path>` を実行する
- [ ] 既存のテスト `RescriptMigrationConverterCliTest` を、version probe を含む API シグネチャに合わせて更新する
- [ ] ToolWindow の UI で v12 検出時のエラーメッセージがそのまま表示される

## 2. ツールウィンドウアイコンの ReScript 赤統一

### 現象
Module Diagram / Switch Flow が `rescript-toolwindow.svg` (currentColor monochrome) を共有しており Dark テーマで暗い。他のツールウィンドウは `AllIcons.*` に依存しているため、`Dependencies` `Type Impact` `Type Coverage` `Interop Risk` `Migration Pilot` `PPX` の 6 つもブランドカラーから外れている。

### 受け入れ条件
- [ ] 以下 8 種類のツールウィンドウすべてが ReScript 赤 (`#E6484F → #CB3939` の既存グラデ) ベースの自前 SVG アイコンを使う:
  - ReScript Module Diagram
  - ReScript Switch Flow
  - ReScript Dependencies
  - ReScript Type Impact
  - ReScript Type Coverage
  - ReScript Interop Risk
  - ReScript Migration Pilot
  - ReScript PPX
- [ ] アイコンは 16×16 viewBox、既存 `rescript-file.svg` と同じグラデ ID を再利用してファミリーとして識別できる外観
- [ ] `plugin.xml` で `AllIcons.*` 参照と `rescript-toolwindow.svg` 共有を解消し、用途別 SVG を指定する
- [ ] `_dark.svg` バリアントは必須ではない (currentColor ではなく塗りを赤固定にするため Dark でも視認できる)

## 3. Type Narrowing: arm 内 binding 横ヒント (Phase 2)

### 現象
fixture (`NarrowingSamples.res` 等) でユーザーが期待した「arm 内の pattern binding (`Some(x)` の `x`) の脇に narrowing 後の型が出る」が表示されない。

### 設計判断
直近の Phase 1 では `=>` 直後にスクラティニーの絞り込み型を出す仕様で意図的に binding 横は未対応。今回 Phase 2 として **arm 内の最初の pattern binding 位置** に絞り込み型のインレイヒントを追加する。

### 受け入れ条件
- [ ] `RescriptSwitchArmCollector` が arm 内の最初の LIDENT pattern binding (例: `| Some(x) =>` の `x`) の offset を返せる
- [ ] `RescriptNarrowingHintProvider` が binding offset に「`: <絞り込み型>`」相当のヒントを追加する (既存の `=>` 直後ヒントと併存)
- [ ] or-pattern (`| Some(x) | None =>`) では最初の有効 binding に対してのみ表示
- [ ] LIDENT binding が無い arm (constructor 単独・ワイルドカード・リテラル) では何も表示しない
- [ ] 既存テストは退行せず、新規 `RescriptNarrowingHintProviderBindingTest` (または既存テストへの追加 case) で binding offset の hint を検証

## 4. Switch Flow Diagram のビジュアル表示

### 現象
`RescriptVariantFlowPanel` は Mermaid `flowchart TD` の **生テキスト** を `JTextArea` で表示しているだけ。ユーザーは図として読みたい。

### 設計判断
JCEF + mermaid.js は外部資産バンドルが必要で plugin verifier 検証コストが高い。`RescriptVariantFlowModel` が既に nodes / edges を持っているため、**Java2D で直接ノードと矢印を描画する自前ビューア** を `JComponent` として実装する方が軽量で確実。Source/Visual トグルでテキストモードと切替可能にする。

### 受け入れ条件
- [ ] `RescriptVariantFlowPanel` に "Visual" / "Source" の表示モードトグルを追加し、デフォルトは Visual
- [ ] Visual モードは新規 `RescriptVariantFlowGraphView` (JComponent) でモデルからノードを縦方向 (TD: top-down) に配置して描画する
  - ルート: scrutinee (`switch <expr>`) を矩形で
  - 第 2 段: 各 arm を矩形で並べ、ルートから矢印を引く
  - arm ラベルは constructor 名 (+ binding) と guard を 2 行で表示
- [ ] Source モードは現行の `JTextArea` (Mermaid テキスト) を保持
- [ ] Visual モードでも既存の Copy Mermaid / Copy DOT アクションは動作する
- [ ] パネルが空 (carret が switch 上にない) 状態の how-to ヒントは Visual モードでも表示される
- [ ] `RescriptVariantFlowGraphView` の純ロジック (ノード配置計算) は単体テスト可能 (`computeLayout` メソッド単体テスト)

## 5. Add Missing Switch Arms が「何もしない」

### 現象
ユーザー環境で Alt+Enter から起動しても UI 上は何も起きない。

### 根本原因
`RescriptLspSignatureParser.parseVariantConstructors` は LSP hover テキストに `|` が含まれる inline variant 定義のみ抽出する。実環境の hover は `type myVariant` のような **型名参照のみ** を返すため constructor 集合が空になり、`ArmsOutcome.NotVariant` → Event Log 通知のみで終了し、エディタ上は無変化に見える。

### 受け入れ条件
- [ ] `parseVariantConstructors` が空集合を返した場合、scrutinee 型名で **プロジェクト内の `type <name> = ...` 宣言** を PSI stub index (`indexing/`) から検索し、定義テキストを取得して再パースする 2nd-pass を持つ
- [ ] 2nd-pass で見つかった変種定義から constructor を抽出できれば従来の Insert 経路に合流する
- [ ] 単純型名以外 (`option<int>` `result<a, b>` など型適用) は既存の hardcoded パスのまま
- [ ] 型定義が見つからない / 型定義の RHS が variant でない場合は既存の `NotVariant` 通知に落ちる
- [ ] テスト: 「LSP hover が `type t` だけを返し、別ファイルで `type t = | A | B` が定義されている」シナリオで constructor 集合 `{A, B}` を取得できる単体テストを追加

## 6. VariantUsage.res のテスト方法 (回答のみ)

ユーザー質問への回答であり、コード変更不要。`manual-test-projects/README.md` の Rename Variant Constructor セクションが手順を明示している (`Alt+Enter` → "Rename variant constructor"、4 occurrences across 2 files)。今回のステアリングでは README の該当行を **より目立つように補強** するだけで対応する。

### 受け入れ条件
- [ ] `manual-test-projects/README.md` の Rename Variant Constructor 行に「`VariantUsage.res` は単独で開いて確認するファイルではなく、`VariantSamples.res` で定義された variant のクロスファイル参照確認用」の注意書きを追加

## 非ゴール

- Migration Pilot に Reason → ReScript の代替変換ロジックを内蔵する (v12 が convert を完全削除しているため、第三者ライブラリ依存になる。スコープ外)
- Switch Flow に mermaid.js / Graphviz バインディングを導入する (Java2D 自前描画で十分)
- Narrowing Phase 3 (ガード条件・ネスト switch・record destructuring) の binding 対応
- Add Missing Arms で record / polymorphic variant 対応
