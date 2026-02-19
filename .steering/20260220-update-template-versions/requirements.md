# Requirements: プロジェクトテンプレートのバージョン更新

## 背景

プロジェクトウィザード（New Project > ReScript）で生成されるテンプレートが ReScript 11 ベースのまま古くなっている。ReScript 12 が安定版としてリリースされており、React も 19 が安定版となっているため、公式テンプレート（`create-rescript-app`）に準拠する形でバージョンを更新する。

## 変更内容

### rescript.json の更新

| 項目 | 現在 | 更新後 | 根拠 |
|------|------|--------|------|
| `package-type` | `"module"` | `package-specs` 形式に変更 | 公式テンプレート準拠 |
| `bs-dependencies` | `["@rescript/core"]` | 維持 | 公式テンプレートで引き続き使用 |
| `bsc-flags` | `["-open RescriptCore"]` | 維持 | 公式テンプレートで引き続き使用 |
| `suffix` | `".res.mjs"` | 維持 | 公式テンプレート準拠 |
| React 時の jsx | `{"version": 4}` | 維持 | 変更なし |

### package.json の更新

| パッケージ | 現在 | 更新後 | 根拠 |
|-----------|------|--------|------|
| `rescript` | `^11.0.0` | `^12.0.0` | 最新安定版 12.1.0 |
| `@rescript/core` | `^1.0.0` | **削除** | v12 でコンパイラに同梱済み |
| `react` | `^18.0.0` | `^19.0.0` | 最新安定版 19.2.4 |
| `react-dom` | `^18.0.0` | `^19.0.0` | 同上 |
| `@rescript/react` | `^0.13.0` | `^0.14.0` | 最新安定版 0.14.1 |

### scripts の更新

| 現在 | 更新後 | 根拠 |
|------|--------|------|
| `"build": "rescript build"` | `"res:build": "rescript"` | 公式テンプレート準拠 |
| `"clean": "rescript clean"` | `"res:clean": "rescript clean"` | 公式テンプレート準拠 |
| `"dev": "rescript build -w"` | `"res:dev": "rescript -w"` | 公式テンプレート準拠 |

### スターターファイルの更新

- React なし: `Console.log("Hello, ReScript!")` に簡略化（公式 basic テンプレート準拠）
- React あり: 変更なし（現在の内容で適切）

## 受け入れ条件

- [ ] `RescriptProjectGenerator.generateRescriptJson` が `package-specs` 形式を生成する
- [ ] `RescriptProjectGenerator.generatePackageJson` が更新されたバージョンを使用する
- [ ] `@rescript/core` が package.json の dependencies から削除される
- [ ] スクリプト名が公式テンプレートに準拠している
- [ ] 既存のユニットテストが更新され、すべてパスする
- [ ] ビルドが成功する

## 制約事項

- 公式 `create-rescript-app` テンプレートを信頼できるソースとして準拠する
- 既存の `RescriptModuleBuilder` の構造（`setupRootModel` のフロー）は変更しない
