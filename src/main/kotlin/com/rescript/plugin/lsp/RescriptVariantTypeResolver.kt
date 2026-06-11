package com.rescript.plugin.lsp

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.rescript.plugin.indexing.RescriptNameIndex
import com.rescript.plugin.lang.RescriptTypeDeclarationParser
import com.rescript.plugin.lang.TypeShape
import com.rescript.plugin.lang.psi.RescriptDeclarationPsiElement

/**
 * Project-aware fallback that resolves a bare type-name reference
 * (e.g. `"color"`) into the list of variant constructors declared for
 * that type somewhere in the project.
 *
 * The ReScript LSP hover frequently returns just the user-defined
 * type name rather than its inline `| A | B(payload)` body, which
 * causes [RescriptLspSignatureParser.parseVariantConstructors] to
 * come back empty for otherwise perfectly valid variant types. This
 * resolver bridges that gap by searching the stub index for
 * matching `type` declarations and re-parsing their RHS with the
 * existing [RescriptTypeDeclarationParser].
 *
 * Indexing-aware: returns `null` when the project is still
 * dumb-indexing so the caller can defer to its no-result path.
 *
 * @see RescriptLspSignatureParser.extractBareTypeName
 * @see RescriptTypeDeclarationParser
 */
object RescriptVariantTypeResolver {
    /**
     * Looks up declarations named [typeName] in [project], picks the
     * first one whose text declares a variant, and returns the
     * parsed constructors.
     *
     * @param project project to search; must not be in dumb mode
     *   (callers can guard with [DumbService.isDumb])
     * @param typeName bare lowercase identifier produced by
     *   [RescriptLspSignatureParser.extractBareTypeName]
     * @return constructors when a variant declaration is found;
     *   empty list when the type exists but is not a variant;
     *   null when the project is currently indexing
     */
    fun resolve(
        project: Project,
        typeName: String,
    ): List<RescriptLspSignatureParser.VariantInfo>? {
        if (DumbService.isDumb(project)) return null
        val scope = GlobalSearchScope.allScope(project)
        var found: List<RescriptLspSignatureParser.VariantInfo>? = emptyList()
        StubIndex.getInstance().processElements(
            RescriptNameIndex.KEY,
            typeName,
            project,
            scope,
            RescriptDeclarationPsiElement::class.java,
        ) { decl ->
            val text = decl.text
            // RescriptNameIndex covers let/type/module/external/exception.
            // Filter to declarations whose head is `type <typeName>`
            // so we don't accidentally pick up a let-binding with the
            // same identifier (e.g. `let color = ...`).
            if (!matchesTypeHead(text, typeName)) return@processElements true
            val shape = RescriptTypeDeclarationParser.parse(text)
            if (shape is TypeShape.Variant) {
                found =
                    shape.constructors.map {
                        RescriptLspSignatureParser.VariantInfo(
                            name = it.name,
                            hasPayload = !it.payload.isNullOrBlank(),
                        )
                    }
                false // stop iteration
            } else {
                true
            }
        }
        return found
    }

    /**
     * Cheap textual check that [declText] starts with `type <name>`.
     *
     * Lets us prune name-index hits whose declared kind is not a
     * type alias without invoking the (more expensive) declaration
     * parser on every hit.
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
        // Match `<typeName>` followed by either whitespace, `=`, `<`
        // (type parameters), or end-of-text. This rejects `type colors`
        // when searching for `color`.
        val tail = rest.substring(nameStart)
        if (!tail.startsWith(typeName)) return false
        val after = tail.getOrNull(typeName.length) ?: return true
        return after.isWhitespace() || after == '=' || after == '<' || after == '('
    }
}
