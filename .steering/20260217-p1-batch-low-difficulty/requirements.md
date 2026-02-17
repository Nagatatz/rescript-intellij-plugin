# Requirements: P1 低難易度バッチ実装

## 概要

P1（高優先度）機能のうち、難易度「低」の4機能を git worktree による並列実装で一括追加する。

## 対象機能

### 1. `.res`/`.resi` 切り替え（Alt+O）

**説明:** `Alt+O` キーボードショートカットで、`.res` ファイルと対応する `.resi` ファイルを素早く切り替える。

**ユーザーストーリー:** ReScript 開発者として、実装ファイル（`.res`）とインターフェースファイル（`.resi`）をキーボードショートカットで素早く切り替えたい。

**受け入れ条件:**
- [ ] `.res` ファイルを開いている状態で `Alt+O` を押すと、同名の `.resi` ファイルが開く
- [ ] `.resi` ファイルを開いている状態で `Alt+O` を押すと、同名の `.res` ファイルが開く
- [ ] 対応するファイルが存在しない場合、何も起こらない（エラーにならない）
- [ ] メニューから「Navigate > Go to Related .res/.resi File」でもアクセス可能

### 2. Live Templates

**説明:** `module`, `try`, `for`, `external`, `switch`, `if` 等の頻出構文をスニペットとして提供する。

**ユーザーストーリー:** ReScript 開発者として、頻出する構文パターンをスニペットで素早く挿入し、定型的なコード入力を効率化したい。

**受け入れ条件:**
- [ ] 以下のスニペットが利用可能:
  - `let` — let バインディング
  - `letfn` — let 関数定義
  - `mod` — module 定義
  - `modt` — module type 定義
  - `typ` — type 定義
  - `typv` — variant type 定義
  - `typr` — record type 定義
  - `ext` — external 宣言
  - `sw` — switch 式
  - `try` — try-catch 式
  - `for` — for ループ
  - `if` — if 式
  - `ife` — if-else 式
  - `pipe` — パイプライン
  - `log` — Console.log
- [ ] Settings > Editor > Live Templates > ReScript で確認・編集可能
- [ ] タブキーで変数部分を移動できる

### 3. File Templates

**説明:** `New > ReScript File` メニューからテンプレートファイルを作成する。

**ユーザーストーリー:** ReScript 開発者として、新しい ReScript ファイルを IDE の「New File」メニューから作成したい。

**受け入れ条件:**
- [ ] `New > ReScript Module` で `.res` ファイルを作成できる
- [ ] `New > ReScript Interface` で `.resi` ファイルを作成できる
- [ ] `New > ReScript Component` で React コンポーネントの `.res` ファイルを作成できる
- [ ] 作成時にファイル名を入力するダイアログが表示される
- [ ] ファイル名は自動的に先頭大文字に変換される（ReScript のモジュール命名規則）

### 4. Spell Checking

**説明:** ReScript ファイル内の識別子・文字列リテラル・コメントに対してスペルチェックを提供する。

**ユーザーストーリー:** ReScript 開発者として、変数名やコメント内のスペルミスを IDE が検出してくれることで、コード品質を向上させたい。

**受け入れ条件:**
- [ ] コメント内のスペルミスが検出される
- [ ] 文字列リテラル内のスペルミスが検出される
- [ ] 識別子（camelCase/snake_case 分割後）のスペルミスが検出される
- [ ] キーワード（`let`, `type` 等）はスペルチェック対象外
- [ ] IDE のスペルチェック辞書に追加可能

## 実装アプローチ

各機能を独立したブランチで並列に実装し、git worktree を使用して同時開発する。

| 機能 | ブランチ名 | worktree パス |
|------|-----------|--------------|
| .res/.resi 切り替え | `feature/res-resi-switch` | `../rescript-wt-switch` |
| Live Templates | `feature/live-templates` | `../rescript-wt-live-templates` |
| File Templates | `feature/file-templates` | `../rescript-wt-file-templates` |
| Spell Checking | `feature/spell-checking` | `../rescript-wt-spell-checking` |

## 制約事項

- 各機能は完全に独立しており、互いに依存しない
- すべてのブランチは `main` から分岐する
- 各ブランチで `./gradlew buildPlugin` が成功すること
- 実装完了後、各ブランチを `main` にマージする
