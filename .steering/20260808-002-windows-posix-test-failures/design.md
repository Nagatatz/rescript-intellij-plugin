# 設計: Windows で残存する POSIX 前提テスト失敗の解消

## 基本方針

`src/main/` は一切変更せず、**テストコードのみ**を修正する。原因 4 種類に対して 3 つの手法を割り当てる。

| 手法 | 対象件数 | 概要 |
|------|:------:|------|
| A. Path API による比較 | 3 | 文字列リテラル比較をやめ `java.nio.file.Path` 同士で比較する |
| B. 常時失敗スクリプトの生成 | 2 | `/usr/bin/false` を、プラットフォーム別の一時スクリプトに置き換える |
| C. `@DisabledOnOs` でスキップ | 3 | POSIX の実行ビット意味論そのものを検証するテストを Windows で除外する |

手法 B を優先し、安易にスキップへ倒さないことで Windows 側のカバレッジを可能な限り維持する。

## 手法 A: Path API による比較（3 件）

### A-1. `RescriptCliDetectorTest`（2 件）

`findCli` の戻り値は `Path.toString()` 由来で、Windows では `\` 区切りになる。現行の部分文字列一致はスラッシュを前提としている。

```kotlin
// Before
assertTrue(result!!.contains("node_modules/.bin/rescript"))

// After
assertTrue(Path.of(result!!).endsWith(Path.of("node_modules/.bin/rescript")))
```

`Path.endsWith(Path)` は**パス要素単位**の後方一致であり、区切り文字表現に依存しない。`Path.of("node_modules/.bin/rescript")` は Windows でも `[node_modules, .bin, rescript]` の 3 要素に分解されるため、両プラットフォームで成立する。

### A-2. `RescriptReanalyzeServerServiceTest`（1 件）

`getSocketPath` は `Path` を返しており、実装は正しい。テストが `toString()` を POSIX 形式のリテラルと比較している点だけが問題。

単に `Path` 同士の `assertEquals` に置き換えると、本体の式をそのまま書き写す**トートロジー**になり検証価値が下がる。そのため意味のある 2 点に分解して検証する。

```kotlin
// Before
assertEquals("/project/root/.rescript-reanalyze.sock", socketPath.toString())

// After
assertEquals(".rescript-reanalyze.sock", socketPath.fileName.toString())
assertEquals(Path.of("/project/root"), socketPath.parent)
```

同クラスの `getSocketPath handles trailing slash` は現状 Windows でも成功しているため変更しない。実装時に念のため再確認する。

## 手法 B: 常時失敗スクリプトの生成（2 件）

### 前提の実測

`runFormatCheck` は `GeneralCommandLine(cliPath, "format", "--stdin", ".$extension")` を組み立てるため、cliPath には「任意の引数を無視して非ゼロ終了する単体の実行可能ファイル」が必要となる。Windows には `false` 相当の標準コマンドが存在しない。

**スパイクで実測済み**（使い捨てテストを作成・実行後に削除）:

- Windows で `.bat`（`@echo off` / `exit /b 1`）を生成し `GeneralCommandLine` に渡す
- 起動に成功し、`runFormatCheck` は期待どおり `null` を返した

よってスキップに倒す必要はなく、両プラットフォームでテストを維持できる。

### 実装

`RescriptFormatCheckAnnotatorTest` に private helper を追加する。

```kotlin
/**
 * Creates a script that ignores its arguments and always exits non-zero.
 * Windows has no `/usr/bin/false` equivalent, so a .bat is generated instead.
 */
private fun createAlwaysFailingCommand(dir: Path): Path =
    if (SystemInfo.isWindows) {
        dir.resolve("alwaysfail.bat").also {
            Files.writeString(it, "@echo off\r\nexit /b 1\r\n")
        }
    } else {
        dir.resolve("alwaysfail.sh").also {
            Files.writeString(it, "#!/bin/sh\nexit 1\n")
            it.toFile().setExecutable(true)
        }
    }
```

- 一時ディレクトリは JUnit 5 の `@TempDir` を使い、後始末をフレームワークに任せる
- プラットフォーム判定は IntelliJ Platform の `com.intellij.openapi.util.SystemInfo.isWindows` を使う（`System.getProperty("os.name")` の直接参照より意図が明確）
- 該当 2 テストの `cliPath = "/usr/bin/false"` をこの helper の戻り値に差し替える

## 手法 C: `@DisabledOnOs` でスキップ（3 件）

Windows には実行ビットが存在せず `File.canExecute()` は読取可能な既存ファイルに対して常に true を返す。以下 3 件は「実行ビットが落ちている」という **POSIX のパーミッション意味論そのもの**を検証対象としており、Windows 上では前提条件を作れない。

| テスト | クラス |
|-------|-------|
| `isValidExecutable returns false for non-executable file` | `util.RescriptSecurityUtilsTest` |
| `validateNodePath throws when file is not executable` | `settings.RescriptSettingsValidatorTest` |
| `validateLspPath throws when non-js file is not executable` | `settings.RescriptSettingsValidatorTest` |

```kotlin
@Test
@DisabledOnOs(
    value = [OS.WINDOWS],
    disabledReason = "Windows has no execute bit; File.canExecute() is always true for readable files.",
)
fun `isValidExecutable returns false for non-executable file`() { ... }
```

- `org.junit.jupiter.api.condition.DisabledOnOs` / `OS` を使用（junit-jupiter 6.1.2 に同梱）
- **削除ではなくスキップ**とすることで、CI (Linux) では従来どおり実行されカバレッジが低下しない
- `disabledReason` に理由を明記し、将来「なぜ落としたのか」を追跡できるようにする

本体側の実装（`canExecute()` による判定）は Windows でも妥当であるため変更しない。検証メッセージが "chmod +x on Unix" と Unix 前提を明示していることもその裏付けとなる。

## 影響範囲

| 項目 | 内容 |
|------|------|
| 変更ファイル | テスト 4 ファイル（`RescriptCliDetectorTest` / `RescriptReanalyzeServerServiceTest` / `RescriptFormatCheckAnnotatorTest` / `RescriptSecurityUtilsTest` / `RescriptSettingsValidatorTest` の 5 クラス、うち Settings は 1 ファイル） |
| `src/main/` | 変更なし |
| 新規クラス | なし |
| Extension Point | 変更なし |
| カバレッジ | 手法 C の 3 件が Windows でのみスキップ。Kover は CI (Linux) で計測するため計測値は不変 |

## 検証方法

1. Windows ローカルで `./gradlew test` を実行し、**失敗 0 件**を確認する
2. 手法 C の 3 件が Windows で `skipped` として集計されることを確認する（silently pass ではない）
3. `./gradlew ktlintCheck` が通ることを確認する
4. CI (Linux) で全件実行され green であることを確認する（スキップ 0 件であること）

## 却下した代替案

| 案 | 却下理由 |
|----|---------|
| 失敗 8 件すべてを `@DisabledOnOs(WINDOWS)` でスキップ | 手法 A / B で両プラットフォーム実行が可能であり、カバレッジを不必要に落とす |
| Windows で `findstr` / `where` を常時失敗コマンドとして使う | 引数次第で終了コードが変わり決定的でない。`format` という名前の実行ファイルが PATH に存在する可能性もある |
| `RescriptCliDetector` に `.cmd` / `.exe` 探索を追加 | 不具合の根拠が確認できていない。requirements の非目標に記載済み |
| 本体の `getSocketPath` を文字列返却に変更 | `Path` を返す現行実装のほうが正しい。テストの都合で本体の型を劣化させない |
