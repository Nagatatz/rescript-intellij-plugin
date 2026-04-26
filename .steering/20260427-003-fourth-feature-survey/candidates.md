# 第 4 回機能調査 — 移植候補リスト

`docs/product-requirements.md` の「将来機能（ロードマップ）」テーブルに追記する形式で整理。`#` は次回採番時に決定する（`docs/product-requirements.md` の最大値 + 1 から開始）。

## 凡例

| 優先度 | 定義（`.claude/rules/roadmap-format.md`） |
|---|---|
| S | 最優先 — 実装で DX が大きく改善され、既存ユーザーから明確な価値が見える |
| A | 重要 — 開発体験を着実に向上させ、競合プラグインとの並びを揃える |
| B | 有用 — あると便利だが緊急性は低い |
| C | 将来検討 — 価値は限定的、または前提が整っていない |

## ⚠ 2026-04-27 追加検証: rescript-vscode の Quick Fix は LSP code action

`rescript-vscode/server/src/codeActions.ts` を直接精査した結果、以下の Quick Fix はすべて LSP server-side（`textDocument/codeAction`）で `p.CodeAction` を返す実装である:

- `simpleAddMissingCases`
- `wrapInSome` / unwrap optional
- `addUndefinedRecordFields` (V10 / V11)
- `simpleConversion`
- `applyUncurried`
- `didYouMean`
- `removeUnusedCode`
- `extractLocalModuleToFile`
- `expandCatchAllPatterns`

本プラグインは IntelliJ 2024.1+ の LSP API で「Quick Fix (LSP Code Actions)」を自動サポート済み（`docs/archive/implemented-features.md` 参照）。**したがって、これらは LSP が起動している環境では既にユーザーに提示されている可能性が高い。** 候補として格上げするには、まず実際に LSP code action が IntelliJ 上で表示・実行できているかを runIde で検証する必要がある。

検証結果に応じた次のアクション:

| 検証結果 | 次のアクション |
|---|---|
| LSP code action が問題なく動作 | 該当候補を削除し、`docs/archive/implemented-features.md` に「LSP code action 経由で利用可能」と追記 |
| LSP code action が一部動かない | 動かない原因を調査（IntelliJ LSP API の仕様 / `RescriptLspServerDescriptor` の設定不足等） |
| LSP 接続なしでも欲しい | NFR-04（LSP 非接続時の機能保証）の観点で PSI ベースのネイティブ Quick Fix を別途実装 |

## S 優先度（着手推奨）

| 機能 | カテゴリ | 説明 | 出典 | 難易度 |
|---|---|---|---|---|
| **LSP Code Action 動作検証** | インフラ | runIde で 9 種の code action が表示・実行されることを確認し、結果を `docs/lsp-fallback-matrix.md` に反映 | 本調査 | 低 |
| 構造化 Build Output Tool Window | ToolWindow | `rescript build` 出力を `BuildView` でツリー化し、エラー / 警告ノードから該当行へジャンプ | IntelliJ Rust の Cargo Build Tool Window / reasonml-idea-plugin の Bsb ToolWindow | 中 |
| Coverage Gutter Marks | 分析 | jest / vitest の lcov を IntelliJ Coverage Engine で取り込み、`.res` 行に緑/赤/黄を表示 | JetBrains 標準 Coverage Engine | 中〜高 |
| Modular Plugin v2 への分割 | インフラ | `plugin.xml` を backend / frontend に分割し、CodeWithMe / Remote Dev / 2026.x split-jar に追従 | IntelliJ Platform 2025.3+ | 中 |
| Switch 不足ケース挿入 ネイティブ Quick Fix（LSP 非接続時のフォールバック） | Quick Fix | LSP 非接続環境向けに PSI + hover ベースで実装。`RescriptCaseSplitIntention` の経験を流用 | rescript-vscode `simpleAddMissingCases` の補完 | 中 |
| reanalyze 未使用コード削除 Quick Fix | Quick Fix | reanalyze annotator 由来の警告に対する Quick Fix。LSP 経由ではなくローカル reanalyze プロセス出力に対するアクション | rescript-vscode `removeUnusedCode` の補完 | 中 |

