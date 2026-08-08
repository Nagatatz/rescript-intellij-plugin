# design: LSP API の LspClient 系への移行

## 1. 実測で確定した事実

推測を排するため、着手前に `javap` と EP 宣言の実測を行った。以下はその結果であり、
本設計の前提となる。

### 1.1 調査に使用した成果物

Gradle は IDE を artifact transform で展開する。実パスは以下:

| ビルド | マーケティング版 | 展開先 |
|---|---|---|
| `261.27258.27` | **2026.1.5 RC**（未 GA） | `~/.gradle/caches/9.6.1/transforms/875baeac.../transformed/idea-261.27258.27-win` |
| `262.9437.65` | 2026.2.1 RC | `~/.gradle/caches/9.6.1/transforms/293ebd65.../transformed/idea-262.9437.65-win` |
| `2026.2.0.1` | 2026.2.0.1（ビルド対象） | `~/.gradle/caches/9.6.1/transforms/655d4ffc.../transformed/idea-2026.2.0.1-win` |

LSP API は `lib/intellij.platform.lsp.jar`、EP 宣言は `lib/intellij.platform.lsp.impl.jar`
内の `intellij.platform.lsp.impl.xml` にある。`javap` は **JDK 25**
(`~/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin/javap.exe`) を使う。
JDK 21 の javap は 2026.2 のクラスファイル（69.0）を読めない。

### 1.2 新 API のシグネチャ（2026.2.0.1 と 261.27258.27 で完全一致）

```
interface LspIntegrationProvider {
    void fileOpened(Project, VirtualFile, LspIntegrationProvider$LspClientStarter)
    default List<LanguageServiceWidgetItem> createWidgetItems(Project, VirtualFile)
    default LspClientWidgetItem createWidgetItem(LspClient, VirtualFile)
}
interface LspIntegrationProvider$LspClientStarter {
    void ensureClientStarted(LspClientDescriptor)
}
interface LspClientManager {
    Collection<LspClient> getClients(Class<? extends LspIntegrationProvider>)
    void startClientsIfNeeded(Class<? extends LspIntegrationProvider>)
    void ensureClientStarted(Class<? extends LspIntegrationProvider>, LspClientDescriptor)
    void stopClients(Class<? extends LspIntegrationProvider>)
    void stopAndRestartClientsIfNeeded(Class<? extends LspIntegrationProvider>)
    void addListener(LspClientManagerListener, Disposable, boolean)
    static LspClientManager getInstance(Project)
}
interface LspClient {
    Class<? extends LspIntegrationProvider> getProviderClass()
    Project getProject()
    LspClientDescriptor getDescriptor()
    LspServerState getState()                      // ← 型名は据え置き
    InitializeResult getInitializeResult()
    void sendNotification(Function1<LanguageServer, Unit>)
    <R> Object sendRequest(Function1<...>, Continuation<R>)
    <R> R sendRequestSync(int, Function1<LanguageServer, CompletableFuture<R>>)
    TextDocumentIdentifier getDocumentIdentifier(VirtualFile)
    int getDocumentVersion(Document)
}
abstract class ProjectWideLspClientDescriptor extends LspClientDescriptor {
    ProjectWideLspClientDescriptor(Project, String)
}
abstract class LspClientDescriptor {
    protected LspClientDescriptor(Project, String, VirtualFile...)
    abstract boolean isSupportedFile(VirtualFile)
    GeneralCommandLine createCommandLine()
    Object createInitializationOptions()
    Lsp4jClient createLsp4jClient(LspServerNotificationsHandler)   // ← 引数型は据え置き
    Class<? extends LanguageServer> getLsp4jServerClass()
    LspCustomization getLspCustomization()
    String getFileUri(VirtualFile)
    ...
}
```

**移行に効く要点:**

- `sendRequestSync(int, Function1)` は新インタフェース `LspClient` 側にあり **deprecated ではない**。
  現行コードが明示している 10 秒タイムアウトはそのまま使える（`sendRequestSync$default` の
  合成メソッドを避けるための既存コメントは移行後も有効なので残す）
- `LspServerState` / `Lsp4jClient` / `LspServerNotificationsHandler` / `customization/*` は
  **名称変更なし**。`LspClient.getState()` の戻り値も `LspServerState` のまま
- `LspClientDescriptor` の override 対象メンバ名は旧 `LspServerDescriptor` と同じ。
  本プラグインが override している 5 メンバ（`lsp4jServerClass` / `createLsp4jClient` /
  `lspCustomization` / `createInitializationOptions` / `isSupportedFile` / `createCommandLine`）は
  すべて名前・シグネチャとも不変

### 1.3 旧 API が強制する「一括移行」

