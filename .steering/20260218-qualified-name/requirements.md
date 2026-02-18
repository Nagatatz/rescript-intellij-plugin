# Requirements: Qualified Name Copy

## 概要
Cmd+Shift+Alt+C で完全修飾名（`Module.SubModule.functionName`）をコピーする。

## 受け入れ条件
- ファイル名をルートモジュール名として使用（先頭大文字化）
- ネストされたモジュール内の要素はドット区切りのパスを生成
- NAVIGABLE_TYPES の要素に対してのみ動作
- qualifiedNameToElement は null を返す（LSP が担当）
