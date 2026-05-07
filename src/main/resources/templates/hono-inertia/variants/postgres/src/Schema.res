// Drizzle PostgreSQL schema. `db:generate` reads this file to emit migration SQL.
// HTTP input validation lives in `Validation.res` (zod or sury variant) so this
// file stays focused on persistence.
@module("drizzle-orm/pg-core")
external pgTable: (string, 'columns) => 'table = "pgTable"

@module("drizzle-orm/pg-core") external serial: string => 'col = "serial"
@module("drizzle-orm/pg-core") external text: string => 'col = "text"

@send external primaryKey: 'col => 'col = "primaryKey"
@send external notNull: 'col => 'col = "notNull"

let posts = pgTable("posts", {
  "id": serial("id")->primaryKey,
  "title": text("title")->notNull,
  "body": text("body")->notNull,
})
