# 要求: IntelliJ Platform 2026.2 互換性対応（navbar 削除 + internal API）

## 背景

`.steering/20260616-001-lsp-sendrequestsync-compat/` で `LspServer.sendRequestSync$default` の 4 件を解消した後、本ブランチ（`chore/test-verifier-2026.2`）で `build.gradle.kts` の verifier 設定を実験的に変更し（`pluginVerifier("1.405")` + `pluginVerification.ides` を `recommended()` 化）、ローカルで `./gradlew verifyPlugin` を実行した（2026-06-16）。

結果:

- **verifier-cli 1.405 は 2026.2 EAP レイアウトをパースできるようになった**（旧 1.403 の `ClosedFileSystemException` ブロッカーが解消）。`recommended()` が 3 IDE を検証した。
  - IU-253.33813.25 (2025.3): ✅ Compatible
  - IU-261.25134.95 (2026.1): ✅ Compatible
  - IU-262.7581.18 (2026.2 EAP): ❌ 2 compatibility problems + 1 internal API
- **sendRequestSync 修正は確認済み**（262 レポートに `sendRequestSync$default` 問題はゼロ。commit `2dfd15e5` が有効）。
- ただし 2026.2 EAP でのみ顕在化する **2 件の新規ブロッカー**が判明した。これらは旧 `2026.1.2` ピンでは検出されていなかった。

## 根本原因

### ブロッカー 1: navbar 基底クラスの削除

`com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension` が 2026.2 で**削除された**（2026.1.1/2026.1.2 では `@Deprecated` すら付かずに存在 → 262 で消滅。EAP churn）。

`navbar/RescriptStructureAwareNavbar.kt:18` がこのクラスを直接 `extends` しているため、262 では未解決クラス参照となり 2 件の compatibility problem（クラス本体 + コンストラクタ、`NoSuchClassError` リスク）が報告される。

262 EAP の jar を直接調査した結果:

- `StructureAwareNavBarModelExtension`（便宜サブクラス）: **削除**
- 親 `com.intellij.ide.navigationToolbar.AbstractNavBarModelExtension`: **存続**
- `NavBarModelExtension` インターフェース / `DefaultNavBarExtension`: **存続**

→ 削除されたのは構造ビュー連携の便宜サブクラスのみ。親 `AbstractNavBarModelExtension` は 2026.1 / 2026.2 双方に存在する。

### ブロッカー 2: internal API 化

`com.intellij.ide.plugins.PluginManagerCore.getPlugin(PluginId)` が 2026.2 で `@ApiStatus.Internal` 化された（2025.3 / 2026.1 のレポートには internal API 報告なし → 262 で新規）。

`RescriptErrorReporter.Companion.pluginVersion()`（`RescriptErrorReporter.kt:179`）がこれを呼ぶため、verifier が `INTERNAL_API_USAGES` でビルドを fail させる。

## 検証時に判明した不整合（要追跡）

ユーザーが共有した Marketplace レポート（公開済み 0.1.15）は compatibility problem を「4 件（すべて sendRequestSync）」と表示し navbar 削除には言及していなかった。一方ローカルの修正済みビルド（同一ソース + sendRequestSync 修正のみ）では navbar 問題が出て sendRequestSync は消えている。deprecated/experimental/internal の件数（34/127/1）は完全一致。

navbar のバイトコード参照は両アーティファクトで同一のはずなので、Marketplace も navbar を flag すべきだが headline は 4 件。最も可能性が高いのは「ユーザーの貼り付けが部分コピー（Marketplace は "Method not found" / "Class not found" を別見出しで折りたたむ）」。本ステアリングの実装判断には影響しない（ローカル 262 検証で navbar 削除は実在が確定）が、リリース前に Marketplace の完全レポートを再確認することを推奨事項として残す。

## スコープ

### 対象

1. **navbar 移行**: `RescriptStructureAwareNavbar` を、削除されたサブクラスではなく存続する `AbstractNavBarModelExtension` ベースに移行する（2026.1 / 2026.2 双方で解決可能なバイトコードを生成）。
2. **internal API 置換**: `PluginManagerCore.getPlugin(id)` を public 代替（`PluginManager.getInstance().findEnabledPlugin(id)`）に置換する。
3. **verifier 設定の正式採用**: 実験中の `build.gradle.kts`（`pluginVerifier("1.405")` + `recommended()`）を、上記 2 修正で 262 が green になることを確認した上で正式にコミットする。
4. **ドキュメント更新**: `docs/product-requirements.md` の「現在の既知ブロッカー」（1.403 が 262 で落ちる旨）を 1.405 解消済みに更新。memory も同様に更新。

### 対象外

- experimental API（127 件、`InlayHintsProvider` 系）への対応 — INFO レベルで fail させていない既存事項。別途検討。
- deprecated API（`MarkedString` 等）の解消 — 既存事項。
- `platformVersion` 自体の 2026.2 への引き上げ — コンパイル対象は 2026.1.2 のまま（前方互換を verifier で担保する方針を維持）。

## 受け入れ条件

- [ ] `RescriptStructureAwareNavbar` が 262 で未解決クラス参照を出さない（`./gradlew verifyPlugin` の IU-262 レポートに navbar の compatibility problem が無い）
- [ ] navbar 機能（カーソル位置の囲み宣言をナビゲーションバーに表示）が 2026.1.2 で従来どおり動作する
- [ ] `PluginManagerCore.getPlugin` の internal API 使用が解消され、`pluginVersion()` の挙動（プラグインバージョン文字列取得、失敗時 "unknown"）が保持される
- [ ] `./gradlew verifyPlugin`（1.405 + `recommended()`）が 3 IDE すべてで green（262 の COMPATIBILITY_PROBLEMS / INTERNAL_API_USAGES がゼロ）
- [ ] `./gradlew ktlintCheck clean buildPlugin test` が成功
- [ ] 新規 deprecated/internal API 導入なし
- [ ] `docs/product-requirements.md` のブロッカー記述が更新されている
