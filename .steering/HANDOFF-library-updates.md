# ライブラリ更新 引き継ぎ

2026-04-17 時点の状態。各項目は独立して着手可能。各 prompt は自己完結しており、貼り付ければすぐ作業開始できる。

共通事項:
- 規約: `CLAUDE.md` を必ず先頭で再読込。`definition-of-done.md` / `steering-workflow.md` / `git-conventions.md` に従う
- worktree 運用: コード変更のある項目は `EnterWorktree` で隔離すること（ドキュメントのみは不要）
- 現在 `main` は `origin/main` より 15 コミット先行。未プッシュ。
- ステアリング命名: `.steering/20YYMMDD-NNN-<スラッグ>/`（既存の最新番号を確認して +1 採番）

---

## P1: IntelliJ Platform 2026.1 バンプ（解除されたら即）

### 前提チェック（これが通らなければ着手禁止）

```bash
curl -sf https://repo1.maven.org/maven2/org/jetbrains/intellij/plugins/verifier-cli/maven-metadata.xml | grep -E '<version>1\.(40[3-9]|4[1-9][0-9]|[5-9][0-9]{2})</version>' | tail -1
```

上が何か出力すれば 1.403+ が公開されている。空なら **まだ 1.402 のみ** なので作業しない。

### Prompt

```
IntelliJ Platform 2026.1 へのバンプを実行してください。memory の
project_platform_2026_1_blocked.md が示す通り、Plugin Verifier
1.402 が 2026.1 の split-jar レイアウトを解析できず verifyPlugin
が失敗する問題で 2026-04-17 に保留していました。

まず前提チェック:
  curl -sf https://repo1.maven.org/maven2/org/jetbrains/intellij/plugins/verifier-cli/maven-metadata.xml \
    | grep -E '<version>1\.(40[3-9]|4[1-9][0-9])</version>' | tail -1
1.403 以降が見えれば作業続行。見えなければここで中断してユーザーに
通知してください。

ステアリング: .steering/YYYYMMDD-NNN-platform-2026-1-bump/ を作成し
requirements/design/tasklist を承認経由で進める。worktree で実施。

変更内容:
  - gradle.properties: platformVersion 2025.3.2 → 2026.1
  - gradle.properties: pluginSinceBuild 253.0 → 261.0
  - build.gradle.kts: pluginVerifier() が 1.403+ を拾うか確認。
    拾わなければ pluginVerifier("1.403") のように明示指定
  - .claude/rules/deprecated-api.md の強制指示に従い、
    ./gradlew verifyPlugin 実行後のレポートを確認:
      build/reports/pluginVerifier/*/plugins/com.rescript.plugin/*/deprecated-usages.txt
    新規 deprecated があれば同 PR で修正し、
    plugin-verifier-ignored-problems.txt を整理
  - compileKotlin 警告 (RescriptLsp4jClient.kt:104 の parameter
    'object' ミスマッチ) が 2026.1 で実害になるか確認

検証: ktlintCheck / clean buildPlugin / test / verifyPluginStructure
/ verifyPlugin のすべてが緑であること。CI (ci.yml, release.yml) は
verifyPlugin を要求するため、ローカルでパスが必須。

コミット分割:
  - ⬆ Bump IntelliJ Platform to 2026.1
  - （必要なら）♻️ Fix deprecated API usages for 2026.1
```

---

## P2: Template zod 4 + @hono/zod-openapi 1 ペア移行（MAJOR）

zod v4 は API 大幅変更。@hono/zod-openapi v1 は zod v4 必須。単独移行不可。

### Prompt