旧 `LspServerManager` のシグネチャは以下であり、**引数の型が旧 provider 型に固定されている**:

```
interface LspServerManager extends LspClientManager {
    Collection<LspServer> getServersForProvider(Class<? extends LspServerSupportProvider>)
    void stopAndRestartIfNeeded(Class<? extends LspServerSupportProvider>)
    ...
}
```

したがって `RescriptLspServerSupportProvider` の親を `LspIntegrationProvider` に変えた瞬間、
旧メソッドには渡せなくなる。**provider とその呼び出し側は同一コミットで移行する必要がある**
（D-1 参照）。

### 1.4 EP は新旧が併存している

`intellij.platform.lsp.impl.xml` は **261.27258.27 / 2026.2.0.1 の両方で** 以下を宣言している:

```xml
<extensionPoint qualifiedName="com.intellij.platform.lsp.integrationProvider"
                interface="com.intellij.platform.lsp.api.LspIntegrationProvider" dynamic="true"/>
<extensionPoint qualifiedName="com.intellij.platform.lsp.serverSupportProvider"
                interface="com.intellij.platform.lsp.api.LspServerSupportProvider" dynamic="true"/>
```

新 EP は 261 系にも存在するため、`pluginSinceBuild = 261.26222` を据え置いたまま
新 EP へ切り替えられる。

### 1.5 `@Suppress("UnstableApiUsage")` の要否

`LspClientManager` / `LspIntegrationProvider` / `LspClientDescriptor` / `LspClient` の
**クラスレベルに `@ApiStatus.Experimental` / `@ApiStatus.Internal` は付いていない**
（`javap -v` の RuntimeVisibleAnnotations を確認。`LspClientManager` 内で
`ApiStatus$Internal` が現れるのは `addLsp4jServerWrapper` メンバのみで、本プラグインは未使用）。

よって `@Suppress("UnstableApiUsage")` も併せて除去する。

## 2. 設計判断

### D-1: provider / descriptor / 呼び出し側を 1 コミットで移行する

前作業の tasklist は「2-1 コア」「2-2 呼び出し側」の 2 コミットに分けていたが、
**1.3 の理由でこの分割は成立しない**。`RescriptLspIntegrationProvider` が
`LspIntegrationProvider` を実装した時点で `getServersForProvider(...)` の呼び出しが
型エラーになるため、2-1 だけではビルドが緑にならない。

→ API 移行は **1 コミット**にまとめる。代わりに「自前クラス名のリネーム」を別コミットに切り出し、
差分の見通しを確保する（R-4 の緩和）。

### D-2: 自前クラスのリネームは API 移行の後に単独コミットで行う

| 旧 | 新 |
|---|---|
| `RescriptLspServerSupportProvider` | `RescriptLspIntegrationProvider` |
| `RescriptLspServerDescriptor` | `RescriptLspClientDescriptor` |
| `RescriptLspUtils.getServer(project): LspServer?` | `RescriptLspUtils.getClient(project): LspClient?` |

リネームしないもの:

- `RescriptLanguageServer` — lsp4j の `LanguageServer` 実装。プラットフォーム API とは無関係
- `RescriptLsp4jClient` — プラットフォーム側の型名 `Lsp4jClient` が不変
- `RescriptReanalyzeServerService` / `RescriptReanalyzeServerStartupActivity` —
  ReScript の analysis server を指す名前
- `RescriptRestartLspAction` / `RescriptDumpLspStateAction` — ユーザー向けアクション名
  （"Restart LSP Server" / "Dump LSP State"）は LSP プロトコル上の server を指すため据え置く

**ローカル変数名** (`server`, `lspServer`) も `client` / `lspClient` へ揃える。
ただしユーザーに見える文字列（通知本文の `**LSP Server**` 等）は変更しない。

### D-3: `plugin.xml` は新 EP のみを登録する（旧 EP と併記しない）

前作業の D-5 を踏襲する。両方登録すると同一 descriptor で LSP が二重起動する恐れがある。

```xml
<!-- before -->
<platform.lsp.serverSupportProvider
        implementation="com.rescript.plugin.lsp.RescriptLspServerSupportProvider"/>
<!-- after (API 移行コミット時点) -->
<platform.lsp.integrationProvider
        implementation="com.rescript.plugin.lsp.RescriptLspServerSupportProvider"/>
<!-- after (リネームコミット時点) -->
<platform.lsp.integrationProvider
        implementation="com.rescript.plugin.lsp.RescriptLspIntegrationProvider"/>
```

### D-4: 意味論は一切変更しない

タイムアウト値・スレッドモデル・エラーハンドリング・`try`/`catch` の範囲は現行のまま。
変更対象は import 行、型名、メソッド名、`@Suppress`、KDoc / インラインコメントに限る。

