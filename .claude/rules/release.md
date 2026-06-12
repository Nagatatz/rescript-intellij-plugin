# リリース手順

**以下は強制的な行動指示であり、例外なく従うこと。**

リリースを行う際は、以下の手順を**この順序どおりに**実行すること。

## 前提条件

- `main` ブランチが最新であること（`git pull origin main`）
- すべての CI チェックがパスしていること
- リリース対象のコミットが `main` に含まれていること
- 直近の Monthly Verify の `template-versions-audit` ジョブが green であること（fail している場合は `TemplateVersions.kt` の該当ピンを引き上げてから。ローカル再現: `node .github/scripts/audit-template-versions.mjs /tmp/ta && cd /tmp/ta && npm i --package-lock-only --legacy-peer-deps && npm audit --audit-level=high`）

## 手順

### 1. リリースノートの作成

前バージョンのタグとの差分を取得し、リリースノートを作成する。

```bash
git log <前タグ>..HEAD --oneline
```

リリースノートは以下のカテゴリで分類する:

| カテゴリ | 対応する絵文字プレフィックス |
|---------|------------------------|
| New Features | ✨ |
| Bug Fixes | 🐛 |
| Refactoring | ♻️ |
| Performance | ⚡ |
| Infrastructure | 🔧 |

### 2. `plugin.xml` の `<change-notes>` 更新

`src/main/resources/META-INF/plugin.xml` の `<change-notes>` セクション先頭に新バージョンのエントリを追加する。このセクションが **Marketplace のプラグインページに表示される変更履歴** となる。

```xml
<h3>X.Y.Z</h3>
<h4>カテゴリ名</h4>
<ul>
    <li>変更内容</li>
</ul>
```

**重要:** `<change-notes>` は `publishPlugin` 実行時のアーティファクトに焼き込まれるため、**タグ作成前に必ず更新・コミットすること**。リリース後に追加しても既にパブリッシュ済みのバージョンには反映されない。

### 3. バージョン番号の更新

`gradle.properties` の `pluginVersion` を新バージョンに更新する。

### 4. ビルド検証

```bash
./gradlew clean buildPlugin
```

### 5. リリースコミット

`plugin.xml` とバージョン更新を1つのコミットにまとめる。

```bash
git add gradle.properties src/main/resources/META-INF/plugin.xml
git commit -m "⬆ Bump version to <新バージョン>"
```

### 6. アノテーション付きタグの作成

リリースノートをタグメッセージとして含める。

```bash
git tag -a v<新バージョン> -m "<リリースノート>"
```

### 7. プッシュ

コミットとタグを一括でプッシュする。

```bash
git push origin main v<新バージョン>
```

### 8. GitHub Release のリリースノート更新

GitHub Actions の Release ワークフロー (`release.yml`) が起動し、GitHub Release が作成され JetBrains Marketplace へのパブリッシュが完了することを確認する。

**以下は強制的な行動指示であり、例外なく従うこと。**

GitHub Release のリリースノートは **`gh release edit` で手動記述する**。`release.yml` の `generate_release_notes: true` による自動生成ノート（コミット一覧）は差し替える。

```bash
gh release edit v<新バージョン> --notes "<手動記述のリリースノート>"
```

リリースノートは以下のフォーマットで記述する:

- カテゴリ見出し (`## New Features`, `## Bug Fixes`, `## Refactoring`, `## Infrastructure`) で分類
- 各項目は **英語** で記述する（コミットメッセージのコピーではなく、変更の意味・価値を説明）
- 主要な新機能には **太字** でタイトルを付け、補足説明を添える
- 末尾に `**Full Changelog**: https://github.com/<owner>/<repo>/compare/<前タグ>...v<新バージョン>` を含める

## 禁止事項

- **`plugin.xml` の `<change-notes>` を更新せずにタグを作成すること** — Marketplace に変更履歴が反映されない（リリース後の修正不可）
- **`gradle.properties` を更新せずにタグを作成すること** — Marketplace パブリッシュが失敗する
- **軽量タグ (`git tag v<version>`) の使用** — 必ずアノテーション付きタグを使うこと
- **タグのみプッシュしてコミットをプッシュしないこと** — 一括プッシュすること

## カバレッジラチェットポリシー

`build.gradle.kts` の `kover.reports.verify.rule.minBound` は、リリースごとに以下のルールで更新する:

1. リリース前に `./gradlew test koverHtmlReport` を実行し、実測カバレッジを確認する
2. `minBound` を **実測値 - 3%** に設定する（例: 実測 88% → minBound 85）
3. `minBound` を前バージョンより下げてはならない（ラチェット: 一方向のみ）
4. カバレッジが低下した場合はテストを追加して回復させること

## バージョニング規則

セマンティックバージョニング (`MAJOR.MINOR.PATCH`) に従う:

- **PATCH**: バグ修正、リファクタリング、ドキュメント更新
- **MINOR**: 新機能追加、後方互換性のある変更
- **MAJOR**: 破壊的変更
