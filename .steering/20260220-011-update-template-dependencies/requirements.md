# Requirements: Update Project Template Dependencies

## 概要

Project Wizard の 12 テンプレートで使用されるライブラリのバージョンが古くなっている。特に React、Next.js、Vite、Electron 等に重大な脆弱性（CVE）が報告されており、最新安定版に更新する必要がある。

## 背景

テンプレートは `package.json` のバージョン範囲を生成するが、以下のケースでは `^` 記法でも修正版に到達しない:

- **pre-1.0 semver** (`^0.x.y` = `>=0.x.y <0.(x+1).0`): `esbuild`, `react-native`, `@rescript/react`
- **メジャーロック** (`^N.0.0` = `>=N.0.0 <(N+1).0.0`): `electron`, `expo`, `wrangler`

## 更新対象

### 必須更新（セキュリティ脆弱性あり）

| パッケージ | 現在 | 更新先 | 理由 |
|-----------|------|--------|------|
| `react` | `^19.0.0` | `^19.0.4` | CVE-2025-55182 (RCE, CVSS 10.0) の修正最低版 |
| `react-dom` | `^19.0.0` | `^19.0.4` | 同上 |
| `next` | `^15.0.0` | `^15.0.7` | CVE-2025-66478 の修正最低版 |
| `vite` | `^6.0.0` | `^7.0.0` | CVE-2025-31125 (CISA KEV)、最新メジャーに更新 |
| `electron` | `^33.0.0` | `^35.0.0` | v33 EOL、Chromium 脆弱性多数 |
| `react-native` | `^0.76.0` | `^0.78.0` | CVE-2025-11953 (RCE)、semver で修正版に到達不可 |
| `esbuild` | `^0.24.0` | `^0.25.0` | GHSA-67mh ソースコード漏洩、semver で修正版に到達不可 |

### 推奨更新（サポート期限・最新化）

| パッケージ | 現在 | 更新先 | 理由 |
|-----------|------|--------|------|
| `expo` | `^52.0.0` | `^53.0.0` | SDK 52 → 53 で React RSC パッチ含む |
| `wrangler` | `^3.0.0` | `^4.0.0` | v3 バグ修正サポート Q1 2026 終了 |
| `@vitejs/plugin-react` | `^4.0.0` | `^5.0.0` | Vite 7 互換版 |

### 更新不要（脆弱性なし、^ で最新に到達可能）

- `rescript ^12.0.0`, `@rescript/core ^1.0.0`, `@rescript/react ^0.14.0`
- `hono ^4.0.0`, `@hono/node-server ^1.0.0`
- `concurrently ^9.0.0`

## 影響テンプレート

| テンプレート | 更新パッケージ |
|-------------|--------------|
| Basic | なし |
| Vite + React | react, react-dom, vite, @vitejs/plugin-react |
| Next.js | react, react-dom, next |
| Electron | react, react-dom, vite, @vitejs/plugin-react, electron |
| React Native | react, react-native, expo |
| Hono | なし |
| Cloudflare Workers | wrangler |
| AWS Lambda | esbuild |
| Google Cloud Run | なし |
| CLI Tool | なし |
| npm Library | なし |
| Monorepo | react, react-dom, vite, @vitejs/plugin-react, hono 系 |

## 受け入れ条件

1. 上記の必須更新と推奨更新がすべてのテンプレートファイルに反映されている
2. `./gradlew buildPlugin` が成功する
3. テンプレートの生成コード（JSX、設定ファイル等）が更新後のバージョンと互換性がある
4. 単体テストが追加され、各テンプレートが正しいバージョン番号を生成することを検証する

## 制約事項

- テンプレートのコード（ReScript ソース、設定ファイル構造等）は原則変更しない。API 互換性がない場合のみ最小限の変更を行う
- メジャーバージョンのジャンプは最新安定版ではなく、テンプレートコードとの互換性を優先して選定する
