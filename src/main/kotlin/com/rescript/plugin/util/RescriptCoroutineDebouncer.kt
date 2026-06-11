package com.rescript.plugin.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Cancel-and-restart debouncer backed by coroutines, replacing the
 * `com.intellij.util.Alarm` pattern (whose `POOLED_THREAD` mode is
 * `@ApiStatus.Internal`).
 *
 * Each [schedule] call cancels the pending action (if any) and arms a
 * new one that runs after [delayMs] on [context] — `Dispatchers.EDT`
 * for UI-thread work, `Dispatchers.Default` for background work. The
 * owning scope (typically [RescriptCoroutineScopeService.scope])
 * bounds the lifetime; [cancel] additionally drops the pending action
 * when the owning component is disposed earlier.
 *
 * @param scope parent scope whose cancellation stops all pending work
 * @param delayMs debounce window in milliseconds
 * @param context dispatcher the action runs on
 */
internal class RescriptCoroutineDebouncer(
    private val scope: CoroutineScope,
    private val delayMs: Long,
    private val context: CoroutineContext = EmptyCoroutineContext,
) {
    private var job: Job? = null

    /**
     * Cancels any pending action and schedules [action] to run after
     * the debounce window.
     */
    @Synchronized
    fun schedule(action: suspend () -> Unit) {
        job?.cancel()
        job =
            scope.launch(context) {
                delay(delayMs)
                action()
            }
    }

    /** Cancels the pending action, if any. */
    @Synchronized
    fun cancel() {
        job?.cancel()
        job = null
    }
}
