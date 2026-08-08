# requirements: IntelliJ Platform 2026.2 へのバンプ

## 背景

`gradle.properties` の `platformVersion` は `2026.1.2` に固定されている。一方、JetBrains の
`intellij-repository/releases` の `maven-metadata.xml` が示す最新安定版は **`2026.2.0.1`** であり、
`2026.1.3` / `2026.1.4` / `2026.2` / `2026.2.0.1` の 4 リリース分遅れている。

この遅れは自動追跡され、Issue #54（`intellij-platform-watch.yml` が維持する notify-only トラッカー）に
2026-08-03 時点で「newer stable available: 2026.2.0.1」として記録されている。

さらに `plugin-verifier-ignored-problems.txt` には、**「platformVersion を 2026.2 系に上げたら再評価する」**
と明記された保留エントリが 3 件存在する。本作業はこれらの解消も射程に含める
（`.claude/rules/deprecated-api.md`「IntelliJ Platform バージョンアップ時」の指示: 新規 deprecated 利用箇所の
修正はバージョンアップ PR に含め、後回しにしない）。

なお 2026.2 EAP への地ならしは既に完了している:

- `build.gradle.kts:43` — `pluginVerifier("1.405")`（2026.2 EAP の bundled-plugin レイアウトを解釈できる版）
- `build.gradle.kts:111` / `:665` — `PluginManager` の記述子探索 API が 2026.2 で内部化されたため、
  `plugin-version.properties` をビルド時生成して回避済み
- `build.gradle.kts:167` — 検証 IDE は `recommended()` に復帰済み

## 目的

1. `platformVersion` を `2026.2.0.1` に更新し、ビルド・テスト・Plugin Verifier をすべて green にする
2. `pluginSinceBuild` を `261.26222`（2026.1.4）に引き上げ、Java 25 バイトコードを読めない IDE を除外する
3. 2026.2 で利用可能になった代替 API へ移行し、`plugin-verifier-ignored-problems.txt` の保留エントリを削減する

## スコープ

### 対象

| # | 項目 | 内容 |
|---|------|------|
| R-1 | `platformVersion` の更新 | `2026.1.2` → `2026.2.0.1` |
| R-2 | `pluginSinceBuild` の引き上げ | `253.0` → `261.26222`（= 2026.1.4） |
| ~~R-3~~ | ~~LSP API の移行~~ | **スコープ外に変更**（下記「R-3 の切り出し」参照） |
| R-4 | `FloatingToolbarProvider` の移行 | `isApplicable(DataContext)` → `isApplicableAsync` |
| R-5 | `FileIncludeProvider` の移行 | `acceptFile(VirtualFile)` → `acceptFile(IndexedFile)` |
| R-6 | ignored-problems の棚卸し | R-3〜R-5 で不要になったエントリを削除し、残るものの `Reviewed` を更新 |
| R-7 | ドキュメント同期 | 対応 IDE バージョン表記を 2026.1.4+ / JDK 25+ に更新（後述の一覧） |

R-4 / R-5 は **2026.2 に代替 API が実在することを実機で確認してから確定する**とし、
セクション 1 の実測で両方の存在を確認したため実施する（tasklist の「セクション 3 の実施可否」参照）。

### R-3 の切り出し（2026-08-09 決定）

セクション 1 の実測により、当初 R-3 を必須とした根拠（design.md F-2: `LspServerManager` が
runtime で null になる）が **本プラグインには当てはまらない**ことが判明した。
`LspServerManager extends LspClientManager` であり `getInstance(Project)` は静的メソッドとして現存し、
旧 API は 2026.2 でも動作する。

一方で移行は単純な rename ではなくメソッド名の変更を伴い、13 ファイルに及ぶ。
ユーザー判断により、**本作業は「2026.2 でビルドでき、リリースできる状態」を最短で作ることに専念し**、
LSP API 移行は独立した後続作業に切り出す。

