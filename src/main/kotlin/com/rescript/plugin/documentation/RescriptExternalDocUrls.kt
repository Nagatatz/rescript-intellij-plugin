package com.rescript.plugin.documentation

/**
 * Defines URL mappings for ReScript standard library external documentation.
 *
 * Maps module paths (e.g., "Belt.Array", "Js.String") to their corresponding
 * documentation page URL segments on rescript-lang.org. Belt pages live in the
 * current API reference; the legacy `Js` modules are only published in the
 * v11 API reference.
 *
 * @see RescriptDocumentationProvider
 */
data object RescriptExternalDocUrls {
    const val BASE_URL = "https://rescript-lang.org/docs/manual/api"

    /** API reference that still publishes the legacy `Js` modules. */
    const val JS_BASE_URL = "https://rescript-lang.org/docs/manual/v11.0.0/api"

    /**
     * Maps ReScript standard library module paths to their documentation URL segments.
     */
    val MODULE_URL_MAP =
        mapOf(
            // Belt modules
            "Belt" to "belt",
            "Belt.Array" to "belt/array",
            "Belt.List" to "belt/list",
            "Belt.Map" to "belt/map",
            "Belt.Map.Dict" to "belt/map/dict",
            "Belt.Map.Int" to "belt/map/int",
            "Belt.Map.String" to "belt/map/string",
            "Belt.Set" to "belt/set",
            "Belt.Set.Dict" to "belt/set/dict",
            "Belt.Set.Int" to "belt/set/int",
            "Belt.Set.String" to "belt/set/string",
            "Belt.HashMap" to "belt/hashmap",
            "Belt.HashMap.Int" to "belt/hashmap/int",
            "Belt.HashMap.String" to "belt/hashmap/string",
            "Belt.HashSet" to "belt/hashset",
            "Belt.HashSet.Int" to "belt/hashset/int",
            "Belt.HashSet.String" to "belt/hashset/string",
            "Belt.MutableMap" to "belt/mutablemap",
            "Belt.MutableMap.Int" to "belt/mutablemap/int",
            "Belt.MutableMap.String" to "belt/mutablemap/string",
            "Belt.MutableSet" to "belt/mutableset",
            "Belt.MutableSet.Int" to "belt/mutableset/int",
            "Belt.MutableSet.String" to "belt/mutableset/string",
            "Belt.MutableQueue" to "belt/mutablequeue",
            "Belt.MutableStack" to "belt/mutablestack",
            "Belt.SortArray" to "belt/sortarray",
            "Belt.SortArray.Int" to "belt/sortarray/int",
            "Belt.SortArray.String" to "belt/sortarray/string",
            "Belt.Int" to "belt/int",
            "Belt.Float" to "belt/float",
            "Belt.Option" to "belt/option",
            "Belt.Result" to "belt/result",
            "Belt.Range" to "belt/range",
            "Belt.Id" to "belt/id",
            // Js modules
            "Js" to "js",
            "Js.Array" to "js/array",
            "Js.Array2" to "js/array2",
            "Js.String" to "js/string",
            "Js.String2" to "js/string2",
            "Js.Promise" to "js/promise",
            "Js.Promise2" to "js/promise2",
            "Js.Json" to "js/json",
            "Js.Math" to "js/math",
            "Js.Date" to "js/date",
            "Js.Re" to "js/re",
            "Js.Dict" to "js/dict",
            "Js.Null" to "js/null",
            "Js.Nullable" to "js/nullable",
            "Js.Undefined" to "js/undefined",
            "Js.Exn" to "js/exn",
            "Js.Console" to "js/console",
            "Js.Float" to "js/float",
            "Js.Int" to "js/int",
            "Js.Obj" to "js/obj",
            "Js.Option" to "js/option",
            "Js.Result" to "js/result",
            "Js.TypedArray2" to "js/typedarray2",
            "Js.Types" to "js/types",
            "Js.Global" to "js/global",
        )

    /**
     * Returns the documentation URL for a module path, or null when the module
     * has no known page.
     */
    fun urlFor(modulePath: String): String? {
        val segment = MODULE_URL_MAP[modulePath] ?: return null
        val base = if (segment == "js" || segment.startsWith("js/")) JS_BASE_URL else BASE_URL
        return "$base/$segment"
    }
}
