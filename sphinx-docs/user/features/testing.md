# Testing

The plugin provides integrated test running for ReScript projects that use Jest or Vitest. Tests are run against the compiled JavaScript output, with results displayed in IntelliJ's standard test runner UI and file paths mapped back to the original `.res` source files.

## Supported Test Frameworks

| Framework | Detection Method | Default Command |
|-----------|-----------------|-----------------|
| **Jest** | `jest` in `package.json` dependencies | `npx jest` |
| **Vitest** | `vitest` in `package.json` dependencies | `npx vitest run` |
| **Custom** | Manual configuration | User-defined |

The plugin automatically detects which framework your project uses. If both are present, Vitest takes priority.

Built-in framework detection means you can start running tests immediately without any manual plugin configuration — just install your preferred test framework and the plugin handles the rest.

### Jest Configuration

To use Jest with ReScript, you need the following setup:

**1. Install dependencies:**

```json
{
  "devDependencies": {
    "jest": "^29.0.0",
    "jest-teamcity": "^1.9.0",
    "@glennsl/rescript-jest": "^0.11.0"
  }
}
```

The `jest-teamcity` reporter is required for the plugin to parse test results and display them in the test tree UI. Without it, test output appears as plain console text without structured results.

**2. Configure Jest for ReScript:**

Create a `jest.config.js` (or `jest.config.ts`) that points to the compiled JavaScript output:

```json
{
  "testMatch": ["**/lib/js/test/**/*_test.bs.js", "**/lib/js/test/**/*Test.bs.js"],
  "moduleDirectories": ["node_modules"],
  "transform": {}
}
```

Key points for the Jest configuration:

- **testMatch** -- Point to the compiled `.bs.js` files in the `lib/js/` directory, not the `.res` source files. Jest runs JavaScript, so it needs the compiled output.
- **transform** -- Set to `{}` (empty object) to disable all transforms. ReScript already compiles to JavaScript, so no additional transpilation is needed.
- **moduleDirectories** -- Ensure `node_modules` is included for dependency resolution.

The exact path under `lib/` depends on your `rescript.json` configuration. Common patterns include:

| `rescript.json` suffix setting | Compiled output path |
|-------------------------------|---------------------|
| `".bs.js"` (default) | `lib/js/src/MyModule.bs.js` |
| `".mjs"` | `lib/js/src/MyModule.mjs` |
| `".js"` | `lib/js/src/MyModule.js` |

### Vitest Configuration

To use Vitest with ReScript, you need the following setup:

**1. Install dependencies:**

```json
{
  "devDependencies": {
    "vitest": "^1.0.0"
  }
}
```

Vitest has built-in TeamCity reporter support, so no additional reporter package is needed.

**2. Configure Vitest for ReScript:**

Create a `vitest.config.ts` (or `vitest.config.js`):

```json
{
  "test": {
    "include": ["lib/js/test/**/*_test.bs.js", "lib/js/test/**/*Test.bs.js"],
    "passWithNoTests": true
  }
}
```

Key points for the Vitest configuration:

- **include** -- Similar to Jest, point to the compiled JavaScript files in `lib/js/`.
- **passWithNoTests** -- Recommended to avoid failures when no tests match (e.g., during initial setup).
- Vitest is invoked with the `run` flag (`npx vitest run`) to execute tests once and exit, rather than entering watch mode.

## Running Tests

### From the Editor

1. Open a ReScript test file (`.res`)
2. Right-click on a test function
3. Select **Run** to execute the test

### From Run Configurations

1. **Run** → **Edit Configurations** → **+** → **ReScript Test**
2. Select the test framework (Jest, Vitest, or Custom)
3. Optionally set a test file path and test name filter
4. Set the working directory (defaults to the project root)
5. Click **Run**

### Configuration Options

| Option | Description |
|--------|-------------|
| **Framework** | Jest, Vitest, or Custom |
| **Working directory** | Project root where `package.json` is located |
| **Test file path** | Path to a specific test file (leave empty to run all tests) |
| **Test name** | Filter tests by name (uses the `-t` flag) |
| **Additional arguments** | Extra CLI flags passed to the test runner |

### From Context Menu

Right-click on a test file in the Project panel and select **Run**. This creates a run configuration automatically if the file name matches the test naming convention.

Multiple ways to run tests — from the editor, Project panel, or run configurations — let you choose the approach that fits your workflow, whether you are running a single test or the entire suite.

## Test Auto-Detection

The plugin uses two mechanisms to detect test files and frameworks:

### Framework Detection

The `RescriptTestFrameworkDetector` identifies which test framework your project uses by examining:

1. **`package.json` dependencies** -- Checks both `dependencies` and `devDependencies` for `vitest` or `jest`. If `vitest` is found, it takes priority over `jest`.
2. **Configuration files** -- If `package.json` detection fails, the detector looks for framework-specific config files in the project root:

| Config file | Framework |
|-------------|-----------|
| `vitest.config.ts` | Vitest |
| `vitest.config.js` | Vitest |
| `vitest.config.mts` | Vitest |
| `jest.config.js` | Jest |
| `jest.config.ts` | Jest |
| `jest.config.mjs` | Jest |

### Test File Detection

The plugin automatically recognizes files as test files based on their naming convention. A `.res` or `.resi` file is treated as a test file if its name (without extension) ends with any of these suffixes:

- `_test` (e.g., `Math_test.res`)
- `Test` (e.g., `MathTest.res`)
- `_spec` (e.g., `Math_spec.res`)
- `Spec` (e.g., `MathSpec.res`)