```
Template の zod を v3 → v4、@hono/zod-openapi を v0.18 → v1.x に
ペアで移行してください。両者は連動しており、片方だけ更新すると型
エラーで生成プロジェクトの pnpm install が壊れます。

ステアリング: .steering/YYYYMMDD-NNN-template-zod4/ を作成し
worktree で実施。

対象:
  - src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateVersions.kt
      ZOD: "^3.23.0" → "^4.0.0"（要 npm で最新 4.x を調べて floor 設定）
      HONO_ZOD_OPENAPI: "^0.18.0" → "^1.3.0"（同上）
  - 影響テンプレート（grep "zod" src/main/kotlin/com/rescript/plugin/wizard/templates/ と
    grep "zod" src/main/resources/templates/）:
      HonoGraphqlTemplateFiles.kt, HonoTemplateFiles.kt, FullStackTemplateFiles.kt 等
  - zod v3 → v4 の breaking changes に合わせて
    src/main/resources/templates/ 下の .res サンプルコードを修正
    （例: z.string().email() の API 変更、z.infer 挙動変更など）

検証:
  1. ./gradlew test --tests "com.rescript.plugin.wizard.templates.*"
     （単体テスト、数秒）
  2. ./gradlew integrationTest（Node + pnpm 必須、~30分。
     HonoGraphql/Hono/FullStack テンプレートが生成 → pnpm install →
     rescript build まで通ることを確認）

integrationTest 失敗時は生成 package.json のバージョン解決ログから
原因を特定。

コミット: ⬆ Migrate template zod to v4 with @hono/zod-openapi v1
```

---

## P3: Template vitest 4 + coverage-v8 4 移行（MAJOR）

vitest は 2 メジャー飛ばし（2→3→4）。config スキーマと API の両方に
breaking change あり。

### Prompt

```
Template の vitest を v2.1 → v4.x、@vitest/coverage-v8 も同時に
v4.x に移行してください。現状 2 メジャー分遅れています。

ステアリング: .steering/YYYYMMDD-NNN-template-vitest4/ を作成。

対象:
  - TemplateVersions.kt:
      VITEST: "^2.1.0" → "^4.x" (npm で最新4系を確認)
      VITEST_COVERAGE_V8: "^2.1.0" → "^4.x"
  - 使用テンプレート（各 package.json 生成関数と、同梱の
    vitest.config.* / vite.config.mjs）:
      ViteReactTemplateFiles, FullStackTemplateFiles, MonorepoTemplateFiles,
      NpmLibraryTemplateFiles, HonoTemplateFiles, HonoGraphqlTemplateFiles,
      ReactNativeCliTemplateFiles 他
  - 確認すべき breaking changes:
      - vitest v3: test globals デフォルト変更、inline snapshot API
      - vitest v4: browser mode 必須化の一部、coverage v8 連動
  - vite-plus は vitest 4 をサポートするか（pre-1.0 の vite-plus と
    の互換性確認）

検証:
  1. ./gradlew test --tests "com.rescript.plugin.wizard.templates.*"
  2. ./gradlew integrationTest（vitest を持つ全テンプレートで
     pnpm install → pnpm test が通ること）

コミット: ⬆ Migrate template vitest to v4
```

---

## P4: Template TypeScript 6 移行（MAJOR）

### Prompt

```
Template の TypeScript を v5.6 → v6.x に移行してください。

ステアリング: .steering/YYYYMMDD-NNN-template-typescript6/ を作成。

対象:
  - TemplateVersions.kt: TYPESCRIPT: "^5.6.0" → "^6.x"
  - TypeScript v6 の breaking changes:
      - moduleResolution デフォルト変更可能性
      - strict モードで追加の型チェック
  - tsconfig.json を同梱するテンプレート（grep "tsconfig" で特定）

検証:
  1. ./gradlew test --tests "com.rescript.plugin.wizard.templates.*"
  2. ./gradlew integrationTest
  3. TypeScript を直接使うテンプレート（.d.ts binding 生成や
     JS/TS → ReScript 変換は影響しないが、interop テンプレートは影響）

コミット: ⬆ Migrate template TypeScript to v6
```

---

## P5: Template Electron 41 + electron-builder 26（MAJOR ペア）

### Prompt

