# バージョン情報

プラグインのバージョン・IDE 互換性・リリース履歴の参照先をまとめる。バージョン情報が 4 ファイルに分散していたため、**本ドキュメントを単一情報源**とする。

## 現行バージョン

| 項目 | 値 | 取得元 |
|---|---|---|
| プラグインバージョン | `pluginVersion` の値 | `gradle.properties` |
| 対象 IDE バージョン (下限) | IntelliJ Platform 2025.3+（`sinceBuild = 253.0`） | `gradle.properties` |
| 対象 IDE バージョン (上限) | 未設定（理由は [architecture.md](architecture.md#pluginuntilbuild-を設定しない理由) 参照） | `gradle.properties` |
| JDK | 21 以上 | `build.gradle.kts` |

最新バージョンは [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/com.rescript.plugin) または [GitHub Releases](https://github.com/Nagatatz/rescript-intellij-plugin/releases) で確認できる。

## バージョニング方針

[セマンティックバージョニング](https://semver.org/lang/ja/) (`MAJOR.MINOR.PATCH`) に従う:

- **PATCH** — バグ修正、リファクタリング、ドキュメント更新
- **MINOR** — 新機能追加、後方互換性のある変更
- **MAJOR** — 破壊的変更

リリース手順の詳細は [.claude/rules/release.md](../.claude/rules/release.md) を参照。

## 情報源の住み分け

| 情報の種類 | 参照先 |
|---|---|
| 現行バージョン番号 | `gradle.properties` の `pluginVersion` |
| バージョンごとの詳細な機能リリースノート（ユーザー向け） | [sphinx-docs/user/version-matrix.md](../sphinx-docs/user/version-matrix.md) |
| Marketplace に表示される変更履歴 | `src/main/resources/META-INF/plugin.xml` の `<change-notes>` |
| GitHub Release の自動生成ノート | [GitHub Releases](https://github.com/Nagatatz/rescript-intellij-plugin/releases) |
| IDE 互換性マトリックス | [sphinx-docs/user/version-matrix.md](../sphinx-docs/user/version-matrix.md) の IDE Compatibility セクション |
| 過去リリースの日付・タグ | Git タグ (`git tag -l 'v*'`) および GitHub Releases |

## リリース運用

リリース手順とカバレッジラチェットポリシーは [.claude/rules/release.md](../.claude/rules/release.md) に記載。主要なルール:

- `plugin.xml` の `<change-notes>` は **タグ作成前に必ず更新**してからコミットする（パブリッシュ後のアーティファクトに焼き込まれるため、リリース後の修正は反映されない）
- タグは **アノテーション付きタグ** (`git tag -a`) のみ使用。軽量タグは禁止
- コミットとタグは **一括プッシュ** する (`git push origin main v<version>`)
- `kover.reports.verify.rule.minBound` はリリースごとに実測値 -3% で更新し、前バージョンから下げない（ラチェット）

## 変更履歴の閲覧方法

| 目的 | 操作 |
|---|---|
| Marketplace 上の変更履歴を見る | Marketplace プラグインページの「What's New」タブ |
| ローカルで変更履歴を確認する | `src/main/resources/META-INF/plugin.xml` の `<change-notes>` を閲覧 |
| コミット粒度で変更を確認する | `git log <前タグ>..HEAD --oneline` |
| GitHub 上で差分を確認する | [Compare](https://github.com/Nagatatz/rescript-intellij-plugin/compare) ページで 2 タグ間を指定 |

新規バージョンを出す際は、本ドキュメントに追記する必要はない（`gradle.properties` が単一情報源）。ただし **バージョニング方針自体を変える場合は本ドキュメントを更新** すること。