When you right-click on a file matching this pattern, the plugin offers to create a **ReScript Test** run configuration automatically, pre-filled with the detected framework and file path.

Automatic detection of test frameworks and test files eliminates manual configuration — the plugin recognizes your testing setup and offers the right run options out of the box.

## Test Results

Test results are displayed in the standard IntelliJ test runner UI (SMTestRunner).

### Test Tree Structure

The test results panel displays a hierarchical tree:

- **Root** -- The test run
  - **Test Suite** -- Corresponds to a `describe` block (or a test file)
    - **Test Case** -- Individual test (`test` or `it` block)

Each node shows:

- **Pass/fail indicator** -- Green checkmark for passing tests, red cross for failures
- **Test name** -- The name string from your `test()` or `it()` call
- **Duration** -- Execution time for each test
- **Output** -- Stdout/stderr captured during the test

### TeamCity Reporter Integration

The plugin relies on TeamCity-format reporters to parse test results into the structured tree UI:

- **Jest**: Uses the `jest-teamcity` reporter. The plugin automatically adds `--reporters=default --reporters=jest-teamcity` to the Jest command line.
- **Vitest**: Uses the built-in `teamcity` reporter. The plugin automatically adds `--reporter=default --reporter=teamcity` to the Vitest command line.

The `default` reporter is included alongside the TeamCity reporter so that human-readable output still appears in the console.

### Source File Navigation

When you click on a test in the results tree, the plugin navigates to the corresponding source file. The `RescriptTestLocator` resolves test file locations using three strategies:

1. **Direct relative path** -- Looks for the file relative to the project root
2. **Compiled JS path conversion** -- Converts paths like `lib/js/src/MyTest.bs.js` back to `src/MyTest.res` by stripping the `lib/js/` (or `lib/es6/`, `lib/bs/`) prefix and replacing the `.bs.js` extension with `.res`
3. **Absolute path** -- Resolves absolute file system paths directly

This path mapping ensures that clicking a test result opens the original `.res` source file, not the compiled JavaScript file.

The structured test tree with pass/fail indicators and source file navigation gives you the same rich testing experience as native JetBrains language plugins, even though ReScript tests run through compiled JavaScript.

## How It Works

1. You write tests in `.res` files using your test framework's bindings (e.g., `@glennsl/rescript-jest`)
2. The ReScript compiler compiles your test `.res` files to JavaScript in the `lib/` folder
3. The plugin runs the compiled `.js` files with Jest or Vitest, including TeamCity reporters
4. Test output is parsed by IntelliJ's SMTestRunner into a structured test tree
5. File paths in test results are mapped back to the original `.res` source files

```rescript
// src/test/Math_test.res
open Jest

describe("Math utilities", () => {
  test("addition", () => {
    expect(MathUtils.add(1, 2))->toBe(3)
  })

  test("multiplication", () => {
    expect(MathUtils.multiply(3, 4))->toBe(12)
  })
})
```

The plugin bridges the gap between ReScript source files and compiled JavaScript test execution, so you work entirely in `.res` files while the testing infrastructure handles the compilation and path mapping transparently.

## Troubleshooting

### Tests Fail with "Module not found"

**Cause**: The ReScript compiler has not compiled the test files before the test runner executes.

**Solution**: Run a ReScript **Build** (or ensure **Build (Watch)** is running) before executing tests. The test runner executes the compiled JavaScript, so the `.bs.js` files must exist in the `lib/` directory.

```
# Compile first, then run tests
npx rescript build
npx jest
```

:::{tip}
Keep a **Build (Watch)** run configuration running during development. This ensures compiled output is always up to date when you run tests.
:::

### Test Tree Shows Plain Text Instead of Structured Results

**Cause**: The TeamCity reporter is not installed or not configured.

**Solution**:
- For Jest: Install `jest-teamcity` as a dev dependency (`npm install --save-dev jest-teamcity`)
- For Vitest: Ensure you are using Vitest 1.0 or later, which includes the built-in TeamCity reporter

The plugin adds the reporter flags automatically, but the reporter package must be installed in your project.

### Path Mapping Failures (Cannot Navigate to Source)

**Cause**: The test locator cannot find the `.res` file corresponding to a compiled `.js` file.

**Solution**: Ensure your project structure follows the standard ReScript convention where the `lib/js/` output mirrors the `src/` directory structure. The path converter expects patterns like:

| Compiled JS path | Resolved `.res` path |
|-----------------|---------------------|
| `lib/js/src/MyTest.bs.js` | `src/MyTest.res` |
| `lib/es6/src/MyTest.mjs` | `src/MyTest.res` |
| `lib/js/test/Math_test.bs.js` | `test/Math_test.res` |

If your `rescript.json` uses a non-standard output directory, the path mapping may not work correctly.

### "package.json not found" Error

**Cause**: The working directory in the run configuration does not contain a `package.json` file.

**Solution**: Set the **Working directory** in the run configuration to your project root (the directory containing `package.json` and `rescript.json`). This is typically auto-detected, but may need manual adjustment in monorepo setups.

### Framework Not Detected

**Cause**: The test framework is not listed in `package.json` dependencies.

**Solution**: Ensure `jest` or `vitest` appears in either `dependencies` or `devDependencies` in your `package.json`. Alternatively, select the framework manually in the run configuration, or use the **Custom** framework option with a user-defined command.

:::{note}
Test running requires that your ReScript project is properly configured with a test framework and that the ReScript compiler has already compiled the test files. The plugin does not automatically trigger a ReScript build before running tests.
:::

These troubleshooting tips address the most common test setup issues, saving you from debugging the build pipeline when the fix is usually a missing dependency or a stale compilation.
