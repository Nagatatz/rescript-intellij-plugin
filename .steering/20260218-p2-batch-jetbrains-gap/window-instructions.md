# 各ウィンドウへの命令文

## 前提

各ウィンドウは `/Users/ngtz/Documents/repos/rescript-intellij-plugin` で Claude Code が起動済み。
命令文の冒頭で worktree ディレクトリへ `cd` する。

| ウィンドウ | worktree パス | ブランチ |
|-----------|-------------|---------|
| Window 1 | `../rescript-wt-quick-fix` | `feature/quick-fix` |
| Window 2 | `../rescript-wt-intentions` | `feature/intention-actions` |
| Window 3 | `../rescript-wt-surround` | `feature/surround-with` |
| Window 4 | `../rescript-wt-import-optimizer` | `feature/import-optimizer` |
| Window 5 | `../rescript-wt-run-marker` | `feature/run-line-marker` |

**共有インフラ:** なし（全機能が独立したローカル実装）

---

## Window 1: Quick Fix (LSP Code Actions)

```
cd /Users/ngtz/Documents/repos/rescript-wt-quick-fix

ブランチ `feature/quick-fix` で Quick Fix（LSP Code Actions）の動作確認を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-quick-fix/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- IntelliJ 2024.1+ の LSP API が `textDocument/codeAction` を自動サポート
- rescript-language-server が提供するコードアクション（import 追加、型注釈追加等）は追加コード不要で Quick Fix / Intention として表示される
- 追加のプラグインコードは不要（ゼロコード）
- 動作確認とドキュメント用のステアリングのみ

### design.md の要約
- 新規ファイル: なし
- 変更ファイル: なし
- テスト省略理由: LSP サーバーとの結合が必須で単体テスト困難

## ステップ 2: 実装
コード変更なし。ステアリングドキュメントの作成のみ。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Verify Quick Fix support via LSP code actions`

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p2-batch-jetbrains-gap` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p2-batch-jetbrains-gap
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/quick-fix
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-quick-fix
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/quick-fix

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 2: Intention Actions

```
cd /Users/ngtz/Documents/repos/rescript-wt-intentions

ブランチ `feature/intention-actions` で ReScript Intention Actions を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-intention-actions/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- Alt+Enter で ReScript 固有のコード変換を提供
- `Wrap with Some(...)` / `Wrap with Ok(...)` / `Wrap with Error(...)` — 選択範囲またはカーソル位置の式をラップ
- `Add @genType annotation` — カーソル行の宣言に @genType を追加
- Settings > Editor > Intentions > ReScript で確認可能
- LSP のコードアクションと共存する（同じ Alt+Enter メニューに表示）

### design.md の要約
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/intention/RescriptWrapWithIntention.kt`
- 変更ファイル: `plugin.xml`
- 共通基底クラス `RescriptWrapWithIntention(wrapper: String)` を作成:
  - `PsiElementBaseIntentionAction` を継承
  - `isAvailable()`: ReScript ファイルかつ選択範囲がある、またはカーソルが式上にある
  - `invoke()`: 選択テキストまたはカーソル位置の式を `Wrapper(expr)` で置換
  - サブクラス: `RescriptWrapWithSomeIntention`, `RescriptWrapWithOkIntention`, `RescriptWrapWithErrorIntention`
- `RescriptAddGenTypeIntention`:
  - `isAvailable()`: カーソルが let/type/module 宣言上
  - `invoke()`: 宣言の直前行に `@genType\n` を挿入
- plugin.xml に `<intentionAction>` を 4 つ登録（language="ReScript", category="ReScript", skipBeforeAfter=true）
- テスト: `isAvailable()` の条件テスト + `invoke()` の変換結果テスト

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add ReScript intention actions`

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p2-batch-jetbrains-gap` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p2-batch-jetbrains-gap
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/intention-actions
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-intentions
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/intention-actions

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 3: Surround With

```
cd /Users/ngtz/Documents/repos/rescript-wt-surround

ブランチ `feature/surround-with` で Surround With 機能を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-surround-with/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- Ctrl+Alt+T で選択コードを構文テンプレートで囲む
- テンプレート: `if (...) { }`, `switch ... { | _ => }`, `try { } catch { | exn => }`, `{ }`（ブロック）
- 囲んだ後、カーソルが適切な位置に配置される（条件式、パターン等）
- LSP 不要（純粋なドキュメント操作）

