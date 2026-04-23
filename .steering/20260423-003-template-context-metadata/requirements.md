# Requirements — TemplateContext への year / Node メタデータ集約

## 背景

現状、テンプレート生成時の「定数だが将来変更されうる値」が複数箇所にハードコードされている:

- `CommonFiles.mitLicense()` の `year: Int = 2026` デフォルト引数
- `TemplateVersions.NODE_ENGINE` / `NODE_MAJOR` を 16 個の `*TemplateFiles.kt` がそれぞれ直接参照
- `CommonFiles.ciWorkflow()` 内の `node-version: 20` ハードコード（`NODE_MAJOR` = 22 と乖離している！）

結果:
1. 年を更新するには `CommonFiles.kt` の引数デフォルトを書き換える必要がある
2. Node バージョンを変えるときに 16 ファイル分の参照を変える必要がある（`TemplateVersions` を変えれば済むが、テスト側もすべて追随が必要）
3. CI workflow の Node 20 が本体の "22+" と**矛盾している**（バグ）

## ゴール

- `TemplateContext` が「共有されるが各テンプレートの主題ではないメタデータ」を担う単一のソースになる
- LICENSE の年は実行時の現在年（もしくは `TemplateContext` で上書き可能）から動的に決定される
- CI workflow の Node セットアップバージョンが `NODE_MAJOR` と一致する
- 新規テンプレートを追加するとき、Node バージョン等を意識せず `ctx.nodeEngine` / `ctx.nodeMajor` / `ctx.year` を参照すればよい

## 非ゴール

- `TemplateVersions` の全面的なインスタンス化（静的 object のまま保持）
- 過去に生成されたプロジェクトへの後方互換性提供
- パッケージマネージャのバージョン（`PNPM` / `NPM` / `YARN` / `BUN`）の同様の扱い（別スコープ）

## 受け入れ条件

- [ ] `TemplateContext` に `year: Int`（デフォルト: `java.time.Year.now().value`）、`nodeMajor: String`（デフォルト: `TemplateVersions.NODE_MAJOR`）、`nodeEngine: String`（デフォルト: `TemplateVersions.NODE_ENGINE`）を追加する
- [ ] `CommonFiles.mitLicense()` の `year` デフォルトを削除し、すべての呼び出し元が `ctx.year` を渡すように変更する
- [ ] `CommonFiles.nvmrc()` が `TemplateContext` を受け取り、`ctx.nodeMajor` を返すように変更する
- [ ] `CommonFiles.ciWorkflow()` 内の `node-version: 20` ハードコードを `ctx.nodeMajor` に置き換える
- [ ] 各 `*TemplateFiles.kt` の `engines = mapOf("node" to TemplateVersions.NODE_ENGINE)` を `engines = mapOf("node" to ctx.nodeEngine)` に置き換える
- [ ] `TemplateVersionsTest` / `CommonFilesTest` / 各テンプレート test が新しいシグネチャでパスする
- [ ] CI workflow を生成したときの `setup-node@v4` の `node-version` が `TemplateVersions.NODE_MAJOR` と一致する新規テストを追加する（バグの回帰防止）
- [ ] LICENSE に今年（実行時）の年が入る新規テストを追加する
- [ ] `./gradlew ktlintCheck buildPlugin test koverVerify` が成功する

## 影響範囲

- `TemplateContext.kt` — フィールド 3 つ追加
- `CommonFiles.kt` — `mitLicense` / `nvmrc` / `ciWorkflow` の signature 変更
- 16 個の `*TemplateFiles.kt` — `engines.node` の参照先を `ctx.nodeEngine` に変更、`CommonFiles.nvmrc()` の呼び出しに `ctx` を渡す
- `CommonFilesTest.kt` — `mitLicense` / `nvmrc` / `ciWorkflow` のテストを新シグネチャに追随
- 12 個のテンプレート test — `.nvmrc` content 検証は `TemplateVersions.NODE_MAJOR` 参照のまま OK（デフォルト値が同じ）

## コミット粒度

1. `TemplateContext` に 3 フィールド追加 + `CommonFiles` の signature 変更 + 全 16 テンプレートの呼び出し更新 + CI node-version バグ修正 → **1 コミット**（単一の refactor として一貫している）
2. テスト追加（現在年 LICENSE / CI node-version = NODE_MAJOR）→ **1 コミット**

計 2 コミット予定。
