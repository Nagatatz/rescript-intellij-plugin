# ツールウィンドウ スモークテスト 手順書

サンドボックス IDE を Remote-Robot で起動し、caret 駆動パネルを JS 実行 API で操作して検証する。マウス座標は使わず、`/js/execute` で JVM 内に Rhino JS を流し込んで PSI/Editor/ToolWindow を直接叩く。

ヘルパー: `assets/robot.sh`（このスキルディレクトリ内）。`ROBOT_PORT`(既定 8082) と `OUT`(既定 `/tmp/ide_smoke`) を環境変数で上書きできる。最初に `chmod +x` してから使う。

## 前提と対象パネル

| ツールウィンドウ ID | パッケージ | LSP 依存 | caret 駆動 |
|---|---|---|---|
| `ReScript Type` | `typeinfo/` | **要 LSP** (hover) | あり (debounce 300ms) |
| `ReScript Switch Flow` | `flow/` | 不要 (純構文) | あり (debounce 200ms) |
| `ReScript Type Impact` | `impact/` | 不要 (word-index) | あり (debounce 200ms) |
| `ReScript PPX` | `ppx/` | 不要 (ヒューリスティック) | あり |
| 他: `ReScript JS` / `ReScript Dependencies` / `ReScript Module Diagram` / `ReScript Type Coverage` / `ReScript Interop Risk` / `ReScript REPL` | — | — | — |

LSP 依存パネル（Type Info）を検証するなら、`@rescript/language-server` が入った ReScript プロジェクトと、ビルド済みの `.res`（`lib/bs` の `.cmt` 生成）が必要。`runIdeForUiTests` は `src/uiTest/testData/sample-project` を自動で開く。

## ステップ 0: 事前準備

1. 作業対象ブランチ／worktree を確認する（`git rev-parse --show-toplevel`、`git log --oneline -5`）。検証したい変更がそのツリーに入っていることを確認する。
2. LSP 機能を試す場合、サンプルプロジェクトの依存導入とビルドを済ませる:

```bash
cd <repo>/src/uiTest/testData/sample-project
npm install --no-audit --no-fund
npx rescript build   # ErrorDemo.res 等で一部失敗してOK。.cmt が生成されれば hover は効く
```

3. ヘルパーを実行可能にする: `chmod +x <skill-dir>/assets/robot.sh`

## ステップ 1: サンドボックス IDE をバックグラウンド起動

`runIdeForUiTests` を **バックグラウンドで** 起動する（Bash の `run_in_background: true`）。フォアグラウンドだとブロックする。

```bash
cd <repo-or-worktree> && ./gradlew runIdeForUiTests
```

robot-server が立つまで待つ（初回は IDE 展開で数分かかる）:

```bash
<skill-dir>/assets/robot.sh wait 600
```

## ステップ 2: 初回モーダルダイアログの解除（最重要）

**これを飛ばすと以降すべて失敗する。** サンドボックス初回起動では複数の `APPLICATION_MODAL` ダイアログが EDT を占有し、LSP が起動せず、caret リスナーのリフレッシュも走らない。症状は「Type Info が常に `No type information`」「`idea.log` に `Starting LSP server` が出ない」「`invokeLater` が発火しない」。

まず開いているモーダルダイアログのテキストを列挙する:

```bash
<skill-dir>/assets/robot.sh jsinline '
var texts = [];
function walk(c){
  if((c instanceof javax.swing.JLabel)||(c instanceof javax.swing.AbstractButton)){
    var t=String(c.getText()==null?"":c.getText()); if(t.trim().length>0) texts.push((c instanceof javax.swing.AbstractButton?"[btn] ":"")+t.trim());
  }
  if(c instanceof java.awt.Container){var cs=c.getComponents();for(var i=0;i<cs.length;i++)walk(cs[i]);}
}
var ws=java.awt.Window.getWindows();
for(var i=0;i<ws.length;i++){var w=ws[i];if(w.isShowing()&&(w instanceof java.awt.Dialog)&&w.isModal())walk(w);}
log.info("DIALOG >>> "+texts.join(" | "));
'
```

