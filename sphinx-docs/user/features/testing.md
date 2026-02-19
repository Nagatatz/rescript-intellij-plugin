# Testing

The plugin provides integrated test running for ReScript projects that use Jest or Vitest.

## Supported Test Frameworks

| Framework | Detection |
|-----------|-----------|
| **Jest** | Detected via `jest` in `package.json` dependencies |
| **Vitest** | Detected via `vitest` in `package.json` dependencies |

The plugin automatically detects which framework your project uses.

## Running Tests

### From the Editor

1. Open a ReScript test file (`.res`)
2. Right-click on a test function
3. Select **Run** to execute the test

### From Run Configurations

1. **Run** → **Edit Configurations** → **+** → **ReScript Test**
2. Configure the test file or directory
3. Click **Run**

### From Context Menu

Right-click on a test file in the Project panel and select **Run**.

## Test Results

Test results are displayed in the standard IntelliJ test runner UI:

- **Test tree** — Hierarchical view of test suites and cases
- **Pass/fail indicators** — Green checkmarks and red crosses
- **Output** — Stdout/stderr for each test
- **Duration** — Execution time per test

## How It Works

1. The plugin compiles your `.res` test file to JavaScript
2. It runs the compiled `.js` file with Jest or Vitest
3. Test output is parsed and displayed in the SMTestRunner UI
4. File paths in test results are mapped back to the original `.res` source files

:::{note}
Test running requires that your ReScript project is properly configured with a test framework and that the ReScript compiler has already compiled the test files.
:::
