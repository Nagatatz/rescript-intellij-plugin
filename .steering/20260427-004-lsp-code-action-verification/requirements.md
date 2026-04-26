# LSP Code Action 動作検証

## 背景

第 4 回機能調査（`.steering/20260427-003-fourth-feature-survey/`）で、rescript-vscode の主要 Quick Fix 9 種（`simpleAddMissingCases` / `wrapInSome` / `addUndefinedRecordFields` / `simpleConversion` / `applyUncurried` / `didYouMean` / `removeUnusedCode` / `extractLocalModuleToFile` / `expandCatchAllPatterns`）が **LSP server-side code action**（`textDocument/codeAction` で `p.CodeAction` を返す実装）であることを確認した。

本プラグインは IntelliJ 2024.1+ の LSP API で「Quick Fix (LSP Code Actions)」を自動サポート済みとされている（`docs/archive/implemented-features.md`）。しかし、**実際にこれらの code action が IntelliJ 上の Alt+Enter メニューに表示・実行できているかは未確認**。

新規機能を実装する前に、現状の LSP code action 動作状況を網羅的に検証し、実装すべきギャップを正確に特定する必要がある。

## 目的

- 本プラグインで rescript-vscode の 9 種 code action がそれぞれ表示・実行できるかを runIde で検証する
- 動作しない code action があれば、原因を特定する（IntelliJ LSP API の仕様 / `RescriptLspServerDescriptor` の設定不足 / `@rescript/language-server` 側の挙動）
- 検証結果を `docs/lsp-fallback-matrix.md` および `docs/archive/implemented-features.md` に反映し、ロードマップ判断の根拠を作る
- 動かない code action のうち、PSI ベースのネイティブ実装で補完すべきものを次回 steering 候補として記録する

## 受け入れ条件

- [ ] 9 種の code action それぞれについて、再現用の `.res` サンプルコードを用意した
- [ ] runIde で `@rescript/language-server` を起動し、各サンプルでカーソルを診断行に置いて Alt+Enter を実行した結果（表示の有無・適用結果・LSP ログ）を記録した
- [ ] 結果テーブルを本 steering の `findings.md` に保存した
- [ ] 動作する code action は `docs/lsp-fallback-matrix.md` に「LSP Code Action として利用可能」エントリを追記した
- [ ] 動作しない code action については、IntelliJ LSP API のドキュメント（`com.intellij.platform.lsp.api.LspServerDescriptor` の code action 関連オプション）と `RescriptLspServerDescriptor.kt` の設定を比較し、不足設定を特定した
- [ ] PSI ベースのネイティブ Quick Fix が必要な code action のリストを `next-steps.md` に記録した
- [ ] 追記の対象になった `docs/lsp-fallback-matrix.md` の日本語訳（該当する `.po`）も同一コミットで更新した

## スコープ外

- ネイティブ Quick Fix の実装（必要性が確認された場合のみ別 steering で扱う）
- LSP プロトコル自体の改修（`@rescript/language-server` 側の修正）
- 他プラグイン（reasonml-idea-plugin 等）との UI 比較

## 成果物

- `requirements.md`（本ファイル）
- `design.md`: 検証手順とサンプル `.res` コードの定義
- `tasklist.md`: 実行タスクリスト
- `findings.md`: 検証結果テーブルと原因分析
- `next-steps.md`: 動作しない code action に対する次のアクション
- `docs/lsp-fallback-matrix.md` への追記
- `docs/archive/implemented-features.md` への追記

## リスクと前提

- ローカル環境で `@rescript/language-server` を最新版にして検証する。バージョン差で挙動が変わる可能性があるため、検証時のバージョンを `findings.md` に明記する
- runIde はビルトインのサンドボックスを使用。`./gradlew runIde` で起動する
- LSP ログは `Help | Show Log in Finder` から `idea.log` を開いて確認する
