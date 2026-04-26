# 第 4 回機能調査 — 調査要約と既実装照合

## 調査対象

| ソース | 主な確認方法 |
|---|---|
| reasonml-idea-plugin | GitHub `plugin.xml` / `src/main/java/com/reason/ide/` のディレクトリツリー |
| rescript-vscode | `package.json` の `contributes`、`server/src/codeActions.ts`、`snippets.json`、CHANGELOG |
| IntelliJ Rust | GitHub README / 公式ブログ（リリースノート 2022.3 以降） |
| IntelliJ Scala | JetBrains Marketplace / 2025 年ブログ記事 |
| IntelliJ Haskell | GitHub README |
| OCaml IntelliJ Plugin | Marketplace ページ |
| Erlang Plugin | Marketplace ページ |
| JetBrains Fleet | 2025-12-22 開発停止の発表記事（→ 調査対象外と判断） |
| IntelliJ Platform 2025.3 / 2026.x | Plugin SDK ドキュメント、Notable API List 2025、複数の Platform ブログ記事 |

## 既実装と判定して候補から除外したもの

実コード（`src/main/kotlin`、`src/main/resources/META-INF/plugin.xml`）との grep / 直接読み込みで以下を確認:

| 候補（除外） | 既実装の根拠 |
|---|---|
| LSP Customization Pack（progress / documentHighlight / signatureHelp / symbolNavigation） | `RescriptLspServerDescriptor.kt:40` で `lspCustomization` を実装済み |
| Search Everywhere v2 移行（itemsProviderFactory） | `searchEverywhereContributor` を 2 件登録済み（旧 API は当面利用継続） |
| compiled JS への Node debugger ブリッジ | `RescriptDebugCompiledJsAction` (Alt+Shift+D) で `node --inspect-brk` 起動済み |
| Pattern Match Case Split / Exhaustiveness Intention（PSI ベース） | `RescriptCaseSplitIntention` 実装済み（ただし LSP 診断駆動の自動修正は未実装—下記候補参照） |
| Goto Implementation / Super | `RescriptGotoImplementationAction` / `RescriptGotoSuperHandler` 実装済み |
| Generate Doc Comment | `RescriptGenerateDocCommentIntention` 実装済み |
| `//#region` 折りたたみ | `RescriptCustomFoldingProvider` 実装済み |
| Run Anything / Tasks | `RescriptRunAnythingProvider` 実装済み（Run Targets dialog はこれと差別化されないと判断） |
| Floating Toolbar | `RescriptFloatingToolbarProvider` 実装済み |
| `//noinspection` Suppressor | `RescriptInspectionSuppressor` 実装済み |
| Project View ノードデコレータ・ネスト | 既存の `RescriptProjectViewNodeDecorator` / `RescriptTreeStructureProvider` でカバー |

## 不確実性が高い項目（追加調査が望ましい）

| 項目 | 不確実性の内容 | 次に確認すべき場所 |
|---|---|---|
| `didYouMean` quickfix | rescript-vscode が独自実装か、それとも LSP が `data.fixes` で送っていて IntelliJ Platform 側が自動的に受けているか不明 | `@rescript/language-server` の `codeActions.ts` 出力ペイロード、IntelliJ LSP API の `LspDiagnostic.codeActions` ハンドリング |
| CMT/CMI viewer（reasonml-idea-plugin） | ReScript v11+ で `.cmt` ファイルが生成されるかどうか不明。reanalyze デバッグでは引き続き有用な可能性 | `rescript-lang.org/docs/manual/latest/build-overview`、`rescript-vscode` のサーバー実装 |
| AI Assistant 公式 Extension Point | 2026.1 時点でも公式ドキュメントに `chatContext` / `inlineAiCompletion` の EP 詳細が見当たらない（社内 SDK 配布の可能性） | JetBrains Plugin Developer Conf 2025 セッション資料、AI Assistant プラグインの open-source 部分 |
| Modular Plugin v2（記述子分割） | 当面 monolith でも動くが、CodeWithMe / 2026.x の split-jar layout への対応は早期着手したい | `Modular Plugins` 公式ドキュメント、2026.1 verifier-cli 1.403+ リリース後 |
| reasonml-idea-plugin の Intentions ディレクトリ | GitHub Tree が省略されており全数把握できていない（十数個ある可能性） | `https://github.com/giraud/reasonml-idea-plugin/tree/master/src/main/java/com/reason/ide/intentions` をブラウザで再確認 |
| `incrementalTypechecking.acrossFiles` 設定 | LSP に送信できる初期化オプションだが、設定 UI に未公開かどうか要確認 | `RescriptProjectSettings` / `RescriptConfigurable` の設定項目を一通り確認 |

## 主要参考リンク

- [reasonml-idea-plugin plugin.xml](https://raw.githubusercontent.com/giraud/reasonml-idea-plugin/master/src/main/resources/META-INF/plugin.xml)
- [rescript-vscode codeActions.ts](https://raw.githubusercontent.com/rescript-lang/rescript-vscode/master/server/src/codeActions.ts)
- [rescript-vscode package.json](https://raw.githubusercontent.com/rescript-lang/rescript-vscode/master/package.json)
- [IntelliJ Platform Notable API List 2025](https://plugins.jetbrains.com/docs/intellij/api-notable-list-2025.html)
- [Search Everywhere v2 (Platform Blog 2025-12)](https://blog.jetbrains.com/platform/2025/12/major-architectural-update-introducing-the-new-search-everywhere-api-built-for-remote-development/)
- [Modular Plugins](https://plugins.jetbrains.com/docs/intellij/modular-plugins.html)
- [LSP API GA (Platform Blog 2025-09)](https://blog.jetbrains.com/platform/2025/09/the-lsp-api-is-now-available-to-all-intellij-idea-users-and-plugin-developers/)
- [Debugger Architecture Redesign 2026.1](https://blog.jetbrains.com/platform/2026/01/platform-debugger-architecture-redesign-for-remote-development-in-2026-1/)
- [IntelliJ Code Coverage Docs](https://www.jetbrains.com/help/idea/code-coverage.html)
- [IntelliJ Rust Updates 2022.3](https://blog.jetbrains.com/rust/2022/12/16/intellij-rust-updates-for-2022-3/)
- [IntelliJ Scala Plugin in 2025](https://blog.jetbrains.com/scala/2026/01/27/intellij-scala-plugin-in-2025/)
- [Future of Fleet (2025-12)](https://blog.jetbrains.com/fleet/2025/12/the-future-of-fleet/)
