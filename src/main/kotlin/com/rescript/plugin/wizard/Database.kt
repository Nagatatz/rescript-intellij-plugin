package com.rescript.plugin.wizard

/**
 * Database backends that the Project Wizard can wire into server-side templates.
 *
 * - [LIBSQL] uses libSQL / SQLite via `@libsql/client` + `drizzle-orm/libsql`. The
 *   default; runs against a local file with no service to manage. Suitable for
 *   prototypes, single-machine deployments, and Turso edge databases.
 * - [POSTGRES] uses PostgreSQL via `postgres` (postgres-js) + `drizzle-orm/postgres-js`.
 *   Templates additionally ship a `compose.yaml` so `docker compose up` brings up the
 *   database without a host install.
 * - [MYSQL] uses MySQL via `mysql2` + `drizzle-orm/mysql2`. Same compose-based dev
 *   loop as Postgres.
 *
 * Selection is surfaced in [RescriptProjectWizardStep] and carried through to each
 * template generator via `TemplateContext.database`. Templates that opt out (frontends,
 * library/CLI, mobile/desktop) declare `ProjectTemplate.supportsDatabaseSelection = false`
 * to hide the combo.
 */
enum class Database(
    val displayName: String,
) {
    LIBSQL("libSQL / SQLite"),
    POSTGRES("PostgreSQL"),
    MYSQL("MySQL"),
    ;

    /**
     * Resource variant key used to look up bundled DB-specific files under
     * `src/main/resources/templates/<template>/variants/<key>/...` and
     * `src/main/resources/templates/common/db/<key>/...`.
     */
    fun variantKey(): String = name.lowercase()

    /**
     * Returns the user-visible label so default Swing renderers display the friendly
     * names (e.g. "PostgreSQL") in the Wizard ComboBox.
     */
    override fun toString(): String = displayName
}
