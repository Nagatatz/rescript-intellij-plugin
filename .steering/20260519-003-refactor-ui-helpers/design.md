# Refactor UI Helpers — 設計

## 機能 1: `util/RescriptColorUtils.kt`

```kotlin
package com.rescript.plugin.util

import java.awt.Color

/**
 * Common Color-related conversions shared by panels that render HTML
 * snippets through JEditorPane. Centralises the CSS hex formatting that
 * was previously duplicated in PPX View and Mermaid colorizer panels.
 */
internal object RescriptColorUtils {
    /**
     * Returns [color] as a CSS hex literal of the form `#RRGGBB`.
     * The conversion always uses upper-case hex digits and zero-pads
     * each channel to two characters so the output is suitable for
     * direct interpolation into HTML `style` attributes.
     */
    fun colorToHexString(color: Color): String =
        String.format("#%02X%02X%02X", color.red, color.green, color.blue)
}
```

### テスト

`util/RescriptColorUtilsTest.kt`:
- 黒・白・赤・グレーで期待 hex を assert
- 単桁 channel が正しく zero-pad されることを確認

## 機能 2: local escapeHtml の削除

各 panel から local `escapeHtml` を削除し、`RescriptSecurityUtils.escapeHtml` を import して呼出:

```kotlin
// before
sb.append(escapeHtml(line.substring(last, m.range.first)))

// after
sb.append(RescriptSecurityUtils.escapeHtml(line.substring(last, m.range.first)))
```

注意: `StringUtil.escapeXmlEntities` の挙動が local 実装と異なる可能性があるので、既存テストを必ず実行する。
- 既存 local `escapeHtml`: `& < > "` を順次 replace
- `StringUtil.escapeXmlEntities`: XML 標準 entity (`& < > " '`) をエスケープ、Unicode 範囲外の文字も処理

`MermaidSourceColorizerTest.kt` の HTML escape ケースは `&lt;` を期待しているので両方とも互換。

### テスト

既存テスト維持。新規追加は不要 (既存ヘルパーの呼出変更のみ)。

## 機能 3: `util/HtmlEditorPaneFactory.kt`

```kotlin
package com.rescript.plugin.util

import com.intellij.util.ui.JBUI
import java.awt.Font
import javax.swing.JEditorPane

/**
 * Factory for read-only HTML-rendering [JEditorPane] instances used by
 * tool window panels that display colourised summaries (PPX View,
 * Variant Flow source mode, Module Dependency source mode).
 *
 * Centralises the common configuration block — monospace font,
 * read-only, HTML content type, display-property honouring — so each
 * panel only has to call one factory function instead of repeating the
 * setup. Per-panel customisation (e.g. border) can still be applied via
 * `apply { }` on the returned instance.
 */
internal object HtmlEditorPaneFactory {
    /** Default font size used when callers do not specify one. */
    const val DEFAULT_FONT_SIZE: Int = 13

    /**
     * Creates a fresh, fully-configured read-only HTML pane.
     *
     * @param fontSize editor font size in points (defaults to
     *   [DEFAULT_FONT_SIZE])
     * @param borderInset optional uniform `JBUI.Borders.empty(inset)`
     *   applied to the pane. Pass null to leave the border unset so
     *   the caller can install a custom border via `apply { ... }`.
     * @return a `JEditorPane` ready to receive HTML via `text = ...`
     */
    fun createReadOnlyHtmlPane(
        fontSize: Int = DEFAULT_FONT_SIZE,
        borderInset: Int? = null,
    ): JEditorPane =
        JEditorPane().apply {
            contentType = "text/html"
            isEditable = false
            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
            font = Font(Font.MONOSPACED, Font.PLAIN, fontSize)
            if (borderInset != null) {
                border = JBUI.Borders.empty(borderInset)
            }
        }
}
```

### Panel 書き換え

#### `ppx/RescriptPpxViewPanel.kt`

