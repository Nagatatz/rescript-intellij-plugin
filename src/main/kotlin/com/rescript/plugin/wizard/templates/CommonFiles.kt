package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.Database
import com.rescript.plugin.wizard.PackageManager

/**
 * Shared file generators for README, .gitignore, .editorconfig, and CI workflow files.
 *
 * These files are identical in spirit across every template and only vary in small
 * details (project description, script names, extra ignore patterns). Centralizing
 * them here keeps individual template files focused on project-specific content.
 */
object CommonFiles {
    /**
     * Generates a `.gitignore` file with common ReScript, Node.js, and OS ignores.
     *
     * @param extra additional patterns to append (e.g. `dist/`, `.next/`, `.wrangler/`)
     */
    fun gitignore(extra: List<String> = emptyList()): String =
        buildString {
            appendLine("# Dependencies")
            appendLine("node_modules/")
            appendLine()
            appendLine("# ReScript build artifacts")
            appendLine("lib/")
            appendLine("*.res.js")
            appendLine("*.res.mjs")
            appendLine()
            appendLine("# Test coverage reports")
            appendLine("coverage/")
            appendLine()
            appendLine("# Logs")
            appendLine("*.log")
            appendLine("npm-debug.log*")
            appendLine("pnpm-debug.log*")
            appendLine("yarn-debug.log*")
            appendLine("yarn-error.log*")
            appendLine("bun-debug.log*")
            appendLine()
            appendLine("# OS")
            appendLine(".DS_Store")
            appendLine("Thumbs.db")
            appendLine()
            appendLine("# Editor")
            appendLine(".idea/")
            appendLine(".vscode/")
            appendLine("*.swp")
            if (extra.isNotEmpty()) {
                appendLine()
                appendLine("# Project-specific")
                extra.forEach { appendLine(it) }
            }
        }

    /**
     * Generates a standard `.editorconfig` for ReScript projects (2-space indent, LF).
     */
    fun editorconfig(): String =
        """
        root = true

        [*]
        indent_style = space
        indent_size = 2
        end_of_line = lf
        charset = utf-8
        trim_trailing_whitespace = true
        insert_final_newline = true

        [*.md]
        trim_trailing_whitespace = false
        """.trimIndent() + "\n"

    /**
     * Generates a README.md for a template project.
     *
     * @param ctx template context (provides project name and PM-specific commands)
     * @param description short description shown under the title
     * @param scripts list of (script name, human-readable description) pairs to document
     * @param extraSections optional extra Markdown sections appended after the scripts table
     * @param extraPrerequisites extra bullet lines appended to the Prerequisites section
     *   (e.g. "Bun 1.3 or later (install from https://bun.sh)" for the res-x template)
     */
    fun readme(
        ctx: TemplateContext,
        description: String,
        scripts: List<Pair<String, String>>,
        extraSections: List<Pair<String, String>> = emptyList(),
        extraPrerequisites: List<String> = emptyList(),
    ): String =
        buildString {
            appendLine("# ${ctx.projectName}")
            appendLine()
            appendLine(description)
            appendLine()
            appendLine("## Prerequisites")
            appendLine()
            appendLine("- Node.js ${ctx.nodeEngine.removePrefix(">=")}+")
            if (ctx.packageManager == PackageManager.BUN) {
                appendLine(
                    "- Bun ${TemplateVersions.BUN} or later (install from https://bun.sh)",
                )
            } else {
                appendLine("- ${packageManagerName(ctx.packageManager)} (managed via Corepack)")
            }
            extraPrerequisites.forEach { appendLine("- $it") }
            appendLine()
            appendLine("## Getting Started")
            appendLine()
            appendLine("```bash")
            appendLine(ctx.installCmd())
            if (scripts.any { it.first == "dev" }) {
                appendLine(ctx.runCmd("dev"))
            } else if (scripts.any { it.first == "res:dev" }) {
                appendLine(ctx.runCmd("res:dev"))
            }
            appendLine("```")
            appendLine()
            appendLine("## Scripts")
            appendLine()
            appendLine("| Command | Description |")
            appendLine("| --- | --- |")
            scripts.forEach { (name, desc) ->
                appendLine("| `${ctx.runCmd(name)}` | $desc |")
            }
            appendLine()
            extraSections.forEach { (heading, body) ->
                appendLine("## $heading")
                appendLine()
                appendLine(body.trimEnd())
                appendLine()
            }
            appendLine("## Extending Bindings")
            appendLine()
            appendLine(TemplateResourceLoader.load("common/readme/extending-bindings.md").trimEnd())
            appendLine()
            appendLine("## Learn More")
            appendLine()
            appendLine("- [ReScript documentation](https://rescript-lang.org/)")
            appendLine("- [ReScript IntelliJ plugin](https://plugins.jetbrains.com/plugin/com.rescript.plugin)")
        }

