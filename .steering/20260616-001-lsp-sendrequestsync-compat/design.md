# 設計: sendRequestSync 明示タイムアウト化

## 方針

タイムアウトを省略している 4 箇所に、`$default` synthetic が代入していたのと**同一の値** `10_000`（10 秒）を明示的に渡す。これにより:

- コンパイラは `invokeinterface LspServer.sendRequestSync(int, Function1)` を発行する（2026.2 でも `LspClient` 経由で解決可能）
- 挙動は完全に保持される（従来も実効タイムアウトは 10000）
- 既に明示タイムアウトを渡している箇所と呼び出し形が揃う

## 変更詳細

各箇所を以下のように変更する（ラムダ本体は不変）:

```kotlin
// Before
server.sendRequestSync { languageServer -> ... }

// After
server.sendRequestSync(LSP_REQUEST_TIMEOUT_MS) { languageServer -> ... }
```

### 定数の扱い

リポジトリ既存の慣習（`RescriptOpenCompiledJsAction` / `RescriptRenameHandler` は各クラスに `private const val TIMEOUT_MS = 10_000` を持つ）に倣う。4 箇所はいずれも別クラスに散在するため、各クラスに `private const val` を1つずつ持たせるとボイラープレートが増える。

判断: **各呼び出しにリテラル `10_000` を直接渡し、`// LSP sync request timeout (ms); matches the platform default` のインラインコメントを添える**。理由は、(1) 既存の 4 箇所も `10_000` という同一値であり意味が自明、(2) 共有定数を新設すると LSP ユーティリティの責務配置を巡る判断が必要になり 4 行修正の範囲を超える、(3) コメントで platform default と一致する旨を明記すればマジックナンバー懸念を解消できる。

将来的に全 LSP 呼び出しのタイムアウトを一元管理したくなった場合は、別途リファクタリングタスクで `RescriptLspUtils` に定数を集約する（本修正のスコープ外）。

## テスト方針

4 箇所はいずれも稼働中の LSP サーバーへ同期リクエストを送るコードであり、`testing.md` の免除対象「LSP サーバー結合必須」に該当する。変更内容はデフォルト値の明示化のみで観測可能な挙動変化はなく、ユニットテストで検証できる新ロジックは存在しない。既存テストスイートが green であることをもって回帰がないことを確認する。tasklist にこの免除理由を明記する。

## リスク

- 低。挙動不変・既存呼び出し形に合わせるのみ。2026.1.2 ビルドでも `sendRequestSync(int, Function1)` は存続しているためコンパイル可能。
