# タスクリスト: Sphinx 品質自動化 + インタラクティブ

## #19 リンクチェック厳格化
- [ ] 既存のリンク切れを修正（navigation.md の #goto-super-res-resi アンカー）
- [ ] docs.yml の Link check ステップから continue-on-error を除去

## #23 pofilter 翻訳品質チェック
- [ ] translate-toolkit を pyproject.toml に追加
- [ ] Makefile に check-po ターゲット追加
- [ ] docs.yml の lint-and-test ジョブに check-po を追加

## #21 pa11y アクセシビリティ監査
- [ ] sphinx-docs/package.json と .pa11yci.json 作成
- [ ] Makefile に a11y ターゲット追加
- [ ] docs.yml に a11y ジョブ追加

## #20 コード例テスト
- [ ] tests/test_code_examples.py を作成（recipes のコードブロック抽出＋検証）
- [ ] pytest 実行でパスすることを確認

## #18 キーマップビジュアライザ
- [ ] user/keymap-visualizer.md 新規作成（raw html で SVG キーボード）
- [ ] _static/js/keymap-visualizer.js 作成
- [ ] _static/css/keymap-visualizer.css 作成
- [ ] conf.py / html_js_files / html_css_files 更新
- [ ] user/index.md の toctree に追加
- [ ] .po 更新・翻訳

## #17 設定ジェネレータ
- [ ] user/settings-generator.md 新規作成
- [ ] _static/js/settings-generator.js 作成
- [ ] _static/css/settings-generator.css 作成
- [ ] conf.py 更新
- [ ] user/index.md の toctree に追加
- [ ] .po 更新・翻訳

## #16 Dokka 統合
- [ ] build.gradle.kts に Dokka プラグイン追加
- [ ] `./gradlew dokkaHtml` が成功することを確認
- [ ] dev/api-reference.md 作成（Dokka 出力へのリンク）
- [ ] dev/index.md の toctree に追加
- [ ] docs.yml に Dokka ビルドステップ追加（任意）
- [ ] .po 更新・翻訳

## 検証
- [ ] `make build-all` 成功
- [ ] `make linkcheck` 成功
- [ ] `make check-po` 成功
- [ ] 各 CI ステップがローカルで通る

## マージ
- [ ] main にマージ