```kotlin
// before
private val infoArea =
    JEditorPane().apply {
        contentType = "text/html"
        isEditable = false
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        font = Font(Font.MONOSPACED, Font.PLAIN, 13)
        border = JBUI.Borders.empty(8)
    }

// after
private val infoArea = HtmlEditorPaneFactory.createReadOnlyHtmlPane(borderInset = 8)
```

#### `flow/RescriptVariantFlowPanel.kt`

```kotlin
// before (in init)
private val textArea: JEditorPane =
    JEditorPane().apply {
        contentType = "text/html"
        isEditable = false
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        font = Font(Font.MONOSPACED, Font.PLAIN, font.size)
    }

// after
private val textArea: JEditorPane = HtmlEditorPaneFactory.createReadOnlyHtmlPane()
```

注意: `font.size` (= 既存 JEditorPane のデフォルト font size) を引数で渡したい場合は明示的に `DEFAULT_FONT_SIZE = 13` で十分。直近のセッションでも 13 を使っていた。

#### `diagram/RescriptDependencyDiagramPanel.kt`

```kotlin
private val textArea: JEditorPane = HtmlEditorPaneFactory.createReadOnlyHtmlPane()
```

### テスト

`util/HtmlEditorPaneFactoryTest.kt`:
- `createReadOnlyHtmlPane()` がデフォルト font size と plain border-less 設定で返ることを assert
- `createReadOnlyHtmlPane(borderInset = 8)` で border が `JBUI.Borders.empty(8)` 相当になることを assert
- `contentType == "text/html"`, `!isEditable` の不変式

## ファイル変更まとめ

| ファイル | 種類 | 変更 |
|---|---|---|
| `util/RescriptColorUtils.kt` | 新規 | colorToHexString |
| `util/RescriptColorUtilsTest.kt` | 新規 | 5 ケース程度 |
| `util/HtmlEditorPaneFactory.kt` | 新規 | createReadOnlyHtmlPane |
| `util/HtmlEditorPaneFactoryTest.kt` | 新規 | 3 ケース程度 |
| `ppx/RescriptPpxViewPanel.kt` | 変更 | local escapeHtml 削除、annotationColorHex を RescriptColorUtils 経由、JEditorPane を factory 経由 |
| `flow/MermaidSourceColorizer.kt` | 変更 | local escapeHtml 削除、hexFor を RescriptColorUtils 経由 |
| `flow/RescriptVariantFlowPanel.kt` | 変更 | JEditorPane を factory 経由 |
| `diagram/RescriptDependencyDiagramPanel.kt` | 変更 | JEditorPane を factory 経由 |
| `docs/repository-structure.md` | 変更 | util/ 行に新規 helper を追加 |

## コミット粒度

3 つの独立コミット + ドキュメント:

1. `♻️ Extract colorToHexString into RescriptColorUtils`
   - 新規 `RescriptColorUtils.kt` + test、Ppx と Mermaid colorizer の差し替え
2. `♻️ Replace local escapeHtml fallbacks with RescriptSecurityUtils.escapeHtml`
   - 2 箇所の local 実装削除、import 追加、既存テスト維持
3. `♻️ Extract HtmlEditorPaneFactory and use across PPX / flow / diagram panels`
   - 新規 `HtmlEditorPaneFactory.kt` + test、3 panel の差し替え
4. `📝 List RescriptColorUtils and HtmlEditorPaneFactory in repository-structure`

## リスク

1. **`StringUtil.escapeXmlEntities` の挙動差** — local 実装は `& < > "` のみ、IntelliJ は `'` も。レンダリング結果がより安全になる方向の変化なので問題なし
2. **font.size の差し替え** — `RescriptVariantFlowPanel` の `font.size` (デフォルト Swing 12 pt) と `HtmlEditorPaneFactory.DEFAULT_FONT_SIZE = 13` に 1 pt の差があるが、見た目への影響は実質ゼロ
3. **テスト UI 免除との整合** — factory 自体は UI 免除外 (純粋関数で `JEditorPane` を返すだけ)、テスト必須