出てきたボタン（典型: `Trust Project` / `Skip` / `New Window` / `This Window` / `Don't ask again`）を名前指定でクリックする。**1 つ閉じると次が出る**ことがあるので、ダイアログが消えるまで列挙→クリックを繰り返す:

```bash
# 例: ラベルが完全一致するボタンを押す（"Trust Project" や "Skip" を順に）
<skill-dir>/assets/robot.sh jsinline '
var label="Skip";   // ← 押したいボタン名に変える
var n=0;
function walk(c){
  if(c instanceof javax.swing.AbstractButton && String(c.getText()).trim()===label){c.doClick();n++;}
  if(c instanceof java.awt.Container){var cs=c.getComponents();for(var i=0;i<cs.length;i++)walk(cs[i]);}
}
var ws=java.awt.Window.getWindows();
for(var i=0;i<ws.length;i++){var w=ws[i];if(w.isShowing()&&(w instanceof java.awt.Dialog)&&w.isModal())walk(w);}
log.info(label+" clicked: "+n);
'
```

EDT が解放されたかは modality で確認する（`NON_MODAL` になっていれば OK）:

```bash
<skill-dir>/assets/robot.sh jsinline 'log.info("modality="+com.intellij.openapi.application.ModalityState.current());'
```

## ステップ 3: プロジェクトを開く / LSP を起動させる

LSP は `.res` を開いた `fileOpened` で起動する。ダイアログ解除後に対象ファイルを開き直して起動を促す:

```bash
<skill-dir>/assets/robot.sh jsinline '
var p=com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects()[0];
var fem=com.intellij.openapi.fileEditor.FileEditorManager.getInstance(p);
var files=fem.getOpenFiles(); for(var i=0;i<files.length;i++) fem.closeFile(files[i]);
var vf=com.intellij.openapi.vfs.LocalFileSystem.getInstance().refreshAndFindFileByPath(p.getBasePath()+"/src/Demo.res");
new com.intellij.openapi.fileEditor.OpenFileDescriptor(p,vf,0).navigate(true);
log.info("reopened Demo.res");
'
```

LSP 起動を `idea.log` で確認する（`OUT`/sandbox パスは後述）。`LSP server initialized` が出るまで 10〜15 秒待つ:

```bash
LOG="<worktree>/.intellijPlatform/sandbox/rescript-intellij-plugin/IU-<ver>/log_runIdeForUiTests/idea.log"
grep -i "Starting ReScript Language Server\|LSP server initialized" "$LOG" | tail -5
```

sandbox パスが不明なときは、robot プロセスの open files から探す:

```bash
lsof -p "$(lsof -ti :8082 | head -1)" | grep idea.log
```

## ステップ 4: ツールウィンドウを表示し caret を動かして読み取る

ツールウィンドウを activate し、caret を目的の宣言/式へ移動 → パネル内容を読む。

ツールウィンドウ activate + caret 移動の汎用スニペット（`__TWID__` / `__NEEDLE__` / `__DELTA__` を置換）:

```bash
<skill-dir>/assets/robot.sh jsinline '
var p=com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects()[0];
com.intellij.openapi.wm.ToolWindowManager.getInstance(p).getToolWindow("__TWID__").activate(null);
var ed=com.intellij.openapi.fileEditor.FileEditorManager.getInstance(p).getSelectedTextEditor();
var off=ed.getDocument().getText().indexOf("__NEEDLE__");
ed.getCaretModel().moveToOffset(off+__DELTA__);
log.info("caret @"+(off+__DELTA__));
'
```

パネル内容の読み取り（EditorComponentImpl の document / JEditorPane の HTML / JLabel を走査）:

