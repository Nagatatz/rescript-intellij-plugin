# Design — strict tsconfig の 4 テンプレート展開

## 共通方針

- 各テンプレートの `tsconfig.json` は `src/main/resources/templates/<name>/tsconfig.json` に静的ファイルとして配置
- `*TemplateFiles.kt` は `TemplateResourceLoader.load("<name>/tsconfig.json")` で読み込むだけ
- テンプレート間で共通するベース設定は重複させる（テンプレート 4 つ分なので DRY 化のコストが割に合わない）
- 全テンプレートで `"strict": true` を明示。`"noImplicitAny": true` は strict に含まれるが、後続リーダーの明確さのために明示することもある

## テンプレートごとの内容

### 1. Next.js

Next.js 16 の標準 tsconfig を踏襲しつつ、`strict: true` + `noUncheckedIndexedAccess: true` を追加。

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["dom", "dom.iterable", "esnext"],
    "allowJs": true,
    "skipLibCheck": true,
    "strict": true,
    "noUncheckedIndexedAccess": true,
    "noImplicitAny": true,
    "noEmit": true,
    "esModuleInterop": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "incremental": true,
    "plugins": [{ "name": "next" }],
    "paths": { "@/*": ["./*"] }
  },
  "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
  "exclude": ["node_modules"]
}
```

**既存コードへの影響**: `app/page.tsx`, `app/greet/GreetForm.tsx`, `app/api/greet/route.ts` を strict で通す必要がある。ReScript から genType 経由で来る関数は実装済みのため、既存コードで strict エラーが出る可能性は低い。出た場合はその場で修正する。

### 2. npm Library

ライブラリ本体は ReScript + genType で書かれており、TypeScript コードは `src/Index.gen.d.ts`（生成物）のみ。tsconfig はツールチェーン整合性のために置く（`tsc --noEmit` で型定義の正当性を確認できる）。

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "esnext",
    "moduleResolution": "bundler",
    "lib": ["ES2022"],
    "strict": true,
    "noImplicitAny": true,
    "declaration": true,
    "declarationMap": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "allowJs": true,
    "isolatedModules": true,
    "noEmit": true
  },
  "include": ["src/**/*.ts", "src/**/*.tsx"],
  "exclude": ["node_modules"]
}
```

### 3. React Native (Expo)

Expo の推奨は `expo/tsconfig.base` を extends する形式。Expo 55 は `tsconfig.json` がプロジェクトルートに必要。

```json
{
  "extends": "expo/tsconfig.base",
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true
  },
  "include": ["**/*.ts", "**/*.tsx", ".expo/types/**/*.ts", "expo-env.d.ts"]
}
```

**devDependencies 追加**: `typescript` と `@types/react` が必要。

### 4. React Native (CLI)

Community CLI テンプレートは `@react-native/typescript-config` を extends する慣習（最近の React Native プロジェクトで標準）。

```json
{
  "extends": "@react-native/typescript-config/tsconfig.json",
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true
  },
  "include": ["**/*.ts", "**/*.tsx"],
  "exclude": ["node_modules"]
}
```

**devDependencies 追加**: `typescript` と `@react-native/typescript-config` が必要。

## devDependencies 更新

それぞれのテンプレートで、以下を `package.json` の `devDependencies` に追加:

| テンプレート | 追加依存 |
|---|---|
| Next.js | `typescript`, `@types/react`, `@types/react-dom`, `@types/node` |
| npm Library | 既に `typescript` あり。変更不要 |
| React Native (Expo) | `typescript`, `@types/react` |
| React Native (CLI) | `typescript`, `@react-native/typescript-config`, `@types/react` |

バージョン指定は既存の `TemplateVersions.TYPESCRIPT` を活用。`@types/*` 系は `TemplateVersions` に新規追加する。

## テスト戦略

各テンプレートテストに以下のアサーションを追加:

```kotlin
@Test
fun `ships strict tsconfig`() {
    val files = NextjsTemplateFiles.generate(ctx)
    val tsconfig = files["tsconfig.json"]!!
    assertTrue(tsconfig.contains("\"strict\": true"))
    assertTrue(tsconfig.contains("\"noImplicitAny\": true"))
}
```

## ロールアウト順序

1. Next.js tsconfig（最も影響が大きいテンプレート、Next.js 16 の規範化）
2. npm Library tsconfig（生成物の整合性確認のため軽量）
3. React Native (Expo) tsconfig
4. React Native (CLI) tsconfig

## リスク

| リスク | 緩和策 |
|---|---|
| Next.js が自動生成する tsconfig と衝突 | 静的配置することで既存の自動生成を抑制する。Next.js は既存の tsconfig を尊重する |
| `@react-native/typescript-config` のバージョン管理 | `TemplateVersions` に `RN_TYPESCRIPT_CONFIG` を追加 |
| `expo/tsconfig.base` が Expo 55 に含まれる保証 | Expo 50+ で `expo` パッケージに同梱されている（実績あり） |
| strict モードで既存 `.tsx` に型エラーが出る | 生成物はシンプルな React コンポーネントで、strict で通るよう手書きする |
