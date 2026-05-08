# Add Missing Switch Arms Intention — Requirements

## 背景

未実装機能リスト #10 として残っていた「AI Exhaustive-Case Suggester」を、**LLM/トークンを一切使わない構文ベースの Intention** として実装する。VS Code 拡張の "Insert missing cases" コードアクションと同等の体験を提供する。

既存資産:

- `RescriptCaseSplitIntention`: LSP hover → variant constructor 列挙 → エディタ書き換えの完全な前例
- `RescriptSwitchArmCollector.collect(source)`: switch 境界・スクラティニー range・各アームの offset を提供
- `RescriptLspUtils.getHoverType` / `parseVariantConstructors`: 型取得と constructor 解析
- `RescriptGenerateSwitchAction.generateSwitchText`: スケルトン文字列のスタイル参照

## ユーザーストーリー

### US-009-01: 不足アームの自動追加

**ReScript 開発者として**、`switch x { | Some(_) => ... }` のように一部 constructor しかカバーしていない switch 式に対して、**Alt+Enter から残りのアームをまとめて追加**したい。

**受け入れ条件:**

- [ ] カーソルが switch キーワードまたはスクラティニー上にあるとき、Intention "Add missing switch arms" が Alt+Enter で表示される
- [ ] LSP hover でスクラティニーの型を取得できる場合、未カバー constructor を `| Name(_) => todo` / `| Name => todo` として閉じ `}` 直前に挿入する
- [ ] すでに全 constructor を網羅している switch、または `_` ワイルドカードを含む switch では Intention が出ない
- [ ] LIDENT 単独 binding パターン（例: `| any => ...`）も「全カバー」として扱う
- [ ] or-pattern (`| Foo | Bar => ...`) は `Foo` と `Bar` の両方をカバーしているとして認識する
- [ ] ネストした switch で、カーソル位置の最内 switch のみが対象になる
- [ ] LSP 未起動・型取得失敗時はクラッシュせず no-op で抜ける

### US-009-02: 純粋関数による高い単体テスト性

**保守者として**、Intention 内のロジックが IDE fixture なしで完全に単体テスト可能であることを保証したい。

**受け入れ条件:**

- [ ] 「未カバー constructor 計算」「挿入オフセット計算」「挿入文字列構築」を `RescriptMissingArmsBuilder` という pure object に切り出す
- [ ] Intention 本体は LSP 呼び出しと write action ラッパーのみを担当し、ロジックは持たない

## スコープ外

- LSP 診断と直接連動した自動 Quick Fix 化（インスペクション化）
- ポリ variant `[#Foo | #Bar]` の網羅判定（既存 `parseVariantConstructors` の対応範囲に依存）
- record / tuple / list パターンの欠落補完
- when ガード付きアームの厳密な網羅性判定（when 以降は無視する既存ポリシーを踏襲）

## 受け入れ確認

- [ ] `RescriptMissingArmsBuilder` の 8 ケースの単体テストが pass
- [ ] `RescriptAddMissingSwitchArmsIntention` の light fixture テストが pass
- [ ] `./gradlew ktlintCheck` / `./gradlew clean buildPlugin` / `./gradlew test` がすべて green
- [ ] `runIde` で 5 シナリオを実機検証
- [ ] DoD Phase 3 のチェック項目をすべて満たす

## 非機能要件

- KDoc を新規 class / object 全てに付与（英語）
- Deprecated API を新規利用しない
- LSP 未接続時もクラッシュしないフォールバック
