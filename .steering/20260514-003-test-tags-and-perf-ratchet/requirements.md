# 要求: テスト分類の整備と perf ratchet 強化

## 背景

`src/test/kotlin/` には 347 件のテストがあり、内訳は以下:

- 純粋ユニットテスト (fixture なし): 大多数
- 軽量 IDE fixture (`IntelliJPlatformExtension`): 約 20 ファイル (`*IntegrationTest.kt` が中心)
- 重量 IDE fixture (`IntelliJPlatformExtensionWithContentRoot`): 1 ファイル (3〜10s/テスト)
- 外部 CLI 依存 (`com/rescript/plugin/cli/`): `mmdc` / `dot` が必要、ローカルでは skip
- perf smoke (`com/rescript/plugin/perf/`): 4 ファイル、ハードコード `timeLimitMs = 500L`

CI の `./gradlew test` は全て走らせており、PR 時の試行錯誤サイクルがやや長い。また perf テストは「500ms 以下」という緩い上限で固定されており、実測コストの 10 倍以上の余裕があるためリグレッション検知力が弱い。

## 目的

1. **PR フィードバック高速化**: fast スイートを切り出し、開発者ローカル / CI PR ジョブで素早く回せるようにする
2. **perf リグレッション検知強化**: ハードコード上限ではなく baseline + slack モデルに切り替え、CI ログから「現在のヘッドルーム」が読み取れるようにする
3. **既存テストへのソース変更を最小化**: ファイル名規約 (`*PerfTest`, `*IntegrationTest`, `cli/*`) を活用し、Gradle filter で振り分ける

## 受け入れ条件

- [ ] `./gradlew testFast` が pure unit test のみを実行する（perf / integration / cli を除外）
- [ ] `./gradlew testPerf` が `perf/` 配下のテストのみを実行する
- [ ] `./gradlew testCli` が `cli/` 配下のテストのみを実行する
- [ ] 既存 `./gradlew test` の挙動は不変（全件走る）
- [ ] perf テスト 4 ファイルが baseline + slack モデルに移行し、現在の elapsed と baseline を `println` で出力する
- [ ] `CLAUDE.md` のビルドコマンド表に新 task が追加される
- [ ] CI workflow は本 PR では変更しない（次の改善案として独立タスク化）

## 範囲外

- CI workflow 更新（PR ジョブを testFast に切り替える）— follow-up
- `@Tag` アノテーション追加 — 本 PR では filter ベースで対応、必要なら follow-up
- mutation testing 拡張 — steering 005 でスコープ化
