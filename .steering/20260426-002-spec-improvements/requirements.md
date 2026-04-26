# 要求内容: 仕様ドキュメントの改善

## 背景

プラグイン全体の仕様体系（`docs/`、`README.md`、`plugin.xml` description、`sphinx-docs/`）を横断調査した結果、以下の不整合・空白が発見された:

1. **テンプレート数の記述乖離**: `plugin.xml` description と `functional-design.md` EP マップで「12 テンプレート」と記述されているが、実装は 16 テンプレート（`src/main/kotlin/com/rescript/plugin/wizard/templates/` に 16 ファイル）
2. **LSP 最低バージョン要件の未記載**: `architecture.md`、`plugin.xml`、`sphinx-docs/installation.md` のいずれにも `@rescript/language-server` の対応下限バージョンが記載されていない
3. **プラットフォーム互換性戦略の不在**: PRD に IDE バージョンアップポリシー、verifier ブロッカー対応方針が記載されていない（IntelliJ Platform 2026.1 移行ブロッカーの扱い等）
4. **Extension Point マップの欠落**: `functional-design.md` § 3 の EP マップに新規パッケージ（`scratch/`, `repl/`, `ppx/`, `typeinfo/`, `worksheet/`, `grazie/`, `diagram/` の一部）の登録が反映されていない
5. **ユーザーストーリーの偏重**: PRD US-01〜10 が初期 7 機能（ハイライト、補完、ジャンプ、診断、折りたたみ、ホバー、ブレース、コメント、参照、インレイ）のみで、現在の主要機能領域（Wizard / REPL / Worksheet / PPX / Type Info / 依存ダイアグラム）が反映されていない
6. **LSP フォールバック仕様の未文書化**: NFR-04「LSP 利用不可時もネイティブ機能は正常動作」と書かれているが、機能ごとの「LSP 要/不要」マトリクスが存在しない
7. **パフォーマンス検証手段の未定義**: NFR-01 の目標値（< 16ms / < 50MB）に対する計測手段・記録方法が定義されていない

## 受け入れ条件

- [ ] `plugin.xml` description の「12 templates」を実際の数（16）に修正
- [ ] `functional-design.md` EP マップの「12 テンプレート」を 16 に修正
- [ ] `architecture.md` 外部依存セクションに `@rescript/language-server` の最低バージョン（1.0.0+）を明記
- [ ] `plugin.xml` description の Requirements セクションに最低バージョンを明記
- [ ] `sphinx-docs/user/installation.md` および `version-matrix.md` に最低バージョンを明記
- [ ] PRD に「7. プラットフォーム互換性戦略」セクションを追加し、年次更新ポリシー、verifier ブロッカー対応、`pluginUntilBuild` 運用方針、2026.1 移行ブロッカーへの言及を含める
- [ ] `functional-design.md` § 3 EP マップに不足している EP（scratch、REPL、PPX、TypeInfo、Worksheet、Grazie、依存ダイアグラム）を追加
- [ ] PRD US-11〜US-15 を新設し、Wizard / Worksheet+REPL / PPX 展開 / Type Info / 依存ダイアグラムをカバー
- [ ] `docs/lsp-fallback-matrix.md` を新規作成し、機能ごとの LSP 依存マトリクスを定義
- [ ] `docs/performance-validation.md` を新規作成し、計測手段と記録方法を定義
- [ ] `sphinx-docs/` 配下を変更しないため `.po` 翻訳更新は不要だが、`installation.md` / `version-matrix.md` を更新する場合は対応する `.po` も同一コミットで更新
- [ ] 全変更が `docs/` のみまたはユーザー向けドキュメントのみで、コード変更を含まない
- [ ] `./gradlew ktlintCheck buildPlugin verifyPluginStructure` が通る（コード変更なしのため通常通る想定）

## 範囲外

- 実装コード（`.kt`、`.java`、`.flex`）の変更
- レイアウト・スタイルの変更（`sphinx-docs/_static/` 等）
- `plugin.xml` の `<change-notes>` 更新（次回リリース時に対応）
- `gradle.properties` の `pluginVersion` 更新（次回リリース時に対応）
