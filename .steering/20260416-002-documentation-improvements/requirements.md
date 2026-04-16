# ドキュメント改善 要求定義

## 背景

プロジェクトのドキュメント（CLAUDE.md, README.md, docs/, sphinx-docs/, .claude/rules/）をレビューした結果、以下の問題が判明した:

- `product-requirements.md` に「全機能実装済み」と明記されているが、168行の実装済みテーブルと「実装済み機能セクションに移動」という documentation.md のルールが残存している
- `CLAUDE.md` のレイヤー 3 機能列挙 (L62-170) が 109 行に肥大化し、新機能追加時の更新コストが高い
- sphinx-docs 日本語翻訳に 98 件の未翻訳 msgstr が存在（10 ファイル）
- README.md と CLAUDE.md の読者対象が混在し、技術詳細が README に残存している
- `.claude/rules/` 12ファイルが階層なしで並列配置され、新規開発者が参照先を判断しづらい
- Project Wizard 15 テンプレートの説明が 3 ドキュメントに分散し同期困難
- `docs/glossary.md` の用語が無秩序に列挙されている
- sphinx-docs トラブルシューティングの深掘り不足
- `pluginUntilBuild` 未設定の背景が `architecture.md` に記載されていない
- バージョン情報が 4 ファイルに分散し信頼できる情報源が不明

## 目的

ドキュメントの責務分離・重複解消・翻訳整備を行い、将来の新機能追加・修正に伴う更新コストを下げる。

## スコープ

以下 10 項目を順次対応する:

1. `docs/product-requirements.md` の整理
   - 「実装済み機能」テーブル（L73-232）を削除
   - ロードマップセクションの整合性調整
   - `documentation.md` の「実装済み機能セクションに移動」ルールも削除
2. `CLAUDE.md` L62-170 の機能列挙を要約化し、詳細は `docs/functional-design.md` に委譲
3. sphinx-docs `.po` 未翻訳 98 件を翻訳
   - 対象 10 ファイル（`features/code-analysis.po` 21件、`features/code-completion.po` 20件、`features/advanced.po` 20件 ほか）
   - `make build-ja` が成功することを確認
4. README.md の Architecture セクションを CLAUDE.md 誘導リンクに置換（読者分離）
5. `.claude/rules/README.md` を新規作成し、目的別グルーピングを提供
6. Project Wizard テンプレート情報を `docs/templates.md` に集約し、CLAUDE.md / README.md / product-requirements.md は要約 + リンクに変更
7. `docs/glossary.md` の用語をカテゴリ内で 50 音／アルファベット順にソート
8. `sphinx-docs/user/troubleshooting.md` に診断手順（LSP 再起動、ログ確認、キャッシュクリア等）を追記
9. `docs/architecture.md` に「`pluginUntilBuild` を設定しない理由」を明記
10. `docs/versions.md`（新規）にバージョン情報を一元化し、他ドキュメントからリンク

## スコープ外

- コード変更（Kotlin/Java/JFlex）
- `docs/functional-design.md` の再構成（肥大化しているが本作業では扱わない）
- ユーザー向け日本語 Sphinx ドキュメントの CI デプロイ設定（翻訳自体は対象）

## 受け入れ条件

- [ ] 10 項目すべてがコミットされている
- [ ] `./gradlew ktlintCheck buildPlugin` が成功（ドキュメント変更のためビルドに影響しないはず）
- [ ] `cd sphinx-docs && make build-ja` が成功し、未翻訳 msgstr が 0 になる（該当 10 ファイル）
- [ ] `CLAUDE.md`, `README.md`, `docs/product-requirements.md` の機能情報が同期されている
- [ ] ドキュメント間のリンク切れが発生していない
- [ ] `.claude/rules/documentation.md` の「実装済み機能セクションに移動」ルールが削除・改訂されている

## 非機能要件

- 既存の文体・絵文字使用方針を維持する
- 日本語訳は既存翻訳のトーンに合わせる（敬体・常体の混在を避ける）
- リポジトリ構造変更は最小限（新規ファイル追加 2 つ: `.claude/rules/README.md`, `docs/templates.md`, `docs/versions.md`）

## リスク

- `CLAUDE.md` 機能列挙の削減が過剰になると、実装済み機能を把握できなくなる → `functional-design.md` または `README.md` に誘導リンクを必ず残す
- `.po` 翻訳で誤訳が入ると誤った情報が日本語ユーザーに伝わる → 既存の翻訳語彙と一致させる
- ドキュメント大量変更でコミットが肥大化 → 項目単位でコミットを分割
