# 設計: Panel navigation

## 1. RescriptMigrationPanel

### 変更点

- 既存の `MouseAdapter` を「シングルクリック → toggle」「ダブルクリック → 開く」に拡張
- `OpenFileDescriptor(project, candidate.file, 0).navigate(true)` を呼ぶ

```kotlin
addMouseListener(object : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent) {
        val idx = locationToIndex(e.point)
        if (idx < 0) return
        val candidate = candidatesModel.get(idx)
        if (e.clickCount == 2 && e.button == MouseEvent.BUTTON1) {
            OpenFileDescriptor(project, candidate.file).navigate(true)
            return
        }
        toggle(candidate.file)
        repaint()
    }
})
```

注意: シングルクリック後にダブルクリックを試みた場合、Swing は単一の MouseEvent でクリック回数 2 を 1 イベントとして配信するため、`mouseClicked` は単独で呼ばれる。シングルクリック → checkbox 切替で 1 回、ダブルクリック → checkbox 切替が 2 回行われた後ナビゲートになる動作が標準。確実に「ダブルクリックは toggle なしで開く」を実現したい場合は `clickCount == 2` 分岐の手前で early return する。

### テスト

- Panel 自体は Swing UI 免除。
- 既存テストはそのまま維持。

## 2. RescriptVariantFlowPanel

### 変更点

- `JumpTarget` data class を Panel 内に追加し、`refresh()` 内で `currentEditorContext()` から得た `(file, offset)` を保持
- toolbar に `Jump to switch` action を追加。enabled は `currentJumpTarget != null` のとき
- action は `OpenFileDescriptor(project, file, offset).navigate(true)`

```kotlin
private data class JumpTarget(val file: VirtualFile, val offset: Int)

@Volatile private var currentJumpTarget: JumpTarget? = null

// refresh() で diagram 生成成功後:
currentJumpTarget = JumpTarget(file, offset)
// renderEmpty() で null にクリア
```

注意: `RescriptVariantFlowModel.buildAtOffset` は switch を内包する innermost を選ぶ。caller が渡したカーソル offset がそのまま正解の jump 先になる（switch 内のどこにキャレットがあっても、戻った時に同じ位置に戻れる）。

### テスト

- Panel 自体は Swing UI 免除。
- jump action enabled/disabled の正しさは `currentJumpTarget != null` の単純な代入なので追加テストは不要。

## 3. RescriptDependenciesPanel

### 変更点

- 内部 `PackageNode` data class を追加し、ツリーノードの `userObject` として格納
- 既存の `addDependencyCategory` で `DefaultMutableTreeNode(label)` の代わりに `DefaultMutableTreeNode(PackageNode(pkgName, pkgJsonFile, label))` を使う
- `PackageNode.toString()` で表示ラベルを返すと既存の `TreeRenderer` がそのまま動く
- `tree` に `MouseListener` を追加し、ダブルクリックで `pkgJsonFile` を `OpenFileDescriptor` で開く

```kotlin
internal data class PackageNode(
    val pkgName: String,
    val packageJsonFile: VirtualFile?,
    val displayLabel: String,
) {
    override fun toString(): String = displayLabel
}
```

純粋ヘルパーを `companion object` に追加:

```kotlin
internal fun buildPackageNode(
    basePath: String,
    pkgName: String,
): PackageNode {
    val version = resolveVersion(basePath, pkgName)
    val pkgJsonFile = findPackageJson(basePath, pkgName)
    val label = if (version != null) "$pkgName ($version)" else pkgName
    return PackageNode(pkgName, pkgJsonFile, label)
}

internal fun findPackageJson(
    basePath: String,
    pkgName: String,
): VirtualFile? =
    VirtualFileManager.getInstance()
        .findFileByUrl("file://$basePath/node_modules/$pkgName/package.json")
```

### テスト

新規 `RescriptDependenciesPackageNodeTest.kt`:

- `PackageNode.toString()` がラベル文字列を返すこと
- `displayLabel` の組立が「version あり: `<pkg> (<ver>)`」「version なし: `<pkg>`」

`findPackageJson` は VirtualFileManager 依存で IDE 固有のため、テストは pure 部分 (`PackageNode.toString` と displayLabel 計算) に限定する。displayLabel 計算は private なので pure helper `displayLabelFor(pkgName, version)` を分離して公開。

## 共通

- すべての navigate 呼び出しは `OpenFileDescriptor(project, file [, offset]).navigate(requestFocus = true)`
- import: `com.intellij.openapi.fileEditor.OpenFileDescriptor`

## 後方互換性

- public シグネチャ無変更
- 既存テスト無変更で通過
- plugin.xml 変更なし