- 本作業のスコープ: R-1 / R-2 / R-4 / R-5 / R-6 / R-7
- 後続作業のスコープ: R-3（LSP API 移行 13 ファイル + EP 変更 + 実機スモークテスト）

後続作業は deprecated 一掃が目的であり、期限は「JetBrains が旧 API を削除する前」。
`plugin-verifier-ignored-problems.txt` の LSP エントリに追跡情報を残すこと。

### 対象外

- `pluginVersion` のバンプおよび Marketplace へのリリース（別作業。本作業は `main` マージまで）
- `template-versions-audit` の npm 脆弱性解消（別作業）
- `RescriptSwitchFileActionTest` のフレーキー対応（別作業）
- 2026.3 EAP への追随（`snapshots` に `263.*` はまだ 1 件も存在しない）

## sinceBuild の設定値

新しい LSP API（`LspIntegrationProvider` / `LspClientDescriptor` / `LspClientManager` 等）は
**2026.1.4 から安定版に入っている**（design.md の F-1 を参照）。したがって本作業の目的を達成するために
2026.2 を下限にする必要はなく、下限は 2026.1.4 とする。

JetBrains の build 番号は以下のとおり（`data.services.jetbrains.com` の IIU リリースフィードで確認）:

| バージョン | build | リリース日 |
|---|---|---|
| 2026.1 | `261.22158.277` | 2026-03-25 |
| 2026.1.1 | `261.23567.138` | 2026-04-23 |
| 2026.1.2 | `261.24374.151` | 2026-05-15 |
| 2026.1.3 | `261.25134.95` | 2026-06-04 |
| **2026.1.4** | **`261.26222.65`** | **2026-07-02** |
| 2026.2 | — | — |
| 2026.2.0.1 | `262.8665.337` | 2026-07-23 |

よって `pluginSinceBuild = 261.26222` とする。`261.4` のような表記は「branch 261 の build 4 以上」を意味し、
新 API を持たない 2026.1〜2026.1.3 まで通してしまうため誤りである。

### トレードオフ

失うもの: **2025.3 系（253.x）および 2026.1.0〜2026.1.3 の利用者は、これ以降の更新を受け取れなくなる**
（Marketplace 側で最後に互換だった `0.1.16.3` に据え置かれる）。ユーザーの明示的な判断としてこれを受け入れる。

得られる対価:

- 新 LSP API を `@Suppress` や条件分岐なしで直接使える
- ignored-problems の「compile target に代替 API が無い」を理由とする保留エントリを一掃できる
- 2026.1 系の利用者は 2026.1.4 に上げるだけで追随できる（メジャー更新を強制しない）

## 受け入れ条件

- [x] AC-1: `gradle.properties` の `platformVersion` が `2026.2.0.1`、`pluginSinceBuild` が `261.26222` である
- [x] AC-2: `./gradlew ktlintCheck` が成功する
- [x] AC-3: `./gradlew clean buildPlugin` が成功。ソース警告は bump 前 6 件 → **4 件**に減少
      （FloatingToolbar の deprecated override と FileInclude の OVERRIDE_DEPRECATION が解消）。
      残る 4 件はいずれも本作業以前から存在する既存警告。`--no-build-cache` で再コンパイルして実測した
      （build cache から復元されると警告が再出力されないため、通常の clean build では測れない）
- [x] AC-4: `./gradlew test` が全件成功する — **4464 tests / failures 0 / errors 0 / skipped 15**。
      作業中は Windows 固有の 8 件が失敗し続けたが、`origin/main` 取り込みで並列セッションの
      POSIX 前提修正が入り解消した（tasklist セクション 5 の記録を参照）
