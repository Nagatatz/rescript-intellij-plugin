# Fable5 徹底検証監査 — requirements（調査記録）

## 目的

Marketplace 稼働中の ReScript IntelliJ プラグイン（v0.1.16.3 / Platform 2026.1.2、296 Kotlin ファイル・54 パッケージ）を **4 観点（コード正確性・アーキテクチャ/技術的負債・パフォーマンス/セキュリティ・テスト/機能UX）で徹底検証**し、優先度付き改善案を確定する。オーケストレーションは大局を Fable5、詳細検証を opus/sonnet が担う多エージェント構成。

本ドキュメントは調査記録であり、コード変更を伴わない。`main` 直接コミット可（`📝`）。実装は別ステアリング `20260702-002-audit-remediation` で行う。

## 監査手法（audit-tasks.md 二段検証を必須適用）

1. **一次調査**: 8 finder（opus/sonnet、4観点。複雑ファイル群は分割）が源コードを Read/Grep し、`{file:line, dimension, severity, failure_scenario, suggested_fix}` 形式で所見を収集。「曖昧なら drop、根拠は file:line 必須、precision 優先」を指示。
2. **二段検証（敵対的）**: 各所見に独立 opus skeptic を割り当て「反証せよ、不確実なら refuted=true」で検証。high/critical は 2 票、その他 1 票。全票非反証のみ生存。観点別 lens（correctness=再現性 / security=悪用可否 / performance=hot-path 実測 / architecture=代替API実在確認）。
3. **自己検証**: 正確性・セキュリティの 9 件は、セッション上限でサブエージェント検証が中断したため、**担当者本人が原典を Read して二段検証**（audit-tasks.md が明示的に許可する手法）。

## 二段検証の実績（除外を含む）

- 正確性 finder 一次 11 件 → **9 件確定**（本人 Read 検証）。
- 新 4 観点（arch/perf/test/ux）一次 25 件 raw → **14 件確定 / 11 件を反証で除外**。
  - 除外例: 「5 ファイルの `UnstableApiUsage` 抑制が `plugin-verifier-ignored-problems.txt` 未文書化」finding は、`./gradlew verifyPlugin` が **exit 0（GREEN）** の実測で反証（UnstableApiUsage は verifier failure を生じず、既存文書化の範囲内）。
- **確定合計: 23 件**（正確性/セキュリティ 9 + アーキ 4 + 性能 5 + テスト 1 + UX 4）。

## ビルド健全性の実測（Phase 0）

推測ではなく実コマンドで確認（feedback_verify_before_assert / build-verification-gotchas 準拠）:

| タスク | 結果 |
|--------|------|
| `./gradlew ktlintCheck buildPlugin test --rerun koverHtmlReport` | **exit 0**（`--rerun` で build cache 偽 green 回避） |
| `./gradlew verifyPlugin` | **exit 0**（非推奨/内部 API は全て文書化済み ignored-problems の範囲内、新規違反なし） |
| Kover カバレッジ | headline 高 80% 台、`minBound` 86% と整合 |

**結論**: プラグインは健全。所見は「火事」ではなく洗練のための改善である。

## 確定所見 23 件（7 テーマ）

### テーマ① LSP offset 変換のチョークポイント（最高 ROI）
- **[High]** `util/RescriptOffsetUtils.kt:44` — `positionToOffset` が `position.character` を無クランプで `lineStart+character` を返す。KDoc 通り 14+ ファイルが使う共有根本原因。範囲外 LSP position で不正 offset。
- **[Med]** `refactor/RescriptRenameHandler.kt:262` — 編集ガードが `startOffset<=endOffset` / `startOffset<=textLength` を検証せず → `replaceString` が throw し rename 全体を中断（①派生、部分適用リスク）。
- **[Med]** `refactor/RescriptRenameHandler.kt:226` — `applyWorkspaceEdit` が `edit.changes` のみ読み、LSP 仕様推奨の `documentChanges` 形式を黙殺 → rename 無反応。

### テーマ② JFlex lexer の隠れ状態（インクリメンタル字句解析契約からの逸脱）
- **[Med-High]** `lang/Rescript.flex:15` — カスタムフィールド `commentDepth`/`inCommentString`/`inJsxOpenTag`/`jsxAttrBraceDepth` が `getState()` に符号化されず → コメント/JSX 内の再字句解析がリスタート時に破綻。
- **[Med]** `lang/Rescript.flex:304` — ブロックコメント内 `"` が奇数個 → `inCommentString` が真のまま `*/` 無視 → ファイル末尾まで飲込み（例: `/* the "foo option */`）。
- **[Low-Med]** `lang/Rescript.flex:283` — `IN_TEMPLATE` の `{EOL} {}` 空アクション → `` `${x}\n${y}` `` で 1 文字カバレッジ欠落。

