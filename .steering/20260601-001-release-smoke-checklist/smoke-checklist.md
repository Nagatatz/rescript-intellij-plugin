# Manual Smoke Checklist — vX.Y.Z

リリース前に `./gradlew runIde` で実施する手動検証チェックリスト。`.claude/rules/release.md` / `docs/performance-validation.md` / `docs/lsp-fallback-matrix.md` を補完し、CI / gradle で代替できない領域（実 IDE 内での挙動）をカバーする。

**実施環境:** macOS / Linux / Windows のいずれか1つ以上、JDK 21+

**準備:**
- [ ] `./gradlew clean runIde` で **クリーン** に起動（古い sandbox jar 起因の PluginException 回避）
- [ ] サンプルプロジェクトを開く（`src/uiTest/testData/sample-project/` か手元の実プロジェクト）
- [ ] 起動時に `PluginException` / 赤エラーバルーンが出ていない
- [ ] ステータスバーに ReScript compiler status widget が表示される

---

## 1. ファイル認識・ハイライト（LSP 非依存）

- [ ] `.res` ファイルを開くと ReScript として認識・専用アイコン
- [ ] `.resi` ファイルを開くと ReScript として認識・専用アイコン
- [ ] `.resnb` を開くと Notebook エディタで開く
- [ ] `.resw` を開くと Worksheet モードで開く
- [ ] キーワード / 文字列 / コメント / 数値 / 演算子 / `@decorator` が色付け
- [ ] テンプレート文字列 `` `...${x}...` `` の補間が色付け
- [ ] `rescript.json` に専用アイコンが表示される
- [ ] Light / Dark の両テーマで色が破綻していない（**Settings → Appearance** で切替）

## 2. 編集系（LSP 非依存）

- [ ] `{}` `[]` `()` のブレースマッチ強調
- [ ] `Ctrl+/` で行コメント / `Ctrl+Shift+/` でブロックコメントのトグル
- [ ] `module` / `let` / `type` / ブロックコメントの折りたたみ
- [ ] 改行時の comment continuation（`/** ... */` 内で Enter）
- [ ] Surround With（選択して `Ctrl+Alt+T`）
- [ ] Smart Enter（`Shift+Enter`）
- [ ] Live Template（`Cmd+J` で snippet 候補）
- [ ] Spell check が `.res` 内識別子で機能
- [ ] Structure view（`Cmd+7`）にモジュール・関数・型が出る
- [ ] Breadcrumb がエディタ下部に表示

## 3. LSP 機能（`@rescript/language-server` インストール状態で）