### design.md の要約
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/surround/RescriptSurroundDescriptor.kt`
  - `SurroundDescriptor` を実装
  - `getElementsToSurround()`: 選択範囲内の PSI 要素を返す（ReScript ファイルのみ）
  - `getSurrounders()`: 4 つの `Surrounder` を返す
  - `isExclusive()`: false
- 4 つの `Surrounder` を同一ファイルに内部クラスまたはトップレベルクラスで定義:
  - `RescriptIfSurrounder` — `if (condition) {\n  <selection>\n}` → カーソルは `condition`
  - `RescriptSwitchSurrounder` — `switch expr {\n| _ => <selection>\n}` → カーソルは `expr`
  - `RescriptTrySurrounder` — `try {\n  <selection>\n} catch {\n| exn => ()\n}` → カーソルは `()`
  - `RescriptBlockSurrounder` — `{\n  <selection>\n}` → カーソルはブロック末尾
- 各 `Surrounder.surroundElements()`:
  1. 選択テキストを取得
  2. `WriteCommandAction.runWriteCommandAction` 内でテンプレートに置換
  3. カーソル位置の `TextRange` を返す
- plugin.xml: `<lang.surroundDescriptor language="ReScript" implementationClass="...">`
- テスト: 各 Surrounder のテンプレート生成テスト

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add Surround With support`

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p2-batch-jetbrains-gap` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p2-batch-jetbrains-gap
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/surround-with
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-surround
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/surround-with

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 4: Import Optimizer

```
cd /Users/ngtz/Documents/repos/rescript-wt-import-optimizer

ブランチ `feature/import-optimizer` で Import Optimizer を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-import-optimizer/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- Ctrl+Alt+O で重複 `open` 文を削除
- 既存の `RescriptDuplicateOpenInspection` と同様のロジックで重複検出
- 未使用 `open` の完全検出はセマンティック解析が必要なため、まず重複削除に注力
- 最適化後に通知バルーンで削除数を表示

### design.md の要約
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/imports/RescriptImportOptimizer.kt`
- 変更ファイル: `plugin.xml`
- `ImportOptimizer` を実装:
  - `supports()`: ReScript ファイルのみ
  - `processFile()`: `CollectingInfoRunnable` を返す
    - READ フェーズ: PSI ツリーから `OPEN_STATEMENT` を収集、モジュール名を抽出、重複を検出
    - WRITE フェーズ: 重複行を逆順で削除（オフセット保持）
    - `getUserNotificationInfo()`: "Removed N duplicate open statement(s)"
- `open` 文のモジュール名抽出: `OPEN_STATEMENT` ノードの子から `UIDENT` トークンを連結
- 重複判定: 同一モジュール名の2回目以降を削除対象
- 既存の `RescriptDuplicateOpenInspection`（`inspection/RescriptDuplicateOpenInspection.kt`）のロジックを参考にする
- plugin.xml: `<lang.importOptimizer language="ReScript" implementationClass="...">`
- テスト: 重複 open の検出・削除ロジックのテスト

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add Import Optimizer for duplicate open removal`

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p2-batch-jetbrains-gap` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p2-batch-jetbrains-gap
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/import-optimizer
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-import-optimizer
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/import-optimizer

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 5: Gutter Run Icons

```
cd /Users/ngtz/Documents/repos/rescript-wt-run-marker

ブランチ `feature/run-line-marker` で Gutter Run Icons を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-run-line-marker/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- .res ファイルのガターに▶実行アイコンを表示
- アイコンクリックで既存の `RescriptRunConfigurationType` によるビルド実行構成を起動
- rescript.json が存在するプロジェクトでのみ表示
- 右クリックメニューに Run/Debug オプション

### design.md の要約
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/run/RescriptRunLineMarkerContributor.kt`
- 変更ファイル: `plugin.xml`
- `RunLineMarkerContributor` を継承:
  - `getInfo(element)`:
    1. リーフ要素のみ処理（`element.node?.elementType` が `LET_KEYWORD` 等のリーフ）
    2. ReScript ファイルか確認
    3. プロジェクトに `rescript.json` が存在するか確認
    4. ファイル内最初のトップレベル宣言（`LET_DECLARATION`, `TYPE_DECLARATION`, `MODULE_DECLARATION`）のキーワード要素か確認
    5. 条件を満たせば `withExecutorActions(AllIcons.RunConfigurations.TestState.Run)` を返す
  - リーフ要素のみに反応することで重複アイコンを防止
- 既存の `RescriptRunConfigurationType`（`run/RescriptRunConfigurationType.kt`）と `RescriptRunConfiguration` を活用
- plugin.xml: `<runLineMarkerContributor language="ReScript" implementationClass="...">`
- テスト: `getInfo()` の条件テスト（ReScript ファイル判定、rescript.json 有無、最初の宣言判定）

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add gutter run icons for ReScript files`

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p2-batch-jetbrains-gap` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p2-batch-jetbrains-gap
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/run-line-marker
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-run-marker
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/run-line-marker

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```
