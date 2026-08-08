# CI Windows で固定的に失敗する 5 件の解消

## 背景

ステアリング `20260808-002` で Windows ローカルのテスト失敗 8 件を解消したあと、`os-matrix.yml` に `./gradlew test` を追加した（コミット `b5144aae`）。これにより **CI の Windows / macOS ランナーでテストが実行されるのは初めて**となり、ubuntu 以外が元から赤だったことが判明した。

同一コミットで 4 回、変更前コミットで 1 回の計 5 サンプルを取得し、失敗の変動幅を測定した。

| OS | 失敗数の範囲（同一コミット 4 回） |
|----|--------------------------------|
| ubuntu | 0（4/4 とも success） |
| macOS | 8〜11 |
| Windows | 21〜37 |

大半は実行ごとに失敗クラスが入れ替わるフレークだが、**2 クラス 5 件だけは全 5 サンプルで例外なく失敗している**。本作業はこの決定的な 5 件を対象とする。

## 対象と原因（特定済み）

### A. `util.RescriptProcessUtilsTest` — 4 件

4 テストすべてが `bash` を直接起動している（`RescriptProcessUtilsTest.kt:42, 78, 87, 96`）。

CI Windows ランナーでは PATH 上の `bash` が **WSL ランチャー `C:\Windows\System32\bash.exe`** に解決され、WSL ディストリビューションが未インストールのため、引数によらず即座に `exit 1` を返す。

| テスト | 期待 | 実測 |
|-------|------|------|
| `executeWithStdin captures stderr` | exit 0 | 1 |
| `executeWithStdin reports non-zero exit code` | exit 42 | 1 |
| `executeWithStdin handles timeout` | timedOut = true | false（即終了） |
| `testRunSimpleCommandTimesOut` | exit -1 | 1（即終了） |

同一ファイル内で `cat` を使う `executeWithStdin passes input to process and captures output` は成功している。`cat` は Git Bash の実体に解決されるためで、`bash` だけが WSL ランチャーに奪われていることの裏付けとなる。

**これは `20260808-002` で扱った POSIX 前提テストと同一カテゴリであり、本来あちらのスコープに含まれるべきだった見落としである。** ローカル Windows では PATH が `bash` を Git Bash に解決するため成功してしまい、ローカル検証だけでは原理的に検出できなかった。

### B. `config.RescriptJsonSchemaProviderFactoryTest` — 1 件

`testProviderSchemaFileResolves` が `provider.schemaFile` を解決する際、バンドルされたスキーマを含むプラグイン jar へアクセスする。この jar はテストサンドボックス配下にあるが、VFS の許可ルートに含まれていない。

```
VfsRootAccessNotAllowedError: File accessed outside allowed roots:
file://D:/a/.../.intellijPlatform/sandbox/.../plugins-test/.../lib/rescript-intellij-plugin-0.1.16.3.jar
```

許可ルートには `.../build` は含まれるが `.intellijPlatform/sandbox` は含まれない。A とは原因系統が異なる。

## 受け入れ条件

1. CI Windows で対象 5 件が失敗しなくなる
2. ubuntu / macOS で新たな失敗が発生しない（ubuntu は 4/4 success を維持）
3. Windows で実行不能なテストは削除ではなくスキップとし、理由をコード上に明記する
4. 本体コード (`src/main/`) は変更しない
5. 検証は CI で行う。**B はローカル Windows で再現しない**ため、ローカル成功をもって完了としない
6. フレーク群（Windows 21〜37 / macOS 8〜11 の変動分）は本作業の対象外であり、対象 5 件の解消をもって完了とする

## 制約・リスク

- **B はローカル再現不可。** ローカル Windows では sandbox のパス構成が異なり成功する。CI 実行でしか検証できないため、修正 → push → CI 確認のサイクルを要する
- A の一部（stdout を閉じたまま生存するプロセス、長時間ブロック）は Windows のバッチで等価な再現が難しい可能性がある。cross-platform 化が困難な場合はスキップに倒す
- 検証には CI Windows ランナーでの実行が必須。フレークが混在するため、**対象 5 件が失敗リストから消えたか**をクラス単位で確認する（総失敗数では判定できない）

## 非目標

- フレークする統合テスト群（`RescriptLexerIntegrationTest` 等）の安定化
- macOS 固有の失敗の解消
- `bash` 依存を排除するための本体コード変更
