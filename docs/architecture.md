# 技術仕様書 (Architecture)

## 1. テクノロジースタック

### コア技術

| 技術 | バージョン | 用途 |
|------|-----------|------|
| Kotlin | 2.3.10 | プラグインの主要実装言語 |
| JFlex | GrammarKit 2023.3.0.2 | レクサー定義・自動生成 |
| Java | 21+ (Temurin) | JFlex 生成コードのコンパイルターゲット |
| IntelliJ Platform SDK | 2025.3.2 | プラグイン基盤 API |
| LSP4J | IntelliJ Platform 内蔵 | LSP クライアント実装 |

### ビルド・品質ツール

| ツール | バージョン | 用途 |
|--------|-----------|------|
| Gradle (Kotlin DSL) | Wrapper 管理 | ビルドシステム |
| IntelliJ Platform Gradle Plugin | 2.11.0 | プラグインビルド・パッケージング |
| GrammarKit | 2023.3.0.2 | JFlex レクサー生成タスク |
| ktlint | 1.6.0 | Kotlin コードスタイルチェック |
| Kover | 0.9.4 | コードカバレッジ計測 |
| JUnit 4 | 4.13.2 | ユニットテストフレームワーク |
| Qodana | JetBrains Cloud | 静的解析（CI 連携） |

### ドキュメント

| ツール | 用途 |
|--------|------|
| Sphinx | ユーザー向け・開発者向けドキュメント (`sphinx-docs/`) |
| GitHub Pages | ドキュメントホスティング |

## 2. 開発ツールと手法

### IDE

- **開発 IDE**: IntelliJ IDEA（Kotlin/JFlex 編集、Gradle タスク実行）
- **デバッグ**: `./gradlew runIde` でサンドボックス IDE インスタンスを起動（内部モード有効、ホットリロード対応）

### CI/CD パイプライン

| ワークフロー | トリガー | 内容 |
|-------------|---------|------|
| CI (`ci.yml`) | `main` への push / PR | ktlint チェック、ビルド、テスト、Kover カバレッジ、プラグイン検証 |
| Qodana (`qodana_code_quality.yml`) | PR / `main` push / 手動 | JetBrains Qodana 静的解析 |
| Release (`release.yml`) | `v*.*.*` タグ push | ビルド・検証後 GitHub Release 作成 |
| Docs (`docs.yml`) | `sphinx-docs/` 変更時 | Sphinx ビルド・GitHub Pages デプロイ |

### バージョン管理

- **VCS**: Git
- **ブランチ戦略**: `main` + feature ブランチ（詳細は `.claude/rules/git-conventions.md`）
- **コミット規約**: 絵文字プレフィックス付き英語メッセージ

## 3. 技術的制約と要件

### プラットフォーム制約

| 制約 | 詳細 |
|------|------|
| **最低 IDE バージョン** | IntelliJ Platform 2025.3+（`sinceBuild = 253.0`） |
| **上限 IDE バージョン** | 未設定（前方互換性のため `untilBuild` は意図的に省略） |
| **JDK** | 21 以上 |
| **対象 IDE** | IntelliJ IDEA (Community/Ultimate)、WebStorm、その他全 JetBrains IDE |
| **OS** | Windows / macOS / Linux（JetBrains IDE が動作する全 OS） |

### 外部依存

| 依存 | 必須/任意 | 用途 |
|------|----------|------|
| Node.js | 任意（LSP 機能に必要） | `@rescript/language-server` の実行 |
| `@rescript/language-server` | 任意（LSP 機能に必要） | コード補完、診断、定義ジャンプ等 |
| JavaScript プラグイン | 任意 | `%raw()` 内 JavaScript ハイライト |
| Markdown プラグイン | 任意 | Markdown コードフェンスハイライト |
| JavaScriptDebugger | 任意 | コンパイル済み JS のデバッグ |

### API 制約

- IntelliJ Platform の **公式 API のみ** を使用する（`@ApiStatus.Internal` や非公開 API の使用禁止）
- LSP API は IntelliJ Platform 2024.1+ で利用可能（2025.3+ で全 IDE に開放）
- Gradle Configuration Cache が有効のため、タスク定義で `Project` インスタンスへの直接参照を避ける

### 設計上の制約

- **式レベルのパースは行わない** — ReScript の複雑な式構文（パイプ演算子、パターンマッチ、JSX のネスト等）のパースは LSP に委譲し、プラグイン側はトップレベル宣言の認識に留める
- **LSP サーバー非依存のフォールバック** — LSP が利用不可でもネイティブ機能（ハイライト、折りたたみ、ストラクチャービュー等）は正常動作する
- **シングルプロセス LSP** — プロジェクトごとに 1 つの LSP サーバープロセスを stdio 経由で管理

## 4. パフォーマンス要件

| 項目 | 目標 | 根拠 |
|------|------|------|
| シンタックスハイライト更新 | < 16ms（60fps 相当） | エディタ入力時の体感即座の応答 |
| IDE 起動への影響 | 無視できるレベル | 遅延ロード (`postStartupActivity`) で起動時コストを最小化 |
| メモリ使用量 | プラグイン単体で 50MB 以下 | 他プラグインとの共存を考慮 |
| LSP 起動時間 | Node.js プロセス起動に依存 | プラグイン側のオーバーヘッドは最小限 |
| 補完・診断の応答 | LSP サーバーの処理速度に依存 | プラグイン側は LSP レスポンスをそのまま転送 |

### パフォーマンス設計

- **レクサーの差分処理**: IntelliJ Platform のインクリメンタルレキシングにより、変更された箇所のみ再トークン化
- **LSP 通信の非同期化**: Platform LSP API が自動的に非同期 I/O を管理
- **Gradle ビルド最適化**: Configuration Cache + Parallel Execution + Build Cache 有効

## 5. セキュリティ考慮事項

| 項目 | 対策 |
|------|------|
| LSP サーバー実行 | ローカル Node.js プロセスのみ（ネットワーク通信なし、stdio 経由） |
| 外部プロセス起動 | `rescript format` CLI、`reanalyze` バイナリのみ（ユーザーのプロジェクト内ツール） |
| ファイルアクセス | プロジェクトディレクトリ内のみ（IntelliJ Platform のサンドボックスに準拠） |
| 依存パッケージ | `node_modules/` 内のローカルインストールを優先（グローバルインストールはフォールバック） |