```bash
<skill-dir>/assets/robot.sh jsinline '
var p=com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects()[0];
var tw=com.intellij.openapi.wm.ToolWindowManager.getInstance(p).getToolWindow("__TWID__");
var out=[];
function walk(c){
  var cn=c.getClass().getName();
  if(cn.indexOf("EditorComponentImpl")>=0){out.push("[editor] "+c.getEditor().getDocument().getText());}
  else if((c instanceof javax.swing.JLabel)||(c instanceof javax.swing.JEditorPane)){var t=String(c.getText());if(t.trim().length>0)out.push(t);}
  if(c instanceof java.awt.Container){var cs=c.getComponents();for(var i=0;i<cs.length;i++)walk(cs[i]);}
}
var cs=tw.getContentManager().getContents();
for(var i=0;i<cs.length;i++)walk(cs[i].getComponent());
log.info("TW[__TWID__] >>> "+out.join(" ||| "));
'
```

caret 移動後は debounce 窓（200〜300ms）+ LSP 往復ぶん `sleep 1〜2` してから読む。スクリーンショットは `robot.sh frame <name>` で IDE ウィンドウのみ取得し、`Read` ツールで画像を確認できる。

### 期待値の例（sample-project）

- `ReScript Type` で `let greet = (user` → `user => string`、`let permissionLevel` → `role => int`
- `ReScript Switch Flow` で `switch user.email` → `flowchart TD ... Some / None`、switch 外 → `No switch expression at the caret`
- `ReScript Type Impact` で `type user` → `user: N reference(s)`、`type role` で件数が変わる
- `ReScript PPX` で `@react.component` を持つファイル → `Line N: @react.component` の一覧

## ステップ 5: デバウンス検証（連打 → 1 回だけ更新）

「停止後に 1 回だけ更新」を示すには、**単一の EDT 実行内**で caret を連続移動して最後に位置 B で止め、(a) 直後は旧値 A のまま、(b) debounce 窓経過後に B の値、を確認する。1 つの runnable 内の `moveToOffset` は同期的に caretPositionChanged を連発するので、cancel-and-restart が効いていれば中間値は反映されない。

```bash
# 1) 位置A(greet)で安定させる → robot.sh jsinline で caret 移動 → sleep 1.5 → 読取で "user => string"
# 2) バースト（A↔B を数回、最後Bで止める）
<skill-dir>/assets/robot.sh jsinline '
var p=com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects()[0];
var ed=com.intellij.openapi.fileEditor.FileEditorManager.getInstance(p).getSelectedTextEditor();
var t=ed.getDocument().getText(),cm=ed.getCaretModel();
var a=t.indexOf("let greet = (user")+5, b=t.indexOf("let permissionLevel")+5;
var seq=[a,b,a,b,a,b]; for(var i=0;i<seq.length;i++) cm.moveToOffset(seq[i]);
log.info("burst done, final="+cm.getOffset());
'
# 3) 直後に読む → まだ "user => string"（旧値のまま＝連打中は更新されない）
# 4) sleep 1 後に読む → "role => int"（停止後に1回だけ最終値へ）
```

## ステップ 6: ライフサイクル検証（close → reopen で例外なし）

coroutine scope の cancel がクリーンか（`@ApiStatus.Internal` Alarm からの移行確認）を見る。`idea.log` の現在行数をマークしてから close→reopen し、新規 `ERROR` / `exception` / `already disposed` / `not disposed` / `leak` が出ないことを確認する。

```bash
LOG="<...>/log_runIdeForUiTests/idea.log"; MARK=$(wc -l < "$LOG")
<skill-dir>/assets/robot.sh jsinline '
var pm=com.intellij.openapi.project.ProjectManager.getInstance();
var p=pm.getOpenProjects()[0]; var path=p.getBasePath();
com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(new java.lang.Runnable({run:function(){
  com.intellij.ide.impl.ProjectUtil.closeAndDispose(p);
  com.intellij.ide.impl.ProjectUtil.openOrImport(java.nio.file.Path.of(path), com.intellij.ide.impl.OpenProjectTask.build());
  log.info("closed+reopened");
}}));
'
sleep 15
tail -n +"$MARK" "$LOG" | grep -i " ERROR \|exception\|already disposed\|not disposed\|leak" | grep -v "mozilla\|getServersForProvider"
```

