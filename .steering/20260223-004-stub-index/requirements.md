# Requirements: #51 Stub Index

## 概要

PSI Stub Index を導入し、Go to Symbol / Search Everywhere のシンボル検索を O(n) ファイル走査から O(log n) インデックスルックアップに高速化する。

## 背景

現在の `RescriptSymbolContributor` と `RescriptSearchEverywhereContributor` は全 `.res`/`.resi` ファイルを `FileTypeIndex.getFiles()` で列挙し、各ファイルの PSI ツリーを走査して宣言を収集している。大規模プロジェクトではレスポンスが遅い。

## 受け入れ条件

1. 5つの宣言型（LET, TYPE, MODULE, EXTERNAL, EXCEPTION）が PSI Stub として永続化される
2. `RescriptNameIndex` で全宣言名を高速ルックアップできる
3. `RescriptModuleIndex` でモジュール宣言のみを高速ルックアップできる
4. `RescriptSymbolContributor` が StubIndex ベースで動作する
5. 既存機能（折りたたみ、構造ビュー、パンくず、Call Hierarchy 等）が従来通り動作する
6. `./gradlew clean buildPlugin` が成功する
