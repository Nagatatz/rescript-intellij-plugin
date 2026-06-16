# 要求: 2026.2 EAP での LspServer.sendRequestSync バイナリ互換性修正

## 背景

JetBrains Marketplace の互換性チェック（verifier-cli 1.405、2026-06-13 実行）が、ReScript プラグイン 0.1.15 を IntelliJ IDEA IU-262.7581.18（2026.2 EAP）に対して検証した結果、**4 件の Critical バイナリ非互換問題**を報告した。

報告内容（要約）:

> ReScript 0.1.15 is binary incompatible with IntelliJ IDEA IU-262.7581.18
> Invocation of unresolved method `LspServer.sendRequestSync$default(...)` (4)
> The method might have been declared in the super interface: LspClient

なお、この verifier-cli 1.405 が 2026.2 EAP のレイアウトをパースできるようになったことは、PRD / memory に「保留中ブロッカー」として記録していた状況の解消を意味する。ただし本作業では gradle 側の verifier 設定（`2026.1.2` ピン）には手を付けない（スコープ外、後日別途検討）。

## 根本原因

2026.2 EAP では `com.intellij.platform.lsp.api.LspServer.sendRequestSync` が新設の `LspClient` スーパーインターフェースへ移動した。その結果、Kotlin がデフォルト引数のために生成する synthetic メソッド `LspServer.sendRequestSync$default(...)` が `LspServer` 上から消えた。

タイムアウト引数を**省略して** `sendRequestSync { ... }` と呼んでいる箇所は、コンパイラが `invokestatic LspServer.sendRequestSync$default` を発行するため、2026.2 で `NoSuchMethodError` 相当の非互換になる。

2026.1.2 SDK の bytecode を確認した結果:

- 実メソッド: `sendRequestSync(int, Function1)` は存続
- `sendRequestSync$default` は内部で `10000`（10 秒）を代入して実メソッドを `invokeinterface` で呼ぶだけ

タイムアウトを明示的に渡している既存 4 箇所（`RescriptOpenCompiledJsAction`、`RescriptRenameHandler` ×2、Java の `RescriptCodeVisionProvider`）は verifier に **flag されていない** = 実メソッド直呼びは 2026.2 でも解決する。

## 対象の 4 箇所

| 呼び出し | ファイル:行 |
|---------|------------|
| `getHoverType` | `lsp/RescriptLspUtils.kt:113` |
| `getInformationHint` | `lsp/RescriptExpressionTypeProvider.kt:41` |
| `actionPerformed` | `navigation/RescriptCreateInterfaceAction.kt:54` |
| `RegenerateInterfaceQuickFix.applyFix` | `inspection/RescriptSignatureSyncInspection.kt:86` |

## スコープ

- **対象:** 上記 4 箇所に明示的タイムアウト `10_000` を渡す（synthetic `$default` 回避）。
- **対象外:** 内部 API 利用（`PluginManagerCore.getPlugin`、INFO レベル、別案件）、gradle verifier の 2026.2 への切替、PRD/memory のブロッカー記述更新。

## 受け入れ条件

- [ ] 4 箇所すべてが明示的タイムアウト引数付きの `sendRequestSync(10_000) { ... }` 形式になっている
- [ ] 既存挙動（10 秒タイムアウト）が保持される
- [ ] `./gradlew ktlintCheck clean buildPlugin test` が成功する
- [ ] 新規警告が増えていない
- [ ] ドキュメント（CLAUDE.md / README / sphinx）は機能変更がないため更新不要であることを確認