既存の「Explicit 10s timeout」コメントは移行後も意味を持つ（`sendRequestSync$default` は
新 `LspClient` にも合成される）ため、文言を新クラス名に合わせて残す。

一方、各ファイル冒頭の
「the replacement LspClientDescriptor API does not exist on the 2026.1.2 compile target」
というコメントは **事実として誤り**（1.2 で存在を確認済み）かつ移行で不要になるため削除する。

### D-5: Verifier の検証対象に下限ビルドを明示追加する

`recommended()` が拾う 261 系は `261.27258.27` = **2026.1.5 RC** であり、
`pluginSinceBuild = 261.26222`（2026.1.4 GA = `261.26222.65`）そのものを検証していなかった。

```kotlin
pluginVerification {
    ides {
        // recommended() は 261 系として 2026.1.5 RC (261.27258.27) を選ぶため、
        // sinceBuild の下限 261.26222 (2026.1.4 GA) 自体は検証されない。
        // 下限で LSP API と EP が揃っていることを直接検証するために明示する。
        ide(IntelliJPlatformType.IntellijIdeaUltimate, "261.26222.65")
        recommended()
    }
    ...
}
```

`pluginSinceBuild` は **据え置く**（2026-08-09 のユーザー判断）。対応 IDE を狭めない。

### D-6: `RescriptCodeVisionProvider.java` は Java のまま移行する

Kotlin 化はスコープ外。`import` の `LspServer` → `LspClient` 置換と
`@SuppressWarnings("deprecation")` の除去に留める。`LspServerState` は import 据え置き
（`LspClient.getState()` の戻り値型が `LspServerState` のため）。

## 3. 変更対象ファイル

コミット割りは以下の 4 本とする。3.1 のテストは `.claude/rules/testing.md` に従い
実装と同一コミットに含める。

| # | 絵文字 | 内容 |
|---|---|---|
| 1 | 🔧 | Verifier の検証対象に下限ビルド `IU-261.26222.65` を追加（3.0） |
| 2 | ♻️ | API 移行 + テスト + ignored-problems 削除（3.1 / 3.3） |
| 3 | ♻️ | 自前クラス名のリネーム（3.2） |
| 4 | 📝 | ドキュメント同期（3.4） |

### 3.0 Verifier 設定（コミット 1）

`build.gradle.kts` の `pluginVerification.ides { }` に D-5 の `ide(...)` を追加する。
API 移行より **先に** 入れることで、移行後の `verifyPlugin` が最初から下限ビルドを含む状態で回る。

### 3.1 API 移行（コミット 2）

| ファイル | 変更内容 |
|---|---|
| `lsp/RescriptLspServerSupportProvider.kt` | 親を `LspIntegrationProvider` へ。`fileOpened` の第 3 引数を `LspIntegrationProvider.LspClientStarter` へ。`ensureServerStarted` → `ensureClientStarted` |
| `lsp/RescriptLspServerDescriptor.kt` | 親を `ProjectWideLspClientDescriptor` へ。override メンバは不変 |
| `lsp/RescriptLspUtils.kt` | `LspServerManager` → `LspClientManager`、`getServersForProvider` → `getClients`、戻り値 `LspServer?` → `LspClient?` |
| `lsp/RescriptRestartLspAction.kt` | `LspClientManager` + `stopAndRestartClientsIfNeeded` |
| `lsp/RescriptDumpLspStateAction.kt` | 同上 + `getClients` |
| `lsp/RescriptLspInstaller.kt` | 同上（`stopAndRestartIfNeeded` の呼び出し 1 箇所） |
| `lsp/RescriptExpressionTypeProvider.kt` | `@Suppress("DEPRECATION")` 除去 |
| `inspection/RescriptSignatureSyncInspection.kt` | 同上 |
| `navigation/RescriptOpenCompiledJsAction.kt` | 同上 |
| `navigation/RescriptCreateInterfaceAction.kt` | 同上 + ヘルパの引数型 `LspServer` → `LspClient` |
| `refactor/RescriptRenameHandler.kt` | 同上（import + ヘルパ引数型） |
| `settings/RescriptConfigurable.kt` | `LspServerManager` → `LspClientManager` + メソッド名 |
| `codevision/RescriptCodeVisionProvider.java` | D-6 のとおり |
| `plugin.xml` | EP を `platform.lsp.integrationProvider` へ |
| `plugin-verifier-ignored-problems.txt` | `com.intellij.platform.lsp.api.*` エントリ削除 |

### 3.2 リネーム（コミット 3）

