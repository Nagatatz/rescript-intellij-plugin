# Tasklist: rescript.json サポート + Run Configuration

## Phase 1: rescript.json ファイル認識

- [x] 1.1 `rescript-config.svg` アイコンを `src/main/resources/icons/` に作成
- [x] 1.2 `RescriptIcons.kt` に `CONFIG_FILE` 定数を追加
- [x] 1.3 `RescriptJsonIconProvider.kt` を `config/` パッケージに作成
- [x] 1.4 `plugin.xml` に `iconProvider` を登録
- [x] 1.5 ビルド確認

## Phase 2: CLI 検出ユーティリティ

- [x] 2.1 `RescriptCliDetector.kt` を `run/` パッケージに作成
- [x] 2.2 ビルド確認

## Phase 3: Run Configuration 基盤

- [x] 3.1 `RescriptRunConfigurationOptions.kt` を作成（設定永続化クラス）
- [x] 3.2 `RescriptRunConfigurationType.kt` を作成
- [x] 3.3 `RescriptConfigurationFactory.kt` を作成
- [x] 3.4 `RescriptRunConfiguration.kt` を作成（`getState()` で `CommandLineState` を返す）
- [x] 3.5 `RescriptSettingsEditor.kt` を作成（設定 UI）
- [x] 3.6 `plugin.xml` に `configurationType` を登録
- [x] 3.7 ビルド確認
- [x] 3.8 `RescriptCommand.kt` を追加（コマンド enum 定義）— 実装中に追加

## Phase 4: コミット

- [x] 4.1 全体のビルド確認（`./gradlew clean buildPlugin`）
- [x] 4.2 コミット（`✨ Add rescript.json icon and ReScript Run Configuration support`）
