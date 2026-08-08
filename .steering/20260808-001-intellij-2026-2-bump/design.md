# design: IntelliJ Platform 2026.2 へのバンプ

## 上流調査で判明した事実

requirements 作成後に JetBrains 公式ドキュメントを確認し、以下を確定させた。
本 design はこれらを前提とする。

### F-1: LSP API は「rename」であり、新 API は 2026.1.4 から入っている

`plugin-verifier-ignored-problems.txt` は 2026.2 EAP 時点の観測をもとに
「`LspClientDescriptor` / `LspClientSupportProvider` へ移行」と記していたが、**クラス名の一部が誤っている**。
公式 SDK ドキュメントによる正しい対応は下表のとおり。

| 旧 (deprecated) | 新 |
|---|---|
| `LspServerSupportProvider` | **`LspIntegrationProvider`**（`LspClientSupportProvider` ではない） |
| `LspServerDescriptor` | `LspClientDescriptor` |
| `ProjectWideLspServerDescriptor` | `ProjectWideLspClientDescriptor` |
| `LspServer` | `LspClient` |
| `LspServerManager` | `LspClientManager` |
| EP `com.intellij.platform.lsp.serverSupportProvider` | EP `com.intellij.platform.lsp.integrationProvider` |

新名称は **2026.1.4 (261.x) 安定版から利用可能**で、旧名称が deprecated になったのが 2026.2。
つまり本移行に必要な最低ラインは 262 ではなく 261.x である（後述 D-2 で扱う）。

### F-2: `LspServerManager` はサービス登録が新名称のみに移行しており、runtime で壊れる

2026.* Incompatible Changes に以下の記載がある:

> `LspServerManager` was renamed to `LspClientManager`. The service now registers exclusively under the
> new interface name. Code retrieving `LspServerManager` via `project.service<LspServerManager>()`
> will return null. Use `LspClientManager.getInstance(project)` instead.

**これは deprecation ではなく破壊的変更である。** 本プラグインは `LspServerManager` を 4 箇所で使用しており
（`RescriptLspUtils.kt:47`, `RescriptDumpLspStateAction.kt:61`, `RescriptRestartLspAction.kt:22`,
`RescriptLspInstaller.kt:133`）、いずれも `LspServerManager.getInstance(project)` 経由。
この静的ファクトリが互換シムとして残っているか、内部で `service<LspServerManager>()` を呼んで null を返すかは
**ドキュメントからは判別できない**。後者ならコンパイルは通るのに LSP が無言で停止する。

→ 結論: **R-3（LSP API 移行）は「やれたらやる」ではなく必須**。かつ AC-7 の実機スモークテストは省略不可。

### F-3: 2026.2 のその他の破壊的変更のうち、本プラグインへの影響

| 変更 | 影響 |
|---|---|
| Kotlin プラグイン K1 の完全削除（K2 Analysis API へ） | **影響なし** — 本プラグインは Kotlin PSI を解析しない |
| PolySymbols の `PsiSourced*` → `PsiLinked*` 一括 rename | **影響なし** — `grep` で参照 0 件（実装タスクで再確認する） |
| CLion Classic (`com.intellij.cidr.lang`) の非バンドル化 | **影響なし** — 依存していない |
| `VirtualFile` の非同期保存対応 | 影響なし（オプトイン） |

## 設計上の決定

### D-1: セクションを 4 段に分け、セクション 1 単独で緑にする

F-2 のとおり、LSP 移行は API 形状の実測なしには確定できない。したがって:

1. **セクション 1**: `platformVersion` のみ更新し、既存の `@Suppress` を一切触らずビルド・テスト・Verifier を緑にする。
   ここで得られる `deprecated-usages.txt` と実際のコンパイルエラーが、以降の設計入力になる
2. **セクション 2**: LSP API 移行（必須）
3. **セクション 3**: `FloatingToolbarProvider` / `FileIncludeProvider` 移行（代替 API の存在を確認できた場合のみ）
4. **セクション 4**: `pluginSinceBuild` 引き上げ + ignored-problems 棚卸し + ドキュメント同期

各セクションは独立にビルド・テストを通過し、単独で `main` にマージできる粒度とする
（`steering-workflow.md`「tasklist のチェックポイント分割」）。

`pluginSinceBuild` の引き上げをセクション 4 に置くのは、セクション 1〜3 の間は 253.0 のままにしておくことで
Verifier が 2025.3 / 2026.1 も検証対象に含め、**「うっかり新 API を使った」箇所を検出できる**ため。
移行が終わってから制約を外す順序にする。

### D-2: `pluginSinceBuild` は `261.26222`（= 2026.1.4）とする

