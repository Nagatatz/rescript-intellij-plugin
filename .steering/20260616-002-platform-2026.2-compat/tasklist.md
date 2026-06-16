# タスクリスト: IntelliJ Platform 2026.2 互換性対応

前提: 本作業は `.steering/20260616-001-lsp-sendrequestsync-compat`（commit `2dfd15e5`、本ブランチ `chore/test-verifier-2026.2` に含む）の上に積む。3 修正すべてが揃って初めて 262 が green になる。

依存関係: セクション 1〜3 は互いに独立に実装可。セクション 4（verifier 正式採用・最終検証）は 1〜3 完了後に実施する。

## セクション 1: navbar 移行（1 コミット）

- [x] `navbar/RescriptStructureAwareNavbar.kt` の基底を `AbstractNavBarModelExtension` に差し替え
- [x] `language` プロパティを削除し、各メソッドに `element.language != RescriptLanguage` ガードを追加
- [x] `getParent` を PSI 親ウォークで実装（`RescriptPsiUtils.findEnclosingDeclaration(..., includeElement=false)` 再利用 / 無ければ `super.getParent`）
- [x] `getPresentableText` / `getIcon` を新基底向けに維持（NAVIGABLE_TYPES フィルタ + 言語ガード）
- [x] KDoc を新しい基底クラス・実装方針に合わせて更新

### テスト

- [x] `RescriptStructureAwareNavbarTest.kt` を新実装向けに更新（既存テストが存在した。stub に `getLanguage`/`getParent` を追加）
  - [x] `getParent`: ネストした module/let 要素・leaf から enclosing 宣言を返す
  - [x] `getPresentableText` / `getIcon`: 非 ReScript 言語ガードで null
  - [x] `getPresentableText`: 宣言名を返す / NAVIGABLE_TYPES 外は null（既存テスト維持）
  - [x] `getIcon`: ReScript 要素でアイコンを返す（既存テスト維持）

### 検証・コミット

- [x] `./gradlew verifyPlugin` で IU-262 の navbar compatibility problem が消えたことを確認（262 = Compatible）
- [x] `./gradlew ktlintCheck test`（navbar 17 tests green）
- [x] `🐛 Migrate navbar to AbstractNavBarModelExtension for 2026.2` でコミット（`daed85c5`）

## セクション 2: internal API 解消（1 コミット）

方針変更: 設計時に想定した「public 代替メソッドへの置換」は不成立だった。verifier で `getPlugin` / `findEnabledPlugin` / `getPluginByClass` の 3/3 がすべて 2026.2 で `@Internal` と確認。ユーザー承認のもと **ビルド時バージョン埋め込み** に切替（PluginManager に一切依存しない、verifier 恒久クリーン）。

- [x] `RescriptErrorReporter.kt` の `pluginVersion()` を、ビルド生成リソース `com/rescript/plugin/plugin-version.properties` の読み取りに置換
- [x] `build.gradle.kts` に `generatePluginVersionProperties` タスクを追加し、`processResources` 依存 + main resources srcDir に登録
- [x] 挙動（version / リソース無し時 "unknown" / 例外時 "unknown"）を保持

### テスト

- [x] 当初は免除予定だったが、リソース読取はテスト classpath で検証可能と判明したため `RescriptErrorReporterTest` にリグレッションテストを追加（`buildBody` 経由で版数が "unknown" でなく semver になることを確認、版数値はハードコードせず）

### 検証・コミット

- [x] `./gradlew clean buildPlugin` でリソースが全 jar に同梱されることを確認
- [x] `./gradlew verifyPlugin` で IU-262 の INTERNAL_API_USAGES が消えたことを確認（internal-api-usages.txt 不在）
- [x] `🐛 Read plugin version from a generated resource to avoid 2026.2 internal API` でコミット（`d5184185`）

## セクション 3: build.gradle.kts 正式採用（1 コミット）

- [x] 実験コメントを正式コメントに置換（`1.405` + `recommended()` の根拠を明記）
- [x] `🔧 Adopt verifier-cli 1.405 + recommended()` でコミット（`cd2b050f`。build.gradle.kts は同一ファイルのため §2 のリソース生成タスクと同梱）

## セクション 4: 最終検証・ドキュメント（1 コミット）

- [x] `./gradlew verifyPlugin` を全 IDE（253 / 261 / 262）で実行し、すべて Compatible（262 の COMPATIBILITY_PROBLEMS / INTERNAL_API_USAGES がゼロ）を確認
- [x] `./gradlew ktlintCheck clean buildPlugin test` 成功
- [x] `docs/product-requirements.md` の「現在の既知ブロッカー」を解消済みに更新
- [x] memory `project_platform_2026_1_blocked.md` を更新（262 解消・recommended() 採用）
- [ ] （任意）`./gradlew runIde` で navbar の囲み宣言ブレッドクラムを目視確認（未実施・任意）
- [x] `📝 Record 2026.2 compatibility resolution` でドキュメント更新をコミット

## セクション 5: マージ

- [ ] requirements.md の受け入れ条件をすべて満たすことを確認
- [ ] ユーザーにマージ可否を確認（セキュリティ影響なし: API 置換と verifier 設定のみ）
- [ ] `main` へマージ・ブランチ削除

## ドキュメント方針

- 機能カテゴリ・ユーザー向け挙動は不変（navbar は機能維持、internal API は内部実装）のため CLAUDE.md / README / sphinx-docs の更新は不要。
- 更新対象は `docs/product-requirements.md` のブロッカー記述と memory のみ（セクション 4）。
