package com.rescript.plugin.refactor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptNameSuggestionProviderTest {
    @Test
    fun `provider can be instantiated`() {
        val provider = RescriptNameSuggestionProvider()
        assertNotNull(provider)
    }

    @Test
    fun `snakeToCamelCase converts correctly`() {
        assertEquals("myVariable", RescriptNameSuggestionProvider.snakeToCamelCase("my_variable"))
        assertEquals("getUserName", RescriptNameSuggestionProvider.snakeToCamelCase("get_user_name"))
        assertEquals("simple", RescriptNameSuggestionProvider.snakeToCamelCase("simple"))
    }

    @Test
    fun `toCamelCase converts PascalCase`() {
        assertEquals("userProfile", RescriptNameSuggestionProvider.toCamelCase("UserProfile"))
        assertEquals("app", RescriptNameSuggestionProvider.toCamelCase("App"))
    }

    @Test
    fun `toCamelCase converts hyphenated names`() {
        assertEquals("userProfile", RescriptNameSuggestionProvider.toCamelCase("user-profile"))
        assertEquals("myApp", RescriptNameSuggestionProvider.toCamelCase("my-app"))
    }
}
