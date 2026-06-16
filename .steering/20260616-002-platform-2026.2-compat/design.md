# 設計: IntelliJ Platform 2026.2 互換性対応

承認済み方針: navbar は **Option A**（機能維持・`AbstractNavBarModelExtension` ベースに自前実装）。

## 1. navbar 移行（`navbar/RescriptStructureAwareNavbar.kt`）

### API 調査結果（2026.1.1 jar の javap / 262 jar の存在確認）

- 削除（262）: `StructureAwareNavBarModelExtension`（構造ビュー連携の便宜サブクラス、`intellij.platform.lang.impl.jar`）
- 存続（2026.1 / 262 双方）: `AbstractNavBarModelExtension`（`intellij.platform.ide.impl.jar`）と `NavBarModelExtension` インターフェース

`AbstractNavBarModelExtension`（2026.1.1）の継承者向け面:

```
abstract class AbstractNavBarModelExtension implements NavBarModelExtension {
  public abstract String getPresentableText(Object)   // ← 唯一の abstract
  public PsiElement adjustElement(PsiElement)          // default あり
  public PsiElement getParent(PsiElement)              // base 実装あり（override 可）
  public Collection<VirtualFile> additionalRoots(Project)
}
interface NavBarModelExtension {
  default Icon getIcon(Object)
  default PsiElement getLeafElement(DataMap)
  default boolean processChildren(Object, Object, Processor<Object>)
  default boolean normalizeChildren()
  ...
}
```

### 変更内容

基底クラスを差し替え、構造ビュー machinery（削除された `createModel` / `processStructureViewChildren` 等）は移植せず、**PSI 親ウォークで enclosing-declaration ブレッドクラムを再現する**。

```kotlin
// Before
class RescriptStructureAwareNavbar : StructureAwareNavBarModelExtension() {
    override val language: Language = RescriptLanguage
    override fun getPresentableText(obj: Any?): String? { ... }
    override fun getIcon(obj: Any?): Icon? { ... }
}

// After
class RescriptStructureAwareNavbar : AbstractNavBarModelExtension() {
    override fun getPresentableText(obj: Any?): String? {
        val element = obj as? PsiElement ?: return null
        if (element.language != RescriptLanguage) return null
        if (element.node?.elementType !in RescriptPsiUtils.NAVIGABLE_TYPES) return null
        return RescriptPsiUtils.extractName(element)
    }

    override fun getIcon(obj: Any?): Icon? {
        val element = obj as? PsiElement ?: return null
        if (element.language != RescriptLanguage) return null
        return RescriptPsiUtils.getIcon(element)
    }

    // Structure-aware breadcrumb via PSI parent walk: climb to the nearest
    // enclosing ReScript navigable declaration (let / type / module / ...).
    override fun getParent(psiElement: PsiElement): PsiElement? {
        if (psiElement.language != RescriptLanguage) return super.getParent(psiElement)
        var current = psiElement.parent
        while (current != null) {
            if (current.node?.elementType in RescriptPsiUtils.NAVIGABLE_TYPES) return current
            current = current.parent
        }
        return super.getParent(psiElement)
    }
}
```

設計判断:

- 旧 `language` プロパティ（`StructureAwareNavBarModelExtension` の abstract）は `AbstractNavBarModelExtension` に存在しないため削除し、代わりに各メソッド先頭で `element.language != RescriptLanguage` ガードを入れる（navbar の EP は全言語に対して呼ばれ得るため、ReScript 以外は早期 return / super 委譲する）。
- `getLeafElement` / `processChildren`（ドロップダウン子要素）は `NavBarModelExtension` の default 実装に委ねる。旧サブクラスは structure-view モデルから子を生成していたが、ReScript navbar の主目的（カーソル位置の囲み宣言の階層表示）は `getParent` チェーンで満たせる。
- クロスバージョン安全性: 参照 API は `AbstractNavBarModelExtension` / `getParent(PsiElement)` / `getIcon` / `getPresentableText` のみ。すべて 262 に存続するため、2026.1.2 でコンパイル → 262 で `invokevirtual` 解決可能。

### plugin.xml

クラス FQN（`com.rescript.plugin.navbar.RescriptStructureAwareNavbar`）不変のため `navBarModelExtension` の EP 登録は**変更不要**。

