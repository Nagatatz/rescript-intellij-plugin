# Design: Code Quality Fixes

## 3A. Silent Exception Logging

### Approach

Add `LOG.debug(...)` to silent catch blocks to preserve error information for debugging while keeping the existing control flow unchanged. For `RescriptErrorReporter`, use `LOG.trace` since the error reporter itself must never throw.

### Changes by File

| File | Current | Change |
|------|---------|--------|
| `DtsParserProcess.kt:62` | `catch (_: IOException) {}` | `catch (e: IOException) { LOG.debug("stderr reader failed", e) }` |
| `DtsNodeDetector.kt:82` | `catch (_: Exception) { false }` | `catch (e: Exception) { LOG.debug("Node.js availability check failed", e); false }` |
| `RescriptPasteAsJsonAction.kt:46` | `catch (_: Exception) { return }` | Add LOG + `catch (e: Exception) { LOG.debug(...); return }` |
| `RescriptSignatureSyncInspection.kt:45` | `catch (_: Exception) { return }` | Add LOG + `catch (e: Exception) { LOG.debug(...); return }` |
| `RescriptSignatureSyncInspection.kt:89` | `catch (_: Exception) {}` | Add LOG + `catch (e: Exception) { LOG.debug(...) }` |
| `RescriptErrorReporter.kt:182` | `catch (_: Exception) { "unknown" }` | `catch (e: Exception) { LOG.trace("Failed to get plugin version", e); "unknown" }` |
| `RescriptErrorReporter.kt:193` | `catch (_: Exception) { "unknown" }` | `catch (e: Exception) { LOG.trace("Failed to get IDE version", e); "unknown" }` |

### LOG Instance

Files that don't have a LOG instance need one added:
- `DtsNodeDetector.kt` — add `private val LOG = logger<DtsNodeDetector>()`
- `RescriptPasteAsJsonAction.kt` — add companion object with LOG
- `RescriptSignatureSyncInspection.kt` — add LOG to existing companion object
- `RescriptErrorReporter.kt` — add LOG to existing companion object

Files that already have LOG: `DtsParserProcess.kt` (line 24).

## 3B. InterruptedException Fix

### Problem

In `RescriptReplExecutor.runWithNode()`, `waitFor()` throws `InterruptedException` which is caught by the generic `catch (e: Exception)` at line 38 without restoring the thread's interrupt status. This violates the Java concurrency contract.

### Solution

Add explicit `InterruptedException` handling before each `waitFor()` call:

```kotlin
val compileCompleted = try {
    compileProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
} catch (e: InterruptedException) {
    compileProcess.destroyForcibly()
    Thread.currentThread().interrupt()
    return "Error: interrupted"
}
```

Same pattern for `runProcess.waitFor()`.

### Test

Add a test to `RescriptReplExecutorTest.kt` that verifies the `execute()` method returns an error message for invalid paths (already exists) — the InterruptedException handling is in private `runWithNode()` which is difficult to test directly without mocking ProcessBuilder. The existing test suite covers the public API adequately.

## No New Files

This unit modifies only existing files. No new test files needed since:
- Logging changes are not unit-testable
- InterruptedException fix is in a private method; existing tests cover the public API
- RescriptReplExecutorTest already exists
