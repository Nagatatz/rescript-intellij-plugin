# Requirements: P2 JetBrains ギャップ 5機能バッチ実装

## 概要

他の JetBrains 言語プラグイン（Kotlin, JS/TS, Elm, Dart 等）との機能ギャップを埋める P2 の 5 機能を git worktree による並列実装で一括追加する。

全機能がローカル実装（PSI/ドキュメント操作）で独立しているため、共有インフラは不要。

## 対象機能

### 1. Quick Fix: Add Import（難易度: 低）

**説明:** LSP コードアクションによる自動修正（未解決参照への `open` 追加等）の動作確認。

**受け入れ条件:**
- [ ] LSP サーバーが提供するコードアクションが Alt+Enter メニューに表示される
- [ ] エラー・警告の波線上で Quick Fix が利用可能
- [ ] コードアクションの適用でコードが正しく修正される

**備考:** IntelliJ 2024.1+ の LSP API は `textDocument/codeAction` を自動サポート。rescript-language-server が提供するコードアクションは追加コード不要で動作する。実装は動作確認とステアリングドキュメント作成が主な作業。

### 2. Intention Actions（難易度: 中）

**説明:** ReScript 固有のコード変換を Alt+Enter メニューで提供する。

**受け入れ条件:**
- [ ] 式を選択して Alt+Enter で意図アクション一覧が表示される
- [ ] `Wrap with Some(...)` — 式を `Some(expr)` でラップ
- [ ] `Wrap with Ok(...)` — 式を `Ok(expr)` でラップ
- [ ] `Wrap with Error(...)` — 式を `Error(expr)` でラップ
- [ ] `Add @genType annotation` — カーソル行の宣言に `@genType` を追加
- [ ] Settings > Editor > Intentions > ReScript で確認可能

### 3. Surround With（難易度: 低〜中）

**説明:** 選択したコードを構文テンプレートで囲む（Ctrl+Alt+T）。

**受け入れ条件:**
- [ ] コードを選択して Ctrl+Alt+T でサラウンドメニューが表示される
- [ ] `if (...) { }` — 条件式で囲む
- [ ] `switch ... { | _ => }` — パターンマッチで囲む
- [ ] `try { } catch { | exn => }` — 例外処理で囲む
- [ ] `{ }` — ブロックで囲む
- [ ] 囲んだ後、カーソルが適切な位置（条件式やパターン等）に配置される

### 4. Import Optimizer（難易度: 中）

**説明:** `Optimize Imports`（Ctrl+Alt+O）で重複・未使用の `open` 文を削除する。

**受け入れ条件:**
- [ ] Ctrl+Alt+O で `open` 文が最適化される
- [ ] 重複する `open` 文が削除される
- [ ] 最適化後、通知バルーンに削除数が表示される
- [ ] Code > Optimize Imports メニューからも実行可能

**備考:** 未使用 `open` の完全な検出にはセマンティック解析が必要だが、まずは重複 `open` の削除に注力する。将来的に LSP 診断を活用して未使用検出を追加可能。

### 5. Gutter Run Icons（難易度: 中）

**説明:** 実行可能なコンテキストにガターアイコン（▶）を表示する。

**受け入れ条件:**
- [ ] `rescript.json` が存在するプロジェクトの `.res` ファイルにガター▶アイコンが表示される
- [ ] アイコンクリックで ReScript ビルド実行構成が起動される
- [ ] 右クリックメニューに Run/Debug オプションが表示される
- [ ] 既存の `RescriptRunConfigurationType` と連携する

## 実装アプローチ

| 機能 | ブランチ名 | worktree パス |
|------|-----------|--------------|
| Quick Fix | `feature/quick-fix` | `../rescript-wt-quick-fix` |
| Intention Actions | `feature/intention-actions` | `../rescript-wt-intentions` |
| Surround With | `feature/surround-with` | `../rescript-wt-surround` |
| Import Optimizer | `feature/import-optimizer` | `../rescript-wt-import-optimizer` |
| Gutter Run Icons | `feature/run-line-marker` | `../rescript-wt-run-marker` |

## 制約事項

- 各機能は完全に独立しており、共有インフラは不要
- すべてのブランチはバッチブランチから分岐する
- 各ブランチで `./gradlew buildPlugin` が成功すること
- 共有ドキュメント更新はバッチブランチでのマージ後に一括で行う
