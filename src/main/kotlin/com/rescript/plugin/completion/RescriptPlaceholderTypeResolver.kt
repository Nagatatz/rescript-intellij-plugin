package com.rescript.plugin.completion

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.rescript.plugin.indexing.RescriptNameIndex
import com.rescript.plugin.lang.RescriptTypeDeclarationParser
import com.rescript.plugin.lang.TypeShape
import com.rescript.plugin.lang.VariantConstructor
import com.rescript.plugin.lang.psi.RescriptDeclarationPsiElement

/**
 * Resolves a head type identifier (as produced by
 * [RescriptTypeAnnotationContext]) into the [TypeShape] used to build
 * placeholder completion candidates.
 *
 * Two resolution paths are tried in order:
 * 1. **Built-ins** — `option` and `result` are hardcoded to their
 *    well-known variant shapes, since they are not declared anywhere
 *    in user projects but are by far the most common annotated types.
 * 2. **Stub index** — for any other name, the project's name index is
 *    searched for a matching `type <name>` declaration whose RHS is
 *    re-parsed with [RescriptTypeDeclarationParser].
 *
 * Indexing-aware: returns `null` while the project is dumb-indexing so
 * the caller can skip offering placeholders rather than risk a stale
 * result.
 *
 * @see RescriptTypeAnnotationContext for how the head identifier is found
 * @see RescriptPlaceholderBuilder for how the resolved shape is rendered
 */
object RescriptPlaceholderTypeResolver {
    /** Built-in variant shapes keyed by their type name. */
    private val BUILT_INS: Map<String, TypeShape.Variant> =
        mapOf(
            "option" to
                TypeShape.Variant(
                    listOf(
                        VariantConstructor("Some", "'a"),
                        VariantConstructor("None", null),
                    ),
                ),
            "result" to
                TypeShape.Variant(
                    listOf(
                        VariantConstructor("Ok", "'a"),
                        VariantConstructor("Error", "'b"),
                    ),
                ),
        )

    /**
     * Resolves [typeName] into a record/variant [TypeShape].
     *
     * @param project project to search; callers should guard with
     *   [DumbService.isDumb]
     * @param typeName the head type identifier (e.g. `person`, `option`)
     * @return the resolved shape, or `null` when the project is indexing
     *   or no record/variant declaration could be found
     */
    fun resolve(
        project: Project,
        typeName: String,
    ): TypeShape? {
        resolveBuiltIn(typeName)?.let { return it }
        if (DumbService.isDumb(project)) return null
        return resolveFromIndex(project, typeName)
    }

    /**
     * Resolves [typeName] against the hardcoded built-in variant shapes
     * (`option`, `result`) without touching the project index.
     *
     * @return the built-in shape, or `null` if [typeName] is not built-in
     */
    internal fun resolveBuiltIn(typeName: String): TypeShape? = BUILT_INS[typeName]

    /**
     * Searches the name index for a `type <typeName>` declaration and
     * parses its RHS, returning the first record or variant shape found.
     */
    private fun resolveFromIndex(
        project: Project,
        typeName: String,
    ): TypeShape? {
        val scope = GlobalSearchScope.allScope(project)
        var found: TypeShape? = null
        StubIndex.getInstance().processElements(
            RescriptNameIndex.KEY,
            typeName,
            project,
            scope,
            RescriptDeclarationPsiElement::class.java,
        ) { decl ->
            val text = decl.text
            // RescriptNameIndex covers let/type/module/external/exception;
            // keep only declarations whose head is `type <typeName>` so a
            // same-named let-binding cannot shadow the type.
            if (!matchesTypeHead(text, typeName)) return@processElements true
            val shape = RescriptTypeDeclarationParser.parse(text)
            if (shape is TypeShape.Record || shape is TypeShape.Variant) {
                found = shape
                false // stop iteration
            } else {
                true
            }
        }
        return found
    }

    /**
     * Cheap textual check that [declText] starts with `type <name>`,
     * rejecting prefix collisions such as `type colors` for `color`.
     *
     * @param declText raw declaration text from a name-index hit
     * @param typeName the type name being resolved
     * @return `true` when the declaration head is exactly `type <typeName>`
     */
    internal fun matchesTypeHead(
        declText: String,
        typeName: String,
    ): Boolean {
        val head = declText.trimStart()
        val prefix = "type"
        if (!head.startsWith(prefix)) return false
        val rest = head.removePrefix(prefix)
        if (rest.isEmpty() || !rest.first().isWhitespace()) return false
        val nameStart = rest.indexOfFirst { !it.isWhitespace() }
        if (nameStart < 0) return false
        val tail = rest.substring(nameStart)
        if (!tail.startsWith(typeName)) return false
        // The name must be followed by whitespace, `=`, type params `<`,
        // payload `(`, or end-of-text — never another identifier char.
        val after = tail.getOrNull(typeName.length) ?: return true
        return after.isWhitespace() || after == '=' || after == '<' || after == '('
    }
}
