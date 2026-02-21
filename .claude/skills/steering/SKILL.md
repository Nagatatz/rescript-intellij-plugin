---
description: 作業指示毎の作業計画、タスクリストをドキュメントに記録するためのスキル。ユーザーからの指示をトリガーとした作業計画時、実装時、検証時に読み込む。
allowed-tools: Read, Glob, Grep, Write, Edit, Bash
disable-model-invocation: true
---

# Steering スキル

ステアリングファイル(`.steering/`)に基づいた実装を支援し、tasklist.mdの進捗管理を確実に行うスキルです。

## 動的コンテキスト

スキル起動時に以下の情報を自動取得する:

- **現在のブランチ:** !`git branch --show-current 2>/dev/null`
- **最新ステアリング:** !`ls -dt .steering/2026* 2>/dev/null | head -1`
- **変更ファイル:** !`git status --short 2>/dev/null | head -10`
- **直近コミット:** !`git log --oneline -3 2>/dev/null`

## スキルの目的

- ステアリングファイル(requirements.md, design.md, tasklist.md)の作成支援
- tasklist.mdに基づいた段階的な実装管理
- **進捗の自動追跡とtasklist.md更新の強制**
- 実装完了後の振り返り記録

## 使い方

- `/steering plan [機能名]` — モード1: ステアリングファイルを作成（計画フェーズ）
- `/steering implement [ディレクトリパス]` — モード2: tasklist.mdに従って実装（実装フェーズ）
- `/steering review [ディレクトリパス]` — モード3: 振り返りを記録（検証フェーズ）

引数なしの `/steering` はモード1として動作する。

---

## モード1: ステアリングファイル作成（計画フェーズ）

### 目的

新しい機能や変更のためのステアリングファイルを作成します。

### 手順

#### 1. ステアリングディレクトリの確認

現在の日付を取得し、`.steering/[YYYYMMDD]-[NNN]-[機能名]/` の形式でディレクトリを作成する。`[NNN]` は 3 桁のシリアル番号（001 から開始、日付が変わるとリセット）。既存の同日ディレクトリを確認して次の番号を採番する。

```bash
# 例: .steering/20260215-001-user-authentication/
```

機能名は引数から取得する。引数がない場合はユーザーに確認する。

#### 2. 永続的ドキュメントの確認

以下のファイルを Read で読み込み、プロジェクトの方針を理解する。ファイルが存在しない場合はスキップする。

- `CLAUDE.md` — コーディング規約、アーキテクチャ概要
- `docs/product-requirements.md` — プロダクト要件
- `docs/functional-design.md` — 機能設計
- `docs/architecture.md` — アーキテクチャ設計
- `docs/repository-structure.md` — リポジトリ構造
- `docs/development-guidelines.md` — 開発ガイドライン

#### 3. テンプレートからファイル作成

以下のテンプレートを Read で読み込み、プレースホルダーを具体的な内容に置き換えてファイルを作成する:

- `.claude/skills/steering/templates/requirements.md` → `.steering/[日付]-[機能名]/requirements.md`
- `.claude/skills/steering/templates/design.md` → `.steering/[日付]-[機能名]/design.md`
- `.claude/skills/steering/templates/tasklist.md` → `.steering/[日付]-[機能名]/tasklist.md`

プレースホルダーの置き換えルール:
- `{{機能名}}` → 実際の機能名
- `{{日付}}` → YYYY-MM-DD形式の日付
- 各セクションの内容は、永続的ドキュメントとユーザーの指示に基づいて具体的に記述する

#### 4. tasklist.mdの詳細化

requirements.md と design.md の内容に基づいて、tasklist.md を詳細化する:

- 各フェーズのタスクを具体的に記述
- サブタスクも明確に定義
- 実装の順序を明記
- 依存関係があるタスクには注記を追加

---

## モード2: 実装（最重要）

### 目的

tasklist.mdに従って実装を進め、**進捗を確実にドキュメントに記録**します。

### 開始手順

1. 引数で指定された `.steering/[ディレクトリ]/` のパスを確認する。引数がない場合は Glob で `.steering/*/tasklist.md` を検索し、最新のものを使用する。
2. `tasklist.md` を Read で読み込み、未完了タスク (`[ ]`) を確認する。
3. `requirements.md` と `design.md` も Read で読み込み、仕様を把握する。

### 重要な原則

**MUST（必須）**:
- tasklist.mdを常に参照した状態で実装する
- タスク開始時に必ず Edit ツールで `[ ]` → `[x]` に更新する
- タスク完了時に必ず Edit ツールで完了を記録する
- **tasklist.md の全タスクが完了するまで作業を継続する**
- 1タスクごとにリアルタイムで tasklist.md を更新する

**NEVER（禁止）**:
- tasklist.md を見ずに実装を進める
- 複数タスクをまとめて更新する（リアルタイムに1つずつ更新する）
- **「時間の都合により」「別タスクとして実施予定」などの理由でタスクをスキップする**
- **未完了タスク (`[ ]`) を残したまま作業を終了する**

### 実装ループ

