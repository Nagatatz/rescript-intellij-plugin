# 設計: コンパイル済み JS ファイルの Project View ネスト修正

## 方針

`RescriptTreeStructureProvider.modify()` を拡張し、`.resi` に加えてコンパイル済み JS ファイルも `NestingTreeNode` の children に含める。

## 対象サフィックス

| サフィックス | ベース名抽出 |
|-------------|-------------|
| `.res.js` / `.res.mjs` / `.res.cjs` | `Demo.res.js` → `Demo` |
| `.resi.js` / `.resi.mjs` / `.resi.cjs` | `Demo.resi.js` → `Demo` |
| `.bs.js` / `.bs.mjs` / `.bs.cjs` | `Demo.bs.js` → `Demo` |

## ネスト構造

```
Demo.res
├── Demo.resi
├── Demo.res.js
```

## 変更ファイル

1. `RescriptCompiledJsNodeDecorator.kt` — `isCompiledJsFile()` を全サフィックスに拡張 + ベース名抽出ヘルパー追加
2. `RescriptTreeStructureProvider.kt` — コンパイル済み JS もネスト対象に追加
3. `RescriptFileNestingProvider.kt` — 全サフィックスのルール追加（フォールバック）
4. テスト 3 ファイル更新
