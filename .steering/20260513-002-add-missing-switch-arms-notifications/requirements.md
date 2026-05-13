# 要求: Add Missing Switch Arms intention に診断 Notification を追加

## 背景

サンドボックステストで、Alt+Enter のメニューに `Add missing switch arms` は表示されたが、選択しても何も起きないという報告があった。原因は `RescriptAddMissingSwitchArmsIntention.invoke()` が以下の早期 return をすべて **無音** で処理していること:

```kotlin
val typeText = RescriptLspUtils.getHoverType(...) ?: return        // LSP 未起動 / hover 失敗
val constructors = RescriptLspUtils.parseVariantConstructors(...)
if (constructors.isEmpty()) return                                  // 型が variant でない
val result = RescriptMissingArmsBuilder.computeMissing(...) ?: return // exhaustive 等
```

ユーザー目線では「何も起きなかった」状態と見分けがつかず、デバッグも不可能。

## ユーザーストーリー

**switch 式を書きかけの開発者として**、Add Missing Switch Arms を実行したのに編集が入らなかったとき、**なぜ何も挿入されなかったかを Notification で確認できる** ことで、LSP の起動状態や型情報の不足に気づき、次のアクションを判断したい。

## 受け入れ条件

- [ ] LSP サーバーが未起動の状態で intention を起動すると、IDE 通知エリアに **"ReScript LSP server is not running"** 系のメッセージが表示される
- [ ] LSP hover が空を返した（型を引けなかった）場合、**"Could not infer the scrutinee type"** 系のメッセージが表示される
- [ ] スクラティニーの型が variant でない場合、**"Scrutinee type is not a variant"** 系のメッセージが表示される
- [ ] すべての constructor がすでに被覆されている場合、**"No missing arms"** 系のメッセージが表示される
- [ ] 既存のグリーンパス（変換成功時）は通知を出さない
- [ ] 通知は既存の `ReScript` グループ（`plugin.xml` 登録済み）を再利用する
- [ ] 診断結果を pure に決定する内部関数を切り出し、ユニットテストで全分岐を検証する

## 制約

- 既存の `isAvailableInRescript` 判定（switch 内かどうか / wildcard 有無）は変更しない
- Notification は **WARNING** タイプ（INFO だと一瞬で消えるため）
- LSP サーバー結合必須クラスの test 免除を逸脱しない範囲で、pure helper のテストを追加する
- 既存の振る舞い（成功時の編集挿入）は不変
