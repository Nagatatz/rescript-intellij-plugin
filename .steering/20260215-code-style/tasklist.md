# tasklist.md - インデント・コードスタイル

## タスク一覧

### Phase 1: コードスタイル設定 UI

- [x] 1.1 `RescriptCodeStyleSettingsProvider.kt` を作成する
  - `LanguageCodeStyleSettingsProvider` を継承
  - デフォルト値: インデント 2、スペース使用、継続インデント 2
  - `customizeSettings()` で Tabs and Indents タブの標準オプションを表示
  - `getCodeSample()` で ReScript のプレビュー用コードサンプルを提供
- [x] 1.2 `plugin.xml` に `langCodeStyleSettingsProvider` を登録する

### Phase 2: スマートインデント

- [x] 2.1 `RescriptLineIndentProvider.kt` を作成する
  - `LineIndentProvider` を実装
  - `isSuitableFor()` で ReScript 言語を判定
  - `getLineIndent()` でインデント計算ロジックを実装:
    - レクサーで前の行のトークン列を取得
    - ホワイトスペース・コメントを除いた末尾トークンを判定
    - `{`, `(`, `[`, `=>` → +1 インデント
    - それ以外 → 前の行のインデントを維持
    - 文字列リテラル内 → `null` を返す
  - `CodeStyle.getSettings()` からインデントサイズを取得
- [x] 2.2 `plugin.xml` に `lineIndentProvider` を登録する

### Phase 3: テスト

- [x] 3.1 `RescriptLineIndentProviderTest.kt` を作成する
  - ブレース `{` の後で +1 インデント
  - 括弧 `(` の後で +1 インデント
  - ブラケット `[` の後で +1 インデント
  - アロー `=>` の後で +1 インデント
  - 通常行は前の行のインデント維持
  - 行末コメント無視
  - ネストしたインデント
  - ファイル先頭は 0 インデント

### Phase 4: ビルド確認・コミット

- [x] 4.1 `./gradlew clean buildPlugin` でビルドが通ることを確認する
- [x] 4.2 コミットする（`✨ Add smart indentation and code style settings`）
