# Requirements: Go to Symbol

## 概要

IntelliJ の「Go to Symbol」ダイアログ（`Cmd+Option+O`）で ReScript の宣言シンボルを検索・ジャンプできるようにする。

## 背景

現在、ReScript ファイル内のシンボル（`let`, `type`, `module`, `external`, `exception`）はプロジェクト全体からの検索に対応していない。LSP の `workspace/symbol` に依存しているが、LSP 未起動時には利用不可。

## 機能要件

### F1: Go to Symbol への宣言シンボル提供

- `Cmd+Option+O`（Go to Symbol）で以下のトップレベル宣言が検索可能であること:
  - `let` 宣言（関数・値）
  - `type` 宣言
  - `module` 宣言
  - `external` 宣言
  - `exception` 宣言
- ネストされた `module` 内の宣言も検索対象に含めること
- 検索結果にアイコン・ファイル名・行番号が表示されること

### F2: PSI ベースの実装

- 既存のパーサー（`RescriptParser.kt`）が生成する PSI ツリーを活用すること
- `RescriptPsiUtils.extractName()` で名前を取得すること
- `RescriptPsiUtils.getIcon()` でアイコンを取得すること
- 初回実装では `StubIndex` は不要（PSI ツリー走査で実装）

## 非機能要件

- ファイル数 100 以下のプロジェクトで遅延なく動作すること
- 既存のテスト・ビルドを壊さないこと

## 受け入れ条件

- [ ] `Cmd+Option+O` で ReScript の宣言が表示される
- [ ] 検索結果から該当箇所にジャンプできる
- [ ] アイコンが正しく表示される
- [ ] ビルドが通る
