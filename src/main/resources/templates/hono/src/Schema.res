// Drizzle SQLite schema. `db:generate` reads this file to emit migration SQL.
// HTTP input validation lives in `Validation.res` (zod or sury variant) so this
// file stays focused on persistence.
@module("drizzle-orm/sqlite-core")
external sqliteTable: (string, 'columns) => 'table = "sqliteTable"

@module("drizzle-orm/sqlite-core")
external intCol: (string, 'opts) => 'col = "integer"
@module("drizzle-orm/sqlite-core")
external textCol: (string, 'opts) => 'col = "text"

let users = sqliteTable("users", {
  "id": intCol("id", {"primaryKey": true, "autoIncrement": true}),
  "name": textCol("name", {"notNull": true}),
  "email": textCol("email", {"notNull": true}),
})