## A 優先度（次サイクル候補）

| 機能 | カテゴリ | 説明 | 出典 | 難易度 |
|---|---|---|---|---|
| Inline Completion Provider | 補完 | LSP completion とは別レーンの ghost text 補完。boilerplate / lambda body / pipe 中継型を先取り表示 | IntelliJ Platform 2024.3+ Inline Completion API | 中 |
| Declarative Inlay Hints API 移行 | InlayHints | 旧 `codeInsight.inlayHintsProvider` × 3 を `codeInsight.declarativeInlayProvider` に移行（Remote Dev 対応） | IntelliJ Platform 2024.1+ | 低〜中 |
| Compile-on-Save | Editor | 保存時に `rescript build` を自動トリガ（watch モード未起動の代替） | reasonml-idea-plugin `CompileOnSave` | 低 |
| `addUndefinedRecordFields` Quick Fix | Quick Fix | レコード初期化時の欠落フィールドを一括追加 | rescript-vscode | 中 |
| `wrapInSome` / unwrap optional Quick Fix | Quick Fix | option 型不整合を Some/None で自動補正 | rescript-vscode | 低 |
| `simpleConversion` Quick Fix | Quick Fix | `int` ⇄ `float` ⇄ `string` 変換関数を自動挿入 | rescript-vscode | 低 |
| `expandCatchAllPatterns` Code Action | Intention | switch の `_ =>` を全コンストラクタに展開（既存 CaseSplit との差別化） | rescript-vscode | 低 |
| `extractLocalModuleToFile` Refactoring | リファクタリング | ローカル `module M = { ... }` を独立ファイルに昇格 | rescript-vscode | 中 |
| Add `@deriving` Quick Fix | Quick Fix | `type t = ...` に `@deriving(show)` / `@deriving(jsonCodable)` をワンクリック追加 | Haskell の derive instance | 低 |
| 冗長型注釈 Inspection | Inspection | 推論で済む `let x: int = ...` の注釈を削除提案 | Scala / Rust | 低 |
| `@deprecated` 装飾 + Quick Doc 警告 | 補完 | `@deprecated` シンボルに打ち消し線、ホバーで代替 API 表示 | Scala / Rust | 低 |
| WolfTheProblemSolver / Problems View 連携 | 分析 | LSP `publishDiagnostics` を Problems Tool Window のカスタムタブに集約 | IntelliJ Platform | 中 |
| WorkspaceFileIndex 移行 | インデキシング | 旧 `AdditionalLibraryRootsProvider` を WorkspaceModel ベースに置換し、`node_modules/@rescript/*` 認識を高速化 | IntelliJ Platform 2024.x+ | 中 |
| ネスト PPX のステップ展開 | ToolWindow | 既存 PPX View に「1 段ずつ展開」UI を追加 | IntelliJ Rust の Recursive Macro Expansion | 中 |

## B 優先度（バックログ）