- [x] AC-5: `./gradlew verifyPlugin` が成功し、検証対象の全 IDE で `Compatible` である。
      `deprecated-usages.txt` は 261 系で **35 件**（LSP のみ）、262 系で **36 件**（LSP 35 + `FileIncludeProvider.acceptFile` 1）。
      `FloatingToolbarProvider.isApplicable` の 1 件はセクション 3 で解消。
      `FileIncludeProvider.acceptFile(VirtualFile)` は 2026.1.x で abstract のため残さざるを得ない（tasklist のセクション 3 の記録を参照）
- [x] AC-6: `plugin-verifier-ignored-problems.txt` の全エントリについて、KEEP の理由が 2026.2 時点で正しく、
      `Reviewed` が本作業日に更新されている。LSP エントリには後続作業への追跡情報が記載されている
- [ ] AC-7: **未達**。LSP 機能（補完・診断・定義ジャンプ・ホバー）の実機確認は未実施。
      R-3 を切り出したため LSP のコードは変更していないが、プラットフォーム側の変更で壊れていないことの
      確認としては依然として有効な検証である。理由と残存リスクは
      tasklist「AC-7（実機スモークテスト）は未実施」を参照
- [x] AC-8: 下表のドキュメントの IDE バージョン表記がすべて更新されている（`docs-lint` で FAIL 0 を確認）

### AC-8 の更新対象

| ファイル | 箇所 |
|---|---|
| `README.md` | L189 `IntelliJ IDEA 2025.3+` |
| `CLAUDE.md` | L13 対象プラットフォーム |
| `docs/architecture.md` | L12 SDK バージョン表 / L62 最低 IDE バージョン |
| `docs/versions.md` | L10 対象 IDE バージョン（下限） |
| `sphinx-docs/dev/building.md` | L67 `pluginSinceBuild` の例示 |
| `sphinx-docs/dev/contributing.md` | L15 JDK 要件 / L17 IntelliJ IDEA |
| `sphinx-docs/dev/setup.md` | L14 IntelliJ IDEA |
| `sphinx-docs/user/faq.md` | L12 対応 IDE の説明 |
| `sphinx-docs/user/installation.md` | L11 前提要件 |
| `sphinx-docs/user/version-matrix.md` | L15 バージョン対応表に新行を追加 |
| `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` | 上記 sphinx 変更に対応する `msgstr`（同一コミット内） |

`docs/functional-design.md:637` と `docs/performance-validation.md:90` は過去の実測記録・LSP API の
沿革説明であり、当時の事実として保持する（更新対象外）。

## リスク

| リスク | 影響 | 緩和策 |
|---|---|---|
| LSP API 移行が広範（13 ファイル） | 移行漏れで LSP 機能が無言で停止する | AC-7 の実機スモークテストを必須とする。tasklist を機能単位に分割し、緑のセクションから刻む |
| `LspClientDescriptor` の API 形状が想定と異なる | 設計のやり直し | セクション 1（bump のみ）を先に緑にし、実 API を確認してから design を確定する |
| sinceBuild 引き上げで既存ユーザーが取り残される | 253.x および 2026.1.0〜2026.1.3 の利用者が更新を受け取れない | ユーザー承認済み。リリースノートで明示する（別作業のリリース時） |
| `recommended()` が解決する検証 IDE が変わる | 回帰の検出力低下 | Verifier レポートの対象 IDE 一覧を確認し、2026.1.4 と 2026.2 系の双方が含まれることを記録する |
| `261.26222` を通す 2026.1.4 で 262 の新 API を誤用する | 2026.1.4 利用者で `NoSuchMethodError` | Verifier が 2026.1.4 を検証対象に含むことを確認し、セクション 4 完了後の verifyPlugin で担保する |

## 参照

- Issue #54 — IntelliJ Platform: upstream updates available
- `.claude/rules/deprecated-api.md` — 「IntelliJ Platform バージョンアップ時」
- `.claude/rules/documentation.md` — ドキュメント同期および `.po` 同時更新
- `plugin-verifier-ignored-problems.txt` — 保留エントリの根拠と再評価条件
