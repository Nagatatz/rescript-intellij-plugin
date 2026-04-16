# 要求: Sphinx ドキュメント品質自動化 + インタラクティブページ

## 概要

前回の改善（#3-#10）に続き、CI 自動化4件 + インタラクティブコンテンツ2件 + API リファレンス統合1件を実装。

## 改善一覧

1. **#19 リンクチェック厳格化** — docs.yml の `continue-on-error` を外す
2. **#23 翻訳品質チェック** — pofilter で .po の整合性を自動検証
3. **#21 a11y 監査** — pa11y-ci でアクセシビリティ自動チェック
4. **#20 コード例テスト** — recipes の ReScript コード断片を pytest で構文チェック
5. **#18 キーマップビジュアライザ** — キーボード上にショートカット表示
6. **#17 設定ジェネレータ** — Web UI で設定 JSON を生成
7. **#16 Dokka 統合** — Kotlin KDoc から API リファレンス生成

## 受け入れ条件

- [ ] CI の docs ワークフローで linkcheck 失敗が CI を落とす
- [ ] CI で pofilter が実行され、翻訳の致命的エラーを検出する
- [ ] CI で pa11y-ci が実行され、WCAG 2.1 Level A 違反を検出する
- [ ] コード例テストが pytest で動作し、既存の recipes を通る
- [ ] キーマップビジュアライザページが HTML として表示される
- [ ] 設定ジェネレータで JSON 出力ができる
- [ ] Dokka が `./gradlew dokkaHtml` で実行でき、API ドキュメントが生成される
- [ ] Sphinx から Dokka 出力へのリンクが存在する
- [ ] `make build-all` が成功し、`make linkcheck` でリンク切れなし
