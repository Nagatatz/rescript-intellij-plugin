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

### 2. バージョン番号の更新

`gradle.properties` の `pluginVersion` を新バージョンに更新する。

### 3. ビルド検証

```bash
./gradlew clean buildPlugin
```

### 4. バージョン更新のコミット

```bash
git add gradle.properties
git commit -m "⬆ Bump version to <新バージョン>"
```

### 5. アノテーション付きタグの作成

リリースノートをタグメッセージとして含める。

```bash
git tag -a v<新バージョン> -m "<リリースノート>"
```

### 6. プッシュ

コミットとタグを一括でプッシュする。

```bash
git push origin main v<新バージョン>
```

### 7. リリース確認

GitHub Actions の Release ワークフロー (`release.yml`) が起動し、以下が完了することを確認する:

- GitHub Release が作成される
- JetBrains Marketplace へのパブリッシュが成功する

## 禁止事項

- **`gradle.properties` を更新せずにタグを作成すること** — Marketplace パブリッシュが失敗する
- **軽量タグ (`git tag v<version>`) の使用** — 必ずアノテーション付きタグを使うこと
- **タグのみプッシュしてコミットをプッシュしないこと** — 一括プッシュすること

## バージョニング規則

セマンティックバージョニング (`MAJOR.MINOR.PATCH`) に従う:

- **PATCH**: バグ修正、リファクタリング、ドキュメント更新
- **MINOR**: 新機能追加、後方互換性のある変更
- **MAJOR**: 破壊的変更
