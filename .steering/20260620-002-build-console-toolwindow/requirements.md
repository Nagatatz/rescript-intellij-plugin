# 要件定義: Build Console 専用 ToolWindow (#119)

## 背景・目的

ロードマップ #119（優先度 B / カテゴリ ToolWindow / 難易度 中〜高）。

`rescript build -w`（watch モード）の出力を専用 ToolWindow に常駐表示し、エラー/警告を
**ファイル別に構造化したツリー**で一覧する。各エントリをクリックすると該当ソース行へジャンプする。

### 既存機能との差別化（重要）

既に以下が存在するため、それらと重複しない価値を定義する:

- `RescriptRunConfiguration`: `rescript build / watch / clean` を `ProcessHandler` で実行し、
  生 stdout を IDE 標準 ConsoleView に流す（手動起動・一回限りのコンソール）
- `RescriptConsoleFilter`: ConsoleView の `path:line:col` テキストをクリック可能リンク化する

本機能はこれらと異なり、次を提供する:

1. **常駐 ToolWindow**（bottom anchor）— Run Configuration を起動しなくても watch を開始できる
2. **構造化ビュー** — 生ログではなく、watch 出力をパースしてファイル別のエラー/警告ツリーに集約。
   ファイルごとの件数バッジ、severity（error / warning）区別を表示
3. **ワンクリックナビゲーション** — ツリーノードのダブルクリック/Enter で `OpenFileDescriptor` ジャンプ

生ログそのものが見たいユーザーは従来どおり Run Configuration を使えばよい。本機能は
「watch を回しっぱなしにして、エラー一覧を IDE 下部に常時出しておく」ユースケースに最適化する。

## ユーザーストーリー

- ReScript 開発者として、`rescript build -w` を IDE 下部の ToolWindow から開始/停止したい。
  そうすれば別ターミナルを切り替えずに保存→再コンパイル結果を確認できる。
- ReScript 開発者として、コンパイルエラー/警告をファイル別ツリーで俯瞰したい。
  そうすれば複数ファイルにまたがるエラーの全体像を把握できる。
- ReScript 開発者として、エラーエントリをクリックして該当行へ即ジャンプしたい。

## スコープ（MVP）

### 含む

1. **Build 出力パーサ**（純ロジック・テスト必須）
   - ReScript コンパイラ出力（stdout/stderr）の行ストリームをパースし、構造化診断リストを生成
   - 検出対象:
     - エラー: `We've found a bug for you!` ブロック直後の `path:line:colStart-colEnd`
     - 警告: `Warning number N` ブロック直後の `path:line:colStart-colEnd`
     - 構文エラー: `Syntax error!` ブロック
   - 抽出フィールド: `filePath`, `line`, `colStart`, `colEnd`, `severity`, `messageHead`
   - watch サイクルの区切り（`>>>> Start compiling` / `>>>> Finish compiling` 等）でバッファをリセット
2. **Build Console サービス**（project-level service）
   - watch プロセスの起動/停止（既存 `RescriptCliDetector` + `RescriptWorkspaceDiscovery` で CLI/ルート解決、
     `ProcessBuilder` または `OSProcessHandler` を明示的引数リストで起動）
   - 出力行をパーサへ供給し、診断リストを保持。リスナへ更新通知
   - プロセス状態（idle / running / stopped）管理
3. **ToolWindow Factory + Panel**（Swing UI・テスト免除）
   - `RescriptToolWindowPanelBase` を継承したパネル
   - center: ファイル別エラー/警告ツリー（`com.intellij.ui.treeStructure.Tree`）
   - toolbar: Start watch / Stop / Clear アクション
   - statusLabel: 「Building… / N errors, M warnings / Idle」
   - ツリーノードのダブルクリック・Enter で `OpenFileDescriptor` ジャンプ
4. **plugin.xml** に `<toolWindow>` 登録（既存 ReScript ツールウィンドウ群の並びに従う）

### 含まない（将来拡張）

- ビルド成功/失敗のトースト通知
- ビルド時間の計測・履歴
- 複数ワークスペース同時 watch（初版は 1 プロジェクト 1 watch）
- LSP 診断との統合（本機能は CLI 出力ベース。LSP 診断は既存のインライン表示が担う）
- 警告の quick fix 連携

## 受け入れ条件

1. **パーサ正常系（エラー）**: `We've found a bug for you!\n  /abs/src/App.res:3:9-11` を含む出力から
   `severity=ERROR, filePath=/abs/src/App.res, line=3, colStart=9, colEnd=11` の診断を 1 件抽出する
2. **パーサ正常系（警告）**: `Warning number 110\n  /abs/src/App.res:5:1-20` から `severity=WARNING` を抽出する
3. **パーサ複数件**: 複数のエラー/警告ブロックを含む出力から全件を順序どおり抽出する
4. **パーサ watch リセット**: `>>>> Start compiling` を検出したら、以降が新しいビルドサイクルの診断として
   集計され、前サイクルの診断は引き継がない
5. **パーサ無関係出力**: 診断ブロックを含まない出力（`>>>> Finish compiling 0 errors` 等）からは空リストを返す
6. **パーサ堅牢性**: 不正な行番号・列番号・パス（数値変換不能等）は該当エントリを安全に無視し、例外を投げない
7. **ToolWindow 登録**: plugin.xml に `id="ReScript Build"`（仮）の toolWindow が登録され、IDE 起動時に
   bottom に表示可能になる（手動検証 / UI smoke）
8. **ナビゲーション**: ツリーの診断ノードを起動すると、`filePath`/`line`/`colStart` に対応する位置へ
   エディタがジャンプする（手動検証 / UI smoke）

受け入れ条件 1〜6 はユニットテストで自動検証する。7〜8 は UI 結合のため手動 / UI smoke で検証する
（`testing.md` の Swing UI / IDE ライフサイクル免除に該当）。

## 非機能要件

- **セキュリティ**: CLI 起動は `ProcessBuilder` に明示的引数リストを渡す（ユーザー入力の文字列連結禁止）。
  CLI パス・ワークスペースルートは既存ユーティリティ経由で解決し、絶対パスを UI/エラーに露出しない
- **パフォーマンス**: watch 出力の行処理はバックグラウンドスレッド。UI 更新は `Dispatchers.EDT` /
  `invokeLater` でデバウンス（`RescriptToolWindowPanelBase` の `scheduleRefresh` パターン踏襲）
- **プロセスリーク防止**: ToolWindow / プロジェクト dispose 時に watch プロセスを確実に終了する
- **Deprecated API 不使用**: 新規 import は非 deprecated API のみ（`deprecated-api.md`）
- **KDoc**: 全クラスに英語 KDoc（`code-comments.md`）

## 確認が必要な設計判断

以下は design.md で確定する:

- watch プロセス起動に `OSProcessHandler`（IDE 標準・dispose 連携が容易）と素の `ProcessBuilder` の
  どちらを使うか
- 構造化ビューを Tree とするか Table とするか（ファイル別グルーピングを優先するなら Tree 推奨）
- ToolWindow id 名（`ReScript Build` 案）
