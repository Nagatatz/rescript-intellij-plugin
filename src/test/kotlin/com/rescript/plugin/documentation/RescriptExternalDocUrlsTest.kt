package com.rescript.plugin.documentation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptExternalDocUrlsTest {
    @Test
    fun baseUrlIsValid() {
        assertTrue(
            RescriptExternalDocUrls.BASE_URL.startsWith("https://rescript-lang.org/"),
            "BASE_URL should point to rescript-lang.org",
        )
    }

    @Test
    fun moduleUrlMapIsNotEmpty() {
        assertTrue(
            RescriptExternalDocUrls.MODULE_URL_MAP.isNotEmpty(),
            "MODULE_URL_MAP should contain entries",
        )
    }

    @Test
    fun beltModulesPresent() {
        val map = RescriptExternalDocUrls.MODULE_URL_MAP
        assertNotNull(map["Belt"], "Belt should be present")
        assertNotNull(map["Belt.Array"], "Belt.Array should be present")
        assertNotNull(map["Belt.List"], "Belt.List should be present")
        assertNotNull(map["Belt.Map"], "Belt.Map should be present")
        assertNotNull(map["Belt.Set"], "Belt.Set should be present")
        assertNotNull(map["Belt.Option"], "Belt.Option should be present")
        assertNotNull(map["Belt.Result"], "Belt.Result should be present")
    }

    @Test
    fun jsModulesPresent() {
        val map = RescriptExternalDocUrls.MODULE_URL_MAP
        assertNotNull(map["Js"], "Js should be present")
        assertNotNull(map["Js.Array"], "Js.Array should be present")
        assertNotNull(map["Js.String"], "Js.String should be present")
        assertNotNull(map["Js.Promise"], "Js.Promise should be present")
        assertNotNull(map["Js.Json"], "Js.Json should be present")
        assertNotNull(map["Js.Math"], "Js.Math should be present")
        assertNotNull(map["Js.Date"], "Js.Date should be present")
    }

    @Test
    fun beltUrlSegmentsStartWithBelt() {
        for ((module, segment) in RescriptExternalDocUrls.MODULE_URL_MAP) {
            if (module.startsWith("Belt")) {
                assertTrue(
                    segment.startsWith("belt"),
                    "Belt module '$module' URL segment should start with 'belt'",
                )
            }
        }
    }

    @Test
    fun jsUrlSegmentsStartWithJs() {
        for ((module, segment) in RescriptExternalDocUrls.MODULE_URL_MAP) {
            if (module.startsWith("Js")) {
                assertTrue(
                    segment.startsWith("js"),
                    "Js module '$module' URL segment should start with 'js'",
                )
            }
        }
    }

    @Test
    fun urlSegmentsContainNoSpaces() {
        for ((module, segment) in RescriptExternalDocUrls.MODULE_URL_MAP) {
            assertTrue(
                !segment.contains(" "),
                "URL segment for '$module' should not contain spaces",
            )
        }
    }

    @Test
    fun urlSegmentsAreLowerCase() {
        for ((module, segment) in RescriptExternalDocUrls.MODULE_URL_MAP) {
            assertEquals(
                segment.lowercase(),
                segment,
                "URL segment for '$module' should be lowercase",
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
                module[0].isUpperCase(),
                "Module key '$module' should start with uppercase",
            )
        }
    }
}
