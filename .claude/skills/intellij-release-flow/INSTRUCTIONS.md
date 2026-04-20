# IntelliJ Plugin リリースフロー

`.claude/rules/release.md` で定義された厳格な順序を実装する。重要な不変条件: **`plugin.xml` の `<change-notes>` と `gradle.properties` の `pluginVersion` はタグ作成より前に必ずコミットすること**。どちらも `publishPlugin` が JetBrains Marketplace にアップロードするアーティファクトに焼き込まれるため、タグ作成後の修正では反映されない。

## 利用タイミング

- ユーザーがバージョン `X.Y.Z` のリリースを依頼した場合
- 次回リリース用のチェンジノート草稿を依頼された場合
- マージ完了後に「タグ付けまで残っている作業は？」と聞かれた場合

## 事前条件（どのファイルにも触れる前に検証）

- `git branch --show-current` が `main`
- `git status` がクリーン
- `git pull --ff-only origin main` が成功
- `main` の CI が緑 (`gh run list --branch main --limit 1 --json conclusion,status`)
- 対象バージョンが SemVer 準拠 (`MAJOR.MINOR.PATCH`) で、`gradle.properties` の現在の `pluginVersion` より厳密に大きい

## ワークフロー

### 1. チェンジログ入力の収集

```bash
PREV_TAG=$(git describe --tags --abbrev=0)
git log "$PREV_TAG"..HEAD --pretty=format:'%s'
```

各コミットを絵文字プレフィックスで分類する（`.claude/rules/git-conventions.md` 準拠）:

| 絵文字 | カテゴリ見出し |
|-------|---------------|
| ✨ | New Features |
| 🐛 | Bug Fixes |
| ♻️ | Refactoring |
| ⚡ | Performance |
| 🔧 / ⬆ | Infrastructure |

該当エントリが 0 件のカテゴリは落とす。各コミット件名を、機構ではなく価値を説明するユーザー向け英文に翻訳する。**リリースノートに日本語を混入させてはならない**（`.claude/rules/language.md`）。

### 2. `src/main/resources/META-INF/plugin.xml` の `<change-notes>` を更新

既存 `<change-notes>` ブロックの先頭に挿入する:

```xml
<h3>X.Y.Z</h3>
<h4>New Features</h4>
<ul>
    <li>User-facing sentence explaining the value.</li>
</ul>
<h4>Bug Fixes</h4>
<ul>
    <li>Symptom + fix in one sentence.</li>
</ul>
```

エントリが 1 件以上ある `<h4>` セクションのみ含める。

### 3. `gradle.properties` の `pluginVersion` をバンプ

```
pluginVersion=X.Y.Z
```

### 4. Kover カバレッジラチェットの更新

```bash
./gradlew test koverHtmlReport
# build/reports/kover/html/index.html を開き INSTRUCTION の値を確認
```

続いて `build.gradle.kts` の `kover { reports { verify { rule { bound { minValue = <測定値 - 3> } } } } }` を更新する:

- ラチェット原則: `minValue` はリリースをまたいで**決して下げてはならない**
- 測定値が既存の下限を下回った場合は、下限を下げずに**先にテストを追加**してからリリースする

### 5. ビルド検証

```bash
./gradlew clean buildPlugin
```

新規警告なしで成功する必要がある。`buildPlugin` は `verifyPluginStructure` を実行するため、ここでの失敗はブロッカーとなる。

### 6. バンプをコミット（単一コミット）

```bash
git add gradle.properties src/main/resources/META-INF/plugin.xml build.gradle.kts
git commit -m "⬆ Bump version to X.Y.Z"
```

コミット絵文字は `⬆`（memory のユーザー設定に従う）であり、`🔧` ではない。

### 7. アノテーション付きタグの作成

タグメッセージがリリースの解説本体となる。Step 1 で分類したチェンジログを含めること。**軽量タグではなく必ずアノテーション付き**。

```bash
git tag -a vX.Y.Z -m "$(cat <<'EOF'
## New Features
- …

## Bug Fixes
- …

**Full Changelog**: https://github.com/<owner>/<repo>/compare/vPREV...vX.Y.Z
EOF
)"
```

### 8. コミットとタグを同時プッシュ

```bash
git push origin main vX.Y.Z
```

アトミックなプッシュが必須。コミットなしでタグだけプッシュしてはならない（Marketplace publish が失敗する）。逆にタグなしでコミットだけプッシュしてもならない（release ワークフローがスキップされる）。

### 9. GitHub Release のノートを書き換える

`release.yml` は `generate_release_notes: true` で動作し、自動生成のコミット一覧が出力される。これを手書きの分類済み英語ノートで差し替える:

```bash
gh release edit vX.Y.Z --notes "$(cat <<'EOF'
## New Features
- **Headline feature** — one-sentence elaboration.

## Bug Fixes
- …

## Refactoring
- …

## Infrastructure
- …

**Full Changelog**: https://github.com/<owner>/<repo>/compare/vPREV...vX.Y.Z
EOF
)"
```

### 10. Marketplace publish の確認

`release.yml` が `publishPlugin` を実行する。以下を確認する:

- `gh run list --workflow release.yml --limit 1` が `completed / success` を示す
- JetBrains Marketplace のプラグインページに `X.Y.Z` が出現する（数分遅延することあり）

## 即時停止すべき事象（継続せず修正）

- `plugin.xml` の `<change-notes>` が未更新 → Marketplace 版の Changelog が永久に空欄になる。タグを破棄して修正し再タグ
- 軽量タグ (`git tag vX.Y.Z` を `-a` なしで作成) を作った → ローカル・リモート両方で削除し `-a` 付きで作り直す
- タグだけプッシュしコミットが未プッシュ → 直ちに `git push origin main` を実行。再試行で Marketplace publish が成功する
- Kover `minValue` を下げてしまった → revert してテストを追加し再リリース

## 参考

- ルール: `.claude/rules/release.md`
- 絵文字プレフィックス: `.claude/rules/git-conventions.md`
- 言語方針: `.claude/rules/language.md`
- ワークフロー: `.github/workflows/release.yml`