| 機能 | カテゴリ | 説明 | 出典 | 難易度 |
|---|---|---|---|---|
| `applyUncurried` Quick Fix | Quick Fix | `f(. x)` ドット呼び出しへの自動変換 | rescript-vscode | 低 |
| `incrementalTypechecking.acrossFiles` 設定公開 | インフラ | LSP 初期化オプションを設定 UI に追加 | rescript-vscode | 低 |
| `signatureHelp.forConstructorPayloads` 設定公開 | 補完 | variant コンストラクタ引数の Signature Help トグル | rescript-vscode | 低 |
| Cargo Features 風 rescript.json トグル UI | ToolWindow | `package-specs` / `bs-dependencies` / `ppx-flags` をツリーで切替編集 | IntelliJ Rust | 中 |
| Convert tuple ⇄ record Intention | Intention | タプル ⇄ レコード相互変換、フィールド名生成 | Scala | 中 |
| Rescript-tools Doc Generator | ToolWindow | `rescript-tools doc-gen` の Markdown 出力をローカルプレビュー | Rust / Scala | 中 |
| Velocity ベースのファイルテンプレート | 編集 | `fileTemplates/internal/` を Velocity 化（著者・日付・Validation 選択を引数化） | IntelliJ Platform | 低 |
| Settings Sync 対応宣言 | インフラ | `RescriptProjectSettings` を `RoamingType.DEFAULT` で公開 | IntelliJ Platform | 低 |
| Inspection Profiles プリセット | 分析 | `ReScript Strict` / `ReScript Default` プロファイルをバンドル | Scala / Rust | 低 |
| Terminal Shell Command Handler | 実行 | ユーザが `rescript build` をターミナルにタイプした際に Run Configuration への振替を提案 | IntelliJ Platform | 低 |
| Console Folding（rewatch / bsb 出力） | ToolWindow | ビルドログを VCS Log ビューでブロック折り畳み | IntelliJ Platform | 低 |
| Diff Extension（型シグネチャ・open 差分強調） | 編集 | `.res` の diff ビューでシグネチャ変更・open 追加削除を独自ハイライト | IntelliJ Platform | 中 |
| `bsb -clean-world -make-world` 同等のクリーンビルドアクション | 実行 | 強制再ビルドアクション | reasonml-idea-plugin | 低 |
| Snippets 補完: JS Module / Global External | 補完 | `external` 系の VSCode 公式スニペットを Live Templates に追加 | rescript-vscode | 低 |

## C 優先度（要再検討）

| 機能 | カテゴリ | 説明 | 出典 | 難易度 |
|---|---|---|---|---|
| CMT/CMI ファイルビューア | ToolWindow | `.cmt` バイナリを開いて型情報を閲覧。ReScript v11+ での生成有無を確認後に判断 | reasonml-idea-plugin | 中 |
| Dump server state / Dump analysis info コマンド | デバッグ | LSP 状態を手動ダンプ（既存 `RescriptDumpLspStateAction` との差分要確認） | rescript-vscode | 低 |
| AI Assistant チャットコンテキスト連携 | その他 | ホバー型情報・PPX 展開・Validation 設定をチャットコンテキストに自動添付 | IntelliJ Platform（公式 EP 公開待ち） | 中 |
| ACP / Multi-Agent 連携 | その他 | `@rescript/language-server` を ACP-compatible として登録 | IntelliJ Platform 2026.1（API 公開状況要確認） | 不明 |

## 実装着手の推奨順序

1. **Switch 不足ケース挿入 Quick Fix**（S, 中）— ユーザー価値が高く、`RescriptCaseSplitIntention` の経験を流用可能
2. **reanalyze 未使用コード削除 Quick Fix**（S, 中）— 既存 `RescriptReanalyzeAnnotator` に Quick Fix を追加するだけで済む
3. **Declarative Inlay Hints 移行**（A, 低〜中）— 既存 3 つのプロバイダを順次置換、Remote Dev の前準備
4. **構造化 Build Output Tool Window**（S, 中）— Run Configuration の出力品質が大幅向上
5. **Modular Plugin v2 への分割**（S, 中）— 2026.1 verifier-cli 1.403+ リリース後に Search Everywhere v2 移行とまとめて実施

## 次のアクション

- 候補から実装する機能を `docs/product-requirements.md` の「将来機能（ロードマップ）」テーブルに採番付きで追加
- 各機能ごとに個別 steering（`.steering/[YYYYMMDD]-[NNN]-<機能名>/`）を作成し、requirements / design / tasklist を起草
- 本調査結果（`requirements.md` / `findings.md` / `candidates.md`）は履歴として保持し、編集しない
