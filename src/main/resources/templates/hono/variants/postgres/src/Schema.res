// Drizzle PostgreSQL schema. `db:generate` reads this file to emit migration SQL.
// HTTP input validation lives in `Validation.res` (zod or sury variant) so this
// file stays focused on persistence.
@module("drizzle-orm/pg-core")
external pgTable: (string, 'columns) => 'table = "pgTable"

@module("drizzle-orm/pg-core") external serial: string => 'col = "serial"
@module("drizzle-orm/pg-core") external text: string => 'col = "text"

// Drizzle's PG column builders chain: `serial("id").primaryKey()`,
// `text("name").notNull()`. We expose the chained methods as `@send` externals
// so the table definition reads naturally in ReScript.
@send external primaryKey: 'col => 'col = "primaryKey"
@send external notNull: 'col => 'col = "notNull"

let users = pgTable("users", {
  "id": serial("id")->primaryKey,
  "name": text("name")->notNull,
  "email": text("email")->notNull,
})