```
Template の Electron を v40 → v41、electron-builder を v25 → v26 に
同時移行してください。Electron は年次メジャーリリース。

ステアリング: .steering/YYYYMMDD-NNN-template-electron41/ を作成。

対象:
  - TemplateVersions.kt:
      ELECTRON: "^40.0.0" → "^41.x"
      ELECTRON_BUILDER: "^25.0.0" → "^26.x"
  - 影響: ElectronTemplateFiles.kt
  - Electron v41 の breaking changes: Chromium バージョン更新、
    一部 API 非推奨化（remote モジュール関連）
  - electron-builder v26: afterSign フック、macOS notarization 変更

検証:
  1. ./gradlew test --tests "*ElectronTemplate*"
  2. ./gradlew integrationTest（Electron テンプレート単独で OK。
     Node + pnpm + electron ビルド環境が必要）

コミット: ⬆ Migrate template Electron to v41
```

---

## P6: Template React Native CLI 20（MAJOR, 5 版飛び）

### Prompt

```
Template の @react-native-community/cli を v15 → v20 に移行して
ください。5 メジャー分遅れており、大きめの作業です。

ステアリング: .steering/YYYYMMDD-NNN-template-rn-cli-20/ を作成。

対象:
  - TemplateVersions.kt:
      RN_COMMUNITY_CLI: "^15.0.0" → "^20.x"
      RN_METRO_CONFIG / RN_BABEL_PRESET: "^0.81.0" → "^0.85.x"
      （React Native 0.85 系に揃える）
  - 影響: ReactNativeCliTemplateFiles.kt
  - 同梱リソース: src/main/resources/templates/react-native-cli/
      - metro.config.js, babel.config.js が v20 の新仕様を要求する
        可能性あり
  - React Native は 0.85 系が現行。react-native パッケージも必要に
    応じて floor を上げる

検証:
  1. ./gradlew test --tests "*ReactNative*"
  2. ./gradlew integrationTest（RN CLI テンプレートで pnpm install
     → rescript build まで。ネイティブビルドは対象外）

コミット: ⬆ Migrate template React Native CLI to v20
```

---

## P7: Template @types/node 25（MAJOR, Node engine 見直し）

### Prompt

```
Template の @types/node を v22 → v25 に移行してください。ついでに
NODE_ENGINE (>=20) の妥当性も再評価。Node 22 LTS / 24 LTS が現行。

ステアリング: .steering/YYYYMMDD-NNN-template-types-node-25/ を作成。

対象:
  - TemplateVersions.kt:
      NODE_TYPES: "^22.0.0" → "^25.0.0" （npm で最新を確認）
      NODE_ENGINE を ">=20" → ">=22" への引き上げを検討
      NODE_MAJOR を "20" → "22" （引き上げる場合 test が検証）
  - Node 22 は 2024-10 に active LTS、24 は 2025-10 に active LTS
  - integrationTest が走る GitHub Actions の Node バージョンも確認
    （.github/workflows/integration-tests.yml は node-version: 20 だが
     これを 22 に揃えるかは別判断）

検証:
  1. ./gradlew test --tests "*TemplateVersions*"（NODE_MAJOR と
     NODE_ENGINE の整合性アサートあり）
  2. ./gradlew integrationTest

コミット: ⬆ Bump template @types/node to v25 (and Node engine floor)
```

---

## P8: Template packageManager pnpm 10 / npm 11（MAJOR）

### Prompt

```
Template の Corepack packageManager フィールドを pnpm 9.12→10.x、
npm 10.9→11.x に更新してください。

ステアリング: .steering/YYYYMMDD-NNN-template-package-managers/ を作成。

対象:
  - TemplateVersions.kt:
      PNPM: "9.12.0" → "10.x" (bare semver, TemplateVersionsTest で
        検証される形式)
      NPM: "10.9.0" → "11.x"
      YARN: "4.5.0" は Berry 4 系で現行維持
  - pnpm v10 breaking changes: lockfile v9 フォーマット、strict peer
    deps 挙動変更
  - npm v11: engine 要件 Node 20+
  - .github/workflows/integration-tests.yml で pnpm/action-setup@v6
    の version も同期（現在 9）:
      version: 9 → version: 10

検証:
  1. ./gradlew test --tests "*TemplateVersions*"（bare semver アサート）
  2. ./gradlew integrationTest（各テンプレートの packageManager
     フィールドで pnpm 10 / npm 11 が使われても pnpm install が通ること）

コミット: ⬆ Bump template packageManager to pnpm 10 and npm 11
```