F-1 のとおり新 LSP API は 2026.1.4 から使えるため、下限を 262 にする必要はない。
ユーザー判断により下限は 2026.1.4 とし、2026.1 系ユーザーを切り捨てない。

**値は `261.26222` でなければならない。** JetBrains のリリースフィード（`code=IIU`）で確認した build 番号:

| バージョン | build |
|---|---|
| 2026.1.3 | `261.25134.95` |
| 2026.1.4 | `261.26222.65` |

`261.4` と書くと「branch 261 の build 4 以上」の意味になり、新 API を持たない 2026.1〜2026.1.3 まで
通してしまう。`261.26222` とすることで 2026.1.4 以降のみを許可できる。

この設定は **「262 でコンパイルし、261.26222 以降で動く」** という構成になる。
2026.2 でのみ追加された API を誤って使うと 2026.1.4 の利用者で `NoSuchMethodError` になるため、
Verifier が 2026.1.4 を検証対象に含むことをセクション 4 で必ず確認する（requirements のリスク表参照）。

### D-3: LSP 移行は「import と型名の置換」を基本とし、意味論の変更を持ち込まない

rename ベースの移行のため、ロジックには手を入れない。変更対象は以下に限定する:

- `import` 行のクラス名
- 型注釈・型引数・`is` / `as` の型名
- `override` するメンバのシグネチャ（親の変更に追従する範囲）
- `plugin.xml:641` の EP 名と、実装クラス名の変更に伴う `implementation` 属性
- `@Suppress("DEPRECATION")` / `@Suppress("OVERRIDE_DEPRECATION")` の除去
- KDoc / インラインコメント内の旧クラス名への言及

**リネームしないもの**: 自前クラス名（`RescriptLspServerSupportProvider` 等）。
プラグイン内部の命名変更は本作業のスコープ外とし、混乱を避けるため別作業に送る。
ただし `plugin.xml` の EP 登録は新 EP 名に変える必要があるため、そこだけは必ず追従する。

### D-4: 対応が未確定な API は実測で決める

以下は公式ドキュメントに記載がなく、対応先が不明:

| 現在の使用 | 使用箇所 | 対応先 |
|---|---|---|
| `LspServerNotificationsHandler` | `RescriptLsp4jClient.kt:34,82,83`, `RescriptLspServerDescriptor.kt:41` | 未確定 |
| `Lsp4jClient` | `RescriptLsp4jClient.kt:36`, `RescriptLspServerDescriptor.kt:41` | 未確定（変更なしの可能性が高い） |
| `LspServerState` | `RescriptCodeVisionProvider.java:99` | 未確定 |
| `LspCustomization` / `LspSemanticTokensSupport` | `customization` パッケージ | 未確定（変更なしの可能性が高い） |

確認手順（セクション 1 完了直後に実施）:

```bash
# Gradle が解決した 2026.2.0.1 の LSP API jar を特定して中身を一覧する
find ~/.gradle/caches/modules-2/files-2.1/com.jetbrains.intellij.idea -name "*.jar" \
  | xargs -I{} sh -c 'unzip -l "{}" 2>/dev/null | grep -q "platform/lsp/api" && echo "{}"' \
  | head -1
# 得られた jar に対して
unzip -l <jar> | grep "platform/lsp/api" | sed 's/.*platform/platform/'
```

`javap -classpath <jar> com.intellij.platform.lsp.api.LspClientManager` でシグネチャまで確認する。
これで判明しない場合は、`runIde` サンドボックスの IDE 上で Go to Declaration を使う。

### D-5: `plugin.xml` の EP 変更は互換性を持たせない

旧 EP `platform.lsp.serverSupportProvider` と新 EP `platform.lsp.integrationProvider` を
両方登録すると LSP サーバーが二重起動する恐れがある。`sinceBuild` を上げる以上、
**新 EP のみを登録する**（セクション 2 で切り替え）。

## 変更対象ファイル

### セクション 1

| ファイル | 変更 |
|---|---|
| `gradle.properties` | `platformVersion = 2026.2.0.1` |

### セクション 2（LSP 移行）

