// Verify that dispatch input stays data and cannot become shell code or Gradle options.
import { strict as assert } from "node:assert";
import { spawnSync } from "node:child_process";
import { chmodSync, existsSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import test from "node:test";

const script = fileURLToPath(new URL("../verify-ide.sh", import.meta.url));
const fixedArguments = ["verifyPlugin", "--info", "-PverificationMaxHeap=2g", "--no-parallel", "--max-workers=1"];

function run(version, product, check, exitCode = 0) {
  const directory = mkdtempSync(join(tmpdir(), "verify-ide-test-"));
  try {
    const wrapper = join(directory, "gradlew");
    writeFileSync(wrapper, `#!/usr/bin/env node
process.stdout.write(JSON.stringify(process.argv.slice(2)));
process.exit(Number(process.env.STUB_EXIT || 0));
`);
    chmodSync(wrapper, 0o755);
    const result = spawnSync("bash", [script], {
      cwd: directory,
      encoding: "utf8",
      env: { ...process.env, STUB_EXIT: String(exitCode), VERIFICATION_IDE_VERSION: version, VERIFICATION_IDE_TYPE: product },
    });
    assert.ifError(result.error);
    check(result, directory);
  } finally {
    rmSync(directory, { recursive: true, force: true });
  }
}

for (const version of ["2026.1.4", "2026.2.3", "263.6259.32", "263.6259.32-EAP-CANDIDATE"]) {
  test(`one IDE is forwarded as exactly one quoted property: ${version}`, () => {
    run(version, "IU", (result) => {
      assert.equal(result.status, 0, result.stderr);
      assert.deepEqual(JSON.parse(result.stdout), [...fixedArguments, `-PverificationIde=${version}`, "-PverificationIdeType=IU"]);
    });
  });
}

test("empty scheduled input retains default matrix and constrained JVM/worker flags", () => {
  run("", "", (result) => {
    assert.equal(result.status, 0, result.stderr);
    assert.deepEqual(JSON.parse(result.stdout), fixedArguments);
  });
});

test("alternate product stays one property", () => {
  run("2026.2.3", "WS", (result) => {
    assert.equal(result.status, 0, result.stderr);
    assert.deepEqual(JSON.parse(result.stdout), [...fixedArguments, "-PverificationIde=2026.2.3", "-PverificationIdeType=WS"]);
  });
});

for (const version of ["--init-script=evil", "2026.1.4 --scan", "$(touch injected)", "2026.1.4; touch injected", "2026.1.4\ntouch injected"]) {
  test(`invalid version cannot run a task or shell payload: ${JSON.stringify(version)}`, () => {
    run(version, "IU", (result, directory) => {
      assert.equal(result.status, 2);
      assert.equal(result.stdout, "");
      assert.equal(existsSync(join(directory, "injected")), false);
    });
  });
}

test("unsupported product cannot become a Gradle option", () => {
  run("2026.2.3", "IU --scan", (result) => {
    assert.equal(result.status, 2);
    assert.equal(result.stdout, "");
  });
});

test("a verifier failure remains a failed workflow step", () => {
  run("2026.2.3", "IU", (result) => {
    assert.equal(result.status, 7);
  }, 7);
});
