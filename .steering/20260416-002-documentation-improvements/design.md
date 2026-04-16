# ドキュメント改善 設計

## 設計方針

- **責務の明確化**: 各ドキュメントが「誰向け」「何を記載するか」を明示する
- **単一情報源**: 重複情報は一箇所に集約し、他はリンクで参照する
- **漸進的リファクタ**: 既存ドキュメントの大枠は維持し、セクション単位で整理
- **機能単位コミット**: 10 項目それぞれを独立したコミットにする

## 項目別設計

### 1. `product-requirements.md` の整理

**現状**: L73-232 に「実装済み機能」テーブル（168 行）、L234-240 にロードマップが「全機能実装済み」と記載。L241 以降で本質的な機能要件を記述。

**変更内容**:
- L73-232 の「実装済み機能」テーブルを削除
- L66-71 の「実装済み機能（当初ロードマップから完了）」見出し文も削除
- L234-240 の「将来機能（ロードマップ）」セクションをシンプル化し、「現時点で計画中の機能はない。新規機能提案は GitHub Issues で受け付ける」と記載
- 機能一覧は `README.md` を正としてリンクで参照

**`.claude/rules/documentation.md` 側の更新**:
- L25 の表で `docs/product-requirements.md` の「実装済み機能セクション（ロードマップから移動）」行を削除
- L40 `コミット前の検証` の項目4を削除または「ロードマップに記載があれば削除」に変更

### 2. `CLAUDE.md` L62-170 の機能列挙要約化

**現状**: レイヤー 3 の 109 行の箇条書きに、機能名・説明・ファイルパスまで含まれている。

**変更内容**:
- レイヤー 3 を **機能カテゴリごとに要約** した短い段落に置換（例: 「ナビゲーション」「コード編集」「分析」の 3〜5 カテゴリ、各 2〜3 行）
- 詳細は `docs/functional-design.md` と `README.md` にリンク
- 更新対象範囲: 現行 L62-170 を ~30 行に圧縮

### 3. sphinx-docs `.po` 翻訳（98 件）

**対象ファイル**:
- `user/features/code-analysis.po` (21)
- `user/features/code-completion.po` (20)
- `user/features/advanced.po` (20)
- `user/features/testing.po` (14)
- `user/features/syntax-highlighting.po` (8)
- `user/features/run-build.po` (8)
- `user/features/index.po` (3)
- `user/configuration.po` (2)
- `user/keyboard-shortcuts.po` (1)
- `dev/architecture.po` (1)

**手順**:
- 既存翻訳のトーン（敬体、半角スペース区切り）に合わせて翻訳
- `cd sphinx-docs && make build-ja` で検証
- コミットは「翻訳のみ」として分離（機能カテゴリごとに分割してもよい）

**翻訳方針**:
- 技術用語は英語表記を維持（例: "Intention Action" → 「Intention アクション」）
- UI ラベル・ショートカットは原文維持（例: "Alt+Enter"）
- 文体は既存 .po に合わせて常体（～する／～できる）

### 4. README.md の読者分離

**現状**: README.md の Architecture セクション（L189 付近）に実装者向け詳細が混在。

**変更内容**:
- Architecture セクションを要約し、詳細は `CLAUDE.md` と `docs/architecture.md` にリンク
- 対象範囲: Architecture / Development セクション（開発者向けの深い内容）

### 5. `.claude/rules/README.md` 新規作成

**内容構成**:

```markdown
# .claude/rules 索引

ルールを目的別にグループ化。新規参加者は用途に応じて参照順を決定する。

## コード品質規約 (実装中に参照)
- code-comments.md — KDoc 規約
- testing.md — テスト配置・免除基準
- flex-rules.md — JFlex レクサー編集時の制約
- plugin-xml-rules.md — Extension Point 登録

## ワークフロー (着手前・完了時に参照)
- steering-workflow.md — 計画フェーズの手順
- definition-of-done.md — DoD 5 フェーズチェック
- git-conventions.md — コミット規約・ブランチ運用
- release.md — リリース手順

## ドキュメント・図表 (資料作成時に参照)
- documentation.md — ドキュメント更新規約
- diagram-rules.md — 図表作成ツール
- roadmap-format.md — ロードマップ表記

## 環境・コンテキスト (必要時に参照)
- context-management.md — セッション運用
```

