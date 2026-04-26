# タスクリスト

## Phase A: advanced.md 修正

- [x] L421 "15 pre-configured" → "16 pre-configured"
- [x] テンプレート一覧表に Hono GraphQL / Full-Stack / res-x を追加
- [x] L505 Hono-based 列挙に Hono GraphQL を追加
- [x] 末尾に templates/index への参照リンク追加

## Phase B: 個別テンプレート詳細ページ作成（16 ファイル）

### Basic 系
- [x] basic.md
- [ ] npm-library.md (agent in flight)
- [ ] cli-tool.md (agent in flight)

### Frontend
- [ ] vite-react.md
- [ ] nextjs.md
- [ ] electron.md
- [ ] react-native.md
- [ ] react-native-cli.md

### Backend
- [ ] hono.md
- [ ] hono-graphql.md

### Serverless
- [ ] cloudflare-workers.md
- [ ] aws-lambda.md
- [ ] google-cloud-run.md

### Full-Stack
- [ ] monorepo.md
- [ ] full-stack.md
- [ ] res-x.md

## Phase C: index.md / カードリンク

- [x] templates/index.md の各 grid-item-card に `:link: <slug>` `:link-type: doc` を追加
- [x] toctree に 16 詳細ページを列挙
- [x] React Native (Community CLI) カードを新規追加（既存は Expo のみだったため）

## Phase D: 翻訳同期

- [ ] `cd sphinx-docs && uv sync` で Python 環境を整える
- [ ] `make gettext` で .pot 再生成
- [ ] `make update-po` で .po に同期
- [ ] 新規 `msgstr ""` を日本語で埋める（advanced.po + templates/*.po）
- [ ] `make build-ja` 成功確認

## Phase E: 検証

- [ ] `make build-all` 成功（警告ゼロが望ましい）
- [ ] 既存リンク切れがないか確認

## Phase F: コミット

- [ ] advanced.md fixes（小規模・1 commit）
- [ ] 16 詳細ページ + index.md toctree（doc コミット ✅ 1 本）
- [ ] 翻訳同期（doc コミット ✅ 同コミット可）

## Phase G: マージ確認

- [ ] tasklist.md を全 [x] にして最終 commit
- [ ] `AskUserQuestion` でマージ可否確認 → 承認後マージ
