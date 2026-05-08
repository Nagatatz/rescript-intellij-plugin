# LSP フォールバック行動マトリクス

`@rescript/language-server` が利用不可の状態（未インストール / 起動失敗 / クラッシュ）でも、本プラグインは「ネイティブ機能のみで動作するモード」へフォールバックする。本ドキュメントは、各機能が LSP に依存しているか、利用不可時にどう振る舞うかを定義する。

`docs/architecture.md` 「NFR-04 信頼性」の根拠資料。サポート問い合わせや「LSP なしで何ができるか」をユーザーに案内する際にも参照する。

## 1. LSP 接続状態の分類

| 状態 | 検出方法 | 振る舞い |
|---|---|---|
| **接続済み** | `RescriptLspDetector` がプロセスを検出し `RescriptLspServerDescriptor` が initialize 完了 | 全機能フル動作 |
| **未検出** | `node_modules/.bin/rescript-language-server` も グローバルパスもヒットしない | エディタ通知バー + プロジェクト起動時バルーン通知でインストールを促進。ネイティブ機能は正常動作 |
| **起動失敗** | LSP プロセス起動でエラー発生（Node.js 不在 / パーミッション / バージョン不整合） | バルーン通知でエラー詳細を表示。LSP 関連機能は無効化、ネイティブ機能は正常動作 |
| **接続後切断** | LSP プロセスが異常終了 | 自動再起動を 1 度試行。失敗時はネイティブ機能のみ稼働 |

## 2. 機能ごとの LSP 依存マトリクス

### Language Support（ネイティブ機能のみ）

| 機能 | LSP 必須 | 非接続時の動作 |
|---|---|---|
| シンタックスハイライト（JFlex） | 不要 | 完全動作 |
| コード折りたたみ | 不要 | 完全動作 |
| ブレースマッチング | 不要 | 完全動作 |
| スマート引用符 | 不要 | 完全動作 |
| コメントトグル | 不要 | 完全動作 |
| パンくずナビゲーション | 不要 | 完全動作 |
| ストラクチャービュー | 不要 | 完全動作 |
| Spellcheck | 不要 | 完全動作 |
| TODO インデクシング | 不要 | 完全動作 |
| Live Templates | 不要 | 完全動作 |
| File Templates | 不要 | 完全動作 |
| Project Wizard | 不要 | 完全動作 |
| `.res`/`.resi` 切り替え | 不要 | 完全動作 |
| Custom folding (`//#region`) | 不要 | 完全動作 |
| JSX 自動閉じタグ | 不要 | 完全動作 |
| ガター実行アイコン | 不要 | 完全動作 |
| Run Configuration | 不要 | 完全動作 |
| Console Filter（コンパイラ出力リンク化） | 不要 | 完全動作 |

### Code Intelligence（LSP 必須）

| 機能 | LSP 必須 | 非接続時の動作 |
|---|---|---|
| コード補完 | 必須 | 補完候補が出ない（PSI 由来のキーワード補完のみ） |
| 定義ジャンプ | 必須 | スタブインデックス由来の同一プロジェクト検索のみ |
| ホバードキュメント | 必須 | `RescriptDocumentationProvider` の PSI ベースフォールバックが動作（KDoc・関数シグネチャの限定表示） |
| 参照検索 | 部分的 | `RescriptFindUsagesProvider` (WordsScanner) でテキスト一致検索は可能（型を考慮しない） |
| リアルタイム診断 | 必須 | 診断は表示されない |
| インレイヒント | 必須 | 表示されない |
| Code Lens | 必須 | 表示されない |
| Signature Help | 必須 | 表示されない |
| Parameter Info | 必須 | LSP 経由のラベル付き引数表示は無効 |
| Rename リファクタリング | 必須 | LSP rename は使えない（`RescriptRenameHandler` が無効化、PSI ベースの簡易リネームへ） |
| Find Usages（型考慮） | 必須 | テキストベースのみ |
| Quick Documentation (`Ctrl+Q`) | 部分的 | PSI ベースのフォールバックが動作 |
| Expression Type | 必須 | 動作しない |
| Pipe chain type hints | 必須 | 表示されない |
| Type Narrowing Visualizer | 必須 | 表示されない |
| 式の型表示 | 必須 | 動作しない |

### Code Analysis