| 対象 | 内容 |
|---|---|
| `lsp/RescriptLspServerSupportProvider.kt` | ファイル名・クラス名を `RescriptLspIntegrationProvider` へ |
| `lsp/RescriptLspServerDescriptor.kt` | ファイル名・クラス名を `RescriptLspClientDescriptor` へ |
| `lsp/RescriptLspUtils.kt` | `getServer` → `getClient` |
| 参照 11 ファイル | `documentation/RescriptDocumentationProvider.kt`、`lsp/RescriptLspDetector.kt`、`util/RescriptProcessUtils.kt`（いずれも KDoc の `@see` のみ）ほか呼び出し側 |
| `intention/RescriptAddMissingSwitchArmsIntention.kt`、`intention/RescriptBatchInsertInferredTypesIntention.kt`、`typeinfo/RescriptTypeInfoPanel.kt` | `getServer` → `getClient` |
| `plugin.xml` | `implementation` 属性のクラス名 |
| テスト | 参照追随。テストクラス名も対応してリネーム |

### 3.3 テスト（コミット 2 に同梱）

移行に伴う新規テストは以下の 2 点に絞る。AC-7 を省略したため、
**テスト可能な範囲を可能な限り押さえる**位置づけになる。

| テスト | 目的 |
|---|---|
| `lsp/RescriptLspUtilsTest.kt` | `getServer`（コミット 3 で `getClient`）が LSP 未起動環境で例外を投げず `null` を返すこと。`LspClientManager.getInstance` の取得口が生きていることの回帰検知 |
| `lsp/RescriptLspServerSupportProviderTest.kt` | `fileOpened` が `.res` / `.resi` でのみ `ensureClientStarted` を呼び、他拡張子では呼ばないこと（`LspClientStarter` をモックして検証）。EP 起動経路のうちユニットテストで押さえられる唯一の部分 |

テストファイル名はコミット 2 時点では移行前のクラス名に合わせ、コミット 3 のリネームで
`RescriptLspIntegrationProviderTest.kt` へ追随させる（`.claude/rules/testing.md` の命名規則）。

既存テスト（`RescriptRestartLspActionTest` / `RescriptLspInstallerTest` /
`RescriptExpressionTypeProviderTest` / `RescriptSemanticTokensSupportTest` /
`RescriptCompilationStatusServiceTest`）は参照追随のみで緑を維持する。

### 3.4 ドキュメント（コミット 4）

更新する（現行実装の説明）:

- `docs/repository-structure.md` / `docs/functional-design.md`（EP マップを含む）
- `docs/glossary.md` / `docs/lsp-fallback-matrix.md`
- `sphinx-docs/dev/architecture.md` / `sphinx-docs/dev/project-structure.md`
- 上記に対応する `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po`（`make gettext && make update-po`）

更新しない（当時の記録・沿革）:

- `docs/archive/implemented-features.md`
- `docs/performance-validation.md`
- `docs/ideas/concept.md`

## 4. 検証方針

1. `./gradlew ktlintCheck clean buildPlugin` — コンパイルと lint
2. `./gradlew test` — 4464 件が緑（現行基準）
3. `./gradlew verifyPlugin` — `IU-261.26222.65` / `IU-261.27258.27` / `IU-262.9437.65` すべて `Compatible`、
   かつ `deprecated-usages.txt` の LSP 関連が 0 件
4. ~~実機スモークテスト（AC-7）~~ — **省略する（2026-08-09 のユーザー判断）**。
   requirements.md「AC-7 省略の経緯と残存リスク」を参照。
   代替として 3 の検証対象に下限ビルドを追加し、`plugin.xml` の `implementation` 属性と
   クラス完全修飾名の一致を `grep` で機械照合する

## 5. 残存リスクと未確認事項

- `261.26222.65` の Verifier 実行は初回に約 1.6GB のダウンロードを伴う。
  CI での `verifyPlugin` 実行時間が延びる（ローカルで所要時間を計測し tasklist に記録する）
- `LspClientManager.getInstance(project)` が実行時に有効なインスタンスを返すことは
  bytecode 上の静的メソッド存在で確認済みだが、**サービス登録の実体は実機でしか確認できない**。
  `Restart LSP Server` / `Dump LSP State` がこの直撃箇所にあたる。
  **AC-7 を省略したため、本作業では未検証のまま残る**
- `LspIntegrationProvider.createWidgetItems` / `createWidgetItem` は本プラグインが override して
  いないため移行の影響を受けないが、LSP ウィジェット表示が EP 切り替えで変化しないことは
  **未検証のまま残る**（AC-7 省略のため）
- 上記 2 件および requirements.md の残存リスク表は、**Marketplace リリース前に別途
  実機確認する機会を設けることを推奨する**。本作業では扱わない
