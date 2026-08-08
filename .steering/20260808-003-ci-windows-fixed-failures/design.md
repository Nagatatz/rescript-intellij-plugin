# 設計: CI Windows で固定的に失敗する 5 件の解消

## 基本方針

`src/main/` は変更せず、テストコードのみを修正する。**スキップは最後の手段**とし、cross-platform 化できるものは実測で確認したうえで維持する。

| 対象 | 件数 | 手法 |
|------|:---:|------|
| A. `RescriptProcessUtilsTest` | 3 | プラットフォーム別スクリプト生成で cross-platform 化 |
| A. `RescriptProcessUtilsTest` | 1 | `@DisabledOnOs(WINDOWS)`（cmd で等価な再現が不可能） |
| B. `RescriptJsonSchemaProviderFactoryTest` | 1 | テスト分割 + Windows スキップ |

## A. `RescriptProcessUtilsTest`（4 件）

### 原因の再掲

CI Windows では PATH 上の `bash` が WSL ランチャー `C:\Windows\System32\bash.exe` に解決され、WSL 未インストールのため引数によらず即 `exit 1` する。

### スパイクによる実測（確認済み）

使い捨てテストを作成・実行して以下を確認し、削除した。**推測ではなく実測に基づく設計である。**

| 検証 | 結果 |
|------|------|
| `.bat` に `echo err 1>&2` | `exit=0` / stderr に `err` を含む |
| `.bat` に `exit /b 42` | `exit=42` |
| `.bat` に `echo started` + `ping -n 61 127.0.0.1 >nul` を書き `runSimpleCommand(timeoutSeconds=1)` | `exit=-1` / `firstLine='started'` / `timedOut=true` |

### A-1〜A-3: cross-platform 化する 3 件

`20260808-002` の `createAlwaysFailingCommand` と同じ発想で、テストクラスに private helper を追加する。

```kotlin
/**
 * Writes a shell script that behaves identically on both platforms.
 *
 * These tests previously invoked `bash` directly, which resolves to the WSL
 * launcher on CI Windows runners and exits 1 regardless of its arguments.
 *
 * @param dir the directory to create the script in
 * @param name the base file name, without extension
 * @param win the cmd.exe body used on Windows
 * @param posix the sh body used elsewhere
 * @return the path to the generated script
 */
private fun script(dir: Path, name: String, win: String, posix: String): Path =
    if (SystemInfo.isWindows) {
        dir.resolve("$name.bat").also { Files.writeString(it, "@echo off\r\n$win\r\n") }
    } else {
        dir.resolve("$name.sh").also {
            Files.writeString(it, "#!/bin/sh\n$posix\n")
            it.toFile().setExecutable(true)
        }
    }
```

置き換え内容:

| テスト | 旧 | 新（win / posix） |
|-------|----|------------------|
| `executeWithStdin captures stderr` | `bash -c "echo err >&2"` | `echo err 1>&2` / `echo err >&2` |
| `executeWithStdin reports non-zero exit code` | `bash -c "exit 42"` | `exit /b 42` / `exit 42` |
| `testRunSimpleCommandTimesOut` | `bash -c "echo started; sleep 60"` | `echo started` + `ping -n 61 127.0.0.1 >nul` / `echo started; sleep 60` |

一時ディレクトリは `@TempDir` で受け取り、後始末をフレームワークに任せる。

`ping -n 61 127.0.0.1 >nul` を使う理由: `timeout /t` は stdin がリダイレクトされている状況でエラー終了する場合があり、ブロック手段として信頼できない。`ping` は stdin に依存せず確実に待機する。

### A-4: `executeWithStdin handles timeout` は Windows でスキップ

`executeWithStdin` は `process.inputStream.reader().readText()` で **stdout を EOF まで読み切ってから** `waitFor` に入る。したがってタイムアウト経路に到達するには「stdout を閉じたまま生存し続けるプロセス」が必要で、POSIX では `exec 1>&-; sleep 60` がこれを満たす。