### テーマ③ switch の行/正規表現ベース再解析（ユーザーコード書換えの最危険クラス）
- **[Med]** `intention/RescriptMergeSwitchCasesIntention.kt:74` — `CASE_SEPARATOR_PATTERN = ^\s*\|` が `\|>`・`\|\|`・ネスト switch 内側アームにも誤一致 → マージ時に本文欠落・誤解析。
- **[Med]** `intention/RescriptCaseSplitIntention.kt:69` — caret 物理行 1 行のみ読取/置換 → 複数行アーム本文が孤立し非コンパイル出力。
- **[Low]** `narrowing/RescriptSwitchArmCollector.kt:82` — `tokenize()`/`isIgnorable()`/`LexedToken` が `intention/RescriptNestedSwitchFlattener.kt` とほぼ重複（③の修正ビークル）。

### テーマ④ 編集ホットパス上の同期 LSP/無制限処理
- **[High]** `lsp/RescriptPipeChainTypeHintsProvider.kt:80` — inlay pass 毎に ARROW トークン数分の同期 hover（`sendRequestSync(10_000)`、上限なし）。しかも ARROW=`=>`（全 lambda/switch arm）に一致し想定の pipe 数を大幅超過。
- **[Med]** `narrowing/RescriptNarrowingHintProvider.kt:134` — 最大 50 アーム×(1+N binding) 同期 hover/pass（背景 daemon スレッドだが LSP flooding + hint 遅延）。
- **[Med]** `navigation/RescriptCreateInterfaceAction.kt:60` — `actionPerformed`（EDT）で 10s 同期 LSP → server 応答不能時 UI フリーズ。`navigation/RescriptOpenCompiledJsAction.kt:56` も同型。
- **[Med]** `ppx/RescriptPpxViewPanel.kt:57` — caret 移動毎に全文書 `document.text` コピー + 行分割 + regex を EDT で実行（他 3 caret パネルは debounce 済、これのみ欠落）。

### テーマ⑤ エラー表示への絶対パス/生 stderr 漏洩（CLAUDE.md セキュリティ規約違反）
- **[Low]** `binding/DtsGenerateBindingAction.kt:66`（`$tsPath` 直接）/ `:128`・`:137`（`e.message` に stderr 由来絶対パス）。
- **[Low]** `lsp/RescriptLspInstaller.kt:98` — パッケージマネージャ生 stderr（`npm ERR! path /Users/...` 等）を notification balloon に verbatim 表示。

### テーマ⑥ ドキュメントが実装を先取り（docs/requirements drift）
- **[Med]** `worksheet/RescriptWorksheetRunner.kt:30` — `evaluate()` は `@Suppress("unused")` の未接続スタブ（gutter/action/plugin.xml 未登録）なのに US-12 が `.resw` 実行を `[x]` 表記。
- **[Low]** `ppx/RescriptPpxViewPanel.kt:89` — `docs/lsp-fallback-matrix.md:115` は「`bsc -bs-ast` 直接呼出がメイン」と主張、実装は regex + hardcoded 辞書（bsc 非呼出）。
- **[Low]** `typeinfo/RescriptTypeInfoPanel.kt:154` — 「LSP 不在」と「型なし位置」が同一 `No type information` メッセージ、fallback matrix は専用 placeholder を過大主張。

### テーマ⑦ 既存共有レイヤーを迂回した重複（純粋 hygiene）
- **[Low]** `flow/RescriptVariantFlowGraphView.kt:87` — `paintNode` が `ui/GraphViewPaintHelpers` を迂回して `diagram/RescriptDependencyDiagramGraphView.kt` と重複。
- **[Low]** `flow/RescriptVariantFlowDotExporter.kt:48` — DOT escape が `diagram/RescriptDependencyDiagramModel.escapeDot()` と別実装（改行挙動差、ただし現状の入力では顕在化せず）。
- **[Low]** `test/RescryptPpxViewPanelTest.kt:23` — 単一行複数アノテーションのテスト欠落（`find` vs `findAll` バグを隠蔽）+ ファイル名 typo `Rescrypt`。

## 残存不確実性（audit-tasks.md: 不確実性が残るのに「完了」宣言しない）

- `narrowing/RescriptSwitchArmCollector.kt:143`（空 scrutinee `switch{` で `TextRange(start>end)` アサーション）は、トークン列が空白トークンを含むか否かが決定要因。**low/条件付き** として据え置き、深追いしない。
- 性能所見（④）はいずれも背景 daemon スレッド上であり、hover の実 latency は通常 ms オーダー。理論最悪（10s×N）は slow/unresponsive server 時のテールであり、typical では「pass あたり秒〜遅延」。EDT 直結フリーズは `CreateInterfaceAction`（cold path）のみ。

## 受け入れ条件

- [x] 4 観点すべてを finder で走査した
- [x] 全所見を二段検証（敵対的 or 本人 Read）し、除外件数を記録した
- [x] ビルド健全性を実コマンドで確認した（推測なし）
- [x] 確定所見を優先度付けし、実装チェックポイントに分割した（design.md）
- [x] 残存不確実性を明記した
