# Requirements: Postfix Completion

## 概要

ReScript 言語プラグインに Postfix Completion 機能を追加する。式の後に `.switch`, `.pipe`, `.log`, `.some`, `.ok`, `.error`, `.ignore` と入力することで、定型コードパターンに自動展開する。

## ユーザーストーリー

**ReScript 開発者として**、式の後にドットとキーワードを入力して定型パターンに展開することで、タイピング量を減らし、コーディング効率を向上させたい。

## テンプレート一覧

| キー | 展開前 | 展開後 | 説明 |
|------|--------|--------|------|
| `.switch` | `expr.switch` | `switch expr { \| _ => }` | switch 式で囲む |
| `.pipe` | `expr.pipe` | `expr->` | パイプ演算子を付加 |
| `.log` | `expr.log` | `Console.log(expr)` | Console.log で囲む |
| `.some` | `expr.some` | `Some(expr)` | Some で囲む |
| `.ok` | `expr.ok` | `Ok(expr)` | Ok で囲む |
| `.error` | `expr.error` | `Error(expr)` | Error で囲む |
| `.ignore` | `expr.ignore` | `expr->ignore` | ignore パイプを付加 |

## 受け入れ条件

- [ ] 各テンプレートが上記の通り正しく展開される
- [ ] ReScript ファイル (.res, .resi) でのみ動作する
- [ ] コメント内・文字列内では動作しない
- [ ] Settings > Editor > General > Postfix Completion で確認可能
- [ ] ビルドが成功する

## 制約事項

- IntelliJ Platform 2025.3+ の `PostfixTemplateProvider` API を使用
- 既存のコードパターンに従い `com.rescript.plugin.completion` パッケージに配置
