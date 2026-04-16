# タスクリスト: Sphinx 品質自動化 + インタラクティブ

## #19 リンクチェック厳格化
- [x] 既存のリンク切れを修正（navigation.md の #goto-super-res-resi アンカー）
- [x] docs.yml の Link check ステップから continue-on-error を除去

## #23 pofilter 翻訳品質チェック
- [x] translate-toolkit を pyproject.toml に追加
- [x] Makefile に check-po ターゲット追加
- [x] docs.yml の lint-and-test ジョブに check-po を追加

## #21 pa11y アクセシビリティ監査
- [x] sphinx-docs/package.json と .pa11yci.json 作成
- [x] Makefile に a11y ターゲット追加
- [x] docs.yml に a11y ジョブ追加

## #20 コード例テスト
- [x] tests/test_code_examples.py を作成（recipes のコードブロック抽出＋検証）
- [x] pytest 実行でパスすることを確認

## #18 キーマップビジュアライザ
- [x] user/keymap-visualizer.md 新規作成（raw html で SVG キーボード）
- [x] _static/js/keymap-visualizer.js 作成
- [x] _static/css/keymap-visualizer.css 作成
- [x] conf.py / html_js_files / html_css_files 更新
- [x] user/index.md の toctree に追加
- [x] .po 更新・翻訳

## #17 設定ジェネレータ
- [x] user/settings-generator.md 新規作成
- [x] _static/js/settings-generator.js 作成
- [x] _static/css/settings-generator.css 作成
- [x] conf.py 更新
- [x] user/index.md の toctree に追加
- [x] .po 更新・翻訳

## #16 Dokka 統合
- [x] build.gradle.kts に Dokka プラグイン追加
- [x] `./gradlew dokkaHtml` が成功することを確認
- [x] dev/api-reference.md 作成（Dokka 出力へのリンク）
- [x] dev/index.md の toctree に追加
- [x] docs.yml に Dokka ビルドステップ追加（任意）
- [x] .po 更新・翻訳

## 検証
- [x] `make build-all` 成功
- [x] `make linkcheck` 成功
- [x] `make check-po` 成功
- [x] 各 CI ステップがローカルで通る

## マージ
- [x] main にマージ