cmd.exe には stdout ハンドルを閉じる構文が存在しない（`1>&-` は無効）。`>nul` は各コマンドの出力先を変えるだけでプロセスの stdout パイプは開いたままとなり、`readText()` がプロセス終了までブロックしてタイムアウト経路に入らない。

```kotlin
@Test
@DisabledOnOs(
    value = [OS.WINDOWS],
    disabledReason = "Needs a process that keeps running with stdout closed; cmd.exe has no equivalent of `exec 1>&-`.",
)
fun `executeWithStdin handles timeout`() { ... }
```

## B. `RescriptJsonSchemaProviderFactoryTest`（1 件）

### 却下した案: `VfsRootAccess.allowRootAccess`

第一候補は `VfsRootAccess.allowRootAccess(myFixture.testRootDisposable, PathManager.getPluginsPath())` でサンドボックスを許可ルートに加えることだった。**しかしコンパイル確認の結果 `com.intellij.testFramework.VfsRootAccess` は本プロジェクトのテスト依存に含まれておらず、`Unresolved reference` となった。** 依存追加はスコープ拡大にあたるため却下する。

### 採用案: テストを 2 つに分割する

現行テストのコメントは「`/schemas/rescript.schema.json` が classpath から失われた場合のみ null になる」と述べており、**本来の検証意図はリソースの同梱確認**である。この意図は VFS を介さずに達成できる。

```kotlin
@Test
fun `testSchemaResourceExistsOnClasspath`() {
    // The real intent of the original assertion: the schema ships with the plugin.
    // Checked via the classloader so it does not depend on where the jar sits.
    assertNotNull(RescriptJsonSchemaProviderFactory::class.java.getResource("/schemas/rescript.schema.json"))
}

@Test
@DisabledOnOs(
    value = [OS.WINDOWS],
    disabledReason = "On Windows CI the plugin jar lives under .intellijPlatform/sandbox, outside the fixture's allowed VFS roots.",
)
fun testProviderSchemaFileResolves() {
    val provider = factory.getProviders(project).single()
    assertNotNull(provider.schemaFile)
}
```

この分割により、**検証意図（スキーマ同梱）は全 OS で担保**され、`provider.schemaFile` の VFS 解決経路は ubuntu / macOS で従来どおり検証される。Windows でのみ環境起因のスキップとなる。

## 影響範囲

| 項目 | 内容 |
|------|------|
| 変更ファイル | テスト 2 ファイル |
| `src/main/` | 変更なし |
| 新規クラス | なし |
| カバレッジ | Windows のみ 2 件スキップ増（A-4 / B）。ubuntu・macOS は不変。B は新規テスト 1 件が全 OS で増える |

## 検証方法

**B はローカル Windows で再現しないため、ローカル成功をもって完了としない。** 以下を順に実施する。

1. ローカル Windows で `./gradlew test` を実行し、失敗 0 と対象クラスの成功を確認する
2. `ktlintCheck` / `clean buildPlugin` が通ることを確認する
3. main に push し、`os-matrix.yml` を `workflow_dispatch` で実行する
4. CI Windows のテストレポートを取得し、**対象 2 クラスが失敗リストから消えたことをクラス単位で確認する**
   - フレークにより総失敗数は 21〜37 の範囲で揺れるため、**総数では判定できない**
5. ubuntu が success を維持していることを確認する

## 却下した代替案

| 案 | 却下理由 |
|----|---------|
| 4 件すべて `@DisabledOnOs(WINDOWS)` | スパイクで 3 件は cross-platform 化可能と実測済み。不必要にカバレッジを落とす |
| `VfsRootAccess` で sandbox を許可 | テスト依存に存在せずコンパイル不可。依存追加はスコープ外 |
| `bash` を `sh` や `git-bash` の絶対パスに変更 | ランナーの Git インストール位置に依存し、可搬性がない |
| 本体の `executeWithStdin` を stdout 非ブロックに変更 | テストの都合で本体の設計を変えることになる。本体に欠陥はない |
