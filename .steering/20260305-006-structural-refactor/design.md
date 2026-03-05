# Design: Structural Refactoring

## Analysis

After reviewing the 4 process execution sites:

1. **LspServerDescriptor.tryExec** — runs `which`/`where`, reads first line, checks file exists
2. **DtsNodeDetector.isNodeAvailable** — runs `node --version`, reads first line, checks output
3. **RescriptReplExecutor.runWithNode** — complex multi-step compile+run with temp files
4. **DtsParserProcess.parse** — uses `GeneralCommandLine`, not `ProcessBuilder`

Sites #1 and #2 share a common "run simple command, read first line, check result" pattern. Sites #3 and #4 are fundamentally different (multi-step, stream threading) and don't benefit from extraction.

For `RescriptConfigurable` (366 lines): it's pure Swing UI boilerplate. Splitting into panels adds indirection without practical benefit — the methods are straightforward field reads/writes. **Deferring the split** — it's exempt from tests and not a maintenance pain point.

## 1. RescriptProcessUtils

Extract the shared "run simple command with timeout" pattern.

```kotlin
object RescriptProcessUtils {
    data class ProcessResult(
        val exitCode: Int,
        val stdout: String,
        val timedOut: Boolean,
    )

    fun runSimpleCommand(
        vararg command: String,
        timeoutSeconds: Long = RescriptSecurityUtils.PROCESS_TIMEOUT_SECONDS,
    ): ProcessResult
}
```

### Migration

- `LspServerDescriptor.tryExec` → `RescriptProcessUtils.runSimpleCommand`
- `DtsNodeDetector.isNodeAvailable` → `RescriptProcessUtils.runSimpleCommand`

### Not migrated (too different)

- `RescriptReplExecutor` — multi-step with temp files, compile+run
- `DtsParserProcess` — GeneralCommandLine, stderr thread, large output

## 2. RescriptProcessUtilsTest (JUnit)

| Test | Description |
|------|-------------|
| `testRunSimpleCommandReturnsOutput` | Run `echo hello`, verify stdout |
| `testRunSimpleCommandExitCode` | Run failing command, verify non-zero exit |
| `testRunSimpleCommandTimeout` | Run with 1s timeout on `sleep`, verify timedOut |

## No production file changes for RescriptConfigurable

Deferred — pure UI, low ROI, exempt from testing.