    /**
     * Generates a minimal GitHub Actions workflow that installs dependencies and
     * runs the ReScript compiler plus an optional project build/test script.
     *
     * @param ctx template context (used for package-manager-specific install step)
     * @param hasBuild whether the project exposes a top-level `build` script to run
     * @param hasTest whether the project exposes a top-level `test` script to run
     * @param setupBun when true, inserts an `oven-sh/setup-bun` step after `setup-node`
     *   so templates whose scripts invoke `bun` (e.g. res-x) have the binary available
     */
    fun ciWorkflow(
        ctx: TemplateContext,
        hasBuild: Boolean = false,
        hasTest: Boolean = false,
        setupBun: Boolean = false,
    ): String =
        buildString {
            val needsBun = setupBun || ctx.packageManager == PackageManager.BUN
            appendLine("name: CI")
            appendLine()
            appendLine("on:")
            appendLine("  push:")
            appendLine("    branches: [main]")
            appendLine("  pull_request:")
            appendLine("    branches: [main]")
            appendLine()
            appendLine("jobs:")
            appendLine("  build:")
            appendLine("    runs-on: ubuntu-latest")
            appendLine("    steps:")
            appendLine("      - uses: actions/checkout@v6")
            appendLine("      - uses: actions/setup-node@v6")
            appendLine("        with:")
            appendLine("          node-version: ${ctx.nodeMajor}")
            if (needsBun) {
                appendLine("      - uses: oven-sh/setup-bun@v2")
                appendLine("        with:")
                appendLine("          bun-version: latest")
            }
            if (ctx.packageManager == PackageManager.PNPM) {
                appendLine("      - uses: pnpm/action-setup@v6")
                appendLine("        with:")
                appendLine("          version: ${TemplateVersions.PNPM.substringBefore('.')}")
            }
            appendLine("      - name: Install dependencies")
            appendLine("        run: ${ctx.installCmd()}")
            appendLine("      - name: ReScript build")
            appendLine("        run: ${ctx.execCmd("rescript")}")
            if (hasBuild) {
                appendLine("      - name: Build")
                appendLine("        run: ${ctx.runCmd("build")}")
            }
            if (hasTest) {
                appendLine("      - name: Test")
                appendLine("        run: ${ctx.runCmd("test")}")
            }
        }

    /**
     * Generates a multi-stage `Dockerfile` for Node-based backend templates.
     *
     * Produces a `builder` stage that installs all deps and runs `rescript`, plus a
     * `runtime` stage that reinstalls production-only deps and copies the compiled
     * `src/` from the builder. Runs as the unprivileged `node` (or `bun`) user that
     * ships with the official base images. `--ignore-scripts` is passed to every
     * install command so transitive lifecycle scripts cannot execute arbitrary code
     * during the build.
     *
     * Suitable for templates whose runtime artifact is a compiled `.res.mjs` server
     * file (no client bundling step). Templates with a Vite/Vite+ client build need
     * a different multi-stage layout — see `res-x/Dockerfile` for an example.
     *
     * @param ctx template context (drives package manager + Node major)
     * @param port port the server listens on (used for `EXPOSE`)
     * @param entryPoint relative path to the compiled server entry, defaults to
     *   `src/ServerMain.res.mjs` to match the convention used by Hono templates
     */
    fun serverDockerfile(
        ctx: TemplateContext,
        port: Int,
        entryPoint: String = "src/ServerMain.res.mjs",
    ): String {
        val isBun = ctx.packageManager == PackageManager.BUN
        val baseImage = if (isBun) "oven/bun:1-slim" else "node:${ctx.nodeMajor}-slim"
        val builderInstall =
            when (ctx.packageManager) {
                PackageManager.NPM -> "npm install --ignore-scripts"
                PackageManager.PNPM -> "corepack enable && pnpm install --ignore-scripts"
                PackageManager.YARN -> "corepack enable && yarn install --ignore-scripts"
                PackageManager.BUN -> "bun install --ignore-scripts"
            }
        val runtimeInstall =
            when (ctx.packageManager) {
                PackageManager.NPM -> "npm install --omit=dev --ignore-scripts"
                PackageManager.PNPM -> "corepack enable && pnpm install --prod --ignore-scripts"
                PackageManager.YARN -> "corepack enable && yarn install --production --ignore-scripts"
                PackageManager.BUN -> "bun install --production --ignore-scripts"
            }
        val runtimeUser = if (isBun) "bun" else "node"
        val runner = if (isBun) "bun" else "node"
        return buildString {
            appendLine("# syntax=docker/dockerfile:1.7")
            appendLine()
            appendLine("# --- Builder stage: install all deps + compile ReScript ---")
            appendLine("FROM $baseImage AS builder")
            appendLine("WORKDIR /app")
            appendLine("COPY package*.json ./")
            appendLine("RUN $builderInstall")
            appendLine("COPY . .")
            appendLine("RUN ${ctx.execCmd("rescript")}")
            appendLine()
            appendLine("# --- Runtime stage: prod deps + compiled output only ---")
            appendLine("FROM $baseImage AS runtime")
            appendLine("ENV NODE_ENV=production")
            appendLine("WORKDIR /app")
            appendLine("COPY package*.json ./")
            appendLine("RUN $runtimeInstall")
            appendLine("COPY --from=builder /app/src ./src")
            appendLine("USER $runtimeUser")
            appendLine("EXPOSE $port")
            append("CMD [\"$runner\", \"$entryPoint\"]")
        }
    }