| ファイル | 変更概要 |
|---|---|
| `src/main/kotlin/.../lsp/RescriptLspServerSupportProvider.kt` | 親を `LspIntegrationProvider` へ |
| `src/main/kotlin/.../lsp/RescriptLspServerDescriptor.kt` | 親を `ProjectWideLspClientDescriptor` へ |
| `src/main/kotlin/.../lsp/RescriptLsp4jClient.kt` | 通知ハンドラ型を D-4 の実測結果に合わせる |
| `src/main/kotlin/.../lsp/RescriptLspUtils.kt` | `LspServerManager` → `LspClientManager`、`LspServer` → `LspClient` |
| `src/main/kotlin/.../lsp/RescriptLspInstaller.kt` | 同上 |
| `src/main/kotlin/.../lsp/RescriptRestartLspAction.kt` | 同上 |
| `src/main/kotlin/.../lsp/RescriptDumpLspStateAction.kt` | 同上 |
| `src/main/kotlin/.../lsp/RescriptExpressionTypeProvider.kt` | 同上 |
| `src/main/java/.../codevision/RescriptCodeVisionProvider.java` | `LspServer` / `LspServerState` |
| `src/main/kotlin/.../inspections/RescriptSignatureSyncInspection.kt` | 同上 |
| `src/main/kotlin/.../actions/RescriptOpenCompiledJsAction.kt` | 同上 |
| `src/main/kotlin/.../actions/RescriptCreateInterfaceAction.kt` | 同上 |
| `src/main/kotlin/.../refactoring/RescriptRenameHandler.kt` | 同上 |
| `src/main/kotlin/.../settings/RescriptConfigurable.kt` | 同上 |
| `src/main/resources/META-INF/plugin.xml` | L641 の EP を `platform.lsp.integrationProvider` へ |

> 上記の後半 6 ファイルのパスは `plugin-verifier-ignored-problems.txt:60-65` の記載に基づく想定値。
> 実装時に `grep` で実パスを確定する。

### セクション 3

| ファイル | 変更 |
|---|---|
| `src/main/kotlin/.../editor/RescriptFloatingToolbarProvider.kt` | `isApplicable` → `isApplicableAsync`（存在すれば） |
| `src/main/kotlin/.../navigation/RescriptFileIncludeProvider.kt` | `acceptFile(VirtualFile)` → `acceptFile(IndexedFile)`（存在すれば） |

### セクション 4

`gradle.properties`（`pluginSinceBuild`）、`plugin-verifier-ignored-problems.txt`、
requirements.md AC-8 に列挙した各ドキュメントと対応する `.po`。

## テスト方針

`.claude/rules/testing.md` に従う。LSP 関連クラスのうち **`LspServerDescriptor` / `LspServerSupportProvider` /
`Lsp4jClient` の実装は免除対象**（「LSP サーバー結合必須」）だが、以下は既存テストがあるため
**移行後も緑であることを確認し、必要なら型名の変更に追従させる**:

| 既存テスト | 対応セクション |
|---|---|
| `lsp/RescriptLspUtilsTest.kt` | 2 |
| `lsp/RescriptRestartLspActionTest.kt` | 2 |
| `lsp/RescriptLspInstallerTest.kt` | 2 |
| `lsp/RescriptExpressionTypeProviderTest.kt` | 2 |
| `lsp/RescriptSemanticTokensSupportTest.kt` | 2 |
| `lsp/RescriptCompilationStatusServiceTest.kt` | 2 |
| `editor/RescriptFloatingToolbarProviderTest.kt` | 3 |
| `navigation/RescriptFileIncludeProviderTest.kt` | 3 |

新規テストは原則不要（本作業は API rename への追従であり、新しいロジックを持ち込まない）。
ただしセクション 2 で `LspClientManager` の取得口が変わるため、`RescriptLspUtilsTest.kt` に
**取得結果が null でないことを検証するリグレッションテスト**を追加する（F-2 の再発防止）。
テスト環境で LSP が起動しない場合は、取得口の呼び出しが例外を投げないことの確認に縮退させ、
その理由を tasklist に明記する。

## 検証手順

各セクション完了時:

```bash
./gradlew ktlintCheck > /tmp/ktlint.log 2>&1; tail -20 /tmp/ktlint.log
./gradlew clean buildPlugin > /tmp/build.log 2>&1; tail -30 /tmp/build.log
./gradlew test > /tmp/test.log 2>&1; tail -40 /tmp/test.log
```

セクション 2 完了時および全セクション完了時に追加で:

```bash
./gradlew verifyPlugin > /tmp/verify.log 2>&1; tail -40 /tmp/verify.log
# 新規 deprecated 利用の確認
cat build/reports/pluginVerifier/*/plugins/com.rescript.plugin/*/deprecated-usages.txt
```

AC-7 の実機確認は `ui-smoke-test` スキルを用い、補完・診断・定義ジャンプ・ホバーの 4 機能について
LSP が実際に応答することを確認する。F-2 の性質上、**この確認を省略すると回帰を検出できない**。

## ロールバック

各セクションが独立コミットのため、問題が出たセクションのみ `git revert` できる。
セクション 1 で 2026.2.0.1 のビルド自体が通らない場合は、`2026.1.4`（261 系最終パッチ）への
縮退を検討する。この場合も F-1 より LSP 新 API は利用可能なため、セクション 2〜3 は実施できる。
