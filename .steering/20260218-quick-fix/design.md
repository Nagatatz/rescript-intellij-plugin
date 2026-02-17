# Design: Quick Fix (LSP Code Actions)

## 実装アプローチ

追加のプラグインコードは不要。IntelliJ Platform の LSP API が `textDocument/codeAction` を自動的にサポートしているため、rescript-language-server が提供するコードアクションはそのまま Quick Fix / Intention として IDE に表示される。

## 新規ファイル

なし

## 変更ファイル

なし

## アーキテクチャ

```
ユーザー操作 (Alt+Enter)
    ↓
IntelliJ Platform LSP API
    ↓
textDocument/codeAction リクエスト
    ↓
rescript-language-server
    ↓
Code Actions レスポンス
    ↓
Quick Fix / Intention メニューに表示
```

既存の LSP 統合 (`RescriptLspServerSupportProvider`, `RescriptLspServerDescriptor`) がこのフローを自動的に処理する。

## テスト

テスト省略。

**理由:** LSP サーバーとの結合が必須であり、単体テストが困難。コードアクションの動作は rescript-language-server の実装に完全に依存しており、プラグイン側にテスト対象のコードが存在しない。

## 影響範囲

既存コードへの影響なし。
