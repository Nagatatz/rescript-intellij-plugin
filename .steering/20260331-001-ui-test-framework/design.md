# Design: UI テストフレームワーク導入 & スクリーンショット自動撮影

## 技術選定

| 項目 | 選定 | 理由 |
|------|------|------|
| UI テストフレームワーク | IntelliJ Remote-Robot 0.11.23 | JetBrains 公式、IntelliJ Platform に最適化 |
| テストフレームワーク | JUnit 6 (Jupiter 6.0.3) | 既存ユニットテストと統一 |
| Gradle 統合 | intellijPlatformTesting DSL | IntelliJ Platform Gradle Plugin v2 の公式手法 |
| IDE 起動 | `runIdeForUiTests` タスク | robot-server-plugin を自動ロード |

## アーキテクチャ

### 実行フロー

```
./gradlew runIdeForUiTests  (1) IDE 起動（robot-server-plugin 同梱、ポート 8082）
                                ↓
./gradlew uiTest            (2) テスト実行（別ターミナル）
                                ↓
RemoteRobot → HTTP:8082     (3) IDE を操作、スクリーンショット撮影
                                ↓
build/screenshots/*.png     (4) 成果物出力
```

> **2 ステップ実行**: IDE の起動とテスト実行は別プロセスで行う。IDE の起動・プロジェクト読み込み・LSP 初期化に時間がかかるため、IDE を起動したまま繰り返しテストを実行できる設計とする。

### ディレクトリ構成

```
src/
├── main/                              # 既存プロダクションコード
├── test/                              # 既存ユニットテスト
└── uiTest/
    ├── kotlin/com/rescript/plugin/
    │   ├── UiTestBase.kt              # 共通基底クラス（接続、スクリーンショット）
    │   ├── fixtures/                  # カスタム Fixture クラス
    │   │   ├── EditorFixture.kt       # エディタ操作ヘルパー
    │   │   └── ToolWindowFixture.kt   # ツールウィンドウ操作ヘルパー
    │   └── screenshot/
    │       └── MarketplaceScreenshotTest.kt  # 全 11 シーンの撮影テスト
    └── testData/
        └── sample-project/            # テスト用 ReScript プロジェクト
            ├── rescript.json
            ├── package.json
            ├── src/
            │   ├── Demo.res           # メインデモファイル（ハイライト、補完等）
            │   ├── Demo.resi          # インターフェースファイル（ネスト表示用）
            │   ├── JsxDemo.res        # JSX サポート表示用
            │   └── ErrorDemo.res      # Error Lens 表示用（意図的な型エラー）
            └── node_modules/          # .gitignore（LSP はローカルで npm install）
```

## Gradle 設定

### build.gradle.kts への追加

```kotlin
// ── Remote-Robot 依存関係 ──
val uiTestImplementation by configurations.creating {
    extendsFrom(configurations.testImplementation.get())
}
val uiTestRuntimeOnly by configurations.creating {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    uiTestImplementation("com.intellij.remoterobot:remote-robot:0.11.23")
    uiTestImplementation("com.intellij.remoterobot:remote-fixtures:0.11.23")
}

// ── UI テスト用ソースセット ──
sourceSets {
    create("uiTest") {
        kotlin.srcDir("src/uiTest/kotlin")
        resources.srcDir("src/uiTest/resources")
        compileClasspath += sourceSets["main"].output + configurations["uiTestImplementation"]
        runtimeClasspath += sourceSets["main"].output + configurations["uiTestRuntimeOnly"]
    }
}

// ── UI テスト用 IDE 起動タスク ──
val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
    task {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf(
                "-Drobot-server.port=8082",
                "-Dide.mac.message.dialogs.as.sheets=false",
                "-Djb.privacy.policy.text=<!--999.999-->",
                "-Djb.consents.confirmation.enabled=false",
            )
        }
    }
    plugins {
        robotServerPlugin()
    }
}

// ── UI テスト実行タスク ──
tasks.register<Test>("uiTest") {
    description = "Run UI tests with Remote-Robot"
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = sourceSets["uiTest"].output.classesDirs
    classpath = sourceSets["uiTest"].runtimeClasspath
    systemProperty("robot-server.port", "8082")
    systemProperty("screenshot.output.dir",
        layout.buildDirectory.dir("screenshots").get().asFile.absolutePath)
    // テストプロジェクトのパス
    systemProperty("test.project.path",
        layout.projectDirectory.dir("src/uiTest/testData/sample-project").asFile.absolutePath)
}
```

## クラス設計

### UiTestBase — 共通基底クラス

