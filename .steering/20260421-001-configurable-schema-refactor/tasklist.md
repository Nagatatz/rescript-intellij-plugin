# Tasklist — RescriptConfigurable スキーマ駆動リファクタリング

**参照:** `.claude/rules/definition-of-done.md` の 5 フェーズに沿う。

---

## Phase 1: 計画

- [x] `.steering/20260421-001-configurable-schema-refactor/` 作成
- [x] `requirements.md` 作成・承認
- [x] `design.md` 作成・承認
- [x] `tasklist.md` 作成・承認
- [x] `EnterWorktree` で `configurable-schema` worktree に入る

---

## Phase 2: 実装

各コミットは独立してビルド・テスト pass する単位で分割する。個別 `git add`。

### コミット 1: `♻️ Extract settings path validation to RescriptSettingsValidator`

- [x] `src/main/kotlin/com/rescript/plugin/settings/RescriptSettingsValidator.kt` 新規作成
  - `validateLspPath`, `validateNodePath`, `validateRescriptBinaryPath`,
    `validatePlatformPath`, `validateRuntimePath`
  - 既存 `apply()` のエラーメッセージ文言を 1 字 1 句変えずに移植
  - 全関数に英語 KDoc
- [x] `src/test/kotlin/com/rescript/plugin/settings/RescriptSettingsValidatorTest.kt` 新規作成
  - 空文字列 pass
  - 存在しないパス → `ConfigurationException` with 既存文言
  - `.js` ファイル (LSP): 実行可チェックスキップで pass
  - 非 .js + 非実行可 → 例外
  - 実行可ファイル → pass
  - ディレクトリ系 (platform/runtime) の存在・不在
  - `ConfigurationException.getMessage` deprecation 回避: Throwable 経由で読む拡張プロパティを追加
- [x] `RescriptConfigurable.apply()` を validator 呼び出しに差し替え（UI 無変更）、未使用 import 削除
- [x] `./gradlew ktlintCheck && ./gradlew test --tests "*RescriptSettingsValidatorTest*"`
      pass (18/18)
- [x] tasklist.md のこのコミット項目を `[x]` に更新
- [ ] 個別 `git add` でコミット

### コミット 2: `♻️ Add RescriptSettingDescriptor sealed hierarchy`

- [ ] `src/main/kotlin/com/rescript/plugin/settings/RescriptSettingDescriptor.kt` 新規作成
  - `sealed class RescriptSettingDescriptor<T>` と `SettingComponent<T>` インターフェース
  - 具象: `BoolDescriptor`, `PathDescriptor` (ファイル/フォルダ切替フラグ),
    `ComboDescriptor`, `IntSpinnerDescriptor`
  - `PathDescriptor` 内に `@Suppress("DialogTitleCapitalization")` を保持
  - 全クラス・public メソッドに英語 KDoc
- [ ] `src/test/kotlin/com/rescript/plugin/settings/RescriptSettingDescriptorTest.kt`
      新規作成（testing.md: Swing UI 例外に該当しないユーティリティロジックが対象）
  - `BoolDescriptor.currentValue/applyValue` の往復
  - `PathDescriptor.currentValue/applyValue` の往復
  - `ComboDescriptor.currentValue/applyValue` の往復
  - `IntSpinnerDescriptor.currentValue/applyValue` の往復
  - （`createComponent` 側は Swing UI 免除）
- [ ] `./gradlew ktlintCheck && ./gradlew test --tests "*RescriptSettingDescriptorTest*"`
      pass
- [ ] この時点では `RescriptConfigurable` から未参照。ビルドは通る
- [ ] tasklist.md のこのコミット項目を `[x]` に更新
- [ ] 個別 `git add` でコミット

### コミット 3: `♻️ Drive RescriptConfigurable via settings schema`

- [ ] `src/main/kotlin/com/rescript/plugin/settings/RescriptSettingsSchema.kt` 新規作成
  - `SchemaEntry.Field` / `SchemaEntry.Separator`
  - `entries: List<SchemaEntry>` を 24 エントリで定義（design.md の並びを厳密に再現）
  - 各エントリの label / tooltip 文字列は既存の `FormBuilder` 呼び出しから 1 字 1 句
    コピー
  - object クラス KDoc で順序固定の旨を明記
- [ ] `src/test/kotlin/com/rescript/plugin/settings/RescriptSettingsSchemaTest.kt`
      新規作成
  - 各 descriptor id が `RescriptProjectSettings` のフィールドに対応することを反射で確認
  - エントリ総数と Separator 位置が design.md の仕様と一致
  - descriptor id の重複がないこと
- [ ] `RescriptConfigurable.kt` を書き換え
  - フィールド宣言を `private var componentMap: Map<String, SettingComponent<*>>` 1 本に
  - `createComponent` を schema 走査で組み立てに変更
  - `isModified` を schema 走査に変更
  - `apply` を「validator 呼び出し + schema 走査 applyValue + 副作用」に変更
    （LSP 再起動などの副作用コードは保持）
  - `reset` を schema 走査に変更
