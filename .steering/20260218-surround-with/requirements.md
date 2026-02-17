# Requirements: Surround With

## 概要

Ctrl+Alt+T（Surround With）で選択コードを ReScript 固有の構文テンプレートで囲む機能を実装する。

## 機能要件

### テンプレート一覧

| テンプレート | 生成コード | カーソル位置 |
|------------|-----------|------------|
| `if` | `if (condition) {\n  <selection>\n}` | `condition` |
| `switch` | `switch expr {\n\| _ => <selection>\n}` | `expr` |
| `try` | `try {\n  <selection>\n} catch {\n\| exn => ()\n}` | `()` |
| `{}` (block) | `{\n  <selection>\n}` | ブロック末尾 |

### 動作仕様

- Ctrl+Alt+T で Surround With ポップアップに ReScript テンプレートが表示される
- 選択テキストがテンプレート内の適切な位置に挿入される
- 囲んだ後、カーソルが編集すべき箇所に配置される
- ReScript ファイル（.res, .resi）でのみ動作する
- LSP 不要（純粋なドキュメント操作）

## 受け入れ条件

- [ ] 4 つのテンプレートが Surround With メニューに表示される
- [ ] 各テンプレートが選択テキストを正しく囲む
- [ ] カーソルが適切な位置に配置される
- [ ] ReScript ファイル以外では表示されない
- [ ] ビルドが成功する
- [ ] ユニットテストが通る
