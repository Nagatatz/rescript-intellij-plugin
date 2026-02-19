# Design: プロジェクトテンプレートのバージョン更新

## 変更対象ファイル

| ファイル | 変更内容 |
|---------|---------|
| `src/main/kotlin/.../wizard/RescriptProjectGenerator.kt` | テンプレート生成ロジックの更新 |
| `src/test/kotlin/.../wizard/RescriptProjectGeneratorTest.kt` | テストの更新 |

## 詳細設計

### 1. `generateRescriptJson` の変更

**Before:**
```json
{
  "name": "...",
  "sources": [{"dir": "src", "subdirs": true}],
  "package-type": "module",
  "suffix": ".res.mjs",
  "bs-dependencies": ["@rescript/core"],
  "bsc-flags": ["-open RescriptCore"]
}
```

**After:**
```json
{
  "name": "...",
  "sources": {
    "dir": "src",
    "subdirs": true
  },
  "package-specs": {
    "module": "esmodule",
    "in-source": true
  },
  "suffix": ".res.mjs",
  "bs-dependencies": ["@rescript/core"],
  "bsc-flags": ["-open RescriptCore"]
}
```

変更点:
- `"sources"` をオブジェクト形式に変更（公式 basic テンプレート準拠）
- `"package-type": "module"` → `"package-specs": {"module": "esmodule", "in-source": true}`

React 有効時は従来どおり `"@rescript/react"` を `bs-dependencies` に追加し、`"jsx": {"version": 4}` を含める。

### 2. `generatePackageJson` の変更

**Before:**
```json
{
  "name": "...",
  "version": "0.1.0",
  "scripts": {
    "build": "rescript build",
    "clean": "rescript clean",
    "dev": "rescript build -w"
  },
  "dependencies": {
    "rescript": "^11.0.0",
    "@rescript/core": "^1.0.0"
  }
}
```

**After:**
```json
{
  "name": "...",
  "version": "0.1.0",
  "scripts": {
    "res:build": "rescript",
    "res:clean": "rescript clean",
    "res:dev": "rescript -w"
  },
  "dependencies": {
    "rescript": "^12.0.0"
  }
}
```

React 有効時の dependencies:
```json
{
  "rescript": "^12.0.0",
  "react": "^19.0.0",
  "react-dom": "^19.0.0",
  "@rescript/react": "^0.14.0"
}
```

### 3. `generateStarterModule` の変更

**Before:**
```rescript
let greeting = "Hello, ReScript!"

Console.log(greeting)
```

**After:**
```rescript
Console.log("Hello, ReScript!")
```

公式 basic テンプレートの `Demo.res` に準拠。

### 4. `generateReactComponent` は変更なし

現在の出力で適切。

## テスト更新方針

`RescriptProjectGeneratorTest.kt` の以下のテストを更新:

- `generateRescriptJson includes package-type and suffix` → `package-specs` の検証に変更
- `generatePackageJson includes scripts` → 新しいスクリプト名の検証に変更
- `generatePackageJson includes rescript dependencies` → `@rescript/core` が含まれないことを検証
- `generateStarterModule produces valid ReScript` → 新しい簡略化された内容の検証に変更

## 影響範囲

- `RescriptModuleBuilder.kt` — 変更なし（`RescriptProjectGenerator` の戻り値をそのまま使用するため）
- `RescriptModuleBuilderTest.kt` — 変更なし
- `RescriptProjectWizardStep.kt` — 変更なし