## 2. internal API 置換（`RescriptErrorReporter.kt:176-187`）

```kotlin
// Before
val pluginManager =
    com.intellij.ide.plugins.PluginManagerCore.getPlugin(
        com.intellij.openapi.extensions.PluginId.getId("com.rescript.plugin"),
    )
pluginManager?.version ?: "unknown"

// After
val descriptor =
    com.intellij.ide.plugins.PluginManager
        .getInstance()
        .findEnabledPlugin(
            com.intellij.openapi.extensions.PluginId.getId("com.rescript.plugin"),
        )
descriptor?.version ?: "unknown"
```

- `PluginManager.getInstance().findEnabledPlugin(PluginId): IdeaPluginDescriptor?` は public API。返り値型・null 時 "unknown"・例外時 "unknown" の挙動を完全保持。
- 注記: 本環境では `PluginManager.class` の javap 確認ができなかった（`src/main/resources` 同様の FS 制約 + 262 が Java 25 bytecode）。実装ステップで `clean buildPlugin`（resolve 確認）+ `verifyPlugin`（262 で internal/unresolved が無いこと）で担保する。万一 `findEnabledPlugin` が解決しない場合のフォールバックは `PluginManagerCore.getPluginSet()` 系ではなく、`PluginManager.getPlugins()` を id で filter する public 経路に切替える。

## 3. build.gradle.kts（verifier 設定の正式採用）

実験コメントを正式コメントに置換:

```kotlin
pluginVerifier("1.405")   // 1.403 → 1.405: parses the 2026.2 EAP layout
...
pluginVerification {
    ides {
        // verifier-cli 1.405 resolves the 2026.2 EAP bundled-plugin layout
        // that 1.403 failed on (ClosedFileSystemException). recommended()
        // now safely includes 2026.2 EAP alongside 2025.3 / 2026.1.
        recommended()
    }
    ...
}
```

## 4. テスト方針

### navbar — テスト作成（必須）

新規 `src/test/kotlin/com/rescript/plugin/navbar/RescriptStructureAwareNavbarTest.kt`。
`getParent` / `getPresentableText` / `getIcon` はほぼ純 PSI 関数のため light fixture（`RescriptTestUtils`）でテスト可能:

- ネストした `module M { let f = ... }` を fixture で生成し、内側要素から `getParent` が enclosing 宣言要素を返すこと
- NAVIGABLE_TYPES 外の要素では `getPresentableText` が null を返すこと
- ReScript 以外（`element.language != RescriptLanguage`）の要素で getParent が `super` に委譲（= ReScript ロジックを適用しない）こと
- `getPresentableText` が宣言名を返すこと

`AbstractNavBarModelExtension()` のインスタンス化が IDE サービスに依存する場合は、対象メソッドのロジックを呼べる範囲でテストする（getParent は PSI のみ依存のため呼べる見込み）。依存で不可なメソッドがあれば tasklist に免除理由を明記。

### internal API — テスト免除

`RescriptErrorReporter.pluginVersion()` は `private`、かつ IDE の plugin descriptor サービスに依存（light fixture で自プラグインの descriptor を取得困難）。`testing.md` 免除「IDE ライフサイクル依存 / IDE 結合必須」に該当。挙動不変の単純 API 置換であり、既存テスト green + `verifyPlugin` で担保。tasklist に免除理由を明記。

## 5. 検証順序とリスク

各セクション実装後に `./gradlew verifyPlugin` を再実行し、IU-262 レポートの該当問題が消えたことを確認する（インクリメンタルに緑へ寄せる）。

- リスク（低）: 262 で `AbstractNavBarModelExtension` のメソッドシグネチャが微変している可能性（javap で 262 を直接確認できず）。→ `verifyPlugin` が検出するため iterative に対応可能。基底クラス自体は存続が確認済みで低リスク。
- リスク（低）: `findEnabledPlugin` の resolve（上記フォールバックあり）。
- 機能リグレッション: navbar ドロップダウンの子要素表示が default 実装依存になるため、旧 structure-view ベースと挙動が異なる可能性。主機能（囲み宣言ブレッドクラム）は維持。`runIde` で目視確認を tasklist の任意項目に含める。