    /**
     * Generates a `compose.yaml` (Compose Specification) that runs the development
     * database for templates that target PostgreSQL or MySQL.
     *
     * The Compose file is intentionally minimal: it spins up the database only and
     * leaves the application running on the host. This avoids the bind-mount I/O
     * penalty that hurts HMR on macOS and Windows while still removing the host
     * install requirement for the database. Returns null for [Database.LIBSQL]
     * because libSQL is a file-based SQLite client with no service to run.
     *
     * Connection strings emitted by the template's `Db.res` and `.env.example`
     * match the credentials and ports declared here so `pnpm dev` can reach the
     * database without further configuration.
     */
    fun composeYaml(database: Database): String? =
        when (database) {
            Database.LIBSQL -> {
                null
            }

            Database.POSTGRES -> {
                """
                # Local development database. Start with `docker compose up -d`, then
                # run the app on the host (e.g. `pnpm dev`). The application
                # connects to localhost:5432 — see Db.res for the URL.
                services:
                  db:
                    image: ${TemplateVersions.POSTGRES_DOCKER_IMAGE}
                    environment:
                      POSTGRES_USER: app
                      POSTGRES_PASSWORD: dev
                      POSTGRES_DB: app
                    ports:
                      - "5432:5432"
                    volumes:
                      - pgdata:/var/lib/postgresql/data
                    healthcheck:
                      test: ["CMD", "pg_isready", "-U", "app"]
                      interval: 5s
                      timeout: 3s
                      retries: 5

                volumes:
                  pgdata:
                """.trimIndent() + "\n"
            }

            Database.MYSQL -> {
                """
                # Local development database. Start with `docker compose up -d`, then
                # run the app on the host (e.g. `pnpm dev`). The application
                # connects to localhost:3306 — see Db.res for the URL.
                services:
                  db:
                    image: ${TemplateVersions.MYSQL_DOCKER_IMAGE}
                    environment:
                      MYSQL_ROOT_PASSWORD: dev
                      MYSQL_DATABASE: app
                    ports:
                      - "3306:3306"
                    volumes:
                      - mysqldata:/var/lib/mysql
                    healthcheck:
                      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-pdev"]
                      interval: 5s
                      timeout: 3s
                      retries: 10

                volumes:
                  mysqldata:
                """.trimIndent() + "\n"
            }
        }

    /**
     * Resource path under `src/main/resources/templates/` for the shared `Db.res`
     * appropriate to the selected [database]. The libSQL path stays at the legacy
     * `common/db/Db.res` so existing templates continue to work without a rename.
     */
    fun sharedDbResPath(database: Database): String =
        when (database) {
            Database.LIBSQL -> "common/db/Db.res"
            Database.POSTGRES -> "common/db/postgres/Db.res"
            Database.MYSQL -> "common/db/mysql/Db.res"
        }

    /**
     * Returns the npm package name for the database driver associated with [database],
     * along with the version string from [TemplateVersions]. Used by templates to
     * compose `package.json#dependencies`.
     */
    fun databaseDriver(database: Database): Pair<String, String> =
        when (database) {
            Database.LIBSQL -> "@libsql/client" to TemplateVersions.LIBSQL_CLIENT
            Database.POSTGRES -> "postgres" to TemplateVersions.POSTGRES_JS
            Database.MYSQL -> "mysql2" to TemplateVersions.MYSQL2
        }