---

## P9: Template 0.x floor bumps（まとめ作業 可）

semver caret は 0.x でマイナーを跨がないため「実質 MAJOR」。ただし
個別リスクは小〜中程度なので、まとめて 1 ステアリング・個別コミット
が現実的。

### Prompt

```
Template の 0.x パッケージ floor を実 MAJOR として一括更新して
ください。caret の性質上、0.A.B → 0.A'.B' は breaking 扱い。

ステアリング: .steering/YYYYMMDD-NNN-template-0x-bumps/ を作成。
全体の breaking 影響を requirements.md で整理してから個別コミット。

対象（TemplateVersions.kt）:
  - RESCRIPT_REACT: "^0.14.0" → "^0.15.0"（@rescript/react 0.15）
  - DRIZZLE_ORM: "^0.36.0" → "^0.45.0"（9 マイナー飛び）
  - DRIZZLE_KIT: "^0.28.0" → "^0.31.0"
  - LIBSQL_CLIENT: "^0.14.0" → "^0.17.0"
  - RN_METRO_CONFIG: "^0.81.0" → "^0.85.0"
  - RN_BABEL_PRESET: "^0.81.0" → "^0.85.0"
  - ESBUILD: "^0.27.0" → "^0.28.0"

優先度: drizzle-orm / kit はスキーマ定義 API に breaking change が
多いので慎重に。実際の src/main/resources/templates/*/drizzle.config.ts
や Db.res サンプルへの影響を確認。

検証:
  1. ./gradlew test --tests "com.rescript.plugin.wizard.templates.*"
  2. ./gradlew integrationTest（影響する全テンプレート）

コミット分割（独立機能ごとに分ける）:
  - ⬆ Bump @rescript/react to 0.15
  - ⬆ Bump drizzle-orm/drizzle-kit floor
  - ⬆ Bump @libsql/client to 0.17
  - ⬆ Bump @react-native/metro-config and babel-preset to 0.85
  - ⬆ Bump esbuild to 0.28
```

---

## P10: sphinx-docs dev deps の lower bound 更新（軽微, 任意）

CI 健全化。ceiling でなく floor の引き上げなので低リスクだが、実装
は単純なので他の作業に含めても良い。

### Prompt

```
sphinx-docs の dev dependency lower bounds を現行に揃えてください。
CI が古い版で通ってしまうのを防ぐ目的。

軽微な修正の例外に該当するため、.steering/ 不要。main 直コミット可。

対象 sphinx-docs/pyproject.toml [dependency-groups].dev:
  ruff:            >=0.9  → >=0.15
  mypy:            >=1.13 → >=1.20
  pytest:          >=8.0  → >=9.0    (major)
  pytest-cov:      >=6.0  → >=7.0    (major)
  markdown-it-py:  >=3.0  → >=4.0    (major)
  translate-toolkit: >=3.13 → >=3.19

検証: cd sphinx-docs && uv sync && make build-all && make check-po

コミット: ⬆ Raise sphinx-docs dev dependency floors
```

---

## 今セッションで確定した事実（再調査不要）

| パッケージ | 最新 | caret で現floor が covers? |
|---|---|---|
| Dokka | 2.2.0 | 適用済 |
| IntelliJ Platform Gradle Plugin | 2.14.0 | 適用済 |
| Kotlin | 2.3.20 | 現行 |
| ktlint | 1.8.0 | 現行 |
| Kover | 0.9.8 | 現行 |
| Gradle wrapper | 9.4.1 | 現行 |
| JUnit Jupiter / Platform | 6.0.3 | 現行 |
| Mockito | 5.23.0 | 現行 |
| Remote Robot | 0.11.23 | 現行 |

未確認だった MAJOR の最新値はセッション時に再取得（LLM cutoff で
古い可能性あり）。
