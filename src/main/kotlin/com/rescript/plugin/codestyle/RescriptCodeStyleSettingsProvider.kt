package com.rescript.plugin.codestyle

import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.rescript.plugin.RescriptLanguage

/**
 * Provides default code style settings for ReScript files.
 *
 * Configures indentation defaults (2 spaces, no tabs) to match ReScript conventions
 * and exposes indent-related settings in Settings > Editor > Code Style > ReScript.
 */
class RescriptCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage(): Language = RescriptLanguage

    override fun customizeDefaults(
        commonSettings: CommonCodeStyleSettings,
        indentOptions: CommonCodeStyleSettings.IndentOptions,
    ) {
        indentOptions.INDENT_SIZE = 2
        indentOptions.CONTINUATION_INDENT_SIZE = 2
        indentOptions.TAB_SIZE = 2
        indentOptions.USE_TAB_CHARACTER = false
    }

    override fun customizeSettings(
        consumer: CodeStyleSettingsCustomizable,
        settingsType: SettingsType,
    ) {
        if (settingsType == SettingsType.INDENT_SETTINGS) {
            consumer.showStandardOptions(
                "INDENT_SIZE",
                "CONTINUATION_INDENT_SIZE",
                "TAB_SIZE",
                "USE_TAB_CHARACTER",
                "SMART_TABS",
            )
        }
    }

    override fun getCodeSample(settingsType: SettingsType): String =
        $$"""
        |module Greeting = {
        |  @react.component
        |  let make = (~name) => {
        |    let message = `Hello, ${name}!`
        |    <div className="greeting">
        |      <h1> {React.string(message)} </h1>
        |    </div>
        |  }
        |}
        |
        |type user = {
        |  name: string,
        |  age: int,
        |  email: option<string>,
        |}
        |
        |let greet = (user: user) =>
        |  switch user.email {
        |  | Some(email) => `${user.name} <${email}>`
        |  | None => user.name
        |  }
        |
        |let numbers = [1, 2, 3, 4, 5]
        |
        |let doubled =
        |  numbers
        |  ->Array.map(n => n * 2)
        |  ->Array.filter(n => n > 4)
        """.trimMargin()
}
