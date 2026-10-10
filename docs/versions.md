# バージョン情報

プラグインのバージョン・IDE 互換性・リリース履歴の参照先をまとめる。バージョン情報が 4 ファイルに分散していたため、**本ドキュメントを単一情報源**とする。

## 現行バージョン

| 項目 | 値 | 取得元 |
|---|---|---|
| プラグインバージョン | `pluginVersion` の値 | `gradle.properties` |
| 対象 IDE バージョン (下限) | IntelliJ Platform 2026.1.4+（`sinceBuild = 261.26222`。理由は [architecture.md](architecture.md#pluginsincebuild-が-202614-である理由) 参照） | `gradle.properties` |
| 対象 IDE バージョン (上限) | 未設定（理由は [architecture.md](architecture.md#pluginuntilbuild-を設定しない理由) 参照） | `gradle.properties` |
| ビルド対象プラットフォーム | 2026.2.3 | `gradle.properties` の `platformVersion` |
| JDK | 25 以上 | `build.gradle.kts` |

最新バージョンは [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/com.rescript.plugin) または [GitHub Releases](https://github.com/Nagatatz/rescript-intellij-plugin/releases) で確認できる。

## CI documentation setup

The Docs workflow adopts [setup-uv v10.1.0](https://github.com/astral-sh/setup-uv/releases/tag/v10.1.0) at commit `bec219d24cd3e171d82865faccec33120bb574f4`. The latest stable checked on 2026-10-10 is [v10.3.0](https://github.com/astral-sh/setup-uv/releases/tag/v10.3.0); it is not adopted by this update. Both v10.0.1 and v10.1.0 use Node 24 and have the same inputs/defaults. v10.1.0 adds NO_PROXY support, download checksum verification through Astral version metadata and an unused Python-runtime identity output.

The Docs workflow pins `astral-sh/setup-uv` to commit `bec219d24cd3e171d82865faccec33120bb574f4` (v10.1.0), following the third-party action pinning policy. The action uses Node 24 on GitHub-hosted Ubuntu runners. Its pin selects the setup action, not the uv executable: the existing latest-uv selection and project Python requirements are retained.

Each documentation job uses `uv sync --locked` and checks that `pyproject.toml` and `uv.lock` remain unchanged. An outdated lock fails installation instead of being regenerated in CI. Explicit `enable-cache: true` is retained; the default dependency glob includes `sphinx-docs/uv.lock`. Python caching and cache pruning remain disabled. A cache miss is valid and does not relax the lock check. See the [official cache documentation](https://github.com/astral-sh/setup-uv/blob/v10.1.0/docs/caching.md) for restore/save keys. Cache state is checked in new-head CI; a previous successful PR head is not evidence for the updated pin.

Changes to the Docs workflow itself trigger its PR validation, including lint, tests, translations, English/Japanese builds and accessibility checks. PR runs do not deploy Pages. This Linux documentation validation does not establish plugin physical or interactive runtime compatibility.

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
