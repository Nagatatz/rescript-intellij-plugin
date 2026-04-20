# Requirements — RescriptConfigurable スキーマ駆動リファクタリング

## 背景

`src/main/kotlin/com/rescript/plugin/settings/RescriptConfigurable.kt` (389 行) は、
20 個の設定項目を 4 箇所（フィールド宣言 / `createComponent` / `isModified` / `apply` / `reset`）
で重複して記述する構造になっており、設定項目の追加・削除のたびに複数箇所の修正が必要で
ある。実際、`incrementalAcrossFilesCheckbox` 追加時のような局所的な追記で「漏れ」が発生
しやすいアンチパターンを抱えている。

また、バリデーション（`apply()` 内のパス検証・実行可否検証）と UI コンポーネント生成が
同じクラスに同居しており、testing.md の「Swing UI 免除」対象であるため、ロジックの単体
テストが書けない状態になっている。

## 目的

- 設定項目の宣言を 1 箇所に集約し、`isModified` / `apply` / `reset` を descriptor リスト
  の走査で汎用化する。
- パスバリデーションなどのロジックを非 UI な層へ抽出し、単体テストを追加する。
- 設定項目を追加する際の修正箇所を 5 箇所 → 1 箇所に削減する。

## 非目的

- 設定項目の**追加・削除・既定値変更は行わない**（純粋なリファクタリング）。
- UI レイアウト（区切り線の位置、ラベル文字列、tooltip 文）は**現状維持**する。
- `RescriptProjectSettings` の永続化形式・フィールド名は**変更しない**
  （既存ユーザーの `.idea/*.xml` を壊さない）。
- 設定以外のパッケージ（`errorlens/`、`lsp/` など）は触らない。

## スコープ

### 変更対象

| パス | 変更内容 |
|------|----------|
| `src/main/kotlin/com/rescript/plugin/settings/RescriptConfigurable.kt` | スキーマ駆動に書き換え |
| `src/main/kotlin/com/rescript/plugin/settings/RescriptSettingDescriptor.kt` (新規) | Descriptor sealed class |
| `src/main/kotlin/com/rescript/plugin/settings/RescriptSettingsSchema.kt` (新規) | Descriptor リストの定義 |
| `src/main/kotlin/com/rescript/plugin/settings/RescriptSettingsValidator.kt` (新規) | `apply()` バリデーションを抽出 |
| `src/test/kotlin/com/rescript/plugin/settings/RescriptSettingsValidatorTest.kt` (新規) | validator の単体テスト |

### 変更しない

- `RescriptProjectSettings.kt`（永続化クラス、既存スキーマを維持）
- `plugin.xml`（Extension Point 登録に変更なし）
- 他パッケージの参照元

## 受け入れ条件

- [ ] **AC-01** 設定 UI を開いたとき、現行と同じフィールド・ラベル・tooltip・区切り線が
      表示される（手動で確認）。
- [ ] **AC-02** 既存の `.idea/workspace.xml` 等を読み込んだとき、すべての設定値が正しく
      反映される（既存ユーザー設定を破壊しない）。
- [ ] **AC-03** 設定を変更して「Apply」したとき、`isModified()` が正しく true→false に
      遷移し、`RescriptProjectSettings` に永続化される。
- [ ] **AC-04** 不正なパス（存在しない、非実行可能）を入力したとき、現行と同じ
      `ConfigurationException` メッセージが表示される。
- [ ] **AC-05** `RescriptSettingsValidator` に対する単体テストが追加され、
      `./gradlew test` で pass する。
- [ ] **AC-06** `RescriptConfigurable.kt` の行数が **280 行以下** になる（現在 389 行）。
- [ ] **AC-07** `./gradlew ktlintCheck && ./gradlew clean buildPlugin && ./gradlew test`
      が通る。
- [ ] **AC-08** Kover 行カバレッジが現状（minBound 85）を下回らない。
- [ ] **AC-09** deprecated API の新規利用がない（`./gradlew verifyPlugin` で確認）。

## リスクと緩和策

| リスク | 緩和策 |
|--------|--------|
| FormBuilder の組み立て順変更による UI レイアウト差異 | Descriptor 順序を現行と厳密に 1:1 対応させ、手動で `runIde` 確認 |
| `@Suppress("DialogTitleCapitalization")` の位置ずれで警告再発 | Path 系 descriptor の初期化関数内で suppress を維持 |
| `RescriptProjectSettings` のフィールド名変更による設定ロスト | 永続化クラスは**一切触らない** |
| Configurable 実装の Swing UI テスト免除対象が外れる | validator のみ抽出して UI クラスは免除対象のまま維持 |