### 6. Project Wizard テンプレート情報を `docs/templates.md` に集約

**新規ファイル**: `docs/templates.md`
- 15 テンプレート一覧をテーブル形式で記載（カラム: 名前、用途、同梱機能、デフォルト PM、備考）
- 各テンプレートの「one step deeper」要素を整理して記載

**更新対象**:
- `CLAUDE.md` の Project Wizard 段落 → 1 文に要約 + `docs/templates.md` リンク
- `README.md` の該当テンプレート段落 → 1 文に要約 + リンク
- `docs/product-requirements.md` の該当行 → 1 文に要約 + リンク

### 7. `docs/glossary.md` のソート

- 各セクション内を英語表記のアルファベット順にソート
- 日本語用語は冒頭の仮名または英語訳でソート基準を統一

### 8. `sphinx-docs/user/troubleshooting.md` の拡充

**追記セクション**:
- **LSP が起動しない場合の診断手順**
  - Node.js PATH 確認コマンド
  - `@rescript/language-server` 存在確認（`ls node_modules/@rescript/language-server`）
  - Tools > Restart ReScript LSP アクションの案内
  - Dump LSP State で内部状態を確認する手順
- **キャッシュ関連の問題**
  - `File > Invalidate Caches` の実行手順
  - `lib/bs` の削除手順
- **ログの確認方法**
  - Help > Show Log in Explorer/Finder
  - `idea.log` でプラグインのログフィルタリング
- 対応する .po 翻訳も追加

### 9. `docs/architecture.md` の `pluginUntilBuild` 背景追記

**追記内容** (1 セクション、~10 行):

```markdown
### pluginUntilBuild を設定しない理由

`pluginSinceBuild` のみ設定し `pluginUntilBuild` は意図的に未設定としている。

- IntelliJ Platform の破壊的変更は稀で、将来の IDE バージョンでもプラグインが動作する可能性が高い
- 上限を設定するとプラグインが新 IDE で利用不可になり、手動リリースを余儀なくされる
- JetBrains Marketplace 側で互換性チェックを実施するため、問題発生時は Marketplace が自動的に公開対象外とする
```

### 10. `docs/versions.md` 新規作成

**新規ファイル**: `docs/versions.md`
- 現行バージョン（`gradle.properties` 参照）
- バージョニング方針（セマンティックバージョニング、リリース規約へのリンク）
- リリース履歴の要約（詳細は `plugin.xml` の `<change-notes>` または GitHub Releases にリンク）
- IDE 互換性表（`sphinx-docs/user/version-matrix.md` を正とし、要約のみ記載）

**他ドキュメントからの参照**:
- `README.md` バージョンバッジ部分 → `docs/versions.md` へのリンク追加
- `docs/product-requirements.md` の「v0.1.7 公開済み」箇所 → `docs/versions.md` リンクに変更
- `sphinx-docs/user/version-matrix.md` → `docs/versions.md` への相互リンク

## コミット分割方針

各項目を独立コミットにする。絵文字プレフィックスは全て `📝`（ドキュメント更新）を使用:

1. `📝 Remove implemented features table from product-requirements.md`
2. `📝 Summarize CLAUDE.md feature list under layer 3`
3. `📝 Translate remaining sphinx-docs .po entries to Japanese`（大きければ 2-3 コミットに分割可）
4. `📝 Separate user/developer content in README.md`
5. `📝 Add .claude/rules/README.md index`
6. `📝 Consolidate project wizard templates to docs/templates.md`
7. `📝 Sort glossary.md entries within each section`
8. `📝 Expand troubleshooting.md with diagnostic procedures`
9. `📝 Document pluginUntilBuild rationale in architecture.md`
10. `📝 Add docs/versions.md as version source of truth`
11. `📝 Mark tasklist complete for documentation improvements`（最終）

## 検証計画

- `cd sphinx-docs && make build-ja` が成功
- `./gradlew ktlintCheck buildPlugin` が成功（ドキュメントのみなのでビルド影響なし）
- 主要ドキュメントの相互リンクが壊れていない（手動確認）
- `.po` 未翻訳 msgstr が 0 件（Python スクリプトで検証）

## 免除事項

- Kotlin テスト: ドキュメントのみの変更のため不要（`definition-of-done.md` の「ドキュメントのみの変更」例外に該当）
- Extension Point 登録: 該当なし
- セキュリティレビュー: 該当なし
