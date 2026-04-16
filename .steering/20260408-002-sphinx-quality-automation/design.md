# 設計: Sphinx ドキュメント品質自動化 + インタラクティブページ

## 実装順序

小さく独立したものから:
1. #19 linkcheck 厳格化（1ファイル変更）
2. #23 pofilter 導入（Makefile + CI）
3. #21 pa11y 導入（Makefile + CI + 設定ファイル）
4. #20 コード例テスト（pytest スクリプト）
5. #18 キーマップビジュアライザ（HTML ページ）
6. #17 設定ジェネレータ（HTML ページ）
7. #16 Dokka 統合（Gradle + Sphinx link）

## 技術詳細

### #19 linkcheck 厳格化
`.github/workflows/docs.yml` の `Link check` ステップから `continue-on-error: true` を除去。
既存のリンク切れは事前に修正する（navigation.md の `goto-super-res-resi` アンカー問題）。

### #23 pofilter
- `translate-toolkit` を `sphinx-docs/pyproject.toml` の dev 依存に追加
- `Makefile` に `check-po` ターゲット追加: 全 .po ファイルに対し `pofilter --excludefilter=untranslated` 相当の実行
- `docs.yml` の lint-and-test ジョブに追加
- `untranslated` は除外（翻訳作業中のものを許容）、`accelerators`, `doublespacing` 等の致命的エラーのみ検出

### #21 pa11y
- `package.json` を sphinx-docs 配下に作成（Node.js 依存管理）
- `pa11y-ci` と `.pa11yci.json` 設定ファイル
- 対象 URL: ビルドされたローカル HTML サーバー (127.0.0.1:8000) の主要ページ
- `Makefile` に `a11y` ターゲット追加
- WCAG2AA 基準、重大な違反のみ fail

### #20 コード例テスト
- `sphinx-docs/tests/test_code_examples.py` を作成
- markdown-it-py を使って recipes 内の \`\`\`rescript と \`\`\`javascript コードブロックを抽出
- ReScript コードは簡易な構文チェック（括弧バランス、let 存在等）で検証
- 実際のコンパイルはリソース集約なので行わない
- pytest で実行、既存の CI (pytest ステップ) に自動的に含まれる

### #18 キーマップビジュアライザ
- `user/keymap-visualizer.md` にページ作成
- `{raw} html` ディレクティブで HTML を直接埋め込み
- QWERTY 英語配列の SVG キーボード
- JavaScript で各キーに対応するショートカットをツールチップ表示
- `_static/js/keymap-visualizer.js` でロジック
- `_static/css/keymap-visualizer.css` でスタイル

### #17 設定ジェネレータ
- `user/settings-generator.md` にページ作成
- HTML フォーム: チェックボックス / セレクトで設定項目を選択
- JavaScript で JSON を生成 → テキストエリア + コピー + ダウンロードボタン
- 対象設定: `codeLens`, `incrementalTypechecking.enabled`, `signatureHelp`, `inlayHints`, `compileStatus`, `lspPath`
- `_static/js/settings-generator.js` でロジック

### #16 Dokka 統合
- `build.gradle.kts` に `org.jetbrains.dokka` プラグイン追加
- `gradle.properties` の Dokka バージョン管理 (version catalog もしくは直接)
- `./gradlew dokkaHtml` で `build/dokka/html/` に出力
- Sphinx 側 `dev/api-reference.md` を新規作成し、GitHub Pages 上の `/api/` パス（または相対パス）にリンク
- GitHub Actions の docs.yml に Dokka 実行と成果物のデプロイを追加
- JDK 21 setup が必要（既に存在する場合は不要）

## 翻訳方針

新規 .md ファイルについては `make update-po` で .po 生成。インタラクティブページは UI テキストが少ないので翻訳負荷は小さい。Dokka ページはリンクのみなので翻訳不要。
