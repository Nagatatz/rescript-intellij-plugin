# Requirements: 静的解析指摘事項の修正

## 概要

Qodana 相当の静的解析で検出された問題を修正し、コード品質を向上させる。

## 対象範囲

### CRITICAL (ビルド不可)
1. `RescriptTodoIndexer.kt` で `IdAndTodoScannerBasedOnFilterLexer` が未解決参照 → IntelliJ Platform 2025.3 互換の API に移行

### HIGH
2. `RescriptParser.kt` の `isTopLevelStart` で `&&` / `||` 演算子優先順位が曖昧 → 括弧で明示化

### MEDIUM
3. `it.node.elementType` への安全でないアクセス (複数箇所) → `it.node?.elementType` に修正
4. `RescriptRenameHandler` の EDT 上でのブロッキング LSP 呼び出し → 非同期化は影響大のため今回はコメント記録のみ
5. フォーマッタと LSP フォーマッティングの競合 → 既存の `canFormat` で制御済みか確認、必要なら対応

### LOW (パフォーマンス)
6. ホットパスでの `listOf()` / `setOf()` / `toRegex()` 毎回生成 → 定数化
7. `PsiTreeUtil.findChildrenOfAnyType` の非効率な PSI 走査 → 対象ノードのみの走査に改善

### LOW (エラーハンドリング)
8. 例外の握りつぶし → 適切なログ出力追加
9. プロセスタイムアウト不足 → `proc.waitFor` にタイムアウト追加

### LOW (コードスタイル)
10. FQN の不統一 → import に統一
11. `RESCRIPT_EXTENSIONS` 定数の重複定義 → 共通化
12. 冗長なオーバーライド → 削除

### LOW (ベストプラクティス)
13. `RescriptFile.getFileType()` が `.resi` で不正な型を返す → `viewProvider.fileType` に修正
14. `VirtualFileManager.findFileByUrl` → `LocalFileSystem.findFileByPath` に修正
15. `saveAllDocuments()` → 個別ドキュメントの保存に修正
16. リソースリーク → `.use {}` で明示的にクローズ

## 受け入れ条件

- `./gradlew buildPlugin` が成功すること
- 既存の動作を壊さないこと
- 全指摘事項が修正されていること
