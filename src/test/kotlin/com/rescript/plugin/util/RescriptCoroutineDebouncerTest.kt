package com.rescript.plugin.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Tests for the coroutine-based debouncer.
 *
 * Uses a real [Dispatchers.Default] scope with generous latch timeouts
 * instead of kotlinx-coroutines-test, so no extra test dependency is
 * needed; the debounce window in the tests is small (50 ms) while the
 * assertions allow seconds, keeping the suite deterministic.
 */
class RescriptCoroutineDebouncerTest {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @AfterEach
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `scheduled action runs after the debounce window`() {
        val debouncer = RescriptCoroutineDebouncer(scope, delayMs = 50)
        val latch = CountDownLatch(1)
        debouncer.schedule { latch.countDown() }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "action did not run")
    }

    @Test
    fun `rescheduling cancels the pending action so only the last one runs`() {
        val debouncer = RescriptCoroutineDebouncer(scope, delayMs = 200)
        val executed = ConcurrentLinkedQueue<String>()
        val latch = CountDownLatch(1)
        debouncer.schedule { executed.add("first") }
        debouncer.schedule { executed.add("second") }
        debouncer.schedule {
            executed.add("third")
            latch.countDown()
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "final action did not run")
        assertEquals(listOf("third"), executed.toList())
    }

    @Test
    fun `cancel drops the pending action`() {
        val debouncer = RescriptCoroutineDebouncer(scope, delayMs = 100)
        val calls = AtomicInteger(0)
        debouncer.schedule { calls.incrementAndGet() }
        debouncer.cancel()
        // Wait well past the debounce window; the cancelled action must not fire.
        Thread.sleep(500)
        assertEquals(0, calls.get())
    }

    @Test
    fun `cancelling the owning scope stops pending actions`() {
        val localScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val debouncer = RescriptCoroutineDebouncer(localScope, delayMs = 100)
        val calls = AtomicInteger(0)
        debouncer.schedule { calls.incrementAndGet() }
        localScope.cancel()
        Thread.sleep(500)
        assertEquals(0, calls.get())
    }
}