```kotlin
abstract class UiTestBase {
    companion object {
        val ROBOT_PORT = System.getProperty("robot-server.port", "8082")
        val SCREENSHOT_DIR = System.getProperty("screenshot.output.dir", "build/screenshots")
        val PROJECT_PATH = System.getProperty("test.project.path", "")
    }

    lateinit var remoteRobot: RemoteRobot

    @BeforeEach
    fun connectToIde() {
        remoteRobot = RemoteRobot("http://127.0.0.1:$ROBOT_PORT")
        File(SCREENSHOT_DIR).mkdirs()
    }

    /** IDE 全体のスクリーンショットを撮影し、指定名で保存する */
    fun takeScreenshot(name: String): File { ... }

    /** 指定コンポーネントのスクリーンショットを撮影する */
    fun takeComponentScreenshot(component: Fixture, name: String): File { ... }

    /** IDE の準備完了を待機する */
    fun waitForIdeReady(timeout: Duration = Duration.ofSeconds(60)) { ... }
}
```

### MarketplaceScreenshotTest — 撮影テスト

```kotlin
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class MarketplaceScreenshotTest : UiTestBase() {

    @Test @Order(1)
    fun `01 - syntax highlighting`() {
        // Demo.res を開き、エディタ全体をスクリーンショット
    }

    @Test @Order(2)
    fun `02 - code completion`() {
        // Demo.res で補完をトリガーし、ポップアップ表示中にスクリーンショット
    }

    // ... 11 シーン分
}
```

### 各撮影シーンの実装方針

| # | シーン | 操作手順 | 撮影対象 |
|---|--------|----------|----------|
| 1 | シンタックスハイライト | Demo.res を開く | エディタ全体 |
| 2 | コード補完 | `Array.` を入力 → 補完ポップアップ待機 | エディタ + ポップアップ |
| 3 | Error Lens | ErrorDemo.res を開く（型エラー含む） | エディタ全体 |
| 4 | インレイヒント | Demo.res で型推論された箇所を表示 | エディタ全体 |
| 5 | ストラクチャービュー | Cmd+7 でストラクチャービュー表示 | IDE 全体 |
| 6 | Code Vision | 関数定義のある箇所を表示 | エディタ部分 |
| 7 | JSX サポート | JsxDemo.res を開く | エディタ全体 |
| 8 | Project View | プロジェクトツリーを展開 | プロジェクトツール |
| 9 | クイックフィックス | Alt+Enter メニューを表示 | エディタ + メニュー |
| 10 | ホバードキュメント | シンボル上でホバー表示 | エディタ + ポップアップ |
| 11 | REPL | REPL ツールウィンドウを開く | IDE 全体 |

## テストプロジェクトのサンプルコード

### Demo.res（シーン 1, 2, 4, 5, 6, 10 用）

```rescript
// Main demo module for ReScript IntelliJ Plugin

type user = {
  name: string,
  age: int,
  email: option<string>,
}

let greet = (user: user): string => {
  let greeting = `Hello, ${user.name}!`
  switch user.email {
  | Some(email) => `${greeting} (${email})`
  | None => greeting
  }
}

let users: array<user> = [
  {name: "Alice", age: 30, email: Some("alice@example.com")},
  {name: "Bob", age: 25, email: None},
]

let result = users->Array.map(greet)->Array.join(", ")
```

### ErrorDemo.res（シーン 3 用）

```rescript
// Intentional type errors for Error Lens demonstration

let x: int = "hello"  // Type error: string vs int
let y = x + 1.5       // Type error: int vs float
```

### JsxDemo.res（シーン 7 用）

```rescript
// JSX component demo

module Button = {
  @react.component
  let make = (~label: string, ~onClick: unit => unit, ~disabled=false) => {
    <button onClick={_ => onClick()} disabled>
      {React.string(label)}
    </button>
  }
}

module App = {
  @react.component
  let make = () => {
    let (count, setCount) = React.useState(() => 0)
    <div className="app">
      <h1>{React.string("ReScript + React")}</h1>
      <Button label={`Count: ${count->Int.toString}`} onClick={() => setCount(c => c + 1)} />
    </div>
  }
}
```

## スクリーンショット品質

| 項目 | 仕様 |
|------|------|
| 解像度 | 1280x800px 以上（Marketplace 推奨） |
| フォーマット | PNG |
| テーマ | Darcula（暗色）— Marketplace で映える |
| フォントサイズ | 可読性を確保するため標準サイズ |

## 制約・注意事項

1. **LSP 依存**: シーン 2, 3, 4, 6, 10 は LSP サーバーが動作している必要がある。テストプロジェクトで `npm install` が事前に必要
2. **待機時間**: IDE 起動・LSP 初期化に時間がかかるため、適切な待機処理が必要
3. **macOS 固有**: アクセシビリティ権限が必要（System Settings > Privacy & Security > Accessibility で java を許可）
4. **CI 非対応**: ヘッドレス環境では動作しないため CI には含めない
5. **Kover 除外**: `src/uiTest/` は Kover カバレッジから除外する

## 実装しないこと

- Marketplace への自動アップロード（API 未提供）
- CI での UI テスト実行
- UI テストでの機能リグレッションの網羅的なアサーション（初期段階ではスクリーンショット撮影に注力）
