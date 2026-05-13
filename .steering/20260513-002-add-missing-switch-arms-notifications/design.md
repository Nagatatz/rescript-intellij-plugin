# 設計: Add Missing Switch Arms に診断 Notification を追加

## 方針

`RescriptAddMissingSwitchArmsIntention.invoke()` を「pure な診断計算 → IDE 副作用」の 2 段に分ける。

### 1. 新規 `ArmsOutcome` sealed class

`RescriptAddMissingSwitchArmsIntention.kt` 内に internal sealed class として配置:

```kotlin
internal sealed class ArmsOutcome {
    object NotInSwitch : ArmsOutcome()              // isAvailable で排除済 (safeguard)
    object NoScrutinee : ArmsOutcome()              // scrutineeOffset 失敗
    object LspUnavailable : ArmsOutcome()           // LSP サーバー未起動
    object HoverEmpty : ArmsOutcome()               // hover が type 取得失敗
    data class NotVariant(val typeText: String) : ArmsOutcome()  // variant でない型
    object NoMissingArms : ArmsOutcome()            // 全 constructor 被覆済 / 不完全 switch
    data class Ready(val result: MissingArmsResult) : ArmsOutcome()
}
```

### 2. 新規 pure helper

```kotlin
internal object RescriptAddMissingArmsDiagnoser {
    fun diagnose(
        source: String,
        offset: Int,
        lspServerAvailable: Boolean,
        hoverProbe: (Int) -> String?,
    ): ArmsOutcome {
        if (!RescriptMissingArmsBuilder.isInsideSwitch(source, offset)) return ArmsOutcome.NotInSwitch
        if (!lspServerAvailable) return ArmsOutcome.LspUnavailable
        val scrutineeOffset = RescriptMissingArmsBuilder.scrutineeOffset(source, offset)
            ?: return ArmsOutcome.NoScrutinee
        val typeText = hoverProbe(scrutineeOffset) ?: return ArmsOutcome.HoverEmpty
        val constructors = RescriptLspUtils.parseVariantConstructors(typeText)
        if (constructors.isEmpty()) return ArmsOutcome.NotVariant(typeText.trim())
        val result = RescriptMissingArmsBuilder.computeMissing(source, offset, constructors)
            ?: return ArmsOutcome.NoMissingArms
        return ArmsOutcome.Ready(result)
    }

    fun messageFor(outcome: ArmsOutcome): String? = when (outcome) {
        ArmsOutcome.NotInSwitch, is ArmsOutcome.Ready -> null
        ArmsOutcome.NoScrutinee -> "Add missing switch arms: could not locate the switch scrutinee."
        ArmsOutcome.LspUnavailable ->
            "Add missing switch arms: ReScript LSP server is not running. " +
                "Start the language server and retry."
        ArmsOutcome.HoverEmpty ->
            "Add missing switch arms: could not infer the scrutinee type from LSP hover."
        is ArmsOutcome.NotVariant ->
            "Add missing switch arms: scrutinee type is not a variant (got `${outcome.typeText}`)."
        ArmsOutcome.NoMissingArms ->
            "Add missing switch arms: no missing constructors — the switch is already exhaustive."
    }
}
```

`hoverProbe` を関数で受けることで、`RescriptLspUtils.getHoverType(project, file, ofs)` の依存を切り、ユニットテストでは固定文字列を返すラムダで挿す。

### 3. `RescriptAddMissingSwitchArmsIntention.invoke()` の改修

```kotlin
override fun invoke(...) {
    val editor = editor ?: return
    val doc = editor.document
    val source = doc.text
    val offset = element.textRange.startOffset
    val virtualFile = element.containingFile?.virtualFile ?: return

    val outcome = RescriptAddMissingArmsDiagnoser.diagnose(
        source = source,
        offset = offset,
        lspServerAvailable = RescriptLspUtils.getServer(project) != null,
        hoverProbe = { RescriptLspUtils.getHoverType(project, virtualFile, it) },
    )

    when (outcome) {
        is ArmsOutcome.Ready ->
            doc.insertInWriteAction(project, outcome.result.insertOffset, outcome.result.insertText)
        else -> RescriptAddMissingArmsDiagnoser.messageFor(outcome)?.let { notify(project, it) }
    }
}

private fun notify(project: Project, message: String) {
    NotificationGroupManager
        .getInstance()
        .getNotificationGroup("ReScript")
        .createNotification(message, NotificationType.WARNING)
        .notify(project)
}
```

## テスト戦略

### 新規ファイル: `RescriptAddMissingArmsDiagnoserTest.kt`

LSP 結合なしで全分岐を網羅:

- `NotInSwitch`: `source = "let x = 1"`, offset = 0
- `LspUnavailable`: switch あり, `lspServerAvailable = false`
- `NoScrutinee`: 異常 switch 文字列で `scrutineeOffset` が null になる入力（scrutinee が抽出できない構造）
- `HoverEmpty`: switch あり, lsp 有, `hoverProbe = { null }`
- `NotVariant`: switch あり, lsp 有, hoverProbe が `"int"` を返す
- `NoMissingArms`: switch あり, 全 constructor を被覆済の switch + hover で variant 型を返す
- `Ready`: 一部だけ被覆 + variant 型 hover

`messageFor` も同じ outcome に対して期待文字列を返すことを検証。

### 既存ファイル: `RescriptAddMissingSwitchArmsIntentionTest.kt`

ラベル smoke テスト 2 件のまま維持。LSP 結合の振る舞いはテスト免除。

## なぜ pure helper を切るか

`RescriptAddMissingSwitchArmsIntention` 本体は IDE fixture / LSP server なしでテストできないが、診断計算は state-free。pure helper に切ることで:

- 全分岐をユニットテストで保証できる
- `messageFor` の文字列回帰を防げる（マニュアルテストで変更すると気付かれない場合があるため）

## 後方互換性

- public API シグネチャ無変更（`invoke` / `getText` / `isAvailableInRescript` のシグネチャはそのまま）
- 既存テスト 2 件は維持
- 通知グループ `ReScript` は既存登録を再利用するため `plugin.xml` 変更なし
