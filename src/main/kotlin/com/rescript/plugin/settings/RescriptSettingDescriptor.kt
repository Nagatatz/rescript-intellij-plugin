package com.rescript.plugin.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import javax.swing.DefaultComboBoxModel
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

/**
 * Describes a single persisted ReScript setting as a self-contained unit.
 *
 * Each descriptor knows how to read/write its typed value on [RescriptProjectSettings]
 * and how to materialize the matching Swing component bound to that value. The
 * [RescriptConfigurable] UI iterates over a [RescriptSettingsSchema] list of these
 * descriptors, eliminating the per-field duplication that previously appeared in
 * `createComponent`, `isModified`, `apply`, and `reset`.
 */
sealed class RescriptSettingDescriptor<T> {
    /** Stable identifier used to look up the live Swing component at apply/reset time. */
    abstract val id: String

    /** Returns the persisted value for this descriptor from [settings]. */
    abstract fun currentValue(settings: RescriptProjectSettings): T

    /** Writes [value] into [settings]. */
    abstract fun applyValue(
        settings: RescriptProjectSettings,
        value: T,
    )

    /** Creates a fresh Swing component bound to this descriptor's type. */
    abstract fun createComponent(project: Project): SettingComponent<T>
}

/**
 * Adapter tying a Swing component to a typed read/write surface used by the schema-driven UI.
 */
interface SettingComponent<T> {
    /** The Swing component to attach to [com.intellij.util.ui.FormBuilder]. */
    val swing: JComponent

    /** Returns the value currently represented by the Swing component. */
    fun getValue(): T

    /** Applies [value] to the Swing component. */
    fun setValue(value: T)
}

/**
 * Boolean setting rendered as a [JCheckBox] with a title.
 *
 * @param id stable identifier (matches the persisted field name)
 * @param title checkbox label shown to the user
 * @param default initial checkbox state used when the component is first created
 * @param getter reads the current persisted value
 * @param setter writes a new value into the persisted settings
 */
class BoolDescriptor(
    override val id: String,
    private val title: String,
    private val default: Boolean,
    private val getter: (RescriptProjectSettings) -> Boolean,
    private val setter: (RescriptProjectSettings, Boolean) -> Unit,
) : RescriptSettingDescriptor<Boolean>() {
    override fun currentValue(settings: RescriptProjectSettings): Boolean = getter(settings)

    override fun applyValue(
        settings: RescriptProjectSettings,
        value: Boolean,
    ) = setter(settings, value)

    override fun createComponent(project: Project): SettingComponent<Boolean> {
        val checkbox = JCheckBox(title, default)
        return object : SettingComponent<Boolean> {
            override val swing: JComponent = checkbox

            override fun getValue(): Boolean = checkbox.isSelected

            override fun setValue(value: Boolean) {
                checkbox.isSelected = value
            }
        }
    }
}

/** Whether a [PathDescriptor] selects a single file or a single folder. */
enum class PathKind { File, Folder }

/**
 * Path setting rendered as a [TextFieldWithBrowseButton] wired to either a file
 * or folder picker.
 *
 * @param id stable identifier (matches the persisted field name)
 * @param kind selects the file or folder chooser descriptor
 * @param title title shown in the chooser dialog header
 * @param description longer chooser-dialog description
 * @param getter reads the current persisted path (empty string means "auto/default")
 * @param setter writes a new path value
 */
class PathDescriptor(
    override val id: String,
    private val kind: PathKind,
    private val title: String,
    private val description: String,
    private val getter: (RescriptProjectSettings) -> String,
    private val setter: (RescriptProjectSettings, String) -> Unit,
) : RescriptSettingDescriptor<String>() {
    override fun currentValue(settings: RescriptProjectSettings): String = getter(settings)

    override fun applyValue(
        settings: RescriptProjectSettings,
        value: String,
    ) = setter(settings, value)

    override fun createComponent(project: Project): SettingComponent<String> {
        val field =
            TextFieldWithBrowseButton().apply {
                val chooser =
                    when (kind) {
                        PathKind.File -> FileChooserDescriptorFactory.singleFile()
                        PathKind.Folder -> FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    }.withTitle(title).withDescription(description)
                @Suppress("DialogTitleCapitalization")
                addBrowseFolderListener(project, chooser)
            }
        return object : SettingComponent<String> {
            override val swing: JComponent = field

            override fun getValue(): String = field.text.trim()

            override fun setValue(value: String) {
                field.text = value
            }
        }
    }
}

