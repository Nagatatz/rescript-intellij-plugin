# 設計: inferred 型注釈の一括挿入

## コンポーネント構成

```
RescriptBatchAnnotationPlanner   (既存・純ロジック / intention)
  ├ collectInferredLets(text)                  … 未注釈トップレベル let の列挙
  ├ normalizeType(raw)                          … 型文字列の sanitize（既存）
  ├ isInsertableType(type)        ← 新規追加     … 構文的に挿入不可能な型を弾く
  └ buildPlan(text, resolver)                   … 降順オフセットの編集計画
RescriptBatchAnnotationRunner    (新規・実行グルー 不純 / intention)
RescriptBatchInsertInferredTypesIntention (新規 / intention)
RescriptTypeCoveragePanel の行アクション        (既存パネルに追加 / coverage)
```

## 1. planner への挿入可能性フィルタ追加

`normalizeType` で長さ・空白・制御文字は処理済み。これに加え、**構文的に ReScript 型注釈として挿入できない文字列**を弾く純関数 `isInsertableType(type: String): Boolean` を追加する。

弾く対象（v1）:

- weak type variable を含む（`'_weak` 等、`'_` で始まる型変数）
- markdown / hover 残渣を示す文字を含む（バッククォート `` ` ``、`#` 見出し、`*`、改行残り等）
- `buildPlan` は `normalizeType` 通過後に `isInsertableType` を適用し、false なら skippedCount に算入する

`buildPlan` の改修は最小限（フィルタ1行追加）。既存テスト契約（降順オフセット・skip 算入）は維持する。

## 2. RescriptBatchAnnotationRunner（実行グルー）

責務: 「対象ファイルに対し、背景で hover 解決 → write で一括適用 → 結果通知」を1メソッドで提供する。

```kotlin
object RescriptBatchAnnotationRunner {
    fun run(project: Project, file: VirtualFile, editorOrNull: Editor?)
}
```

処理手順:

1. `FileDocumentManager.getDocument(file)` を取得（閉じファイルでも可）。null なら中断
2. EDT で `document.text` と `document.modificationStamp` を控える
3. 純スキャン `collectInferredLets(text)` で候補件数 N。0 件なら「No inferred bindings」通知して終了
4. 確認ダイアログ（件数 N とファイル名を提示）。キャンセルなら終了
5. `Task.Backgroundable`（cancelable）で:
   - `RescriptHoverTypeResolver.forFile(project, psiFile)` を生成
   - `buildPlan(text) { offset -> indicator.checkCanceled(); indicator.fraction 更新; resolver.resolveAt(offset) }`
   - hover はブロッキング（10秒タイムアウト/件）。背景スレッドで実行
6. `onSuccess`（EDT）で `WriteCommandAction.runWriteCommandAction`:
   - `document.modificationStamp` が控えた値と一致するか再検証。不一致なら中断通知
   - `plan.edits`（降順オフセット）を `document.insertString(offset, text)` で適用
   - `PsiDocumentManager.commitDocument`
7. 結果 balloon: `Annotated ${plan.annotatedCount}, skipped ${plan.skippedCount}`

スレッド安全性のポイント:

- hover は1件ごとブロッキングなので EDT で回さない（背景タスク必須）
- オフセットは text スナップショット基準。背景〜write の間の編集は modificationStamp ガードで検出
- 降順適用で先頭から適用しても後続オフセットがずれない（planner 保証）

通知・ダイアログには絶対パスを出さず `file.name` のみ使う。

## 3. RescriptBatchInsertInferredTypesIntention

`IntentionAction` 実装（または `PsiElementBaseIntentionAction`）。

- `getText` / `getFamilyName`: "Insert inferred type annotations (file)"
- `isAvailable`: `.res` ファイル + LSP サーバ在席（`getServer != null` 相当の安価判定）+ `collectInferredLets(text).isNotEmpty()`。**hover は呼ばない**
- `invoke`: `RescriptBatchAnnotationRunner.run(project, file, editor)` に委譲
- `startInWriteAction()` = false（背景タスク + 自前 write action のため）
- `generatePreview`: LSP 依存のため `IntentionPreviewInfo.EMPTY` を返す

plugin.xml に既存 Intention の並びに従って `<intentionAction>` を登録。

## 4. Heat Map 行アクション

`RescriptTypeCoveragePanel` のテーブルにコンテキストアクションを追加する。

- 行選択 → ツールバーボタンまたは右クリックメニュー「Insert inferred type annotations」
- 選択行の `VirtualFile` を取得し、`FileEditorManager.openFile` で開いてから `RescriptBatchAnnotationRunner.run` を呼ぶ（hover を効かせるため対象を開く）
- 低 coverage 行が上位に並ぶ既存ソートと組み合わせ、「型を足したいファイル」から順に処理できる

パネルは Swing UI のためテスト免除対象。アクションのロジックは Runner に寄せる。

## テスト方針

| クラス | テスト |
|---|---|
| `RescriptBatchAnnotationPlanner`（`isInsertableType` 追加分） | 必須・追加。weak typevar / バッククォート / 正常型 の振り分け、buildPlan の skip 算入 |
| `RescriptBatchInsertInferredTypesIntention` | `isAvailable` の純判定部分をテスト（LSP 非在席・候補0件で false） |
| `RescriptBatchAnnotationRunner` | **免除**（LSP 結合 + IDE スレッド + write action）。理由を tasklist 明記 |
| Heat Map 行アクション | **免除**（Swing UI）。理由を tasklist 明記 |

## ドキュメント同期

- CLAUDE.md レイヤー3: 本 Intention を追記（`intention/` の段落）
- README Features: Intention/コード編集カテゴリに追記
- sphinx `user/features/code-editing.md`（EN）+ 対応 `.po`（JA）に変換例つきで追記
- `docs/product-requirements.md`: ロードマップ #117 行を削除（実装済みへ）
