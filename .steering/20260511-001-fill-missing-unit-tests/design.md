# 設計

## 方針

各テストは「対象クラスの shape を assert する小さなテスト」に留め、結合相手のロジックには立ち入らない。失敗した場合に「何が壊れたか」が一発で分かる粒度を維持する。

## 共通基盤

- 既存パターン踏襲: VirtualFile を要するクラスは `com.intellij.testFramework.LightVirtualFile` で代替。サブクラス化で `getPath` を上書きするテンプレートは `RescriptMigrationFinderTest` に既存。
- JUnit 5 (`org.junit.jupiter.api`) を使用。`assertEquals` / `assertTrue` / `assertNotEquals` / `assertSame` を中心に。
- KDoc は `code-comments.md` に従いクラスのみ（テストメソッドは省略可）。

## 各テストの観点

### 1. `RescriptInteropModelTest`
- `InteropKind.values()` が 5 件、各定数の名前と順序
- `RiskLevel.values()` が 3 件、HIGH → MEDIUM → LOW の順
- `InteropEntry` の data class equality と全プロパティ保持

### 2. `RescriptTypeImpactModelTest`
- `TypeTarget` の data class equality と name / localName / declarationFile / declarationOffset 保持
- `TypeRefKind.values()` が 5 件、定数名検証
- `ReferenceEntry` の data class equality

### 3. `RescriptMigrationModelTest`
- `MigrationCandidate` の data class equality と relativePath 保持
- `ConversionStatus.values()` が 2 件 (SUCCESS / FAILED)
- `ConversionResult` の data class equality と message 保持

### 4. `RescriptHoverTypeResolverTest`
- SAM lambda が `resolveAt` で値を返すこと
- `null` を返せること
- `forFile` で生成された resolver はオフ EDT で実行可能（ここでは LSP を呼ばないモック差し替えは不可 — `forFile` 自体は call-through なので、`forFile` 呼び出しが `RescriptHoverTypeResolver` 型を返すことの shape 確認のみで OK）

### 5. `RescriptConstructorOccurrenceTest`
- `ConstructorOccurrenceKind.values()` が 4 件、各定数名検証
- `RescriptConstructorOccurrence` の data class equality
- `TextRange` を持つフィールドの保持確認

### 6. `RescriptTypeAstTest`
- `UnitT` がシングルトン (`assertSame`)
- `Ctor("int")` の equality
- `TypeVar("a")` の equality
- `App("option", listOf(Ctor("int")))` のネスト equality
- `Tuple` / `Arrow` / `ReturnQuery` の構築

### 7. `RescriptTypeSignatureSearchHitTest`
- 全 6 プロパティの保持 + data class equality

### 8. `RescriptLanguageTest`
- `RescriptLanguage.isCaseSensitive()` が `true`
- `RescriptLanguage.id` が `"ReScript"`
- `RescriptLanguage` がシングルトン (`assertSame`)
- `readResolve` の戻り値が同一インスタンス（リフレクション経由でも OK だが、簡潔に Java の `private` メソッドを reflectively 呼ぶ）

### 9. `RescriptWorkspaceLayoutTest`
- `EMPTY.isRescriptProject()` が `false`
- 単一ルートで `isRescriptProject()` が `true`
- `nodeModulesDirs()` が `<root>/node_modules` を含む
- `lspBinCandidates()` が `node_modules/.bin/<LSP_BIN_NAME>` を含む
- `lspPackageDirs()` が `node_modules/@rescript/language-server` 相当を含む
- 複数ルートで結果のサイズが root 数と一致

## 留意

- `RescriptLanguage` は IntelliJ Platform の `Language` を継承するため、テストはクラスパスに platform jar があれば走る（既存テストで `LightVirtualFile` を使えているため OK）。Heavy fixture 不要。
- 新規プロダクションコードは追加しない（純テストのみ）。
- どのテストも 50 行以内に収まる見込み。

## ガードレール

- `@Suppress("DEPRECATION")` を新規追加しない
- `Thread.sleep` を含めない（純粋ロジックのみ）
- `Project` インスタンスを必要とするテストは含めない (IDE fixture 回避)
