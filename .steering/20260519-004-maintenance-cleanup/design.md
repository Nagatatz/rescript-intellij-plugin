# Maintenance Cleanup — 設計

## P1: Gradle wrapper

```diff
-distributionUrl=https\://services.gradle.org/distributions/gradle-9.5.0-bin.zip
+distributionUrl=https\://services.gradle.org/distributions/gradle-9.5.1-bin.zip
```

Gradle Wrapper の自己更新は `./gradlew wrapper --gradle-version 9.5.1` で行えるが、distributionUrl の手動編集で十分 (SHA は自動 fetch)。

## P2: IntelliJ Platform

```diff
- platformVersion = 2026.1.1
+ platformVersion = 2026.1.2
```

```diff
-            create(IntelliJPlatformType.IntellijIdea, "2026.1.1")
+            create(IntelliJPlatformType.IntellijIdea, "2026.1.2")
```

検証: `./gradlew verifyPlugin` を実行し、新規 deprecated 警告がないか確認。新規警告がある場合は `plugin-verifier-ignored-problems.txt` に追記または該当箇所のソース修正。

メモリ更新: `project_platform_2026_1_blocked.md` の現バージョンを 2026.1.2 に更新 (verifier-cli 1.404+ 待ちのブロッカーは継続中)。

## F21: ドキュメント補正

`docs/product-requirements.md` の 2 箇所:

```diff
- 既存の reasonml-idea-plugin がメンテナンス停止状態にあるため、ReScript 専用のクリーンな代替プラグインを提供する
+ 既存の reasonml-idea-plugin は 2025-09-01 の v0.131 リリース以降コードコミットがほぼ停止し低頻度メンテに移行しているため、活発に保守される ReScript 専用のクリーンな代替プラグインを提供する
```

```diff
- 1. **代替手段の不在** — 既存プラグインがメンテナンス停止状態で、最新の ReScript バージョンに対応していない
+ 1. **活発な代替手段の不在** — 既存プラグイン (reasonml-idea-plugin) は 2025-09 を最後にリリースが停滞し、最新の ReScript バージョンへの追従が遅い
```

検証: `grep -rn "メンテナンス停止" .` で CLAUDE.md や README.md に同表記が残っていないか確認。

## 技術負債 1: Alarm UnstableApiUsage

調査内容:
1. 2026.1.2 リリースノートで `Alarm` 周りの変更があるか確認
2. `IntelliJ Platform` の Kotlin 公式ガイド (https://plugins.jetbrains.com/docs/intellij/coroutine-read-actions.html) で `Alarm(POOLED_THREAD)` の推奨置換を確認
3. 結果に応じて以下のいずれか:
   - **代替が存在しない**: inline コメントの review date を更新 (`Reviewed: 2026-05-19`)
   - **代替が存在**: 移行 (e.g. coroutines via `Disposer.newDisposable()` + `CoroutineScope`)

予想される結果: 2026.1.x では `Alarm` 自体は非 deprecated だが `ThreadToUse.POOLED_THREAD` 列挙値が `@ApiStatus.Internal`。代替の coroutines 移行は別ステアリング相当の規模 → 今回は review date 更新のみで決着。

```diff
-    // UnstableApiUsage: Alarm(ThreadToUse.POOLED_THREAD) — review on platform upgrade
+    // UnstableApiUsage: Alarm(ThreadToUse.POOLED_THREAD) — Reviewed 2026-05-19
+    // for 2026.1.2. The enum value is @ApiStatus.Internal but the simple
+    // debounce pattern has no straightforward replacement; migration to
+    // coroutines would change the panel's lifecycle model. Re-evaluate on
+    // next major platform bump.
     @Suppress("UnstableApiUsage")
     private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, parentDisposable)
```

## 技術負債 2: UNCHECKED_CAST 集約

現状 (`RescriptConfigurable.kt`):

```kotlin
// 4 箇所:
@Suppress("UNCHECKED_CAST")
(components[id] as SettingComponent<String>).getValue()  // line 67

@Suppress("UNCHECKED_CAST")
val component = components[entry.descriptor.id] as SettingComponent<T>  // line 117 in entryIsModified

@Suppress("UNCHECKED_CAST")
val component = components[entry.descriptor.id] as SettingComponent<T>  // line 126 in applyEntry

@Suppress("UNCHECKED_CAST")
val component = components[entry.descriptor.id] as SettingComponent<T>  // line 135 in resetEntry
```

リファクタ:

```kotlin
/**
 * Type-safe accessor for the heterogeneous [components] map. The cast
 * is safe by construction because each [entry] is registered into the
 * map under the same descriptor id with a matching `SettingComponent<T>`
 * during panel build-up — see `createComponent()`.
 */
@Suppress("UNCHECKED_CAST")
private fun <T> componentFor(entry: SchemaEntry.Field<T>): SettingComponent<T> =
    components[entry.descriptor.id] as SettingComponent<T>

/**
 * String-typed sibling of [componentFor] used in the apply() path,
 * which validates raw paths against [RescriptSettingsValidator] before
 * applying any value. Kept as a separate function because the call
 * site uses the path-descriptor id list rather than a typed entry.
 */
@Suppress("UNCHECKED_CAST")
private fun pathComponent(id: String): SettingComponent<String> =
    components[id] as SettingComponent<String>
```

これで 4 つの `@Suppress` が helper 2 つに集約され、ロジック側は型安全な呼出に変わる:

```kotlin
// after:
val pathSnapshot = RescriptSettingsSchema.pathDescriptorIds.associateWith { pathComponent(it).getValue() }
val component = componentFor(entry)
```

## ファイル変更まとめ

| ファイル | 変更 | コミット |
|---|---|---|
| `gradle/wrapper/gradle-wrapper.properties` | distributionUrl bump | 1 |
| `gradle.properties` | platformVersion bump | 2 |
| `build.gradle.kts` | verifier IDE pin bump | 2 |
| `docs/product-requirements.md` | 2 箇所の reasonml-idea-plugin 記述 | 3 |
| `src/main/kotlin/com/rescript/plugin/typeinfo/RescriptTypeInfoPanel.kt` | inline コメント更新 | 4 |
| `src/main/kotlin/com/rescript/plugin/settings/RescriptConfigurable.kt` | helper 抽出、4 callsite 差し替え | 5 |
| (memory) `project_platform_2026_1_blocked.md` | 現バージョン 2026.1.2 更新 | 2 と同コミット |

## リスク

1. **IntelliJ 2026.1.2 で deprecated 警告が増える** — verifyPlugin で検出、必要なら別コミットで `plugin-verifier-ignored-problems.txt` 追記
2. **`pathComponent` の型安全性** — 既存パターンと等価、新規バグの導入なし
3. **`componentFor` の型推論** — Kotlin が `T` を `entry` から推論できるので呼出側で明示不要
4. **Alarm review がスコープ拡大した場合** — coroutines 移行は別ステアリング (本ステアリングではコメント更新のみで結着)
