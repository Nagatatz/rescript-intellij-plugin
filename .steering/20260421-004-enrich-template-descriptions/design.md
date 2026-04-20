# 設計: プロジェクトテンプレート説明の拡充

## 変更対象

`src/main/kotlin/com/rescript/plugin/wizard/ProjectTemplate.kt` の 15 エントリの `description` 引数のみを差し替える。Enum シグネチャ (`description: String`) は変更しない。

## 説明のフォーマット

Kotlin raw string リテラル (`"""..."""`) + `trimIndent()` で複数行を保持する。各テンプレートは以下の統一構造に従う:

```
<1 行サマリ>

Includes:
- <主要ライブラリ or 機能 1>
- <主要ライブラリ or 機能 2>
- <主要ライブラリ or 機能 3>
...

Requires: <Node バージョン等>
```

- サマリ行は既存の 1 行説明を踏襲し、主語・目的を明確化
- 「Includes:」ブロックには対応 `*TemplateFiles.kt` の KDoc または `generate()` 内の `dependencies` / `devDependencies` から抽出した具体名を列挙
- 「Requires:」は `TemplateVersions.NODE_ENGINE` 等を参照しつつ、`package.json` `engines.node` 指定がある場合に表示

## 根拠となる情報源

各テンプレートの `*TemplateFiles.kt` の:

- クラス KDoc（既に詳細が書かれているものは再利用）
- `generate()` 内の `packageJson(...)` の `dependencies` / `devDependencies`
- `packageJson(...)` の `engines` 指定

これらはソースファイルから読み取り、説明文の「Includes」「Requires」に反映する。

## UI 側への影響

`RescriptProjectWizardStep.kt` の `descriptionArea` は既に `lineWrap = true, wrapStyleWord = true` で設定されており、raw string 内の改行はそのまま描画される。UI 変更は不要。

## テストへの影響

- `ProjectTemplateTest` は `description.isNotBlank()` のみチェック → 影響なし
- 他テストで description の具体文字列を比較している箇所は無い (`grep` 確認済)

## KDoc / ドキュメント

- `ProjectTemplate` enum 自体の KDoc は変更不要（説明文の拡充は「型の使い方」ではなく「データの充実」）
- `CLAUDE.md` / `README.md` / `sphinx-docs` への影響なし（Wizard の説明拡充はユーザー向け README に記載済みの Features とは直交）

## リスク

- 説明文が長すぎると JTextArea のスクロールが必要になる。Includes を 3〜5 項目程度に制限することで緩和
- raw string リテラルの末尾改行・インデント崩れ → `trimIndent()` 必須とし、視覚的に揃える
