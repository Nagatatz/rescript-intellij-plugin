# 設計: パフォーマンス調査結果

## 調査結果

### 良好な実装パターン（問題なし）

- **Stub Index**: `RescriptNameIndex` / `RescriptModuleIndex` による O(log n) シンボル検索 ✓
- **Startup Activity**: 全4件が `ProjectActivity`（非ブロッキング）で遅延実行 ✓
- **Service**: 全3件が `@Service(Service.Level.PROJECT)` で遅延初期化 ✓
- **ExternalAnnotator**: reanalyze / format check はバックグラウンドスレッドで実行 ✓
- **LSP**: IntelliJ Platform LSP API 経由でスレッドセーフ ✓

### 発見されたバグ

#### BUG-1: RescriptUnusedCodeInspection にタイムアウトなし（高）

- **ファイル**: `RescriptUnusedCodeInspection.kt`
- **問題**: `process.waitFor()` がタイムアウトパラメータなしで呼ばれている
- **影響**: reanalyze プロセスがデッドロックした場合、IDE が無期限にハングする
- **修正**: `process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)` + タイムアウト処理を追加

### パフォーマンスボトルネック

#### PERF-1: Search Everywhere の全ファイルスキャン（高）

- **ファイル**: `RescriptSearchEverywhereContributor.kt`, `RescriptTypeSignatureSearchContributor.kt`
- **問題**: `FileTypeIndex.getFiles()` で全 .res/.resi ファイルを取得し、各ファイルの PSI ツリーをウォーク
- **計算量**: O(N × M)（N=ファイル数、M=ファイルあたりシンボル数）
- **緩和策**: `progressIndicator.checkCanceled()` でキャンセル対応済み
- **改善案**: Stub Index (`RescriptNameIndex`) を活用して PSI ツリーウォークを回避
- **対応時期**: Search Everywhere API 移行（2026.1+）と同時に改善

#### PERF-2: 依存ダイアグラムのキャッシュなし（中）

- **ファイル**: `RescriptDependencyDiagramProvider.kt`
- **問題**: ダイアグラム表示のたびに全 .res ファイルをスキャンし、open/include を正規表現でパース
- **改善案**: `CachedValueProvider` でファイル変更までキャッシュ

#### PERF-3: Project View ネスティングの O(n²)（中）

- **ファイル**: `RescriptTreeStructureProvider.kt`
- **問題**: 子ノードのネスティング処理でリストを複数回ループ
- **緩和策**: .resi/.res の不在時に早期リターンで回避
- **改善案**: Map ベースのルックアップに変更

#### PERF-4: Format Check の毎回プロセス起動（低）

- **ファイル**: `RescriptFormatCheckAnnotator.kt`
- **問題**: 有効時、ファイル編集のたびに `rescript format --check` プロセスを起動
- **緩和策**: 設定で ON/OFF 可能

### 外部プロセスのタイムアウト一覧

| プロセス | タイムアウト | 状態 |
|---------|------------|------|
| rescript format | 10秒 | ✓ |
| REPL コンパイル/実行 | 各30秒 | ✓ |
| reanalyze (ExternalAnnotator) | 30秒 | ✓ |
| format check | 10秒/30秒 | ✓ |
| reanalyze サーバー起動 | 10秒 | ✓ |
| LSP インストール | 300秒 | ✓ |
| DTS パーサー | 30秒 | ✓ |
| LSP サーバー | Platform API 管理 | ✓ |
| Run/Test/Debug | なし（ユーザー制御） | ✓ 意図的 |
| **Global Inspection (reanalyze)** | **なし** | **❌ バグ** |

## 対応優先度

| 項目 | 深刻度 | 修正難易度 | 対応 |
|------|--------|----------|------|
| BUG-1: タイムアウト欠落 | 高 | 低（1行修正） | **即時修正** |
| PERF-1: SE 全ファイルスキャン | 高 | 高 | API 移行と同時（2026.1+） |
| PERF-2: ダイアグラムキャッシュ | 中 | 中 | 改善タスクとして記録 |
| PERF-3: TreeStructure O(n²) | 中 | 低 | 改善タスクとして記録 |
| PERF-4: Format Check プロセス | 低 | — | 現状維持（設定で制御可能） |