ヒットが「Exception reporter ID:（INFO 行）」だけなら無害。reopen 後はパネルが再び動くこと（scope/debouncer 再生成）も再度 caret を動かして確認する。

## ステップ 7: プロジェクト分離検証（任意・2 プロジェクト同時）

2 つ目の ReScript プロジェクト（独自の `type` と `@react.component` を持つ）を別ウィンドウで開き、片方の caret 移動が他方のパネルを更新しないことを**双方向**で確認する。

- 2 つ目を開くと `New Project / This Window or New Window?` ダイアログ → `New Window` を押す（ステップ 2 のクリック法）。続けて `Trust Project` も解除する。
- パネル読取はプロジェクトごとに行う:

```bash
<skill-dir>/assets/robot.sh jsinline '
var ps=com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects();
function read(p,id){var tw=com.intellij.openapi.wm.ToolWindowManager.getInstance(p).getToolWindow(id);var o=[];
 function w(c){if(c.getClass().getName().indexOf("EditorComponentImpl")>=0)o.push(c.getEditor().getDocument().getText());
  if(c instanceof java.awt.Container){var cs=c.getComponents();for(var i=0;i<cs.length;i++)w(cs[i]);}}
 var cs=tw.getContentManager().getContents();for(var i=0;i<cs.length;i++)w(cs[i].getComponent());return o.join(" | ");}
for(var i=0;i<ps.length;i++) log.info("["+ps[i].getName()+"] ReScript Type = "+read(ps[i],"ReScript Type"));
'
```

片方だけ caret を動かして再読取し、もう片方の値が変わらないことを確認する。2 つ目のプロジェクトは `node_modules` を 1 つ目から symlink するとビルドが速い。

## ステップ 8: クリーンアップ

- バックグラウンドで起動した `runIdeForUiTests` タスクは **TaskStop で止める**（プロセス kill コマンドはサンドボックス分類で弾かれることがある）。
- `$OUT`（既定 `/tmp/ide_smoke`）の一時スクリプト・スクショ、2 つ目に作った一時プロジェクトは手動掃除でよい。同一セッション内で worktree の `git worktree remove` はしない（CWD が壊れる）。

## トラブルシュート

| 症状 | 原因と対処 |
|---|---|
| Type Info が常に `No type information` | (1) モーダルダイアログが EDT を塞いでいる → ステップ 2。(2) LSP 未起動 → `idea.log` で `Starting LSP server` 確認、`.res` を開き直す。(3) `.cmt` 未生成 → `npx rescript build` |
| `/screenshot` が JSON テキストを返す | 正常。byte 配列を PNG 化する必要がある → `robot.sh shot/frame` が変換する |
| `getServersForProvider` で Rhino エラー | KClass 解決が面倒。LSP 状態は OS の `pgrep -fl "@rescript/language-server"` か `idea.log` で見るほうが確実 |
| `invokeLater` が発火しない / `runInEdt` がハングする | モーダルダイアログが EDT を占有している → ステップ 2 |
| パネルが空のまま | activate 後に caret を一度動かして refresh をトリガする。debounce ぶん sleep してから読む |

## 報告

検証項目ごとに「期待値 / 実測値 / PASS|FAIL」を表で示す。`idea.log` の ERROR 件数と、close→reopen 後の新規例外有無を必ず添える。LSP 未起動など環境要因で落ちた場合は、プラグインの不具合と切り分けて報告する（初回ダイアログ起因かどうかを明記）。