/**
 * Enum-like selector rendered as a [ComboBox] of strings.
 *
 * @param id stable identifier (matches the persisted field name)
 * @param options choices shown in the combo, in display order
 * @param default value selected when the component is first created
 * @param getter reads the current persisted selection
 * @param setter writes a new selection
 */
class ComboDescriptor(
    override val id: String,
    private val options: Array<String>,
    private val default: String,
    private val getter: (RescriptProjectSettings) -> String,
    private val setter: (RescriptProjectSettings, String) -> Unit,
) : RescriptSettingDescriptor<String>() {
    override fun currentValue(settings: RescriptProjectSettings): String = getter(settings)

    override fun applyValue(
        settings: RescriptProjectSettings,
        value: String,
    ) = setter(settings, value)

    override fun createComponent(project: Project): SettingComponent<String> {
        val combo = ComboBox(DefaultComboBoxModel(options))
        combo.selectedItem = default
        return object : SettingComponent<String> {
            override val swing: JComponent = combo

            override fun getValue(): String = (combo.selectedItem as? String) ?: default

            override fun setValue(value: String) {
                combo.selectedItem = value
            }
        }
    }
}

/**
 * Integer spinner with bounded range, rendered as a [JSpinner].
 *
 * @param id stable identifier (matches the persisted field name)
 * @param default initial spinner value
 * @param min inclusive minimum
 * @param max inclusive maximum
 * @param step increment step
 * @param getter reads the current persisted integer
 * @param setter writes a new integer
 */
class IntSpinnerDescriptor(
    override val id: String,
    private val default: Int,
    private val min: Int,
    private val max: Int,
    private val step: Int,
    private val getter: (RescriptProjectSettings) -> Int,
    private val setter: (RescriptProjectSettings, Int) -> Unit,
) : RescriptSettingDescriptor<Int>() {
    override fun currentValue(settings: RescriptProjectSettings): Int = getter(settings)

    override fun applyValue(
        settings: RescriptProjectSettings,
        value: Int,
    ) = setter(settings, value)

    override fun createComponent(project: Project): SettingComponent<Int> {
        val spinner = JSpinner(SpinnerNumberModel(default, min, max, step))
        return object : SettingComponent<Int> {
            override val swing: JComponent = spinner

            override fun getValue(): Int = spinner.value as? Int ?: default

            override fun setValue(value: Int) {
                spinner.value = value
            }
        }
    }
}

/**
 * Multi-line text area whose persisted value is a list of newline-separated
 * non-blank entries (typically relative file paths).
 *
 * Empty/whitespace lines are stripped on read and write, so users can format
 * the input freely and the underlying list stays clean.
 *
 * @param id stable identifier (matches the persisted field name)
 * @param rows visible row count for the text area
 * @param getter reads the current persisted list of entries
 * @param setter writes a new list of entries
 */
class StringListDescriptor(
    override val id: String,
    private val rows: Int,
    private val getter: (RescriptProjectSettings) -> List<String>,
    private val setter: (RescriptProjectSettings, List<String>) -> Unit,
) : RescriptSettingDescriptor<List<String>>() {
    override fun currentValue(settings: RescriptProjectSettings): List<String> = getter(settings)

    override fun applyValue(
        settings: RescriptProjectSettings,
        value: List<String>,
    ) = setter(settings, value)

    override fun createComponent(project: Project): SettingComponent<List<String>> {
        val area =
            JBTextArea(rows, 32).apply {
                lineWrap = false
            }
        val scroll = JBScrollPane(area)
        return object : SettingComponent<List<String>> {
            override val swing: JComponent = scroll

            override fun getValue(): List<String> =
                area.text
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toList()

            override fun setValue(value: List<String>) {
                area.text = value.joinToString("\n")
            }
        }
    }
}
