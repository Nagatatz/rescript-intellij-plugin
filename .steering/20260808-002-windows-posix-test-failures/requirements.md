# Windows で残存する POSIX 前提テスト失敗の解消

## 背景

`.gitattributes` による改行コード正規化（コミット `78f2032d`）で、Windows ローカルの `./gradlew test` は 85 件失敗から 8 件失敗まで減った。残る 8 件はいずれも **テストコードが POSIX 環境を前提にしている** ことが原因で、CI (Linux) では green である。

Windows 開発時に毎回 8 件の赤を目視で「既知の失敗」と読み替える必要があり、本物の回帰を見落とすリスクがある。

## 調査結果（確定した分類）

失敗 8 件の内訳と原因。**いずれも本体コードの欠陥ではない**。

| # | テスト | 失敗内容 | 原因 |
|---|-------|---------|------|
| 1 | `run.RescriptCliDetectorTest.findCli returns path when CLI exists in workingDirectory` | `expected: <true> but was: <false>` | `assertTrue(result.contains("node_modules/.bin/rescript"))` がスラッシュをハードコード。Windows の戻り値は `\` 区切り |
| 2 | `run.RescriptCliDetectorTest.findCli returns path when CLI exists in projectBasePath` | 同上 | 同上 |
| 3 | `analysis.RescriptReanalyzeServerServiceTest.getSocketPath returns correct path` | `expected: </project/root/....sock> but was: <\project\root\....sock>` | `Path.toString()` を POSIX 形式の文字列リテラルと比較している |
| 4 | `analysis.RescriptFormatCheckAnnotatorTest.runFormatCheck returns null for command with non-zero exit` | `ProcessNotCreatedException: Cannot run program "\usr\bin\false"` | `/usr/bin/false` をハードコード |
| 5 | `analysis.RescriptFormatCheckAnnotatorTest.runFormatCheck cleans up process on non-zero exit` | 同上 | 同上 |
| 6 | `util.RescriptSecurityUtilsTest.isValidExecutable returns false for non-executable file` | `expected: <false> but was: <true>` | Windows に実行ビットの概念がなく `File.canExecute()` が true を返す |
| 7 | `settings.RescriptSettingsValidatorTest.validateNodePath throws when file is not executable` | 例外が投げられない | 同上（`isValidExecutable` 経由） |
| 8 | `settings.RescriptSettingsValidatorTest.validateLspPath throws when non-js file is not executable` | 同上 | 同上 |

### 本体コードを変更しない根拠

当初 `RescriptCliDetector` が Windows で CLI を検出できない実バグではないかと疑ったが、**誤りであることを確認した**:

- 失敗しているのは `assertNotNull(result)` の次行の `assertTrue(...contains(...))` であり、`assertNotNull` は通過している
- 同一クラスの `findCli prefers workingDirectory over projectBasePath` と `findCli searches parent directories` は Windows でも成功している

したがって `Files.isExecutable` は Windows でも拡張子なしファイルに対し true を返しており、**検出そのものは機能している**。

`isValidExecutable` の `File.canExecute()` についても、Windows には実行ビットが存在せず拡張子で実行可否が決まるため、現在の実装は妥当である（エラーメッセージも "chmod +x on Unix" と Unix 前提を明示している）。

## 受け入れ条件

1. Windows 上で `./gradlew test` を実行したとき、失敗件数が **0 件** になる
2. Linux (CI) 上でも従来どおり全件成功する
3. POSIX 固有セマンティクスのため Windows で実行不能なテストは、**削除ではなくスキップ**とし、スキップ理由をコード上のコメントで明示する
4. Windows でスキップになるテストは、CI (Linux) では従来どおり実行され、カバレッジが低下しない
5. 本体コード (`src/main/`) は変更しない
6. `windows-known-test-failures` の記憶内容が古くなるため、実態に合わせて更新する

## 非目標

- `RescriptCliDetector` に Windows 拡張子（`.cmd` / `.bat` / `.exe`）探索を追加すること（根拠となる不具合が確認できていないため、別件とする）
- Windows 実機での npm インストール構成の検証
- 8 件以外のテストのリファクタリング
