# 技術仕様書 (Architecture)

## 1. テクノロジースタック

### コア技術

| 技術 | バージョン | 用途 |
|------|-----------|------|
| Kotlin | 2.3.21 | プラグインの主要実装言語 |
| JFlex | GrammarKit 2023.3.0.3 | レクサー定義・自動生成 |
| Java | 21+ (Temurin) | JFlex 生成コードのコンパイルターゲット |
| IntelliJ Platform SDK | 2026.1.2 | プラグイン基盤 API |
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
| **上限 IDE バージョン** | 未設定（詳細は下記「`pluginUntilBuild` を設定しない理由」参照） |
| **JDK** | 21 以上 |
| **対象 IDE** | IntelliJ IDEA (Community/Ultimate)、WebStorm、その他全 JetBrains IDE |
| **OS** | Windows / macOS / Linux（JetBrains IDE が動作する全 OS） |

#### `pluginUntilBuild` を設定しない理由

`gradle.properties` では `pluginSinceBuild` のみ設定し、`pluginUntilBuild` は意図的に未設定としている。

- **前方互換性の維持** — IntelliJ Platform の破壊的変更は稀で、多くの場合新しい IDE バージョンでも既存プラグインは動作する。上限を明示すると、新 IDE のリリースごとに手動でタグを切り直してパブリッシュする必要が生じる
- **Marketplace 側で検証されるため安全** — JetBrains Marketplace は新規プラグインと既存プラグインの互換性を継続的にチェックし、問題があれば該当バージョンを自動的に互換性リストから外す。プラグイン側で上限を設けなくても、ユーザーが不具合のあるビルドを掴むリスクは限定的
- **上限を設定すべきケース** — 実際に特定の破壊的変更で動作しないことが判明した場合は、`pluginUntilBuild` を設定した緊急パッチをリリースし、修正版で再度上限を外す運用とする

### 外部依存

| 依存 | 最低バージョン | 必須/任意 | 用途 |
|------|---------------|----------|------|
| Node.js | 18 LTS+ | 任意（LSP 機能に必要） | `@rescript/language-server` の実行 |
| `@rescript/language-server` | 1.0.0+（ReScript 11+ 対応版） | 任意（LSP 機能に必要） | コード補完、診断、定義ジャンプ等 |
| ReScript | 11.0+ | 任意（LSP 機能に必要） | Language Server が解析対象とする ReScript ランタイム |
| JavaScript プラグイン | IDE 同梱版 | 任意 | `%raw()` 内 JavaScript ハイライト |
| Markdown プラグイン | IDE 同梱版 | 任意 | Markdown コードフェンスハイライト |
| JavaScriptDebugger | IDE 同梱版（Ultimate/WebStorm） | 任意 | コンパイル済み JS のデバッグ |

**`@rescript/language-server` のバージョンに関する注記**:

- 最新の安定版（1.x 系）を推奨。プラグインは LSP 拡張プロトコル（`rescript/compilationStatus`、`textDocument/createInterface`、`textDocument/openCompiled`、Semantic Tokens）を使用するため、これらをサポートする 1.0.0 以降のバージョンが必要。
- 0.x 系（ReScript 10 以前向け）は本プラグインの動作対象外。
- LSP の自動インストール機能（`RescriptLspInstaller`）はバージョン未指定で `@rescript/language-server` をインストールするため、常に最新版が選択される。

### API 制約

- IntelliJ Platform の **公式 API のみ** を使用する（`@ApiStatus.Internal` や非公開 API の使用禁止）
- LSP API は IntelliJ Platform 2024.1+ で利用可能（2025.3+ で全 IDE に開放）
- Gradle Configuration Cache が有効のため、タスク定義で `Project` インスタンスへの直接参照を避ける

### 設計上の制約

- **式レベルのパースは行わない** — ReScript の複雑な式構文（パイプ演算子、パターンマッチ、JSX のネスト等）のパースは LSP に委譲し、プラグイン側はトップレベル宣言の認識に留める
- **LSP サーバー非依存のフォールバック** — LSP が利用不可でもネイティブ機能（ハイライト、折りたたみ、ストラクチャービュー等）は正常動作する。機能ごとの LSP 依存マトリクスは [lsp-fallback-matrix.md](lsp-fallback-matrix.md) を参照
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

### パフォーマンス検証

各指標の計測手段、リリース時の検証手順、ラチェット運用方針は [performance-validation.md](performance-validation.md) を参照。

## 5. セキュリティ考慮事項

| 項目 | 対策 |
|------|------|
| LSP サーバー実行 | ローカル Node.js プロセスのみ（ネットワーク通信なし、stdio 経由） |
| 外部プロセス起動 | `rescript format` CLI、`reanalyze` バイナリのみ（ユーザーのプロジェクト内ツール） |
| ファイルアクセス | プロジェクトディレクトリ内のみ（IntelliJ Platform のサンドボックスに準拠） |
| 依存パッケージ | `node_modules/` 内のローカルインストールを優先（グローバルインストールはフォールバック） |
