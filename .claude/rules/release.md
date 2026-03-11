# リリース手順

**以下は強制的な行動指示であり、例外なく従うこと。**

リリースを行う際は、以下の手順を**この順序どおりに**実行すること。

## 前提条件

- `main` ブランチが最新であること（`git pull origin main`）
- すべての CI チェックがパスしていること
- リリース対象のコミットが `main` に含まれていること

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

### 8. GitHub Release のリリースノート確認

GitHub Actions の Release ワークフロー (`release.yml`) が起動し、以下が完了することを確認する:

- GitHub Release が作成される
- JetBrains Marketplace へのパブリッシュが成功する

`release.yml` は `generate_release_notes: true` で GitHub の自動生成ノートを使用するため、タグメッセージは GitHub Release には反映されない。必要に応じて `gh release edit` でリリースノートを手動で更新する。

```bash
gh release edit v<新バージョン> --notes "<リリースノート>"
```

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
