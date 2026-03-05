package com.rescript.plugin.documentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptExternalDocUrlsTest {
    @Test
    fun baseUrlIsValid() {
        assertTrue(
            "BASE_URL should point to rescript-lang.org",
            RescriptExternalDocUrls.BASE_URL.startsWith("https://rescript-lang.org/"),
        )
    }

    @Test
    fun moduleUrlMapIsNotEmpty() {
        assertTrue(
            "MODULE_URL_MAP should contain entries",
            RescriptExternalDocUrls.MODULE_URL_MAP.isNotEmpty(),
        )
    }

    @Test
    fun beltModulesPresent() {
        val map = RescriptExternalDocUrls.MODULE_URL_MAP
        assertNotNull("Belt should be present", map["Belt"])
        assertNotNull("Belt.Array should be present", map["Belt.Array"])
        assertNotNull("Belt.List should be present", map["Belt.List"])
        assertNotNull("Belt.Map should be present", map["Belt.Map"])
        assertNotNull("Belt.Set should be present", map["Belt.Set"])
        assertNotNull("Belt.Option should be present", map["Belt.Option"])
        assertNotNull("Belt.Result should be present", map["Belt.Result"])
    }

    @Test
    fun jsModulesPresent() {
        val map = RescriptExternalDocUrls.MODULE_URL_MAP
        assertNotNull("Js should be present", map["Js"])
        assertNotNull("Js.Array should be present", map["Js.Array"])
        assertNotNull("Js.String should be present", map["Js.String"])
        assertNotNull("Js.Promise should be present", map["Js.Promise"])
        assertNotNull("Js.Json should be present", map["Js.Json"])
        assertNotNull("Js.Math should be present", map["Js.Math"])
        assertNotNull("Js.Date should be present", map["Js.Date"])
    }

    @Test
    fun beltUrlSegmentsStartWithBelt() {
        for ((module, segment) in RescriptExternalDocUrls.MODULE_URL_MAP) {
            if (module.startsWith("Belt")) {
                assertTrue(
                    "Belt module '$module' URL segment should start with 'belt'",
                    segment.startsWith("belt"),
                )
            }
        }
    }

    @Test
    fun jsUrlSegmentsStartWithJs() {
        for ((module, segment) in RescriptExternalDocUrls.MODULE_URL_MAP) {
            if (module.startsWith("Js")) {
                assertTrue(
                    "Js module '$module' URL segment should start with 'js'",
                    segment.startsWith("js"),
                )
            }
        }
    }

    @Test
    fun urlSegmentsContainNoSpaces() {
        for ((module, segment) in RescriptExternalDocUrls.MODULE_URL_MAP) {
            assertTrue(
                "URL segment for '$module' should not contain spaces",
                !segment.contains(" "),
            )
        }
    }

    @Test
    fun urlSegmentsAreLowerCase() {
        for ((module, segment) in RescriptExternalDocUrls.MODULE_URL_MAP) {
            assertEquals(
                "URL segment for '$module' should be lowercase",
                segment.lowercase(),
                segment,
            )
        }
    }

    @Test
    fun urlConstructionForBeltArray() {
        val segment = RescriptExternalDocUrls.MODULE_URL_MAP["Belt.Array"]
        assertNotNull(segment)
        val url = "${RescriptExternalDocUrls.BASE_URL}/$segment"
        assertEquals(
            "https://rescript-lang.org/docs/manual/latest/api/belt/array",
            url,
        )
    }

    @Test
    fun urlConstructionForJsPromise() {
        val segment = RescriptExternalDocUrls.MODULE_URL_MAP["Js.Promise"]
        assertNotNull(segment)
        val url = "${RescriptExternalDocUrls.BASE_URL}/$segment"
        assertEquals(
            "https://rescript-lang.org/docs/manual/latest/api/js/promise",
            url,
        )
    }

    @Test
    fun allModuleUrlMapKeysAreCapitalized() {
        for (module in RescriptExternalDocUrls.MODULE_URL_MAP.keys) {
            assertTrue(
                "Module key '$module' should start with uppercase",
                module[0].isUpperCase(),
            )
        }
    }
}