以下のサイクルを全タスク完了まで繰り返す:

```
1. tasklist.md を Read して次の未完了タスクを確認
2. タスクを Edit で `[x]` に更新（着手マーク）
3. 実装を行う
4. 必要に応じて tasklist.md にメモを追記
5. 1に戻る
```

### 実装中に発見した追加タスク

実装中に新たなタスクが必要と判明した場合:
- tasklist.md の該当フェーズに Edit でタスクを追加する
- 追加理由をメモとして記載する

### 実装完了時のコミット

tasklist.md の全タスクが `[x]` になったら、以下の手順でコミットする:

1. `./gradlew clean buildPlugin` でビルドが通ることを確認する
2. CLAUDE.md の Git コミット規約に従い、適切な粒度で絵文字付きコミットを作成する
3. `git add .` や `git add -A` は使用せず、個別ファイル指定で `git add` する
4. push はユーザーから明示的に指示がない限り実行しない

---

## モード3: 検証と振り返り

### 目的

実装完了後の振り返りを記録し、次回の開発に活かします。

### 手順

#### 1. tasklist.mdの完了確認

tasklist.md を Read し、すべてのタスクが `[x]` になっているか確認する。

- 未完了タスクがある場合: ユーザーに報告し、モード2に戻るか確認する
- 全タスク完了の場合: 振り返りの記録に進む

#### 2. 振り返りの記録

tasklist.md の末尾にある「振り返り」セクションに、以下の内容を Edit で記録する:

- **実装で工夫した点**: 技術的な工夫や設計判断の理由
- **発生した問題と解決策**: 実装中に発生した問題と、どう解決したか
- **設計変更の理由**: 当初の設計から変更した場合、その理由を詳細に記録
- **次回への改善点**: 今回の実装で気づいた、プロセスやドキュメントの改善点

#### 3. 永続的ドキュメントの更新確認

実装により判明した設計の変更点があれば、該当する永続的ドキュメントの更新をユーザーに提案する:

- `docs/architecture.md` — 技術選定やレイヤー構成の変更
- `docs/repository-structure.md` — ディレクトリ構成の変更
- `docs/development-guidelines.md` — 開発規約の追加・変更
- `docs/functional-design.md` — 機能設計の変更

---

## git worktree 運用ガイド

ステアリングを伴うコード実装は **Claude Code のビルトイン worktree 機能**で隔離して行う。

### 単一機能実装

1. メインリポジトリでステアリングドキュメントを作成・承認（main ブランチ上）
2. `EnterWorktree` ツール（または `claude --worktree <機能名>`）で worktree を作成
   - worktree は `.claude/worktrees/<機能名>/` に作成される
   - ブランチ `worktree-<機能名>` が HEAD から自動生成される
3. worktree 内で実装・ビルド確認・コミット
4. セッション終了時に Claude Code が自動クリーンアップを提案する
   - 変更なし: 自動削除
   - 変更あり: keep/remove の確認プロンプト
5. メインリポジトリで main にマージ:
   ```bash
   git merge worktree-<機能名>
   git branch -d worktree-<機能名>
   ```

### 並列実装（複数機能の同時実装）

バッチブランチを中間ブランチとして使い、全機能のマージ完了後に main へマージする。

```
main
 └── feature/<バッチ名>               ← バッチブランチ（計画・マージ用）
      ├── worktree-<機能名1>          ← Claude Code worktree ブランチ
      ├── worktree-<機能名2>          ← Claude Code worktree ブランチ
      └── worktree-<機能名3>          ← Claude Code worktree ブランチ
```

**手順:**

1. `main` からバッチブランチ `feature/<バッチ名>` を作成
2. バッチブランチ上でステアリングディレクトリを作成、requirements.md/design.md/tasklist.md/window-instructions.md を作成・承認
3. 各ウィンドウで `claude --worktree <機能名>` を実行して worktree を作成
4. 各ウィンドウで実装（共有ドキュメント更新は不要 — マージフェーズで一括更新）
5. 全機能ブランチをバッチブランチに順次マージ、`./gradlew buildPlugin` で確認
6. 共有ドキュメント（CLAUDE.md, docs/ 等）を一括更新、コミット: `📝 Update docs for <バッチ名>`
7. バッチブランチを `main` にマージ、削除

### worktree の配置場所

```
.claude/worktrees/<機能名>/
```

`.claude/worktrees/` は `.gitignore` に登録済み。

### 命令文テンプレート（並列実装用）

各ウィンドウへの命令文は `.steering/[YYYYMMDD]-[NNN]-[バッチ名]/window-instructions.md` に記録する。

```
ブランチ `worktree-<機能名>` で <機能名> を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。

## ステップ 1: ステアリングドキュメント作成
`.steering/[YYYYMMDD]-[NNN]-[機能名]/` を作成し、requirements.md, design.md, tasklist.md を作成。

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメントはバッチブランチで一括更新するため更新不要。

## ステップ 5: マージ確認
ユーザーに「バッチブランチにマージして worktree を削除しますか？」と確認。
```
