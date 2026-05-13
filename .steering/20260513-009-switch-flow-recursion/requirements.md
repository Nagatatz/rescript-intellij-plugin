# 20260513-009 Switch Flow 再帰表示の修正

## 背景

`flow/` パッケージの Switch Flow Diagram は、`RescriptVariantFlowModel` レイヤーでは `FlowNode.children` を `MAX_NESTING_DEPTH = 3` まで再帰的に構築する。Mermaid / DOT exporter もこの `children` を再帰描画する。

ところが Visual モードを描く `RescriptVariantFlowGraphView.computeLayout` は、子ノードを意図的に折りたたみ、親アームのラベルに `patternSummary\nbodyPreview` を入れるだけになっている (クラス KDoc にも明記)。結果、ユーザーがネストした switch を含むコードにキャレットを置くと、内側 switch のアームが描画されず、外側アームの本文に `switch blink {` のような切れた 1 行だけが見える。

スクリーンショット (2026-05-13 17:40 撮影):

- `let stepLight = (...) => switch light { | Red => Green | Yellow => switch blink { | true => Red | false => Yellow } | Green => Yellow }`
- ToolWindow には `light` 直下に `Red Green` / `Yellow switch blink {` / `Green Yellow` の 3 アームしか出ず、内側 `switch blink` のアームが表示されない

## 要求

1. Visual GraphView でネストした switch のアームを **独立した box として描画** すること
   - 親アーム box の下に子アーム box を行で配置し、エッジで接続する
   - 親アーム box のラベルから body preview を除去 (子 box が body を表現するため重複しない)
   - 再帰の深さは `RescriptVariantFlowModel.MAX_NESTING_DEPTH` の制御に従う (深すぎる場合は `(deeper switch hidden)` プレースホルダーが既にモデル側で挿入される)
2. 既存のフラットな 1 階層の switch については、これまで通り **行ラッピング動作** (狭い viewport で 2 段に折り返す) を維持すること
3. テストで新挙動を保護する

## 受け入れ条件

- [ ] スクリーンショットのコード (`stepLight` + ネスト `switch blink`) で、Visual モードに `light` → (`Red`/`Yellow`/`Green`) と、`Yellow` の下に (`true`/`false`) が階層構造で表示される
- [ ] `RescriptVariantFlowGraphViewTest` に「ネストアームの子 box が親 box の下に追加される」「親アームのラベルから body preview が除外される」の 2 ケース以上が追加され、緑である
- [ ] 既存の wrap テスト (`arm boxes wrap to a second row when viewport is narrow`) が壊れていない
- [ ] `./gradlew ktlintCheck clean buildPlugin test` が緑
- [ ] CLAUDE.md `flow/` の説明文と、sphinx-docs `advanced.md` / 日本語 `.po` も同一コミット内で同期される

## 非要件

- インナー scrutinee (`switch blink` の `blink` 部分) を独立ノードに昇格させること。Mermaid exporter と同様、親アームの直接子として描く
- レイアウト全体の見た目の磨き上げ (アニメーション、選択ハイライト、ホバー強調等)
- LSP 依存追加 (本機能は LSP 不要のまま維持)
