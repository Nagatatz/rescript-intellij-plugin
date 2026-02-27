---
globs: ["docs/**/*.md", ".steering/**/*.md"]
---

# 図表・ダイアグラムルール

図表を作成・更新する場合は **[beautiful-mermaid](https://github.com/lukilabs/beautiful-mermaid)** を使用すること。

## 作図手順

1. Mermaid 記法で図を定義する
2. `npx beautiful-mermaid` で SVG にレンダリングする
3. 生成された SVG ファイルをドキュメントから参照する

```bash
# SVG レンダリング
npx beautiful-mermaid render <input.mmd> -o <output.svg>

# テーマ指定
npx beautiful-mermaid render <input.mmd> -o <output.svg> --theme tokyo-night
```

## 対応図の種類

| 図の種類 | Mermaid 記法 |
|---------|-------------|
| フローチャート | `graph TD` / `flowchart TD` |
| シーケンス図 | `sequenceDiagram` |
| 状態遷移図 | `stateDiagram-v2` |
| クラス図 | `classDiagram` |
| ER 図 | `erDiagram` |
| XY チャート | `xychart-beta` |

## 禁止事項

- ASCII アート（罫線文字 `┌─┐│└─┘` 等）での図表作成
- 手書きテキストベースのツリー図（`├──`, `└──` 等）

## ファイル配置

- Mermaid ソース: `docs/diagrams/<名前>.mmd`
- レンダリング済み SVG: `docs/diagrams/<名前>.svg`
- ドキュメントからは SVG を参照する（`![図の説明](diagrams/<名前>.svg)`）
