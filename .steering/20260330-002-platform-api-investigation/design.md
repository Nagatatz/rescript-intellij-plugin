# 設計: プラットフォーム API マイグレーション調査

## 調査結果

### 1. Search Everywhere API 移行

**現状:**
- `RescriptSearchEverywhereContributor` (183行) — シンボル名検索
- `RescriptTypeSignatureSearchContributor` (230行) — 型シグネチャ逆引き検索
- 両方とも `WeightedSearchEverywhereContributor<Any>` を実装

**新 API の状況（IntelliJ Platform 2025.3 時点）:**
- `SeItemsProviderFactory` / `SeTabFactory` は `@Experimental` かつ `@Internal`
- サードパーティプラグインでの使用は不可
- 後方互換アダプタにより、レガシー API は引き続き動作

**タイムライン:**
| 時期 | 予定 |
|------|------|
| 2025.2〜 | 新アーキテクチャがリモート開発環境でのみ有効 |
| 2026.1 | 新アーキテクチャがモノリス（ローカル）環境でもデフォルト化 |
| 2026.2 | 旧 API 非推奨化、新 API が experimental を脱する見込み |

**対応方針:** 2026.1 リリース時に新 API の安定化を確認し、移行作業を開始する。現時点ではコード変更不要。

### 2. LSP Code Lens ネイティブ対応

**現状:**
- `RescriptCodeVisionProvider.java` (145行) — `DaemonBoundCodeVisionProvider` 実装
- LSP の `textDocument/codeLens` をカスタム実装で IDE の CodeVision に変換

**プラットフォーム動向:**
- 2026.1 で LSP Code Lens がプラットフォームレベルでサポート予定
- サポート後はカスタム実装を簡素化または削除可能

**対応方針:** 2026.1 リリース後に API を確認し、カスタム実装の簡素化を検討する。

### 3. リモート開発対応

**現状:**
- プラグインは IntelliJ の LSP Platform API (`com.intellij.platform.lsp`) を使用
- LSP サーバーは stdio 経由で起動（リモートトンネリング対応）
- Gateway 固有のコードは不要（プラットフォームが自動対応）

**検証結果:**
- ファイルシステムアクセス: LSP URI ベースで抽象化済み
- UI コンポーネント: ToolWindow（REPL, Type Info 等）はローカル UI のみだが、リモート環境でも機能する
- プロセス起動: ProcessBuilder 経由で LSP サーバーを起動、リモート側で自動実行

**対応方針:** 現時点で対応不要。JetBrains Gateway の正式リリース時に実機検証を推奨。

### 4. ReScript 新構文対応

**現状:**
- `dict` キーワード: 実装済み（`Rescript.flex` line 119, `RescriptTokenTypes.kt` line 75）
- `dict{}` リテラル: レクサーテスト済み
- 関連機能（JSON 生成、型マッピング、ペースト変換）も dict 対応済み

**対応方針:** 対応完了。今後の ReScript コンパイラ更新に伴う新構文は継続的にウォッチする。

### 5. パフォーマンス（別途調査）

**調査予定項目:**
- 大規模プロジェクト（100+ .res ファイル）でのインデックス構築時間
- 補完レスポンスタイム
- メモリ使用量プロファイリング

**対応方針:** 別タスクとしてベンチマーク調査を実施する。

## 優先度まとめ

| 項目 | 対応時期 | 理由 |
|------|---------|------|
| Search Everywhere API 移行 | 2026.1 以降 | API が experimental/internal |
| LSP Code Lens ネイティブ化 | 2026.1 以降 | プラットフォーム API 待ち |
| リモート開発検証 | Gateway 正式リリース時 | 現時点で問題なし |
| dict{} 構文 | 完了 | 実装済み |
| パフォーマンスベンチマーク | 次タスク | コード変更なし |
