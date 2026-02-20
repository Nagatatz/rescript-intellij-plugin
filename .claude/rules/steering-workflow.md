# ステアリングワークフロー

## 実装前の必須プロセス

**以下は強制的な行動指示であり、例外なく従うこと。**

ユーザーから機能追加・変更・バグ修正など、コードの変更を伴う指示を受けた場合、**コードを1行も書く前に**以下のステアリングワークフローを必ず実行すること:

1. `.steering/[YYYYMMDD]-[NNN]-[開発タイトル]/` ディレクトリを作成する
2. `requirements.md` を作成し、ユーザーの承認を得る
3. `design.md` を作成し、ユーザーの承認を得る
4. `tasklist.md` を作成し、ユーザーの承認を得る
5. 承認された `tasklist.md` に従って実装を進める
6. 実装完了後、ビルドが通ることを確認し、適切な粒度でコミットする（Git コミット規約に従うこと）

**実装中の tasklist.md 更新ルール:**
- タスクに着手したら、即座に `tasklist.md` の該当タスクを `[x]` に更新すること
- タスクを飛ばしたり、未完了のまま `[ ]` で放置しないこと
- 実装中に新たに必要なタスクが判明した場合は、`tasklist.md` に追記すること
- **コミットタスクの場合:** `tasklist.md` のコミットタスクを `[x]` に更新してからコミットすること（コミットに `tasklist.md` の更新が含まれるようにする）
- **マージタスクの場合:** `tasklist.md` のマージタスク（「main にマージして worktree を削除」等）を `[x]` に更新し、その更新を**マージ前の最終コミットに含める**こと。マージ後に tasklist を更新する個別コミットは禁止する。マージ実行時点で tasklist.md の全タスクが `[x]` になっている状態を保証すること
- **ドキュメント更新:** ソースコードの変更により以下のドキュメントの更新が必要な場合は、該当コードのコミットにドキュメント更新を含めること（ドキュメント更新のみの個別コミットは不要）:
  - `CLAUDE.md` — プロジェクト構成図・アーキテクチャ説明等
  - `README.md` — 機能一覧・要件等
  - `docs/product-requirements.md` — 実装済み機能一覧・ロードマップの更新（新機能を実装した場合、ロードマップから実装済みへ移動）
  - `docs/functional-design.md` — Extension Point 登録マップ・機能対比表の更新
  - その他 `docs/` 配下 — 変更がアーキテクチャや設計に影響する場合
  - `sphinx-docs/` — ユーザー向け Sphinx ドキュメント。新機能追加・変更時に以下を更新すること:
    - 英語ソース（`sphinx-docs/user/` や `sphinx-docs/dev/` 配下の該当 `.md` ファイル）
    - 日本語翻訳（`sphinx-docs/locale/ja/LC_MESSAGES/` 配下の対応する `.po` ファイル。`make update-po` で `.pot` を再生成してから翻訳を追記する）
    - 該当ページ例: `user/installation.md`（インストール手順）、`user/features/*.md`（機能説明）、`user/changelog.md`（変更履歴）、`dev/project-structure.md`（構造変更時）

**禁止事項:**
- ステアリングファイルを作成せずにコード変更を行うことは禁止する
- ユーザーが「すぐに実装して」「ドキュメントは不要」と言った場合でも、最低限 `requirements.md` と `tasklist.md` は作成すること
- 既存の `.steering/` ディレクトリのドキュメントを使い回さず、新しい作業には必ず新しいディレクトリを作成すること

**例外:** タイポ修正、1行の設定変更など、明らかに軽微な修正の場合はステアリングワークフローを省略してよい。

## コード実装時の git worktree 運用

**以下は強制的な行動指示であり、例外なく従うこと。**

ステアリングワークフローを伴うすべてのコード実装は、**git worktree で専用の隔離空間を作成し、そこで実装すること**。メインリポジトリのワーキングツリーではステアリングドキュメントの作成・承認のみ行い、コード実装は行わない。

**理由:** メインリポジトリでコード実装を行うと、ステアリングドキュメントのコミットとコード変更が混在し、ブランチ汚染やコンフリクトの原因になる。

- 軽微な修正（タイポ修正、1行変更等）は git worktree 不要
- ステアリングワークフローを伴う実装作業は、単一機能であっても必ず git worktree を使用すること

### 単一機能実装の手順

1. **メインリポジトリ:** ステアリングドキュメント（`.steering/[YYYYMMDD]-[NNN]-[機能名]/`）を作成・承認（`main` ブランチ上）
2. **メインリポジトリ:** git worktree を作成（feature ブランチ付き）
   ```bash
   git worktree add ../rescript-wt-<機能名> -b feature/<機能名>
   ```
3. **worktree:** 実装・ビルド確認・コミット
4. **メインリポジトリ:** `main` にマージし、worktree を削除
   ```bash
   git merge feature/<機能名>
   git worktree remove ../rescript-wt-<機能名>
   git branch -d feature/<機能名>
   ```

```
main (メインリポジトリ: steering 作成・承認のみ)
 └── feature/<機能名> (worktree: コード実装)
```

### 並列実装（複数機能の同時実装）

複数の独立した機能を同時に実装する場合、git worktree と複数の Claude Code ウィンドウを使用した並列実装を行う。