- [ ] 旧 `private var xxxCheckbox` / `xxxField` / `xxxCombo` / `xxxSpinner` を全削除
- [ ] `./gradlew ktlintCheck` pass
- [ ] `./gradlew clean buildPlugin` pass
- [ ] `./gradlew test` 全体 pass
- [ ] `./gradlew runIde` で設定 UI を手動確認
  - [ ] 全 20 フィールドが表示される
  - [ ] ラベル・tooltip が現行と同じ
  - [ ] 区切り線位置が現行と同じ
  - [ ] Apply ボタンが modified 時に有効になる
  - [ ] 値変更 → Apply → 再起動 → 値が保持される
  - [ ] 不正パス入力で既存文言のエラーが表示される
- [ ] `RescriptConfigurable.kt` の行数が 280 行以下
- [ ] tasklist.md のこのコミット項目を `[x]` に更新
- [ ] 個別 `git add` でコミット

### コミット 4: `📝 Document settings schema architecture`

- [ ] `CLAUDE.md` レイヤー 3 の「settings/」記述に「スキーマ駆動設定 UI
      (`RescriptSettingsSchema` + `RescriptSettingDescriptor`)」を追記
- [ ] `docs/repository-structure.md` の `settings/` 行を更新
  - 代表クラスに `RescriptSettingsSchema`, `RescriptSettingDescriptor`,
    `RescriptSettingsValidator` を追加
- [ ] README.md / sphinx-docs / product-requirements.md: **ユーザー可視機能の変更なし
      のため更新不要**
- [ ] ktlint pass
- [ ] tasklist.md のこのコミット項目を `[x]` に更新
- [ ] 個別 `git add` でコミット

---

## Phase 3: コミット前検証

各コミットで以下を満たすこと（`.claude/rules/definition-of-done.md` Phase 3）。

### 自己検証

- [ ] `./gradlew ktlintCheck` 成功
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] `./gradlew test` 成功
- [ ] ビルド警告の新規増加なし
- [ ] Deprecated API 新規利用なし（`@Suppress("DialogTitleCapitalization")` は既存と同数）

### コード品質

- [ ] 新規 `class` / `object` / `sealed class` / `interface` すべてに英語 KDoc
- [ ] `RescriptSettingsValidator`, `RescriptSettingDescriptor`, `RescriptSettingsSchema`
      に対応するテストファイルが存在（Swing UI 部分は免除）

### ドキュメント同期

- [ ] `CLAUDE.md` 更新（コミット 4）
- [ ] `docs/repository-structure.md` 更新（コミット 4）
- [ ] sphinx-docs 未更新（ユーザー可視変更なし）

### Git

- [ ] コミット 1〜4 が機能単位で分割されている
- [ ] 絵文字プレフィックス付与（♻️ ×3, 📝 ×1）
- [ ] 個別 `git add`（`-A` / `.` 禁止）

### セキュリティ

- [ ] `RescriptSettingsValidator` のパス検証文言・ロジックを既存から改変していない
- [ ] 絶対パスを UI・エラーメッセージに露出する変更を加えていない
- [ ] 外部プロセス呼び出しの追加なし

---

## Phase 4: マージ前

- [ ] tasklist.md のすべての Phase 2 / Phase 3 項目が `[x]`
- [ ] requirements.md の AC-01〜AC-09 すべて満たす
- [ ] `./gradlew clean buildPlugin` pass
- [ ] `./gradlew test` pass
- [ ] Kover 行カバレッジ minBound 85 を下回らない
  （`./gradlew test koverHtmlReport` で確認）
- [ ] `./gradlew verifyPluginStructure` pass
- [ ] `AskUserQuestion` でマージ可否をユーザーに確認
  - セキュリティ影響: なし（validator はロジック保存のみ、新規外部 I/O なし）を明示
- [ ] tasklist.md のこのセクションを `[x]` に更新（マージ前最終コミット）

---

## Phase 5: マージ後

- [ ] worktree 内で `git checkout main && git merge worktree-configurable-schema`
- [ ] `git branch -d worktree-configurable-schema`
- [ ] セッション終了で worktree 自動クリーンアップ

---

## 備考

- 設定 UI の手動確認は `runIde` 必須。CI では Swing UI の目視確認ができないため、
  コミット 3 の直前で必ず実施する。
- Descriptor の型パラメータ `T` を保つため、`componentMap` は `Map<String,
  SettingComponent<*>>` とし、各参照箇所で `@Suppress("UNCHECKED_CAST")` を
  descriptor 側ヘルパーに閉じ込める。
- 文字列差分（label / tooltip / エラー文言）はリファクタの合否を左右するため、
  既存コードからのコピーに限定し、リワードしない。