| 機能 | LSP 必須 | 非接続時の動作 |
|---|---|---|
| Error Lens | 必須 | LSP 診断ベースのため表示されない |
| 重複 open 検出（Inspection） | 不要 | 完全動作（PSI ベース） |
| 空モジュール検出 | 不要 | 完全動作 |
| `rescript.json` 未検出 | 不要 | 完全動作 |
| Signature sync 検出 | 不要 | 完全動作（PSI 比較） |
| Style lint | 不要 | 完全動作 |
| Mutability 検出 | 不要 | 完全動作 |
| Format check | 不要 | `rescript format` CLI 経由で完全動作（LSP 不要） |
| reanalyze デッドコード分析 | 不要 | reanalyze バイナリで完全動作 |
| LSP Code Actions（Quick Fix） | 必須 | 表示されない（接続時の対応一覧は §5 を参照） |
| 不解決参照 Quick Fix | 不要 | スタブインデックスベースで動作 |
| Generate function from usage | 不要 | テンプレートベースで動作 |

### Editing Assistance（ほぼネイティブ）

| 機能 | LSP 必須 | 非接続時の動作 |
|---|---|---|
| Intention Actions | 不要 | 完全動作（PSI 操作） |
| Surround With | 不要 | 完全動作 |
| Postfix Completion | 不要 | 完全動作 |
| Smart Enter | 不要 | 完全動作 |
| Comment continuation | 不要 | 完全動作 |
| Statement mover | 不要 | 完全動作 |
| Move element left/right | 不要 | 完全動作 |
| Unwrap/Remove | 不要 | 完全動作 |
| Generate actions | 不要 | 完全動作 |
| Paste as JSON.t / JSX / ReScript | 不要 | 完全動作 |
| .d.ts → ReScript バインディング生成 | 不要 | 完全動作 |
| Backspace handler | 不要 | 完全動作 |

### Build / Run / Tools

| 機能 | LSP 必須 | 非接続時の動作 |
|---|---|---|
| ビルドステータスウィジェット | 必須 | LSP の `rescript/compilationStatus` 通知に依存。表示されない |
| Compiled JS プレビュー | 不要 | コンパイル済み `.js` ファイルから直接読み込み |
| `Open Compiled JS` アクション | 不要 | ファイルパス計算のみで動作 |
| `Create Interface File` アクション | 必須 | LSP `textDocument/createInterface` を使用。動作しない（ユーザーには通知） |
| Module hierarchy | 不要 | PSI ベース |
| Call hierarchy | 部分的 | LSP 接続時はより精度が高い。非接続時は PSI ベースの簡易表示 |
| 依存ダイアグラム | 不要 | PSI ベース |
| 依存ツールウィンドウ | 不要 | `rescript.json` 解析のみ |
| Variant Flow Diagram | 不要 | レクサーベースの switch 解析のみ。LSP 不要で完全動作 |
| Type Impact Preview | 不要 | PsiSearchHelper の word index ベースの参照検索。LSP 不要で完全動作 |
| Notebook 風 Worksheet (`.resnb`) | 不要 | セル評価は `RescriptReplExecutor`（rescript CLI + node）に委譲。LSP 不要で完全動作 |
| JS Interop Risk Map | 不要 | FileTypeIndex + 行ベース・トークン分類器のみ。LSP 不要で完全動作 |
| Reason → ReScript Migration Pilot | 不要 | FilenameIndex + `rescript convert` CLI（ProcessBuilder）。LSP 不要で完全動作 |
| Type Info ツールウィンドウ | 必須 | `LSP hover` に依存。「LSP not available」プレースホルダーを表示 |
| REPL ツールウィンドウ | 不要 | `rescript-tools` CLI に直接接続 |
| Worksheet モード | 部分的 | コメント評価には ReScript runtime のみ必要。型注釈表示は LSP 必須 |
| PPX 展開ビュー | 部分的 | `bsc -bs-ast` 直接呼び出しがメイン。LSP 経由のフォールバックパスもあり |

## 3. ユーザー視点のフロー

LSP 未検出時、プラグインは段階的に案内する:

1. **エディタ通知バー** — `.res` ファイルを開くと上部に「Language Server not detected」バナーが表示され、Install / Configure / Don't show again を提示（`RescriptEditorNotificationProvider`）
2. **起動時バルーン通知** — プロジェクト起動から 5 秒後に右下バルーンを表示、ワンクリックインストールを案内（`RescriptLspStartupActivity`）
3. **インストール実行** — ロックファイル検出で自動的に `npm` / `yarn` / `pnpm` / `bun` を選択し、バックグラウンドで `<pm> install -D @rescript/language-server` を実行（`RescriptLspInstaller`）
4. **再起動不要** — インストール完了後、Language Server は自動起動

LSP 起動失敗時の通知例:

- **Node.js が PATH に無い** → 「Install Node.js 18+」を案内
- **バージョン不整合（0.x 系検出）** → 「Upgrade to @rescript/language-server 1.0.0+」を案内
- **異常終了** → 1 度自動再起動。再失敗時は通知のみ（ユーザーが Tools > ReScript > Restart Language Server で手動再開可）

## 4. 検証方針

CI に「LSP 未検出環境での E2E テスト」を追加することを推奨する:

```yaml
# .github/workflows/ci.yml への追加例
jobs:
  lsp-fallback-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: ./gradlew test -PtestProfile=no-lsp
```

`no-lsp` プロファイルでは `RescriptLspDetector` がモック化され、ネイティブ機能のみで全テストが通ることを確認する。ネイティブ機能のリグレッションを早期検出できる。

## 5. LSP 接続時に提供される Quick Fix 一覧

`@rescript/language-server` は `textDocument/codeAction` のレスポンスとして次の 9 種の `CodeAction` (kind=`QuickFix`, `WorkspaceEdit` 直挿入) を返す。本プラグインの `RescriptLspServerDescriptor.lspCustomization` は IntelliJ Platform 標準の `LspCodeActionsSupport`（`quickFixesSupport=true` / `intentionActionsSupport=true`）と `LspCommandsSupport` をデフォルトのまま採用しており、これらは Alt+Enter（および gutter の電球アイコン）から自動的に表示・適用される。

| # | Code Action | トリガとなる診断 | 主な編集 | 補足 |
|---|---|---|---|---|
| 1 | `simpleAddMissingCases` | `You forgot to handle a possible case here, for example: (Foo\|Bar)` | switch ブロック末尾に未処理ケース行を挿入 | rescript-vscode `codeActions.ts` 由来 |
| 2 | `wrapInSome` / `unwrapOptional` | option 型不整合 (`This has type X / Wanted option<X>` 等) | 値を `Some(...)` で包む / Belt.Option で剥がす | rescript-vscode `codeActions.ts` 由来 |
| 3 | `addUndefinedRecordFields` (V10/V11) | `Record literal X is missing field Y` | record リテラルに欠落フィールドを補完 | バージョンごとに別実装。両方とも `WorkspaceEdit` 直挿入 |
| 4 | `simpleConversion` | `int` / `float` / `string` 型不整合 | `int_of_string` 等の変換関数で式を包む | rescript-vscode `codeActions.ts` 由来 |
| 5 | `didYouMean` | `The value X can't be found. Did you mean Y?` | 識別子 `X` を `Y` に置換 | rescript-vscode `codeActions.ts` 由来 |
| 6 | `removeUnusedCode` | reanalyze の "unused" 警告 | 該当宣言行を削除 | `rescript.json` の `reanalyze` 有効化が必要 |
| 7 | `extractLocalModuleToFile` | カーソルがローカル `module M = { ... }` 上 | `M.res` を新規作成して内容を移動（`WorkspaceEdit.documentChanges` の `CreateFile` 操作を含む） | `rescript-editor-analysis` バイナリ由来 |
| 8 | `expandCatchAllPatterns` | カーソルが switch の `_ =>` ケース上 | `_` を全コンストラクタに展開 | `rescript-editor-analysis` バイナリ由来 |
| 9 | `applyUncurried` | uncurried 関数の curried 呼び出し（v10/v11 系） | `f(x)` → `f(. x)` | ReScript v11+ uncurried-by-default では発火しない（N/A） |

設定の追加オーバーライドは不要。`@rescript/language-server` がインストール済みであれば、本プラグインは API レベルで全 9 種を受領・適用できる前提条件を満たしている。実機での個別動作（特に `06_removeUnusedCode` の reanalyze 連動と `07_extractLocalModuleToFile` の `CreateFile` リソース操作）は `.steering/20260427-004-lsp-code-action-verification/next-steps.md` に runIde 検証タスクとして記録している。