#### 前提条件

- 各機能が互いに独立していること（ファイル競合が最小限であること）
- メインウィンドウで全体のステアリングドキュメントが作成・承認済みであること

#### ブランチ戦略

並列実装では **バッチブランチ** を使用する。main ブランチに直接マージするのではなく、バッチブランチを中間ブランチとして使い、全機能のマージ完了後に main へマージする。

```
main
 └── feature/<バッチ名>          ← バッチブランチ（計画・マージ用）
      ├── feature/<機能名1>      ← worktree ブランチ
      ├── feature/<機能名2>      ← worktree ブランチ
      └── feature/<機能名3>      ← worktree ブランチ
```

#### 手順

1. **メインウィンドウ（バッチブランチ作成・計画）:**
   - `main` から バッチブランチ `feature/<バッチ名>` を作成
   - バッチブランチ上で全体のステアリングディレクトリ `.steering/[YYYYMMDD]-[NNN]-[バッチ名]/` を作成
   - requirements.md, design.md, tasklist.md, window-instructions.md を作成・承認
   - ステアリングドキュメントをバッチブランチにコミット
   - バッチブランチから各機能用の git worktree を作成（ブランチ命名規則に従う）

2. **各ウィンドウ（ステアリング + 実装）:**
   - メインリポジトリディレクトリから `cd` で worktree ディレクトリに移動
   - 命令文を貼り付け、以下を自律的に実行:
     - **機能固有のステアリングディレクトリ** `.steering/[YYYYMMDD]-[NNN]-[機能名]/` を作成
     - requirements.md, design.md, tasklist.md を作成（承認確認は不要 — 親ウィンドウで承認済み）
     - 実装・ビルド確認
     - コミット
   - **注意:** 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md 等）は各ウィンドウでは更新しない（コンフリクト防止のため、マージフェーズで一括更新する）

3. **メインウィンドウ（バッチブランチへマージ）:**
   - 全機能ブランチをバッチブランチ `feature/<バッチ名>` に順次マージ（`plugin.xml` 等の競合は手動解決）
   - マージ後 `./gradlew buildPlugin` で最終確認
   - `git worktree remove` でクリーンアップ

4. **メインウィンドウ（ドキュメント一括更新）:**
   - 全機能のマージ完了後、共有ドキュメントを一括更新する:
     - `CLAUDE.md` — プロジェクト構成図・アーキテクチャ説明
     - `docs/product-requirements.md` — 実装済み機能一覧・ロードマップ
     - `docs/functional-design.md` — Extension Point 登録マップ・機能対比表
     - `README.md` — 機能一覧（該当箇所がある場合）
   - コミット: `📝 Update docs for <バッチ名>`

5. **メインウィンドウ（main へマージ）:**
   - バッチブランチ `feature/<バッチ名>` を `main` にマージ
   - `./gradlew buildPlugin` で最終確認
   - バッチブランチを削除

### worktree 命名規則（共通）

```
../rescript-wt-<機能名>/
```

例:
- `../rescript-wt-switch/` — .res/.resi 切り替え
- `../rescript-wt-live-templates/` — Live Templates

#### worktree の作成方法（並列実装）

バッチブランチから worktree を作成する:

```bash
# バッチブランチに切り替え
git checkout feature/<バッチ名>

# バッチブランチから worktree を作成
git worktree add ../rescript-wt-<機能名> -b feature/<機能名>
```

#### 命令文のフォーマット

各ウィンドウへの命令文は `.steering/[YYYYMMDD]-[NNN]-[バッチ名]/window-instructions.md` に記録する。
命令文には以下を含めること:

- ブランチ名と対象機能の説明
- **ステアリングドキュメント作成の指示**（機能固有の requirements.md, design.md, tasklist.md の内容の要約）
- 具体的な実装内容（新規ファイル、変更ファイル、API の使い方）
- 完了条件（ビルド成功、コミットメッセージ）
- マージ先はバッチブランチ `feature/<バッチ名>` であること
- **共有ドキュメント更新は不要**である旨の明記（バッチブランチで一括更新するため）

#### 命令文のテンプレート

```
cd <worktreeの絶対パス>

ブランチ `<ブランチ名>` で <機能名> を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/[YYYYMMDD]-[NNN]-[機能名]/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/<バッチ名>` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C <メインリポジトリパス> checkout feature/<バッチ名>
  git -C <メインリポジトリパス> merge <ブランチ名>
  git -C <メインリポジトリパス> worktree remove <worktreeパス>
  git -C <メインリポジトリパス> branch -d <ブランチ名>

## ステップ 6: 元のディレクトリに戻る
cd <メインリポジトリの絶対パス>
```

### ステアリングディレクトリの命名規則

```
.steering/[YYYYMMDD]-[NNN]-[開発タイトル]/
```

`[NNN]` は 3 桁のシリアル番号（001 から開始、日付が変わるとリセット）。同一日付内で実装順序を明確にするために付与する。

**例：**
- `.steering/20250103-001-initial-implementation/`
- `.steering/20250115-001-add-tag-feature/`
- `.steering/20250115-002-fix-filter-bug/`
- `.steering/20250201-001-improve-performance/`
