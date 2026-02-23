---
globs: ["docs/**/*.md", ".steering/**/*.md"]
---

# 図表・ダイアグラムルール

図表を作成・更新する場合は **draw.io MCP ツールを使用すること**。

## ツール選択

| 図の種類 | 推奨ツール | 入力形式 |
|---------|-----------|---------|
| フローチャート | `mcp__drawio__open_drawio_mermaid` | `graph TD` / `flowchart TD` |
| シーケンス図 | `mcp__drawio__open_drawio_mermaid` | `sequenceDiagram` |
| 状態遷移図 | `mcp__drawio__open_drawio_mermaid` | `stateDiagram-v2` |
| クラス図 | `mcp__drawio__open_drawio_mermaid` | `classDiagram` |
| レイヤー図・カスタムレイアウト | `mcp__drawio__open_drawio_xml` | draw.io XML |
| テーブル構造・組織図 | `mcp__drawio__open_drawio_csv` | CSV |

## 禁止事項

- ASCII アート（罫線文字 `┌─┐│└─┘` 等）での図表作成
- Markdown コードブロック内の Mermaid 記法（` ```mermaid ` ブロック）での図表埋め込み
- 手書きテキストベースのツリー図（`├──`, `└──` 等）

既存の ASCII / Mermaid 図を発見した場合、更新の機会があれば draw.io MCP で描き直すこと。

## MCP サーバー確認

作図が必要な場合、`mcp__drawio__open_drawio_mermaid` 等の drawio ツールが
利用可能か確認すること。利用不可の場合は、ユーザーに以下を案内する:

「drawio MCP サーバーが無効です。`/mcp` で drawio を有効化してください。」

作図完了後、トークン節約のため `/mcp` で drawio を無効化することを提案する。
