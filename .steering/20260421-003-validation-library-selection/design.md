# Design — Wizard Validation Library Selection (zod / sury)

## 全体

`PackageManager` 選択の実装経路をそのまま踏襲し、新オプション `ValidationLibrary` を並べる。

```
[Wizard Step UI]
   ├─ Package manager: [NPM | PNPM | YARN]
   └─ Validation library: [zod | sury]    ← 追加
       ↓
[RescriptModuleBuilder]
   var packageManager: PackageManager
   var validationLibrary: ValidationLibrary  ← 追加
       ↓
[TemplateContext(projectName, packageManager, validationLibrary)]
       ↓
[<Name>TemplateFiles.generate(ctx)] ← 分岐ロジック
       ↓
[生成ファイル群]
   - package.json: zod または sury 依存
   - src/Schema.res: 選択ライブラリの API
   - 呼び出し側 (Server.res 等): 無変更
```

## コンポーネント

### `ValidationLibrary` enum

```kotlin
enum class ValidationLibrary(val displayName: String, val npmPackage: String) {
    ZOD("zod", "zod"),
    SURY("sury", "sury"),
    ;

    fun variantKey(): String = name.lowercase()  // "zod" | "sury"
}
```

### Wizard UI 追加

`RescriptProjectWizardStep.kt` 既存 ComboBox 配置の直下に 1 行。`JLabel("Validation library:")` + `JComboBox(ValidationLibrary.entries.toTypedArray())`。`updateDataModel()` で `builder.validationLibrary = combo.selectedItem`。

### TemplateContext

```kotlin
data class TemplateContext(
    val projectName: String,
    val packageManager: PackageManager,
    val validationLibrary: ValidationLibrary = ValidationLibrary.ZOD,
)
```

デフォルト引数により既存コール箇所は無改修で動く。

### テンプレート分岐

**依存ライブラリ**:
```kotlin
val validationDep = when (ctx.validationLibrary) {
    ValidationLibrary.ZOD -> "zod" to TemplateVersions.ZOD
    ValidationLibrary.SURY -> "sury" to TemplateVersions.SURY
}
```

**リソース**:
```
src/main/resources/templates/<name>/
├── src/                             # 共通ファイル（Server.res 等）
└── variants/
    ├── zod/src/Schema.res           # zod 固有
    └── sury/src/Schema.res          # sury 固有
```

`generate()` 内:
```kotlin
"src/Schema.res" to TemplateResourceLoader.load(
    "$RESOURCE_ROOT/variants/${ctx.validationLibrary.variantKey()}/src/Schema.res",
    vars
)
```

## Schema.res 公開 API 統一

どちらのバリアントも以下の形に揃える（例: greet エンドポイント）:

```rescript
// result を返す純関数。呼び出し側で一貫したエラーハンドリングが可能
let parseGreetInput: JSON.t => result<greetInput, string>
```

zod 版:
- `@module("zod")` 経由で JS 側の `z.object({ ... })` を ReScript から構築
- `safeParse` の結果を `result` に翻訳

sury 版:
- `Sury.S.object(s => { ... })` でスキーマ定義
- `S.parseOrThrow` を try/catch で `result` に翻訳

## Server.res 側

大半のテンプレートで `Server.res` などの呼び出し側は 1 本のまま。`Schema.parseGreetInput(body)` の戻りを `result` として扱い、`Error` 時は `ctx->Hono.status(400)->Hono.json({"error": msg})` のようなレスポンスを返す。

既存 hono テンプレートでは `c.req.valid("json")` 相当の zod/hono アダプタを使っているが、本プランでは「Schema モジュール経由で parse → 手動で 400 応答」に統一する（両ライブラリで同じフローにするため）。この統一に伴い、既存 hono でも Server.res の一部が変化するが、機能等価。デフォルト ZOD 時の byte-identical は hono は崩れる可能性があり、**AC-04 は hono / hono-graphql では現実的ではないため、AC-04 を「hono 系以外の既存 zod 部分は byte-identical、hono 系は『既存 zod API の範囲内で意図的に統一化』を許容」と読み替える**（steering レビュー時に明記）。

> 代替案: hono 系だけは既存の `c.req.valid` アダプタを残す。この場合、Server.res の分岐が複雑になる。統一 API（`parseXxx: JSON.t => result<_, _>`）を優先する方を採用。

## 実装順序

1. **Foundation**: enum + Wizard UI + Builder + Context（デフォルト ZOD、全テンプレート無変更）。ビルド pass。
2. **TemplateVersions**: `SURY` 追加のみ。
3. **Hono 系**: 既存 zod 実装を `variants/zod/` に移し、`variants/sury/` を新規追加。Server.res を統一 API に寄せる。
4. **残り 6 テンプレート**: 1 コミット/テンプレートで zod/sury 両バリアントを新規追加。
5. **Docs**: CLAUDE.md / repository-structure.md 更新。

## snapshot 検証

`TemplateSnapshotDumper` ベースの仕組みを再利用（既に worktree に 1 本存在）。各コミットで:

- ZOD snapshot（= 既存の生成物の延長）
- SURY snapshot（新規ビルド）

2 種類を diff して、ZOD 側は「既存との差分が該当テンプレートの意図変更分のみ」であること、SURY 側は「zod が sury に置換されている以外の差分がないこと」を確認。

## 非互換リスクチェック

- `TemplateContext` の data class に field を増やすが、デフォルト引数で既存呼び出しは互換。
- `RescriptModuleBuilder.validationLibrary` の永続化は Wizard セッション内のみ。`.idea/*.xml` の破壊なし。
- Plugin Verifier に deprecated API 追加なし。