    /**
     * Default DATABASE_URL value documented in `.env.example` for the selected database.
     * Matches the credentials baked into [composeYaml] so the dev loop works without
     * editing the env file.
     */
    fun defaultDatabaseUrl(database: Database): String =
        when (database) {
            Database.LIBSQL -> "DATABASE_URL=file:./data/app.db"
            Database.POSTGRES -> "DATABASE_URL=postgres://app:dev@localhost:5432/app"
            Database.MYSQL -> "DATABASE_URL=mysql://root:dev@localhost:3306/app"
        }

    /**
     * Generates a `.dockerignore` that prevents host-side artifacts (caches, IDE state,
     * git metadata, test fixtures, secrets) from inflating the build context shipped to
     * the daemon. Pairs with [serverDockerfile].
     */
    fun dockerignore(): String =
        """
        # VCS / IDE
        .git
        .github
        .idea
        .vscode

        # Node / package manager state
        node_modules
        npm-debug.log*
        yarn-debug.log*
        yarn-error.log*
        pnpm-debug.log*
        bun-debug.log*

        # ReScript build artifacts (regenerated inside the builder stage)
        lib
        .merlin

        # Test artifacts
        coverage
        .nyc_output

        # Local env / secrets — never bake into an image
        .env
        .env.*
        !.env.example
        """.trimIndent() + "\n"

    /**
     * Generates a `.nvmrc` file pinning the Node.js major version used by the template.
     *
     * Pairs with the `engines.node` field in `package.json` so tools like nvm, fnm, and
     * volta auto-switch when entering the project directory.
     *
     * @param ctx template context (provides [TemplateContext.nodeMajor])
     */
    fun nvmrc(ctx: TemplateContext): String = "${ctx.nodeMajor}\n"

    /**
     * Generates the standard MIT license text.
     *
     * @param ctx template context (provides [TemplateContext.year])
     * @param holder the copyright holder (typically the project or author name)
     */
    fun mitLicense(
        ctx: TemplateContext,
        holder: String,
    ): String =
        buildString {
            appendLine("MIT License")
            appendLine()
            appendLine("Copyright (c) ${ctx.year} $holder")
            appendLine()
            appendLine("Permission is hereby granted, free of charge, to any person obtaining a copy")
            appendLine("of this software and associated documentation files (the \"Software\"), to deal")
            appendLine("in the Software without restriction, including without limitation the rights")
            appendLine("to use, copy, modify, merge, publish, distribute, sublicense, and/or sell")
            appendLine("copies of the Software, and to permit persons to whom the Software is")
            appendLine("furnished to do so, subject to the following conditions:")
            appendLine()
            appendLine("The above copyright notice and this permission notice shall be included in all")
            appendLine("copies or substantial portions of the Software.")
            appendLine()
            appendLine("THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR")
            appendLine("IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,")
            appendLine("FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE")
            appendLine("AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER")
            appendLine("LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,")
            appendLine("OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE")
            append("SOFTWARE.")
        }

    /**
     * Generates a minimal Dependabot configuration that polls npm dependencies weekly.
     *
     * See https://docs.github.com/code-security/dependabot for the full schema if you
     * want to add GitHub Actions, docker, or terraform ecosystems.
     */
    fun dependabotYaml(): String =
        buildString {
            appendLine("version: 2")
            appendLine("updates:")
            appendLine("  - package-ecosystem: \"npm\"")
            appendLine("    directory: \"/\"")
            appendLine("    schedule:")
            appendLine("      interval: \"weekly\"")
            appendLine("    open-pull-requests-limit: 10")
            appendLine("  - package-ecosystem: \"github-actions\"")
            appendLine("    directory: \"/\"")
            appendLine("    schedule:")
            append("      interval: \"weekly\"")
        }

    /**
     * Generates a `.env.example` file documenting the environment variables the template reads.
     *
     * Each entry is rendered as an optional comment line followed by `KEY=value`. Pass the
     * comment as empty to omit it for that entry.
     *
     * @param entries list of (commentOrEmpty, keyEqualsValue) pairs
     */
    fun envExample(entries: List<Pair<String, String>>): String =
        buildString {
            entries.forEachIndexed { index, (comment, keyValue) ->
                if (index > 0) appendLine()
                if (comment.isNotEmpty()) {
                    comment.lines().forEach { line -> appendLine("# $line") }
                }
                append(keyValue)
                if (index < entries.size - 1) appendLine()
            }
        }

    private fun packageManagerName(pm: PackageManager): String =
        when (pm) {
            PackageManager.NPM -> "npm"
            PackageManager.PNPM -> "pnpm"
            PackageManager.YARN -> "Yarn"
            PackageManager.BUN -> "Bun"
        }
}
