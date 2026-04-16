---
paths:
  - "**/*.md"
  - "docs/**"
  - ".steering/**"
---

# ドキュメント管理規約

## ドキュメントの分類

- **永続的ドキュメント (`docs/`)**: プロジェクト全体の設計・方針。大きな設計変更時のみ更新
- **作業単位のドキュメント (`.steering/`)**: 特定の開発作業の計画・記録。作業ごとに新規作成

## 原則

- ドキュメントの作成・更新は段階的に行い、各段階で承認を得る
- `.steering/` のディレクトリ名は `[YYYYMMDD]-[NNN]-[開発タイトル]` で識別する
- 永続的ドキュメントと作業単位のドキュメントを混同しない

## 機能実装時のドキュメント更新

**以下は強制的な行動指示であり、例外なく従うこと。**

コードの変更を伴う機能実装・バグ修正を行う場合、以下のドキュメントを同時に更新すること:

| ドキュメント | 更新対象 | 必須/任意 |
|-------------|---------|----------|
| `CLAUDE.md` | アーキテクチャセクション（レイヤー 3: IDE 統合機能） | 必須 |
| `README.md` | Features セクション（該当カテゴリ） | 必須 |
| `sphinx-docs/user/features/` | 該当する機能ページ | 必須 |
| `docs/product-requirements.md` | ロードマップに新規機能が記載されている場合のみ該当行を削除 | 任意 |

### 更新タイミング

- ドキュメント更新は **該当機能のコミットに含める**（機能コード + テスト + ドキュメント更新 = 1コミット）
- または、全機能実装後に **1つのドキュメント更新コミット** としてまとめてもよい
- tasklist.md にドキュメント更新タスクを必ず含めること

### コミット前の検証

**以下は強制的な行動指示であり、例外なく従うこと。**

ドキュメント更新コミットを行う前に、以下のすべてを確認すること:

1. **CLAUDE.md**: 新機能がレイヤー 3 の一覧に追加されているか。既存エントリの説明が実装と一致しているか
2. **README.md**: 該当する Features カテゴリに機能が追加されているか
3. **sphinx-docs/user/features/**: 該当ページに機能の説明・使用例が記載されているか。Intention/Quick Fix は具体的な変換例を含むこと
4. **docs/product-requirements.md**: ロードマップに該当機能が記載されていた場合、その行を削除したか

**上記のうち該当するドキュメントすべてが更新されるまでドキュメント更新コミットを行ってはならない。**

### Sphinx ドキュメントの配置

| 機能カテゴリ | ファイル |
|-------------|---------|
| ナビゲーション | `sphinx-docs/user/features/navigation.md` |
| コード補完・テンプレート・Signature Help | `sphinx-docs/user/features/code-completion.md` |
| コード編集・Intention・Generate・Surround | `sphinx-docs/user/features/code-editing.md` |
| コード分析・Inspection・Quick Fix・Error Lens | `sphinx-docs/user/features/code-analysis.md` |
| その他（ツールウィンドウ、プロジェクト統合等） | `sphinx-docs/user/features/advanced.md` |

### 日本語訳の同時更新

**以下は強制的な行動指示であり、例外なく従うこと。**

`sphinx-docs/` 配下の `.md` を新規作成・更新した場合、**同一コミット内で** 対応する日本語訳 (`sphinx-docs/locale/ja/LC_MESSAGES/**/*.po`) も更新すること。英語のみ追加して `.po` を後回しにしてはならない。

手順:

```bash
cd sphinx-docs
make gettext      # .pot ファイルを再生成
make update-po    # .po ファイルを .pot に同期 (新規は空 msgstr、既存は追記)
# 各 .po の空 msgstr を日本語で埋める
make build-ja     # 翻訳が通ることを確認
```

検証:

- 新規 `.md` を追加したら、対応する `.po` がコミットに含まれているか確認する
- 既存 `.md` を編集したら、該当 `.po` の `msgstr` が更新されている（または新しい `msgid` が翻訳済み）か確認する
- `make build-ja` が成功することを必ず確認する

例外: `.po` の POT-Creation-Date 行のみの変更や、リンクターゲットが変わっただけの参照行シフトは許容するが、未翻訳の `msgstr ""` を残したままコミットしてはならない。

## 図表・ダイアグラム

**図表を作成・更新する場合は draw.io MCP ツールを使用すること。** ASCII アートや Markdown Mermaid コードブロックでの図表作成は禁止。ツール選択の詳細は `.claude/rules/diagram-rules.md` を参照。