- [ ] LSP サーバが起動し、ステータスが green
- [ ] コード補完（候補に型情報付き）
- [ ] 定義ジャンプ（`Cmd+B` / `Cmd+Click`）— 同一ファイル / 別ファイル / `node_modules`
- [ ] Hover（`Cmd` ホバー）で型とドキュメント表示
- [ ] Find Usages（`Alt+F7`）で参照一覧
- [ ] Signature Help（関数呼び出しで `(` を打ったとき）
- [ ] インレイヒント（推論型）が表示・Settings で ON/OFF 切替可
- [ ] エラー / 警告がインライン波線で表示
- [ ] Problems パネルに診断が出る
- [ ] Error Lens(行末インライン診断）
- [ ] CodeVision（関数行の型注釈）
- [ ] セマンティックトークン色付けが LSP 起動後に上書き反映
- [ ] Type Narrowing Visualizer: `switch x { | Some(y) => ... }` で arm 直後と pattern binding 直後に narrowing 後の型ヒント
- [ ] Shift+F6 リネーム（LSP rename）

## 4. LSP **未インストール** 状態（一時的に `node_modules/@rescript/language-server/` をリネームして再起動）

- [ ] エディタ上部の通知バーで LSP インストール促進が表示
- [ ] プロジェクト起動時バルーン通知が表示
- [ ] パッケージマネージャ自動検出（npm / yarn / pnpm / bun）
- [ ] 「Install」ボタンでバックグラウンドインストールが走る
- [ ] LSP 非接続時にもネイティブ機能（ハイライト / 折りたたみ等）が壊れない
- [ ] **検証後、`@rescript/language-server` を元に戻す**

## 5. Intention / Quick Fix

- [ ] `switch` 上で **Add Missing Switch Arms**（Alt+Enter）→ 不足アームが `| Name(_) => todo` で挿入
- [ ] Variant constructor 上で **Rename Variant Constructor**（Alt+Enter）→ 件数確認ダイアログ → プロジェクト全体一括リネーム → Undo が 1 ステップ
- [ ] 未解決参照に **Add open ...** Quick Fix
- [ ] **Wrap with** Intention
- [ ] `@genType` 追加 Intention

## 6. ToolWindow 系

- [ ] **ReScript Variant Flow**: `switch` 式上で Visual / Source トグル / Mermaid・DOT コピー / ネスト switch 描画 / 凡例表示
- [ ] **ReScript Dependencies**（モジュール依存ダイアグラム）: Visual / Source トグル / Kahn BFS のレイヤ表示 / Cycle ハイライト / Mermaid・DOT コピー
- [ ] **ReScript Type Impact**: `type` 宣言上で Action 実行 → 参照が `[kind]` カラーラベル付きで一覧 / 行クリックでジャンプ
- [ ] **ReScript Interop Risk Map**: `%raw` / `external` / `Obj.magic` / `@bs.*` を含むコードで起動 → HIGH/MEDIUM/LOW 順 / 行頭色帯
- [ ] **ReScript Type Coverage**: 列ソート / coverage % 昇順 / < 30% 赤・≥ 70% 緑の色分け
- [ ] **ReScript PPX View**: `@deriving` / `@react.component` を含むファイルで起動 → 展開結果が色付きで表示
- [ ] **ReScript Type Info**: カーソル位置の型がリアルタイム更新
- [ ] **ReScript REPL**: 入力エリアにハイライトが効く / 評価結果が出る / 履歴ナビ
- [ ] **ReScript Compiled JS Preview**: `.res` 編集中にコンパイル後 JS が更新
- [ ] **ReScript Module Hierarchy**

## 7. ナビゲーション

- [ ] Symbol search（`Cmd+Alt+O`）でモジュール / 関数 / 型がヒット
- [ ] **Search Everywhere の "ReScript Types" タブ**: `(int, string) => result<int, string>` で検索 → 構造マッチが表示 / `=> option<'a>` で返り値検索 / 結果クリックでジャンプ
- [ ] Switch File（`.res` ↔ `.resi`）
- [ ] Navbar が ReScript 構造を表示

## 8. Project Wizard（最も壊れやすい）

代表 3〜4 テンプレートで以下まで通すこと:

- [ ] **Basic** + npm + zod
- [ ] **Vite+React** + pnpm + sury
- [ ] **Hono Inertia** + bun + zod
- [ ] **Tauri** + pnpm + zod（Rust ツールチェイン要）
- [ ] **Validation 選択 UI 非表示** を確認: TanStack Start / Remix RR v7 / Astro / Waku
- [ ] 各テンプレートで `<pm> install` → `npx rescript build` または `<pm> run build` 成功
- [ ] 生成された `Validation.res` が選択したライブラリのコードになっている

## 9. 実行構成 / デバッグ

- [ ] **ReScript Build** 実行構成で `rescript build` が走る
- [ ] **Test (jest / vitest)** 実行構成が起動
- [ ] Debug 設定が GUI に出る（実走行は環境依存なので任意）

## 10. パフォーマンス目視（NFR-01）

- [ ] 500 行以上の `.res` で打鍵レスポンスにラグを感じない
- [ ] IDE 起動 → プロジェクト open → エディタ操作可能までが体感数秒以内
- [ ] 5 分操作後に Heap snapshot を取り `com.rescript.plugin.*` が ~50MB 以内（メジャーリリース時のみ）

## 11. 互換性 / 警告

- [ ] `idea.log` に新規 ERROR / WARN が増えていない（`Help → Show Log in Finder`）
- [ ] Plugin Verifier 警告（`./gradlew verifyPlugin`）が前回比で増えていない

---

## NG だった場合

| 症状 | アクション |
|------|------|
| `PluginException` 起動失敗 | `./gradlew clean runIde` し直し / `gradle.properties` の `pluginVersion` が前回 sandbox と被っていないか確認 |
| LSP 機能だけ動かない | `@rescript/language-server` のバージョン確認 / ステータスウィジェットでログ参照 |
| ハイライト崩れ | `Rescript.flex` に手を入れた場合は `./gradlew generateRescriptLexer` を再走 |
| 新機能だけ落ちる | 当該機能のみ単体 PR に切り出し、リリースから除外する判断も検討 |

---

**実施者:** _______________
**実施日:** YYYY-MM-DD
**対象バージョン:** vX.Y.Z
**結果:** PASS / FAIL（FAIL の場合は失敗項目と issue へのリンクを記録）
