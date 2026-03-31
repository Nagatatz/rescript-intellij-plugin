# 要求: コンパイル済み JS ファイルの Project View ネスト修正

## 背景

Project View で `.res.js` が `.res` の下にネストされない。`.resi` は `TreeStructureProvider` で正しくネストされているが、`.res.js` は `ProjectViewNestingRulesProvider`（suffix ベース）を使っており機能していない。

## 要求

1. `.res.js` / `.res.mjs` / `.res.cjs` を `.res` の下にネスト表示する
2. `.bs.js` / `.bs.mjs` / `.bs.cjs` も同様にネスト表示する
3. `.resi` と同じ `TreeStructureProvider` 方式で実装する
4. コンパイル済み JS の灰色表示を全サフィックスに拡張する

## 受け入れ条件

- [ ] `Demo.res.js` が `Demo.res` の下にネスト表示される
- [ ] `.res.mjs`, `.res.cjs`, `.bs.js`, `.bs.mjs`, `.bs.cjs` も同様にネストされる
- [ ] 灰色表示が全コンパイル済みサフィックスに適用される
- [ ] 既存の `.resi` ネストが壊れていない
- [ ] テストがすべてパスする
