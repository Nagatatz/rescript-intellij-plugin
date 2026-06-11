package com.rescript.plugin.util

import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope

/**
 * Project-level holder for the platform-injected [CoroutineScope].
 *
 * The IntelliJ Platform supplies the scope through the light service
 * constructor and cancels it when the project closes, so coroutines
 * launched from it never outlive the project. Used by the tool window
 * panels' [RescriptCoroutineDebouncer] instances.
 *
 * @see RescriptCoroutineDebouncer
 */
@Service(Service.Level.PROJECT)
internal class RescriptCoroutineScopeService(
    val scope: CoroutineScope,
)
