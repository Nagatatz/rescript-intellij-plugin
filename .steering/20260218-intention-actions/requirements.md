# Requirements: ReScript Intention Actions

## 概要

Alt+Enter で ReScript 固有のコード変換を提供する Intention Actions を実装する。

## 機能要件

### Wrap with 系 Intention

- **Wrap with Some(...)** — 選択範囲またはカーソル位置の式を `Some(expr)` でラップ
- **Wrap with Ok(...)** — 選択範囲またはカーソル位置の式を `Ok(expr)` でラップ
- **Wrap with Error(...)** — 選択範囲またはカーソル位置の式を `Error(expr)` でラップ

### Annotation 系 Intention

- **Add @genType annotation** — カーソル行の宣言（let/type/module）に `@genType` アノテーションを追加

### 表示・設定

- Settings > Editor > Intentions > ReScript で確認可能
- LSP のコードアクションと共存する（同じ Alt+Enter メニューに表示）

## 受け入れ条件

- [ ] ReScript ファイルでのみ Intention が利用可能
- [ ] Wrap with 系は選択範囲がある場合に動作する
- [ ] Add @genType は let/type/module 宣言上でのみ利用可能
- [ ] 既に @genType が存在する場合は Intention を表示しない
- [ ] ビルドが通る
- [ ] ユニットテストがある
